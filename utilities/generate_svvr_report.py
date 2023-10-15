import os
import re
import csv
import json

def print_dict_list(dict_list):
    pretty_str = json.dumps(dict_list, indent=4, sort_keys=True)
    pretty_str = pretty_str.replace('},', '},\n')
    print(pretty_str)

def generate_csv(data, filename):
    key_to_header_map = {
        'signature': 'Method Signature',
        'file_name': 'File Name',
        'class_name': 'Class Name',
        'desc': 'Description',
        'task': 'Task ID',
        'story': 'Story ID'
    }
    
    custom_order = ['file_name', 'class_name', 'signature', 'desc', 'task', 'story']
    
    with open(filename, 'w', newline='') as output_file:
        dict_writer = csv.DictWriter(output_file, fieldnames=custom_order)
        dict_writer.writerow({k: key_to_header_map[k] for k in custom_order})
        
        for row in data:
            filtered_row = {k: row.get(k, '') for k in custom_order}
            dict_writer.writerow(filtered_row)

def get_files(base_path, relative_path, extension):

    file_list = []

    absolute_path = os.path.join(base_path, relative_path)

    for root, _, files in os.walk(absolute_path):

        for file in files:

            if file.endswith(extension):

                file_list.append(os.path.join(root, file))

    return file_list

def process_test_files(file_paths, parse_function):
    all_test_signatures = []
    for file_path in file_paths:
        all_test_signatures.extend(parse_function(file_path))
    return all_test_signatures

def parse_test_methods_java(file_path):
    test_signatures = []
    file_name = os.path.basename(file_path)
    with open(file_path, 'r') as f:
        content = f.read()
        class_pattern = r'public class (\w+)'
        class_name_match = re.search(class_pattern, content)
        class_name = class_name_match.group(1) if class_name_match else "Unknown"
        
        method_blocks = re.findall(r'(/\*\*(.*?)\*/)?\s*@Test\s*(public|protected|private|static|\s) +([\w\<\>\[\]]+\s+(\w+) *\([^\)]*\))', content, re.DOTALL)
        
        for block in method_blocks:
            annotations_block = block[1] if block[1] else ""
            annotations = annotations_block.strip().split('\n')
            desc = task = story = "TBA"
            for annotation in annotations:
                desc_match = re.search(r'@desc\s*(.*)', annotation)
                task_match = re.search(r'@task\s*(.*)', annotation)
                story_match = re.search(r'@story\s*(.*)', annotation)

                if desc_match:
                    desc = desc_match.group(1).strip()
                if task_match:
                    task = task_match.group(1).strip()
                if story_match:
                    story = story_match.group(1).strip()


            test_signatures.append({
                'signature': block[3].strip(),
                'file_name': file_name,
                'class_name': class_name,
                'desc': desc,
                'task': task,
                'story': story
            })
    return test_signatures

def find_it_blocks_in_describe(content, describe_name, file_name):
    test_signatures = []
    
    # Find all 'it' blocks in this 'describe'
    it_blocks = re.findall(r'(/\*\*(.*?)\*/)?\s*it\s*\(\s*["\'](.*?)["\']\s*,\s*function\s*\(', content, re.DOTALL)
    for it_block in it_blocks:
        annotations_block = it_block[1] if it_block[1] else ""
        annotations = annotations_block.strip().split('\n')
        desc = it_block[2]  # Default to the content of it()
        task = story = "TODO"
        for annotation in annotations:
            desc_match = re.search(r'@desc\s*(.*)', annotation)
            task_match = re.search(r'@task\s*(.*)', annotation)
            story_match = re.search(r'@story\s*(.*)', annotation)

            if desc_match:
                desc = desc_match.group(1).strip()
            if task_match:
                task = task_match.group(1).strip()
            if story_match:
                story = story_match.group(1).strip()
                
        test_signatures.append({
            'signature': it_block[2],
            'file_name': file_name,
            'class_name': describe_name,
            'desc': desc,
            'task': task,
            'story': story
        })
    
    # Find nested 'describe' blocks
    nested_describes = re.findall(r'describe\s*\(\s*["\'](.*?)["\']\s*,\s*function\s*\([^)]*\)\s*\{(.*?)\}', content, re.DOTALL)
    for nested_name, nested_content in nested_describes:
        full_describe_name = f"{describe_name} -> {nested_name}"
        test_signatures += find_it_blocks_in_describe(nested_content, full_describe_name, file_name)
        
    return test_signatures

def parse_js_test_file(file_path):
    test_signatures = []
    file_name = os.path.basename(file_path)
    with open(file_path, 'r') as f:
        content = f.read()
        
        it_blocks = re.findall(r'(/\*\*(.*?)\*/)?\s*it\s*\(\s*["\'](.*?)["\']\s*,\s*function\s*\(', content, re.DOTALL)
        
        for it_block in it_blocks:
            annotations_block = it_block[1] if it_block[1] else ""
            annotations = annotations_block.strip().split('\n')
            task = story = desc =  "TBA"
            for annotation in annotations:
                desc_match = re.search(r'@desc\s*(.*)', annotation)
                task_match = re.search(r'@task\s*(.*)', annotation)
                story_match = re.search(r'@story\s*(.*)', annotation)

                if desc_match:
                    desc = desc_match.group(1).strip()
                if task_match:
                    task = task_match.group(1).strip()
                if story_match:
                    story = story_match.group(1).strip()
                    
            test_signatures.append({
                'signature': "N/A",
                'file_name': file_name,
                'class_name': "N/A",
                'desc': desc,
                'task': task,
                'story': story
            })
    return test_signatures

path_to_src = "../base/server/src/"

java_files = get_files(path_to_src, "test/java/se/lth/base/server", ".java")
js_files = get_files(path_to_src, "main/resources/webassets/spec", ".js")

java_tests = process_test_files(java_files, parse_test_methods_java)
js_tests = process_test_files(js_files, parse_js_test_file)

print_dict_list([java_tests + js_tests])
generate_csv(java_tests + js_tests, 'svvr_report.csv')

