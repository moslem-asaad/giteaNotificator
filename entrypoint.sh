#!/bin/sh

cd /app  # Ensure we're in the project directory

if [ "$1" = "test" ]; then
    echo "Running tests..."
    mvn test
else
    echo "Starting application..."
    java -jar app.jar
fi
