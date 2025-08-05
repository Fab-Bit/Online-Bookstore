# API Automation Testing Assessment: Online Bookstore

This repository contains an automated testing framework for the Online‑Bookstore RESTful API. The project is written in **Java** using **RestAssured** and **JUnit 5**, with Maven for dependency management and build automation.

The tested API includes endpoints for managing **books** and **authors**. The test suite covers happy paths as well as comprehensive edge cases.

## Project Structure

```
.
├── pom.xml                     # Maven project configuration with dependencies
├── Dockerfile                 # Container build file to run tests
└── src
    └── test
        └── java
            └── com
                └── example
                    └── api
                        ├── BaseTest.java       # Common RestAssured configuration
                        ├── BooksApiTest.java   # Tests for the Books endpoints
                        └── AuthorsApiTest.java # Tests for the Authors endpoints
```

## Running Tests Locally

Make sure you have Java 17 and Maven installed on your machine.

1. Clone this repository.
2. Start the Online‑Bookstore API server locally and note its base URL (e.g., `http://localhost:3000`).
3. Run the tests using Maven:

```sh
# Use default base URL (https://fakerestapi.azurewebsites.net)
mvn test

# OR specify a different base URL
mvn test -DbaseUrl=http://localhost:8080
```

The base URL can also be provided via environment variable:

```sh
BASE_URL=http://localhost:8080 mvn test
```

Test results will appear in the console and detailed reports can be found in the `target/surefire-reports` directory.

## Running Tests in Docker

You can run the entire test suite inside a Docker container. The provided `Dockerfile` builds an image, installs dependencies, and runs the tests.

Build the image:

```sh
docker build -t online-bookstore-api-tests .
```

Run the tests:

```sh
docker run --rm -e BASE_URL=http://host.docker.internal:3000 online-bookstore-api-tests
```

Adjust the `BASE_URL` environment variable to point to your API server.

## Continuous Integration

A GitHub Actions workflow (`.github/workflows/ci.yml`) is included. On each push or pull request, the workflow:

- Checks out the code.
- Sets up Java and caches dependencies.
- Builds the project and runs the test suite with Maven.
- Collects the Surefire reports as artefacts.

## Reporting

The test suite uses the Maven Surefire plugin to generate JUnit XML reports located in the `target/surefire-reports` directory. These reports can be consumed by CI systems to display results. You can also open the reports locally to inspect test outcomes.

