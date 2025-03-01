# Base image for building and testing
FROM maven:3.9.1-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .

# Run tests before packaging the application
RUN mvn clean package

# Final runtime image (must include Maven for test execution)
FROM maven:3.9.1-eclipse-temurin-17

WORKDIR /app
COPY --from=build /app /app
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
