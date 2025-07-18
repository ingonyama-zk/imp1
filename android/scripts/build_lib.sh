#!/bin/bash
set -e

# Set JAVA_HOME to use Java 21+ (generic approach)
# Try to find Java 21+ in common installation paths
find_java_21_plus() {
    # Common Java 21+ installation paths
    local java_paths=(
        "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"  # macOS
        "/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home"  # macOS
        "/usr/lib/jvm/java-21-openjdk"                               # Linux OpenJDK
        "/usr/lib/jvm/java-23-openjdk"                               # Linux OpenJDK
        "/usr/lib/jvm/java-21-oracle"                                # Linux Oracle JDK
        "/usr/lib/jvm/java-23-oracle"                                # Linux Oracle JDK
        "/usr/java/jdk-21"                                           # Linux alternative
        "/usr/java/jdk-23"                                           # Linux alternative
        "C:/Program Files/Java/jdk-21"                               # Windows
        "C:/Program Files/Java/jdk-23"                               # Windows
        "C:/Program Files/Eclipse Adoptium/jdk-21"                   # Windows Eclipse Temurin
        "C:/Program Files/Eclipse Adoptium/jdk-23"                   # Windows Eclipse Temurin
    )
    
    for path in "${java_paths[@]}"; do
        if [ -d "$path" ] && [ -f "$path/bin/java" ]; then
            echo "$path"
            return 0
        fi
    done
    
    # Try to find Java 21+ using system commands
    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        if [[ "$java_version" == 21* ]] || [[ "$java_version" == 22* ]] || [[ "$java_version" == 23* ]]; then
            # If current Java is 21+, try to find its home
            if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ]; then
                echo "$JAVA_HOME"
                return 0
            else
                # Try to find Java home from the java executable
                local java_path=$(which java)
                if [[ "$java_path" == /usr/bin/java ]]; then
                    # On macOS, this is likely a symlink to the actual Java installation
                    local real_java_path=$(readlink -f "$java_path" 2>/dev/null || echo "$java_path")
                    if [[ "$real_java_path" != "$java_path" ]]; then
                        local java_home=$(dirname "$(dirname "$real_java_path")")
                        if [ -d "$java_home" ] && [ -f "$java_home/bin/java" ]; then
                            echo "$java_home"
                            return 0
                        fi
                    fi
                fi
                
                # Try to find Java home using /usr/libexec/java_home on macOS
                if command -v /usr/libexec/java_home &> /dev/null; then
                    local java_home=$(/usr/libexec/java_home -v "$java_version" 2>/dev/null)
                    if [ -n "$java_home" ] && [ -d "$java_home" ]; then
                        echo "$java_home"
                        return 0
                    fi
                fi
            fi
        fi
    fi
    
    return 1
}

# Set JAVA_HOME if not already set or if we need Java 21+
if [ -z "$JAVA_HOME" ] || ! java -version 2>&1 | grep -q "version \"2[123]"; then
    JAVA_21_PLUS_HOME=$(find_java_21_plus)
    if [ -n "$JAVA_21_PLUS_HOME" ]; then
        export JAVA_HOME="$JAVA_21_PLUS_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "Set JAVA_HOME to: $JAVA_HOME"
    else
        echo "Warning: Java 21+ not found in common locations."
        echo "Please ensure Java 21+ is installed and JAVA_HOME is set correctly."
        echo "Current Java version:"
        java -version 2>&1 || echo "Java not found"
    fi
fi

# Verify Java version
echo "Using Java version:"
java -version

# Check if local testing is requested
DEBUG=""
if [ "$1" = "--debug" ]; then
    DEBUG="--debug"
    echo "Debug mode enabled"
fi

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
