#!/bin/bash
set -e

# --- Configuration ---
DEFAULT_NDK_VERSION="r26b"
API_LEVEL=24

# --- Argument Parsing ---
NDK_VERSION=""
SAVE_NDK=false
for arg in "$@"; do
  case $arg in
    --save-ndk)
      SAVE_NDK=true
      ;;
    --ndk-version=*)
      NDK_VERSION="${arg#--ndk-version=}"
      ;;
    *)
      if [ -z "$NDK_VERSION" ]; then
        NDK_VERSION="$DEFAULT_NDK_VERSION"
      fi
      ;;
  esac
done

# Project paths relative to this script's location
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ANDROID_PROJECT_DIR="$SCRIPT_DIR/../imp1"
RUST_PROJECT_DIR="$SCRIPT_DIR/../../vendor/icicle-snark"
RUST_TARGET_DIR="$RUST_PROJECT_DIR/target"
JNI_LIBS_DIR="$ANDROID_PROJECT_DIR/app/src/main/jniLibs"
HOST_OS=$(uname -s)
HOST_ARCH=$(uname -m)

# Create a temporary directory for NDK download and ensure it's cleaned up on exit
TMP_DOWNLOAD_DIR=$(mktemp -d)
trap 'echo "Cleaning up temporary files..."; rm -rf "$TMP_DOWNLOAD_DIR"' EXIT

# --- Functions ---

# Function to check for and install rustup targets
install_rust_targets() {
    echo "--- Checking for Rust Android targets... ---"
    # local targets=("aarch64-linux-android" "armv7-linux-androideabi" "x86_64-linux-android" "i686-linux-android")
    local targets=("aarch64-linux-android")
    local installed_targets=$(rustup target list --installed)
    
    for target in "${targets[@]}"; do
        if ! echo "$installed_targets" | grep -q "$target"; then
            echo "Installing target: $target"
            rustup target add "$target"
        else
            echo "Target already installed: $target"
        fi
    done
}

# Function to find a local NDK or download it
find_or_download_ndk() {
    echo "--- Checking for Android NDK $NDK_VERSION... ---"
    local full_version_string=$(ls -d "$HOME/Library/Android/sdk/ndk/"* 2>/dev/null | grep "$NDK_VERSION" | head -n 1)

    if [ -n "$full_version_string" ] && [ -d "$full_version_string" ]; then
        echo "Found NDK at: $full_version_string"
        export ANDROID_NDK_HOME="$full_version_string"
        return
    fi
    
    echo "NDK not found locally. Proceeding to download..."
    
    local download_host_tag=""
    case "$HOST_OS" in
        Linux) download_host_tag="linux" ;;
        Darwin) download_host_tag="darwin" ;;
        *) echo "Unsupported host OS for download: $HOST_OS"; exit 1 ;;
    esac

    local download_url="https://dl.google.com/android/repository/android-ndk-${NDK_VERSION}-${download_host_tag}.zip"
    local zip_path="$TMP_DOWNLOAD_DIR/ndk.zip"

    echo "Downloading NDK from $download_url..."
    curl -# -L -o "$zip_path" "$download_url"
    echo "Unzipping NDK..."
    unzip -q "$zip_path" -d "$TMP_DOWNLOAD_DIR"
    
    local unzipped_folder=$(find "$TMP_DOWNLOAD_DIR" -type d -name "android-ndk*")
    
    if [ "$SAVE_NDK" = true ]; then
        local install_dir=""
        if [[ "$HOST_OS" == "Darwin" ]]; then
            install_dir="$HOME/Library/Android/sdk/ndk"
        else
            install_dir="$HOME/Android/Sdk/ndk" # Common on Linux
        fi
        
        echo "Saving NDK to $install_dir..."
        mkdir -p "$install_dir"
        mv "$unzipped_folder" "$install_dir/"
        unzipped_folder="$install_dir/$(basename "$unzipped_folder")"
        echo "NDK permanently installed at: $unzipped_folder"
        export ANDROID_NDK_HOME="$unzipped_folder"
    else
        export ANDROID_NDK_HOME="$unzipped_folder"
        echo "NDK temporarily installed at: $ANDROID_NDK_HOME"
    fi
}

build_for_target() {
    local target_triple=$1
    local android_abi=$2
    local c_target_triple=$3
    
    echo ""
    echo "--- Building for $target_triple ---"
    # Set explicit compiler paths for any build scripts that need them.
    export CC="$TOOLCHAIN_PATH/${c_target_triple}${API_LEVEL}-clang"
    export CXX="$TOOLCHAIN_PATH/${c_target_triple}${API_LEVEL}-clang++"
    export AR="$TOOLCHAIN_PATH/llvm-ar"
    export CARGO_TARGET_$(echo "$target_triple" | tr 'a-z-' 'A-Z_')_LINKER="$CC"
    local sysroot_target_triple="$target_triple"
    if [[ "$target_triple" == *"armv7"* ]]; then
        sysroot_target_triple="arm-linux-androideabi"
    fi
    export ADDITIONAL_SYSTEM_LIBRARY_PATHS="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG/sysroot/usr/lib/$sysroot_target_triple/$API_LEVEL"

    cd "$RUST_PROJECT_DIR"
    cargo clean
    cargo build --target "$target_triple" --release
    cd "$SCRIPT_DIR"

    unset CMAKE_CONFIGURE_OPTIONS CC CXX AR CARGO_TARGET_$(echo "$target_triple" | tr 'a-z-' 'A-Z_')_LINKER
}

# --- Main Execution ---
install_rust_targets
find_or_download_ndk
HOST_TAG=""
case "$HOST_OS" in
    Linux) HOST_TAG="linux-$HOST_ARCH" ;;
    Darwin) HOST_TAG="darwin-x86_64" ;;
    *)
        echo "Unsupported host OS for NDK prebuilt toolchain: $HOST_OS"
        exit 1
        ;;
esac
TOOLCHAIN_PATH="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG/bin"
echo "--- Building Rust library for all Android targets... ---"
build_for_target "aarch64-linux-android" "arm64-v8a" "aarch64-linux-android"
# build_for_target "armv7-linux-androideabi" "armeabi-v7a" "armv7a-linux-androideabi"
# build_for_target "x86_64-linux-android" "x86_64" "x86_64-linux-android"
# build_for_target "i686-linux-android" "x86" "i686-linux-android"

echo "--- Copying .so files to Android project... ---"
mkdir -p "$JNI_LIBS_DIR/arm64-v8a"
cp "$RUST_TARGET_DIR/aarch64-linux-android/release/libicicle_snark.so" "$JNI_LIBS_DIR/arm64-v8a/"

# mkdir -p "$JNI_LIBS_DIR/armeabi-v7a"
# cp "$RUST_TARGET_DIR/armv7-linux-androideabi/release/libicicle_snark.so" "$JNI_LIBS_DIR/armeabi-v7a/"

# mkdir -p "$JNI_LIBS_DIR/x86_64"
# cp "$RUST_TARGET_DIR/x86_64-linux-android/release/libicicle_snark.so" "$JNI_LIBS_DIR/x86_64/"

# mkdir -p "$JNI_LIBS_DIR/x86"
# cp "$RUST_TARGET_DIR/i686-linux-android/release/libicicle_snark.so" "$JNI_LIBS_DIR/x86/"

echo "--- Build complete! ---"
echo "You can now open the 'imp1' project in Android Studio and build the AAR."
