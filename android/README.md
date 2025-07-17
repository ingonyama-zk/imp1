# IMP1 Android Library

This directory contains the Android implementation of IMP1, a mobile-first zero-knowledge proving framework powered by ICICLE.

## Overview

IMP1 for Android provides:
- **Native Kotlin library** with JNI bridge to Rust
- **Groth16 protocol** implementation for zero-knowledge proofs
- **Optimized for arm64-v8a** architecture
- **Drop-in integration** for Android applications

## Prerequisites

Before building the library, ensure you have:
- **Android Studio** (latest version recommended)
- **Android SDK** with API level 24+
- **Rust** toolchain installed (`rustup`)
- **Android NDK** (r26b or later)
- **Git** with access to the IMP1 repository

## Building the Library

### Step 1: Clone and Setup
```bash
git clone https://github.com/ingonyama-zk/imp1.git
cd imp1
git submodule update --init --recursive
```

### Step 2: Build Rust Dependencies
```bash
cd android
./scripts/build_vendor.sh
./scripts/build_vendor.sh --ndk-version=27.1.12297006
```

This script will:
- **Download Android NDK** if not present locally
- **Install Rust Android targets** (aarch64-linux-android)
- **Build ICICLE-SNARK** for Android arm64-v8a
- **Copy native libraries** to the Android project

### Step 3: Build the AAR Library
```bash
cd imp1
./gradlew assembleRelease
```

This creates:
- `app/build/outputs/aar/imp1-0.2.0.aar` - The final AAR library

### Step 4: Build and Test the Example App
```bash
cd ../ExampleApp
./gradlew assembleDebug
./gradlew installDebug
```

This will:
- **Build the example app** using the IMP1 library
- **Install it on a connected device** (make sure to authorize USB debugging)
- **Test the ZK proof functionality** on your Android device

## Complete Workflow

For a full development cycle:

```bash
# 1. Build the library
cd android/imp1
./gradlew assembleRelease

# 2. Build and deploy the example app
cd ../ExampleApp
./gradlew installDebug

# 3. When you make changes to the library, repeat:
cd ../imp1
./gradlew assembleRelease
cp app/build/outputs/aar/imp1-0.2.0.aar ../ExampleApp/app/libs/
cd ../ExampleApp
./gradlew installDebug
```

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
└── scripts/                 # Build utilities
```

## Integration

### Using the Library

1. **Copy the AAR** to your Android project's `libs/` directory
2. **Add dependency** in your `build.gradle.kts`:
   ```kotlin
   implementation(files("libs/imp1-0.2.0.aar"))
   ```
3. **Import** the library in your Kotlin code:
   ```kotlin
   import com.ingonyama.imp1.NativeBridge
   import com.ingonyama.imp1.DeviceType
   ```

### API Usage

The library provides a simple interface through `NativeBridge`:

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

Check out the `ExampleApp/` directory for a complete implementation showing:
- Library integration
- Proof generation and verification
- UI examples with different circuit types
- Error handling and progress indicators

export JAVA_HOME=/opt/homebrew/opt/openjdk && ./scripts/build_example_app.sh --debug

## Architecture Support

- **Android**: arm64-v8a only
- **Minimum SDK**: API level 24 (Android 7.0)
- **Target SDK**: API level 36 (Android 14)

## Performance

The library is optimized for mobile devices and includes:
- **Native Rust implementation** for maximum performance
- **JNI bridge** for seamless Kotlin integration
- **Memory-efficient** implementations
- **Up to 3x faster** than RapidSnark on mobile devices

## Build Configuration

### Key Build Settings

- **NDK Version**: r26b (automatically downloaded if needed)
- **Rust Targets**: aarch64-linux-android
- **C++ Standard**: C++17
- **STL**: c++_shared

### Build Scripts

- `scripts/build_vendor.sh` - Builds Rust dependencies and native libraries
- `imp1/app/build.gradle.kts` - Main library build configuration
- `imp1/gradle.properties` - Gradle configuration

## Troubleshooting

### Common Issues

1. **NDK not found**: The build script will automatically download NDK r26b
2. **Rust targets missing**: Run `rustup target add aarch64-linux-android`
3. **Build fails**: Ensure all submodules are initialized
4. **Native library errors**: Verify the Rust build completed successfully

### Build Commands

```bash
# Clean build
./gradlew clean

# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease

# Run tests
./gradlew test
```

## License

MIT License - see the main project LICENSE file for details. 