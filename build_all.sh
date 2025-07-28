#!/bin/bash

# IMP1 v0.2.2 script to build all artifacts

set -e

VERSION="0.2.2"
RELEASE_TAG="v${VERSION}"
RELEASE_BRANCH="stas/release/v${VERSION}"

# Check if artifacts exist
ANDROID_AAR="android/imp1/app/build/outputs/aar/imp1-${VERSION}.aar"
ANDROID_DEBUG_AAR="android/imp1/app/build/outputs/aar/imp1-${VERSION}-debug.aar"
IOS_XCFRAMEWORK="ios/Build/imp1.xcframework"
IOS_XCFRAMEWORK_ZIP="ios/Build/imp1.xcframework.zip"


echo "🚀 Building IMP1 v${VERSION} artefacts"
echo "======================================"

# Clean up old artifacts and build directories
echo "🧹 Cleaning build directories..."

# Clean Android build directories
echo "  📱 Android..."
rm -rf android/imp1/app/build
rm -rf android/imp1/build
rm -rf android/ExampleApp/build
rm -rf android/ExampleApp/app/build

# Clean iOS build directories
echo "  🍎 iOS..."
rm -rf ios/Build
rm -rf ios/imp1/build

# Clean vendor build artifacts
echo "  🦀 Rust vendor..."
rm -rf vendor/icicle-snark/target
rm -rf vendor/icicle-snark/Cargo.lock

# Build Android AAR
echo "🔨 Building Android AAR..."
cd android  
./scripts/build_lib.sh
cd ..

if [ ! -f "$ANDROID_AAR" ]; then
    echo "❌ Android AAR not found: $ANDROID_AAR"
    exit 1
fi

if [ ! -f "$ANDROID_DEBUG_AAR" ]; then
    echo "❌ Android Debug AAR not found: $ANDROID_DEBUG_AAR"
    exit 1
fi

echo "🔨 Building iOS XCFramework..."

cd ./ios    
./scripts/build_xcframework.sh
cd ..

if [ ! -d "$IOS_XCFRAMEWORK" ]; then
    echo "❌ iOS XCFramework directory not found: $IOS_XCFRAMEWORK"
    exit 1
fi

if [ ! -f "$IOS_XCFRAMEWORK_ZIP" ]; then
    echo "❌ iOS XCFramework zip not found: $IOS_XCFRAMEWORK_ZIP"
    exit 1
fi

echo "✅ All artifacts found!"
echo ""
echo "📦 Generated artifacts:"
echo "  📱 Android Release AAR: $ANDROID_AAR"
echo "  📱 Android Debug AAR: $ANDROID_DEBUG_AAR"
echo "  🍎 iOS XCFramework: $IOS_XCFRAMEWORK"
echo "  🍎 iOS XCFramework Zip: $IOS_XCFRAMEWORK_ZIP"

