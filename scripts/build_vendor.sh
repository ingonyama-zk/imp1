#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRAMEWORK_ROOT="$(dirname "$SCRIPT_DIR")"
RUST_DIR="$FRAMEWORK_ROOT/vendor/icicle-snark"
OUTPUT_DIR="$FRAMEWORK_ROOT/Build"

echo "SCRIPT_DIR: $SCRIPT_DIR"
echo "FRAMEWORK_ROOT: $FRAMEWORK_ROOT"
echo "RUST_DIR: $RUST_DIR"
echo "OUTPUT_DIR: $OUTPUT_DIR"

echo -e "${GREEN}Building Rust libraries for iOS...${NC}"

# Ensure we're in the Rust directory
cd "$RUST_DIR"

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Function to build for a specific target
build_target() {
    local target=$1
    local output_name=$2
    
    echo -e "${YELLOW}Building for $target...${NC}"
    
    # Install target if not already installed
    rustup target add "$target" 2>/dev/null || true
    
    # Build with iOS-compatible flags
    RUSTFLAGS="-C opt-level=3" \
    cargo build --target "$target" --release
    
    # Copy Rust library to output directory
    cp "target/$target/release/libicicle_snark_ios.a" "$OUTPUT_DIR/$output_name"
    
    # Copy icicle libraries
    local icicle_dir="target/$target/release/deps/icicle/lib"
    if [ -d "$icicle_dir" ]; then
        local arch_suffix=""
        case "$target" in
            "aarch64-apple-ios") arch_suffix="_ios_arm64" ;;
            "aarch64-apple-ios-sim") arch_suffix="_sim_arm64" ;;
        esac
        
        # Copy each icicle library with architecture suffix
        cp "$icicle_dir/libicicle_curve_bn254.a" "$OUTPUT_DIR/libicicle_curve_bn254${arch_suffix}.a"
        cp "$icicle_dir/libicicle_device.a" "$OUTPUT_DIR/libicicle_device${arch_suffix}.a"
        cp "$icicle_dir/libicicle_field_bn254.a" "$OUTPUT_DIR/libicicle_field_bn254${arch_suffix}.a"
        cp "$icicle_dir/libicicle_hash.a" "$OUTPUT_DIR/libicicle_hash${arch_suffix}.a"
        
        # Copy icicle metal backends as well
        # cp "$icicle_dir/backend/bn254/metal/libicicle_backend_metal_curve_bn254.a" "$OUTPUT_DIR/libicicle_metal_backend_curve_bn254${arch_suffix}.a"
        # cp "$icicle_dir/backend/bn254/metal/libicicle_backend_metal_field_bn254.a" "$OUTPUT_DIR/libicicle_metal_backend_field_bn254${arch_suffix}.a"
        # cp "$icicle_dir/backend/metal/libicicle_backend_metal_device.a" "$OUTPUT_DIR/libicicle_metal_backend_device${arch_suffix}.a"
        # cp "$icicle_dir/backend/metal/libicicle_backend_metal_hash.a" "$OUTPUT_DIR/libicicle_metal_backend_hash${arch_suffix}.a"

        echo -e "${GREEN}✓ Copied icicle libraries for $target${NC}"
    else
        echo -e "${YELLOW}⚠ No icicle libraries found for $target${NC}"
    fi
    
    echo -e "${GREEN}✓ Built $target${NC}"
}

# Build for iOS device (arm64)
build_target "aarch64-apple-ios" "libicicle_snark_ios_arm64.a"

# Build for iOS simulator (arm64)
build_target "aarch64-apple-ios-sim" "libicicle_snark_sim_arm64.a"

# List the created files
echo -e "${GREEN}Built libraries:${NC}"
ls -la "$OUTPUT_DIR"/*.a

echo -e "${GREEN}✅ Vendor build complete!${NC}" 