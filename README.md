[![CodeScene Code Health](https://codescene.cs.lth.se/4/status-badges/code-health)](https://codescene.cs.lth.se/4/analyses/latest)

# TEAM 1: 

The base repo for the project. 

## Prerequisites

- Java JDK 17 or higher
- Node 18 or higher
- Maven 3.x

First, navigate to the `base/server` folder and run:

```bash
npm install
```

## Build and Run

You can either use Maven directly or use the provided Makefile for common tasks. The commands are 
to be executed in the `base/server` directory.

### Using Maven
- To run the project: `mvn package && java -jar target/base-server-jar-with-dependencies.jar`
- To run the tests: `mvn test`
- To check code formatting: `npm run prettier-check && mvn validate -P check-java-format`
- To format the code: `mvn package -P format-js,format-java`

### Using make
- To run the project: `make run`
- To run the tests: `make test`
- To check code formatting: `make check`
- To format the code: `make format`

## Code Formatting
The project uses [prettier](https://prettier.io/) for javascript formatting and the Eclipse 
formatting standard used in the [Eclipse JDT Language Server](https://github.com/eclipse-jdtls/eclipse.jdt.ls).

### Configure your editor

#### IntelliJ Plugins
- Adapter for Eclipse Code Formatter - To get the correct formatter for the project
- Prettier - To get the correct formatter for javascript files

#### VS Code
- [CodeScene plugin](https://marketplace.visualstudio.com/items?itemName=CodeScene.codescene-vscode)(only available in VS Code)
- [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) - Gives many language features including formatting
- [Prettier](https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode) - For Javascript formatting
