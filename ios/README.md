# IMP1 iOS Framework

This directory contains the iOS implementation of IMP1, a mobile-first zero-knowledge proving framework powered by ICICLE.

## Overview

IMP1 for iOS provides:
- **Native Swift framework** with Metal GPU acceleration
- **Groth16 protocol** implementation for zero-knowledge proofs
- **Parallel proof generation** capabilities
- **Drop-in integration** for iOS applications

## Prerequisites

Before building the framework, ensure you have:
- **Xcode** (latest version recommended)
- **Rust** toolchain installed (`rustup`)
- **Git** with access to the IMP1 repository

## Building the Framework

### Step 1: Clone and Setup
```bash
git clone https://github.com/ingonyama-zk/imp1.git
cd imp1
git submodule update --init --recursive
```

### Step 2: Build the XCFramework
```bash
cd ios
./scripts/build_xcframework.sh
```

This script will:
1. **Build Rust libraries** from the ICICLE-SNARK submodule
2. **Compile for iOS device** (arm64) with Metal support
3. **Compile for iOS simulator** (arm64)
4. **Create a universal XCFramework** that works on both device and simulator
5. **Package the framework** for distribution

### Build Output

The build process creates:
- `Build/imp1.xcframework` - Universal framework for distribution
- `Build/imp1.xcframework.zip` - Compressed package for easy sharing

## Integration

### Using the Framework

1. **Download** the `imp1.xcframework` from the build output
2. **Drag and drop** into your iOS project
3. **Import** the framework in your Swift code:
   ```swift
   import imp1
   ```

### API Usage

The framework provides three main functions:

```swift
// Generate a single proof
let result = prove(witnessPath, zkeyPath, proofPath, publicPath, errorBuffer, errorSize, device)

// Verify a proof
let verificationResult = verify(proofPath, publicPath, vkPath)

// Generate multiple proofs in parallel (iOS only)
let results = parallel_prove(witnessPaths, zkeyPath, proofPaths, publicPaths, numProofs, errorBuffer, errorSize, device, batchSize)
```

## Example App

Check out the `ExampleApp/` directory for a complete implementation showing:
- Framework integration
- Proof generation and verification
- Parallel proof processing
- UI examples for different circuit types

## Architecture Support

- **iOS Device**: arm64 (Apple Silicon and Intel Macs with Rosetta)
- **iOS Simulator**: arm64 (Apple Silicon Macs)

## Performance

The framework is optimized for mobile devices and includes:
- **Metal GPU acceleration** for faster proving
- **Parallel processing** capabilities
- **Memory-efficient** implementations
- **Up to 3x faster** than RapidSnark on mobile devices

## Troubleshooting

### Common Issues

1. **Rust toolchain not found**: Install Rust via `rustup`
2. **Build fails**: Ensure all submodules are initialized
3. **Metal framework errors**: Verify Xcode version supports Metal

### Build Scripts

- `scripts/build_vendor.sh` - Builds Rust dependencies
- `scripts/build_xcframework.sh` - Complete framework build process

## License

MIT License - see the main project LICENSE file for details. 