[![CodeScene Code Health](https://codescene.cs.lth.se/4/status-badges/code-health)](https://codescene.cs.lth.se/4/analyses/latest)

# TEAM 1

The base repo for the project.

## Prerequisites

- Java JDK 17 or higher
- Node 18 or higher
- Maven 3.x

### For windows users

To run make-files in windows, use [Chocolatey](https://chocolatey.org/install).

# Running tests and building:

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

## Start working on a new feature

1. Select the feature you will work on in Favro

2. Assign yourself to the card.

3. Note the Card ID of the feature, e.g. ETS-766

4. Create a branch with the same name as the Card ID of the feature: git switch -c <card-id>

5. Write code and tests covering the code you wrote. Document new functions with doc strings and lines of code that are not described by the code itself.
Commit as often as you like, preferably after every big change.
When the feature is ready to be reviewed, push the branch to GitLab: git push -u origin <card-id>

6. Create a merge request to the main branch on the [coursegit website](https://coursegit.cs.lth.se/etsn05/team-1-2023/-/branches).

### Configure your editor

#### IntelliJ Plugins

- Adapter for Eclipse Code Formatter - To get the correct formatter for the project
- Prettier - To get the correct formatter for javascript files

#### VS Code

- [CodeScene plugin](https://marketplace.visualstudio.com/items?itemName=CodeScene.codescene-vscode)(only available in VS Code)
- [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) - Gives many language features including formatting
- [Prettier](https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode) - For Javascript formatting
