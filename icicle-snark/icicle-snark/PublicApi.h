#ifndef PublicApi_h
#define PublicApi_h

#include <Foundation/Foundation.h>
#include <Metal/Metal.h>

typedef NS_ENUM(NSInteger, DeviceType) {
    Cpu,
    Metal,
    CpuMetal
};

typedef NS_ENUM(NSInteger, ProofResult) {
    ProverSuccess,
    ProverFailure
};

typedef NS_ENUM(NSInteger, VerifierResult) {
    VerifierSuccess,
    VerifierFailure
};

ProofResult prove(
    const char* witness_path,
    const char* zkey_path,
    const char* proof_path,
    const char* public_path,
    const char* error_msg,
    unsigned long long error_msg_maxsize,
    DeviceType device
);

VerifierResult verify(
    const char* proof_path,
    const char* public_path,
    const char* vk_path
);

#endif 
