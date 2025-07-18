# IMP1 Android Library

This directory contains the Android implementation of IMP1, a mobile-first zero-knowledge proving framework powered by ICICLE.

## Overview

IMP1 for Android provides:
- **Native Kotlin library** with JNI bridge to Rust
- **Groth16 protocol** implementation for zero-knowledge proofs
- **Parallel proof generation** for batch processing
- **Optimized for arm64-v8a** architecture
- **Drop-in integration** for Android applications

## Prerequisites

Before building the library, ensure you have:
- **Android Studio** (latest version recommended)
- **Android SDK** with API level 24+
- **Java 21+** (required for Android Gradle plugin compatibility)
- **Rust** toolchain installed (`rustup`)
- **Android NDK** (r27c or later)
- **Git** with access to the IMP1 repository

## Setup

First, clone and setup the repository:

```bash
git clone https://github.com/ingonyama-zk/imp1.git
cd imp1
git submodule update --init --recursive
```

## Building the Library

### Quick Start (Recommended)

Use the automated build script for the complete workflow:

```bash
# Build library and example app
cd android
./scripts/build_example_app.sh

# For debug build with local testing on USB-connected device
./scripts/build_example_app.sh --debug
```

This script will:
- **Build the Rust vendor library**
- **Build the AAR library** (both debug and release)
- **Copy the AAR to ExampleApp**
- **Build and install the ExampleApp** (with optional local testing)

### Manual Build Process

#### Step 1: Build Rust Dependencies
```bash
./scripts/build_vendor.sh
```

This script will:
- **Download Android NDK** if not present locally
- **Install Rust Android targets** (aarch64-linux-android)
- **Build ICICLE-SNARK** for Android arm64-v8a
- **Copy native libraries** to the Android project

#### Step 2: Build the AAR Library
```bash
./scripts/build_lib.sh
```

This creates:
- `imp1/app/build/outputs/aar/imp1-0.2.1-release.aar` - Release version
- `imp1/app/build/outputs/aar/imp1-0.2.1-debug.aar` - Debug version

#### Step 3: Build and Test the Example App
```bash
./build_example_app.sh
```

## Complete Workflow

For a full development cycle:

```bash
# 1. Build everything (recommended)
cd android
./scripts/build_example_app.sh

# 2. For development with debug output
./scripts/build_example_app.sh --debug

# 3. When you make changes to the library, repeat:
./scripts/build_lib.sh
./scripts/build_example_app.sh
```

**Note**: All scripts should be run from the `android` directory.

## Project Structure

```
android/
├── imp1/                    # Main library project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/        # Kotlin source code
│   │   │   ├── cpp/         # C++ JNI bridge
│   │   │   └── jniLibs/     # Native libraries
│   │   └── build.gradle.kts # Library configuration
│   └── scripts/
│       └── build_vendor.sh  # Rust dependency builder
├── ExampleApp/              # Example application
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/        # Example app source code
│   │   │   └── assets/      # Circuit files and examples
│   │   └── libs/            # AAR library location
└── scripts/                 # Build utilities
    ├── build_lib.sh         # Library build script
    ├── build_example_app.sh # Complete build script
    └── build_vendor.sh      # Rust dependency builder
```

## Integration

### Using the Library

1. **Copy the AAR** to your Android project's `libs/` directory
2. **Add dependency** in your `build.gradle.kts`:
   ```kotlin
   implementation(files("libs/imp1-0.2.1.aar"))
   ```
3. **Import** the library in your Kotlin code:
   ```kotlin
   import com.ingonyama.imp1.NativeBridge
   import com.ingonyama.imp1.DeviceType
   import com.ingonyama.imp1.ProverException
   import com.ingonyama.imp1.VerifierResult
   ```

### API Usage

The library provides a simple interface through `NativeBridge`:

