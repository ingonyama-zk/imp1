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

#endif 
