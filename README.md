# icicle-snark xcframework

iOS framework containing icicle-snark for cpu and/or metal

## Building the xcframework

> [!IMPORTANT]
> xcodebuild version 16.2 and all prereqs for building icicle are needed

1. Run `git submodule update --remote --init --recursive`
2. Clone or link `icicle-metal-backend` in `./vendor/icicle-snark/vendor/icicle/icicle/backend/metal`. **Make sure it is on the branch `stas/ios/port1`**
3. Run ./scripts/build_xcframework.sh
4. Distribute the framework's .zip file

## Project Layout

- icicle-snark contains the iOS xcframework project (build settings), public API headers (PublicAPI.h), and metal bindings (include_metal.cpp)
- metal-cpp contains the actual metal cpp headers
- scripts contains build scripts for compiling icicle-snark prover and for building the xcframework
- vendor contains git submodules for icicle-snark and icicle
