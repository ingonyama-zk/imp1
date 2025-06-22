#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
FRAMEWORK_ROOT="$(pwd)"
PROJECT_NAME="imp1"
FRAMEWORK_NAME="imp1"
BUILD_DIR="$FRAMEWORK_ROOT/Build"
ARCHIVES_DIR="$BUILD_DIR/Archives"
XCFRAMEWORK_OUTPUT="$BUILD_DIR/$FRAMEWORK_NAME.xcframework"
PROJECT_PATH="$FRAMEWORK_ROOT/imp1/imp1.xcodeproj"

echo -e "${BLUE}🚀 Building $FRAMEWORK_NAME XCFramework...${NC}"
echo -e "${YELLOW}Framework root: $FRAMEWORK_ROOT${NC}"
echo -e "${YELLOW}Project path: $PROJECT_PATH${NC}"

# Clean previous builds
echo -e "${YELLOW}Cleaning previous builds...${NC}"
rm -rf "$BUILD_DIR"
mkdir -p "$ARCHIVES_DIR"

# Step 1: Build Rust libraries
echo -e "${BLUE}📦 Step 1: Building Rust libraries...${NC}"
./scripts/build_vendor.sh

# Check if Xcode project exists
if [ ! -d "$PROJECT_PATH" ]; then
    echo -e "${RED}❌ Xcode project not found at $PROJECT_PATH${NC}"
    exit 1
fi

# Step 2: Build iOS Device Framework
echo -e "${BLUE}📱 Step 2: Building iOS Device Framework...${NC}"
xcodebuild -project "$PROJECT_PATH" \
    -scheme "$FRAMEWORK_NAME" \
    -destination "generic/platform=iOS" \
    -archivePath "$ARCHIVES_DIR/ios.xcarchive" \
    archive \
    SKIP_INSTALL=NO \
    BUILD_LIBRARY_FOR_DISTRIBUTION=YES \
    LIBRARY_SEARCH_PATHS="$BUILD_DIR" \
    HEADER_SEARCH_PATHS="$FRAMEWORK_ROOT/imp1/metal-cpp/**" \
    MODULEMAP_FILE="$FRAMEWORK_ROOT/imp1/imp1/module.modulemap" \
    OTHER_LDFLAGS="\
    -force_load $BUILD_DIR/libicicle_snark_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_device_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_curve_bn254_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_field_bn254_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_hash_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_curve_bn254_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_field_bn254_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_device_ios_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_hash_ios_arm64.a \
    -lc++ \
    -framework Foundation \
    -framework Security \
    -framework Metal \
    -framework MetalKit \
    -framework QuartzCore" \
    VALID_ARCHS="arm64" \
    ARCHS="arm64"

# Step 3: Build iOS Simulator Framework
echo -e "${BLUE}📱 Step 3: Building iOS Simulator Framework...${NC}"
xcodebuild -project "$PROJECT_PATH" \
    -scheme "$FRAMEWORK_NAME" \
    -destination "generic/platform=iOS Simulator" \
    -archivePath "$ARCHIVES_DIR/ios-simulator.xcarchive" \
    archive \
    SKIP_INSTALL=NO \
    BUILD_LIBRARY_FOR_DISTRIBUTION=YES \
    HEADER_SEARCH_PATHS="$FRAMEWORK_ROOT/imp1/metal-cpp/**" \
    LIBRARY_SEARCH_PATHS="$BUILD_DIR" \
    MODULEMAP_FILE="$FRAMEWORK_ROOT/imp1/imp1/module.modulemap" \
    OTHER_LDFLAGS="\
    -force_load $BUILD_DIR/libicicle_snark_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_device_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_curve_bn254_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_field_bn254_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_hash_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_curve_bn254_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_field_bn254_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_device_sim_arm64.a \
    -force_load $BUILD_DIR/libicicle_metal_backend_hash_sim_arm64.a \
    -lc++ \
    -framework Foundation \
    -framework Security \
    -framework Metal \
    -framework MetalKit \
    -framework QuartzCore" \
    VALID_ARCHS="arm64" \
    ARCHS="arm64"

# Step 4: Create XCFramework
echo -e "${BLUE}🏗️  Step 4: Creating XCFramework...${NC}"
xcodebuild -create-xcframework \
    -framework "$ARCHIVES_DIR/ios.xcarchive/Products/Library/Frameworks/imp1.framework" \
    -framework "$ARCHIVES_DIR/ios-simulator.xcarchive/Products/Library/Frameworks/imp1.framework" \
    -output "$XCFRAMEWORK_OUTPUT"

# Step 5: Verify XCFramework
echo -e "${BLUE}🔍 Step 5: Verifying XCFramework...${NC}"
if [ -d "$XCFRAMEWORK_OUTPUT" ]; then
    echo -e "${GREEN}✅ XCFramework created successfully!${NC}"
    echo -e "${GREEN}📍 Location: $XCFRAMEWORK_OUTPUT${NC}"
    
    # Show framework info
    echo -e "${YELLOW}Framework Information:${NC}"
    xcodebuild -checkFirstLaunchExperience -project "$PROJECT_PATH" -scheme "$FRAMEWORK_NAME" || true
    
    # Show architectures
    echo -e "${YELLOW}Supported Architectures:${NC}"
    find "$XCFRAMEWORK_OUTPUT" -name "*.framework" -exec echo "  {}" \; -exec lipo -info "{}/$(basename {} .framework)" \; 2>/dev/null || true
    
    # Show size
    echo -e "${YELLOW}Framework Size:${NC}"
    du -sh "$XCFRAMEWORK_OUTPUT"
    
else
    echo -e "${RED}❌ Failed to create XCFramework${NC}"
    exit 1
fi

# Step 6: Package for distribution (optional)
echo -e "${BLUE}📦 Step 6: Packaging for distribution...${NC}"
cd "$BUILD_DIR"
zip -r "$FRAMEWORK_NAME.xcframework.zip" "$FRAMEWORK_NAME.xcframework"
echo -e "${GREEN}✅ Distribution package created: $BUILD_DIR/$FRAMEWORK_NAME.xcframework.zip${NC}"

echo -e "${GREEN}🎉 XCFramework build complete!${NC}"
echo -e "${GREEN}📋 Next steps:${NC}"
echo -e "1. Test the framework in your iOS app"
echo -e "2. Distribute the .xcframework or .zip file"
echo -e "3. Add to your app via Xcode or Swift Package Manager"

echo -e "${BLUE}📖 Integration Instructions:${NC}"
echo -e "1. Drag $FRAMEWORK_NAME.xcframework into your Xcode project"
echo -e "2. Add to 'Frameworks, Libraries, and Embedded Content'"
echo -e "3. Import in Swift: import $FRAMEWORK_NAME"
echo -e "4. Use: GrothProofFramework.generateProof(...)" 