# IMP1

**Drop-in framework for mobile ZK proofs. iOS & Android ready.**

Built with **ICICLE**, this mobile-first proving framework brings **privacy-preserving zero-knowledge proofs** directly to iOS and Android. It’s lightweight, open-source, and optimized for fast, client-side performance with minimal setup required. Try it.

---

## ✨ Features

- 🔧 **ICICLE Engine Inside**  
  Accelerated proving, powered by ICICLE.

- ⚡ **Fastest mobile proving experience**  
  Unmatched speed for client-side ZK proofs.

- 🔓 **Open-source (MIT license)**  
  Free to use, fork, and build upon.

- 📦 **Minimal dependencies**  
  Lightweight and easy to plug in.

- 📱 **Built for iOS and Android**  
  Seamless integration across both major platforms.

- ✅ **Built-in Groth16 support**  
  Ready-to-go proving system included.

---

## 🚀 Quickstart

### Step 1: Download  
Download the package from [GitHub](#).

### Step 2: Install  
Drag **IMP1** into your iOS or Android app.

### Step 3: Prove  
Call `prove()` or `verify()` from your client.

---

## 🔧 Building the iOS XCFramework

> **Note**: Requires `xcodebuild` version **16.2+** and all prerequisites for building ICICLE.

### Steps:

1. Run:
   ```bash
   git submodule update --remote --init --recursive
   ```

2. Clone or link `icicle-metal-backend` into:
   ```
   ./vendor/icicle-snark/vendor/icicle/icicle/backend/metal
   ```
   > Make sure it’s on branch `stas/ios/port1`.

3. Build the xcframework:
   ```bash
   ./scripts/build_xcframework.sh
   ```

4. Distribute the output `.zip` file.

---

## 📁 Project Layout

- `icicle-snark/` – iOS xcframework project: build settings, public headers (`PublicAPI.h`), and Metal bindings (`include_metal.cpp`)
- `metal-cpp/` – Metal C++ headers
- `scripts/` – Build scripts for ICICLE-snark and xcframework
- `vendor/` – Git submodules for ICICLE-snark and ICICLE

---

## 🛠 License

[MIT License](./LICENSE)
