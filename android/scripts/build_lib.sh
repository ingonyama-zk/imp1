#!/bin/bash
set -e

# Set JAVA_HOME to use Java 21 for Android Gradle plugin compatibility
if [ -z "$JAVA_HOME" ] || ! java -version 2>&1 | grep -q "version \"21"; then
    # Try to find Java 21
    JAVA_21_PATHS=(
        "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"  # macOS
        "/usr/lib/jvm/java-21-openjdk"                               # Linux OpenJDK
        "/usr/lib/jvm/java-21-oracle"                                # Linux Oracle JDK
        "/usr/java/jdk-21"                                           # Linux alternative
        "C:/Program Files/Java/jdk-21"                               # Windows
        "C:/Program Files/Eclipse Adoptium/jdk-21"                   # Windows Eclipse Temurin
    )
    
    for path in "${JAVA_21_PATHS[@]}"; do
        if [ -d "$path" ] && [ -f "$path/bin/java" ]; then
            export JAVA_HOME="$path"
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "Set JAVA_HOME to: $JAVA_HOME"
            break
        fi
    done
fi

# Check if local testing is requested
DEBUG=""
if [ "$1" = "--debug" ]; then
    DEBUG="--debug"
    echo "Debug mode enabled"
fi

# Verify Java version
echo "Using Java version:"
java -version

# Build the AAR library located at android/imp1/app
echo "Building AAR library..."

# Step 1: Build the Rust vendor library first
echo "Step 1: Building Rust vendor library..."
"$(dirname "$0")/build_vendor.sh" $DEBUG --save-ndk

# Navigate to the Android project directory
cd "$(dirname "$0")/../imp1"

# Clean previous builds
echo "Step 2: Cleaning previous Android builds..."
./gradlew clean

# Build the AAR library
echo "Step 3: Building AAR library..."
./gradlew assembleRelease

# Build debug version as well
echo "Step 4: Building debug AAR library..."
./gradlew assembleDebug

echo "Build completed successfully!"
echo "AAR files can be found in:"
echo "  - Release: $(pwd)/app/build/outputs/aar/"
echo "  - Debug: $(pwd)/app/build/outputs/aar/"
