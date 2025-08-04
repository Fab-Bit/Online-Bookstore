# Use Maven image with JDK 17 to build and run the tests
FROM maven:3.9.5-eclipse-temurin-17 as builder

# Set work directory
WORKDIR /app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Resolve dependencies
RUN mvn -B dependency:resolve

# Default base URL for the API; can be overridden at runtime
ENV BASE_URL=http://localhost:3000

# Run the test suite
CMD ["mvn", "-B", "test"]
