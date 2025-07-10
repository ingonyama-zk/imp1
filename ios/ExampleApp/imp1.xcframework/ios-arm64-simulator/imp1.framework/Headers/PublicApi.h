#ifndef PublicApi_h
#define PublicApi_h

#include <Foundation/Foundation.h>

/**
 * @enum DeviceType
 * @brief Specifies the type of device to use for computation
 * @constant CpuMetal Hybrid CPU and Metal computation
 */
typedef NS_ENUM(NSInteger, DeviceType) {
    CpuMetal
};

/**
 * @enum ProofResult
 * @brief Result of proof generation attempt
 * @constant ProverSuccess Proof was successfully generated
 * @constant ProverFailure Proof generation failed
 */
typedef NS_ENUM(NSInteger, ProofResult) {
    ProverSuccess,
    ProverFailure
};

/**
 * @enum VerifierResult
 * @brief Result of proof verification attempt
 * @constant VerifierSuccess Proof was successfully verified
 * @constant VerifierFailure Proof verification failed
 */
typedef NS_ENUM(NSInteger, VerifierResult) {
    VerifierSuccess,
    VerifierFailure
};

/**
 * @brief Generates a proof
 * @param witness_path Path to the witness file containing private inputs
 * @param zkey_path Path to the zkey file containing the proving key
 * @param proof_path Path where the generated proof will be saved
 * @param public_path Path where the public inputs will be saved
 * @param error_msg Buffer to store error messages if proof generation fails
 * @param error_msg_maxsize Maximum size of the error message buffer
 * @param device Type of device to use for computation
 * @return ProofResult indicating success or failure of proof generation
 */
ProofResult prove(
    const char* witness_path,
    const char* zkey_path,
    const char* proof_path,
    const char* public_path,
    const char* error_msg,
    unsigned long long error_msg_maxsize,
    DeviceType device
);

/**
 * @brief Verifies a proof
 * @param proof_path Path to the proof file to verify
 * @param public_path Path to the public inputs file
 * @param vk_path Path to the verification key file
 * @return VerifierResult indicating success or failure of verification
 */
VerifierResult verify(
    const char* proof_path,
    const char* public_path,
    const char* vk_path
);

/**
 * @brief Generates multiple proofs in parallel using the same circuit
 * @param witness_paths Array of paths to witness files containing private inputs
 * @param zkey_path Path to the zkey file containing the proving key (shared for all proofs)
 * @param proof_paths Array of paths where the generated proofs will be saved
 * @param public_paths Array of paths where the public inputs will be saved
 * @param num_proofs Number of proofs to generate in parallel
 * @param error_msg Buffer to store error messages if parallel proof generation fails
 * @param error_msg_maxsize Maximum size of the error message buffer
 * @param device Type of device to use for computation
 * @param max_batch_size Maximum number of proofs to process in a single batch (0 for default of 10)
 * @return Pointer to array of long long integers indicating success (0) or failure (1) of each proof generation
 */
long long* parallel_prove(
    const char** witness_paths,
    const char* zkey_path,
    const char** proof_paths,
    const char** public_paths,
    unsigned long long num_proofs,
    char* error_msg,
    unsigned long long error_msg_maxsize,
    DeviceType device,
    unsigned long long max_batch_size
);

/**
 * @brief Frees the memory allocated for parallel proof results
 * @param results Pointer to the results array returned by parallel_prove
 * @param count Number of results in the array
 */
void free_parallel_results(long long* results, unsigned long long count);

#endif 
