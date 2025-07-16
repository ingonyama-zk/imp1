# Android IMP1 Example app

## Zkey files

As zkey files are quite large, none of the example circuits' zkey files are part of the repo. They must be downloaded separately and added to each zkey_pack_X as follows:

- zkey_pack_0 should contain:
    - 100k_circuit_final.zkey
    - 200k_circuit_final.zkey
    - 400k_circuit_final.zkey
- zkey_pack_1 should contain:
    - 800k_circuit_final.zkey
    - 1600k_circuit_final.zkey
    - keccak_circuit_final.zkey
    - sha256_circuit_final.zkey
- zkey_pack_rarimo should contain:
    - rarimo_circuit_final.zkey
- zkey_pack_zkp2p should contain:
    - aes_128_ctr_circuit_final.zkey
    - aes_256_ctr_circuit_final.zkey
    - chacha20_circuit_final.zkey

## Build

Use the [build_example_app](../scripts/build_example_app.sh) to build the app for both debug and release buildTypes.

Optionally provide the `--debug` flag to include additional debug capabilities in native code.
