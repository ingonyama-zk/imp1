# Android IMP1 Example app

## Zkey files

As zkey files are quite large, none of the example circuits' zkey files are part of the repo. They must be downloaded separately and placed in the correct directory structure for Android asset packs.

### Important: Correct Directory Structure

The .zkey files must be placed in the `src/main/assets/` subdirectory within each zkey_pack directory:

```
ExampleApp/
├── zkey_pack_0/
│   └── src/main/assets/
│       ├── 100k_circuit_final.zkey
│       ├── 200k_circuit_final.zkey
│       └── 400k_circuit_final.zkey
├── zkey_pack_1/
│   └── src/main/assets/
│       ├── 800k_circuit_final.zkey
│       ├── 1600k_circuit_final.zkey
│       ├── keccak_circuit_final.zkey
│       └── sha256_circuit_final.zkey
├── zkey_pack_rarimo/
│   └── src/main/assets/
│       └── rarimo_circuit_final.zkey
└── zkey_pack_zkp2p/
    └── src/main/assets/
        ├── aes_128_ctr_circuit_final.zkey
        ├── aes_256_ctr_circuit_final.zkey
        └── chacha20_circuit_final.zkey
```

### Required Files by Asset Pack

- **zkey_pack_0** should contain in `src/main/assets/`:
    - 100k_circuit_final.zkey
    - 200k_circuit_final.zkey
    - 400k_circuit_final.zkey
- **zkey_pack_1** should contain in `src/main/assets/`:
    - 800k_circuit_final.zkey
    - 1600k_circuit_final.zkey
    - keccak_circuit_final.zkey
    - sha256_circuit_final.zkey
- **zkey_pack_rarimo** should contain in `src/main/assets/`:
    - rarimo_circuit_final.zkey
- **zkey_pack_zkp2p** should contain in `src/main/assets/`:
    - aes_128_ctr_circuit_final.zkey
    - aes_256_ctr_circuit_final.zkey
    - chacha20_circuit_final.zkey

### Note
The .wtns and .json files are placed in the main app's assets directory (`app/src/main/assets/`), while the .zkey files go in the asset pack directories as shown above.

## Build

Use the [build_example_app](../scripts/build_example_app.sh) to build the app for both debug and release buildTypes.

Optionally provide the `--debug` flag to include additional debug capabilities in native code.
