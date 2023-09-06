run:
	mvn package && java -jar target/base-server-jar-with-dependencies.jar

test:
	mvn test

check:
	npm run prettier-check && mvn validate -P check-java-format

format:
	mvn package -P format-js,format-java