#### Single Proof Generation
```kotlin
// Generate a proof
try {
    NativeBridge.prove(
        witnessPath = "path/to/witness.wtns",
        zkeyPath = "path/to/circuit_final.zkey",
        proofPath = "path/to/output.proof",
        publicPath = "path/to/output.public",
        deviceType = DeviceType.Cpu
    )
    println("Proof generated successfully!")
} catch (e: ProverException) {
    println("Proof generation failed: ${e.message}")
}
```

#### Parallel Proof Generation
```kotlin
// Generate multiple proofs in parallel
try {
    val results = NativeBridge.parallelProve(
        witnessPaths = arrayOf("witness1.wtns", "witness2.wtns", "witness3.wtns"),
        zkeyPath = "path/to/circuit_final.zkey",
        proofPaths = arrayOf("proof1.proof", "proof2.proof", "proof3.proof"),
        publicPaths = arrayOf("public1.public", "public2.public", "public3.public"),
        deviceType = DeviceType.Cpu,
        maxBatchSize = 0L  // 0 = auto-detect optimal batch size
    )
    
    // Process results
    for ((index, result) in results.withIndex()) {
        when (result.value) {
            0 -> println("Proof ${index + 1}: Success")
            1 -> println("Proof ${index + 1}: Failed")
        }
    }
} catch (e: ProverException) {
    println("Parallel proof generation failed: ${e.message}")
}
```

#### Proof Verification
```kotlin
// Verify a proof
val result = NativeBridge.verify(
    proofPath = "path/to/proof.proof",
    publicPath = "path/to/public.public",
    vkPath = "path/to/verification_key.json"
)

when (result) {
    VerifierResult.VerifierSuccess -> println("Proof verified!")
    VerifierResult.VerifierFailure -> println("Proof verification failed!")
}
```

## Example App

The `ExampleApp/` directory contains a complete implementation showing:
- **Library integration** with proper error handling
- **Single and parallel proof generation**
- **Proof verification** for all generated proofs
- **UI examples** with different circuit types (SHA256, AES, etc.)
- **Progress indicators** and user feedback
- **Asset management** for circuit files

### Example App Features
- **Multiple circuit examples**: SHA256, AES-128-CTR, AES-256-CTR, ChaCha20
- **Parallel proof testing**: Generate multiple proofs simultaneously
- **Real-time feedback**: Progress bars and detailed status messages
- **Error handling**: Comprehensive error reporting and recovery
- **Performance metrics**: Timing information for proof generation and verification

## Architecture Support

- **Android**: arm64-v8a only
- **Minimum SDK**: API level 24 (Android 7.0)
- **Target SDK**: API level 36 (Android 14)
- **Java**: 21+ (automatically detected and configured)

## Performance

The library is optimized for mobile devices and includes:
- **Native Rust implementation** for maximum performance
- **Parallel proof generation** for batch processing
- **JNI bridge** for seamless Kotlin integration
- **Memory-efficient** implementations
- **Up to 3x faster** than RapidSnark on mobile devices

## Build Configuration

### Key Build Settings

- **NDK Version**: r27c (automatically downloaded if needed)
- **Rust Targets**: aarch64-linux-android
- **C++ Standard**: C++17
- **STL**: c++_shared
- **Java**: 21+ (auto-detected)

### Build Scripts

- `scripts/build_vendor.sh` - Builds Rust dependencies and native libraries
- `scripts/build_lib.sh` - Builds the AAR library  
- `scripts/build_example_app.sh` - Complete build workflow

## Troubleshooting

### Common Issues

1. **Java version issues**: The build scripts automatically detect and configure Java 21+
2. **NDK not found**: The build script will automatically download NDK r27c
3. **Rust targets missing**: Run `rustup target add aarch64-linux-android`
4. **Build fails**: Ensure all submodules are initialized
5. **Native library errors**: Verify the Rust build completed successfully

## Recent Updates

### Version 0.2.1
- **Parallel proof generation** support
- **Automated build scripts** for easier development

## License

MIT License - see the main project LICENSE file for details. 