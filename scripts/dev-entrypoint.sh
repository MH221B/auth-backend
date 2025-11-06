#!/bin/sh
set -e

# Ensure the Gradle distribution is available before starting concurrent Gradle invocations.
# This avoids the wrapper trying to download/extract the distribution from multiple
# processes at the same time and hitting the exclusive-access timeout.
# Running a lightweight task (help) forces the wrapper to download/extract once and exit.
echo "Preparing Gradle (will download distribution if needed)..."
./gradlew --no-daemon help

# Run continuous compile in the background so Gradle recompiles Java sources when they change.
# Gradle's continuous mode (-t) watches inputs and reruns the task; compileJava will update
# the classes on disk. Spring Boot DevTools will detect classpath changes and restart the app.
./gradlew -t compileJava --no-daemon &

# Start the application. bootRun will pick up the compiled classes; DevTools will handle restarts.
./gradlew bootRun --no-daemon
