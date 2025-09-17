#!/bin/bash

# Simplified gradlew script for GitHub Actions

# Set Java home if not set
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
fi

# Set Android home
if [ -z "$ANDROID_HOME" ]; then
    export ANDROID_HOME=/usr/local/lib/android/sdk
fi

# Run gradle with the wrapper jar
exec "$JAVA_HOME/bin/java" \
    -Dgradle.user.home=/home/runner/.gradle \
    -jar gradle/wrapper/gradle-wrapper.jar \
    "$@"
