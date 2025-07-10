# IMP1

<div align="center">
  <img src="https://github.com/user-attachments/assets/67d85e08-5739-40a4-84a1-f631d3280eaf" alt="Ice Imp" width="300"/>
</div>

**Drop-in framework for mobile ZK proofs.**

Built with ICICLE, this mobile-first proving framework brings privacy-preserving zero-knowledge proofs directly to iOS and Android. It’s lightweight, open-source, and optimized for fast, client-side performance with minimal setup required. IMP1 is built on ICICLE-SNARK, an end-to-end prover powered by ICICLE.

> [!NOTE]
> Android support for arm64-v8a architecture only.

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
  _⚠ Android support for arm64-v8a architecture only._

✅ **Mobile-optimized Groth16 prover**
An efficient implementation of Groth16, built for mobile devices.

---

## 📊 Performance

<div align="center">

<table>
  <tr>
    <td align="center" style="padding-right: 20px;">
      <img src="./IMP iphone.png" alt="IMP1 performance by circuit size" width="400"/>
      <br/>
      <em>Proof time by circuit size (iPhone 16 Pro)</em>
    </td>
    <td align="center" style="padding-left: 20px;">
      <img src="./IMP rarimo.png" alt="IMP1 performance across devices" width="400"/>
      <br/>
      <em>Device comparison on Rarimo–Bionetta circuit</em>
    </td>
  </tr>
</table>

</div>

---

## 🚀 Quickstart

### iOS

1. [Download the xcframework](https://github.com/ingonyama-zk/imp1/releases/download/v0.2.0/imp1.xcframework.zip)
2. Unzip & Drag **imp1.xcframework** into your iOS app.
3. Use the `prove()` and `verify()` functions from the framework’s public interface. See [`PublicApi.h`](./ios/imp1/imp1/PublicApi.h) for function definitions, usage comments, and required types.

### Android

1. [Download the aar](https://github.com/ingonyama-zk/imp1/releases/download/v0.2.0/imp1-0.2.0.aar)
2. Drag **imp1-0.2.0.aar** into your Android app.
3. Use the `prove()` and `verify()` functions from the library’s native bridge. See [`NativeBridge.kt`](./android/imp1/app/src/main/java/com/ingonyama/imp1/NativeBridge.kt) for function definitions, usage comments, and required types.

---

## 🧪 Examples

To get started quickly, check out the example [iOS](./ios/ExampleApp) or [Android](./android/ExampleApp/) App inside — a minimal project showing how to integrate and use imp1 in a real mobile environment.

## 🛠 License

[MIT License](./LICENSE)
