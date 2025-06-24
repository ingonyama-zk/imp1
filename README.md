# IMP1

<div align="center">
  <img src="https://github.com/user-attachments/assets/67d85e08-5739-40a4-84a1-f631d3280eaf" alt="Ice Imp" width="300"/>
</div>

**Drop-in framework for mobile ZK proofs. iOS-ready, Android coming soon.**

Built with ICICLE, this mobile-first proving framework brings privacy-preserving zero-knowledge proofs directly to iOS and Android. It’s lightweight, open-source, and optimized for fast, client-side performance with minimal setup required. IMP1 is built on ICICLE-SNARK, an end-to-end prover powered by ICICLE.

> **_NOTE:_** Android support is coming soon!

## ✨ Features

🔧 **ICICLE Engine Inside**  
  Accelerated proving, powered by ICICLE.

⚡ **Fastest mobile proving experience**  
Up to 3x faster than Rapidsnark

🔓 **Open-source (MIT license)**  
  Free to use, fork, and build upon.

📦 **Minimal dependencies**  
  Lightweight and easy to plug in.

📱 **Built for iOS and Android**  
  Seamless integration across both major platforms.  
  _⚠ Android support coming soon_

✅ **Mobile-optimized Groth16 prover**
An efficient implementation of Groth16, built for mobile devices.

---
## 📊 Performance

<div align="center">
  <img src="IMP iphone.png" alt="IMP1 performance by circuit size" width="400"/>
</div>

<div align="center">
  <img src="IMP rarimo.png" alt="IMP1 performance across devices" width="400"/>
</div>

---

## 🚀 Quickstart

### Step 1: Download  
[Download the package](https://github.com/ingonyama-zk/imp1/releases/download/v0.2.0/imp1.xcframework.zip) 

### Step 2: Install  
Unzip & Drag **IMP1.xcframework** into your iOS or Android app.

### Step 3: Prove
Use the `prove()` and `verify()` functions from the framework’s public interface.
See [`PublicApi.h`](https://github.com/ingonyama-zk/imp1/blob/main/icicle-snark/icicle-snark/PublicApi.h) for function definitions, usage comments, and required types.

---

## 🧪 Examples
To get started quickly, check out the [IMP1 Example App](https://github.com/ingonyama-zk/imp1/tree/main/ios/ExampleApp) inside — a minimal iOS project showing how to integrate and use the framework in a real mobile environment.

📱 Android support is coming soon. An Android example will be available once the Android version of the framework is released.


## 🛠 License

[MIT License](./LICENSE)
