import pandas as pd
import sys


# Script that reads the exported cards from favro (export backlog as CSV) and converts them into the format used in the PRD.
def main(input_file):
    # Read the CSV file
    df = pd.read_csv(input_file)

    # Filter the DataFrame to start from the row where 'Card ID' is 'B33-836'
    df_filtered = df.loc[df[df['Card ID'] == 'B33-836'].index[0] + 1:]

    # Initialize variables
    current_index = 1
    current_userstory_idx = 0
    task_idx = 0

    # Initialize list to hold formatted lines
    formatted_lines = []

    for _, row in df_filtered.iterrows():
        card_type = row['Card Type']
        title = row['Title']
        card_id = row['Card ID']
        card_id = card_id.replace('B33', 'ETS') # for some reason the export writes B33 instead of ETS

        if card_type == 'Epic':
            formatted_line = f"{current_index}. [{card_id}] {title}"
            current_index += 1
            current_userstory_idx = 0
            task_idx = 0
        elif card_type == 'User Story':
            current_userstory_idx += 1
            task_idx = 0
            #formatted_line = f"\t{current_index - 1}.{current_userstory_idx} {title}"
            formatted_line = f"\t{current_index - 1}.{current_userstory_idx} [{card_id}] {title}"
        elif card_type == 'Task':
            task_idx += 1
            #formatted_line = f"\t\t{current_index - 1}.{current_userstory_idx}.{task_idx} {title}"
            formatted_line = f"\t\t{current_index - 1}.{current_userstory_idx}.{task_idx} [{card_id}] {title}"
        else:
            continue

        formatted_lines.append(formatted_line)

    formatted_text = "\n".join(formatted_lines)

    with open("res.txt", 'w') as f:
        f.write(formatted_text)


if __name__ == "__main__":
    input_file = sys.argv[1]
    main(input_file)
