#!/bin/bash
set -e

# Set JAVA_HOME to use Java 21
# Try to find Java 21 in common installation paths
find_java_21() {
    # Common Java 21 installation paths
    local java_paths=(
        "/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home"  # macOS Homebrew
        "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"      # macOS
        "/usr/lib/jvm/java-21-openjdk"                                   # Linux OpenJDK
        "/usr/lib/jvm/java-21-oracle"                                    # Linux Oracle JDK
        "/usr/java/jdk-21"                                               # Linux alternative
        "C:/Program Files/Java/jdk-21"                                   # Windows
        "C:/Program Files/Eclipse Adoptium/jdk-21"                       # Windows Eclipse Temurin
    )
    
    for path in "${java_paths[@]}"; do
        if [ -d "$path" ] && [ -f "$path/bin/java" ]; then
            echo "$path"
            return 0
        fi
    done
    
    # Try to find Java 21 using system commands
    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        if [[ "$java_version" == 21* ]]; then
            # If current Java is 21, try to find its home
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

# Set JAVA_HOME if not already set or if we need Java 21
if [ -z "$JAVA_HOME" ] || ! java -version 2>&1 | grep -q "version \"21"; then
    JAVA_21_HOME=$(find_java_21)
    if [ -n "$JAVA_21_HOME" ]; then
        export JAVA_HOME="$JAVA_21_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo "Set JAVA_HOME to: $JAVA_HOME"
    else
        echo "Warning: Java 21 not found in common locations."
        echo "Please ensure Java 21 is installed and JAVA_HOME is set correctly."
        echo "Current Java version:"
        java -version 2>&1 || echo "Java not found"
    fi
fi

# Verify Java version
echo "Using Java version:"
java -version

# Get the script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "SCRIPT_DIR: $SCRIPT_DIR"

# --- Argument Parsing ---
ARR_VERSION="0.2.1"
DEBUG=false
for arg in "$@"; do
  case $arg in
    --arr-version=*)
      ARR_VERSION="${arg#--arr-version=}"
      ;;
    --debug)
      DEBUG=true
      ;;
    *)
      echo "Unknown argument: $arg"
      exit 1
      ;;
  esac
done

echo "Building example app..."

# Step 1: Build the AAR library
echo "Step 1: Building AAR library..."
if [ "$DEBUG" = true ]; then
    "$SCRIPT_DIR/build_lib.sh" -debug
else
    "$SCRIPT_DIR/build_lib.sh"
fi

# Step 2: Copy the AAR to ExampleApp
echo "Step 2: Copying AAR to ExampleApp..."
LIB_DIR="$SCRIPT_DIR/../imp1/app/build/outputs/aar"
EXAMPLE_LIBS_DIR="$SCRIPT_DIR/../ExampleApp/app/libs"

# Create libs directory if it doesn't exist
mkdir -p "$EXAMPLE_LIBS_DIR"

# Copy the debug AAR for local testing (debug version has better logging)
if [ "$DEBUG" = true ]; then
    if [ -f "$LIB_DIR/imp1-$ARR_VERSION-debug.aar" ]; then
        cp "$LIB_DIR/imp1-$ARR_VERSION-debug.aar" "$EXAMPLE_LIBS_DIR/imp1-$ARR_VERSION.aar"
        echo "Copied imp1-$ARR_VERSION-debug.aar to ExampleApp (for better logging)"
    elif [ -f "$LIB_DIR/imp1-$ARR_VERSION-release.aar" ]; then
        cp "$LIB_DIR/imp1-$ARR_VERSION-release.aar" "$EXAMPLE_LIBS_DIR/imp1-$ARR_VERSION.aar"
        echo "Copied imp1-$ARR_VERSION-release.aar to ExampleApp"
    else
        echo "Error: Could not find AAR file in $LIB_DIR"
        exit 1
    fi
else
    # Copy the release AAR (we'll use release version for the example app)
    if [ -f "$LIB_DIR/imp1-$ARR_VERSION-release.aar" ]; then
        cp "$LIB_DIR/imp1-$ARR_VERSION-release.aar" "$EXAMPLE_LIBS_DIR/imp1-$ARR_VERSION.aar"
        echo "Copied imp1-$ARR_VERSION-release.aar to ExampleApp"
    elif [ -f "$LIB_DIR/imp1-$ARR_VERSION.aar" ]; then
        cp "$LIB_DIR/imp1-$ARR_VERSION.aar" "$EXAMPLE_LIBS_DIR/imp1-$ARR_VERSION.aar"
        echo "Copied imp1-$ARR_VERSION.aar to ExampleApp"
    else
        echo "Error: Could not find AAR file in $LIB_DIR"
        exit 1
    fi
fi

# Step 3: Build the example app
echo "Step 3: Building example app..."
cd "$SCRIPT_DIR/../ExampleApp"

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build the example app
if [ "$DEBUG" = true ]; then
    echo "Building debug AAB bundle for local testing..."
    ./gradlew bundleDebug
else
    echo "Building release AAB bundle..."
    ./gradlew bundleRelease
fi

# Step 4: Handle local testing if requested
if [ "$DEBUG" = true ]; then
    echo "Step 4: Setting up local testing..."
    
    # Set the bundle path based on debug/release build
    if [ "$DEBUG" = true ]; then
        BUNDLE_PATH="$(pwd)/app/build/outputs/bundle/debug/app-debug.aab"
    else
        BUNDLE_PATH="$(pwd)/app/build/outputs/bundle/release/app-release.aab"
    fi
    
    # Check if the bundle file exists
    if [ ! -f "$BUNDLE_PATH" ]; then
        echo "❌ Error: AAB bundle not found at $BUNDLE_PATH"
        echo "Please ensure the build completed successfully"
        exit 1
    fi
    
    # Check if bundletool exists
    BUNDLETOOL_PATH=""
    if command -v bundletool &> /dev/null; then
        BUNDLETOOL_PATH="bundletool"
    elif [ -f "bundletool-all.jar" ]; then
        BUNDLETOOL_PATH="java -jar bundletool-all.jar"
    elif [ -f "$SCRIPT_DIR/bundletool-all.jar" ]; then
        BUNDLETOOL_PATH="java -jar $SCRIPT_DIR/bundletool-all.jar"
    else
        echo "📥 Bundletool not found. Downloading..."
        cd "$SCRIPT_DIR"
        curl -L -o bundletool-all.jar https://github.com/google/bundletool/releases/download/1.15.6/bundletool-all-1.15.6.jar
        BUNDLETOOL_PATH="java -jar $SCRIPT_DIR/bundletool-all.jar"
    fi

    if ! adb devices | grep -q "device$"; then
        echo "❌ Error: No Android device connected"
        echo "Please connect a device and enable USB debugging"
        exit 1
    fi

    echo "📱 Connected devices:"
    adb devices
    
    # Generate APK set
    echo "📦 Generating APK set..."
    APKS_PATH="$SCRIPT_DIR/../ExampleApp/app-debug.apks"
    rm -f "$APKS_PATH"
    $BUNDLETOOL_PATH build-apks --bundle="$BUNDLE_PATH" --output="$APKS_PATH" --local-testing
    
    # Install APKs
    echo "Installing APKs..."
    $BUNDLETOOL_PATH install-apks --apks="$APKS_PATH"
    
    echo "Local testing setup completed!"
    echo "AAB bundle can be found in: $(pwd)/app/build/outputs/bundle/debug/"
    echo "APKs for local testing can be found in: $(pwd)/output.apks"
    echo ""
else
    echo "Build completed successfully!"
    echo "AAB bundle can be found in: $(pwd)/app/build/outputs/bundle/release/"
fi

