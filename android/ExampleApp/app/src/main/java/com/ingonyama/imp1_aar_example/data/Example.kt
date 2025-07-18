package com.ingonyama.imp1_aar_example.data

/**
 * Data class to hold the filenames for each test case.
 * The name will be displayed in the Spinner.
 */
data class Example(
    val name: String,
    val witnessAsset: String,
    val zkeyAsset: String,
    val vkAsset: String,
    val assetPack: String  // Which asset pack contains the zkey
) {
    // This makes the spinner show the 'name' property
    override fun toString(): String = name
}

/**
 * List of examples with their corresponding asset packs.
 */
object Examples {
    val list = listOf(
        Example("100k", "100k_witness.wtns", "100k_circuit_final.zkey", "100k_verification_key.json", "zkey_pack_0"),
        Example("200k", "200k_witness.wtns", "200k_circuit_final.zkey", "200k_verification_key.json", "zkey_pack_0"),
        Example("400k", "400k_witness.wtns", "400k_circuit_final.zkey", "400k_verification_key.json", "zkey_pack_0"),
        Example("800k", "800k_witness.wtns", "800k_circuit_final.zkey", "800k_verification_key.json", "zkey_pack_1"),
        Example("1600k", "1600k_witness.wtns", "1600k_circuit_final.zkey", "1600k_verification_key.json", "zkey_pack_1"),
        Example("Sha256", "sha256_witness.wtns", "sha256_circuit_final.zkey", "sha256_verification_key.json", "zkey_pack_1"),
        Example("Rarimo", "rarimo_witness.wtns", "rarimo_circuit_final.zkey", "rarimo_verification_key.json", "zkey_pack_rarimo"),
        Example("Keccak", "keccak_witness.wtns", "keccak_circuit_final.zkey", "keccak_verification_key.json", "zkey_pack_1"),
        Example("aes-128", "aes_128_ctr_witness.wtns", "aes_128_ctr_circuit_final.zkey", "aes_128_ctr_verification_key.json", "zkey_pack_zkp2p"),
        Example("aes-256", "aes_256_ctr_witness.wtns", "aes_256_ctr_circuit_final.zkey", "aes_256_ctr_verification_key.json", "zkey_pack_zkp2p"),
        Example("chacha20", "chacha20_witness.wtns", "chacha20_circuit_final.zkey", "chacha20_verification_key.json", "zkey_pack_zkp2p"),
    )
} 