#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <android/log.h>

enum DeviceType {
    CPU = 1,
};

struct ProofResult {
    int value;
};

enum ProverResult {
    PROVER_SUCCESS = 0,
    PROVER_FAILURE = 1,
};

enum VerifierResult {
    VERIFIER_SUCCESS = 0,
    VERIFIER_FAILURE = 1,
};

extern "C" {
    ProverResult prove(
            const char* witness_path,
            const char* zkey_path,
            const char* proof_path,
            const char* public_path,
            char* error_msg,
            unsigned long long error_msg_maxsize,
            DeviceType device_type
    );

    VerifierResult verify(
            const char* proof_path,
            const char* public_path,
            const char* vk_path
    );

    // Parallel proof functions
    const ProofResult* parallel_prove(
            const char** witness_paths,
            const char* zkey_path,
            const char** proof_paths,
            const char** public_paths,
            unsigned long long num_proofs,
            char* error_msg,
            unsigned long long error_msg_maxsize,
            DeviceType device_type,
            unsigned long long max_batch_size
    );

    void free_parallel_results(const ProofResult* results, unsigned long long count);
}

// Helper class to convert jstring to cstr with correct memory management
class JniString {
public:
    JniString(JNIEnv* env, jstring jstr) : env_(env), jstr_(jstr), cstr_(nullptr) {
        if (jstr) {
            cstr_ = env->GetStringUTFChars(jstr, nullptr);
        }
    }

    ~JniString() {
        if (cstr_) {
            env_->ReleaseStringUTFChars(jstr_, cstr_);
        }
    }

    const char* get() const {
        return cstr_;
    }

private:
    JNIEnv* env_;
    jstring jstr_;
    const char* cstr_;
};


jint jni_prove(
        JNIEnv *env,
        jobject /* this */,
        jstring witness_path,
        jstring zkey_path,
        jstring proof_path,
        jstring public_path,
        jobject error_msg_buffer, // A Java ByteBuffer for the error message
        jint device_type
) {
    JniString witness(env, witness_path);
    JniString zkey(env, zkey_path);
    JniString proof(env, proof_path);
    JniString public_p(env, public_path);

    char* error_buf = nullptr;
    jlong error_buf_size = 0;
    if (error_msg_buffer != nullptr) {
        error_buf = static_cast<char*>(env->GetDirectBufferAddress(error_msg_buffer));
        error_buf_size = env->GetDirectBufferCapacity(error_msg_buffer);
    }

    if (error_buf != nullptr) {
        // Clear the buffer before use
        error_buf[0] = '\0';
    }

    ProverResult result = prove(
            witness.get(),
            zkey.get(),
            proof.get(),
            public_p.get(),
            error_buf,
            static_cast<unsigned long long>(error_buf_size),
            static_cast<DeviceType>(device_type)
    );

    return static_cast<jint>(result);
}

jint jni_verify(
        JNIEnv *env,
        jobject /* this */,
        jstring proof_path,
        jstring public_path,
        jstring vk_path
) {
    JniString proof(env, proof_path);
    JniString public_p(env, public_path);
    JniString vk(env, vk_path);

    VerifierResult result = verify(proof.get(), public_p.get(), vk.get());

    return static_cast<jint>(result);
}

jlong jni_parallel_prove(
        JNIEnv *env,
        jobject /* this */,
        jobjectArray witness_paths,
        jstring zkey_path,
        jobjectArray proof_paths,
        jobjectArray public_paths,
        jobject error_msg_buffer,
        jint device_type,
        jlong max_batch_size
) {
    // Get array lengths
    jsize num_proofs = env->GetArrayLength(witness_paths);
    
    // Convert Java string arrays to C string arrays using raw C strings
    std::vector<const char*> witness_ptrs;
    std::vector<const char*> proof_ptrs;
    std::vector<const char*> public_ptrs;
    
    // Reserve space to avoid reallocation
    witness_ptrs.reserve(num_proofs);
    proof_ptrs.reserve(num_proofs);
    public_ptrs.reserve(num_proofs);
    
    // Process witness paths
    for (int i = 0; i < num_proofs; i++) {
        jstring witness_str = static_cast<jstring>(env->GetObjectArrayElement(witness_paths, i));
        const char* witness_cstr = env->GetStringUTFChars(witness_str, nullptr);
        witness_ptrs.push_back(witness_cstr);
    }
    
    // Process proof paths
    for (int i = 0; i < num_proofs; i++) {
        jstring proof_str = static_cast<jstring>(env->GetObjectArrayElement(proof_paths, i));
        const char* proof_cstr = env->GetStringUTFChars(proof_str, nullptr);
        proof_ptrs.push_back(proof_cstr);
    }
    
    // Process public paths
    for (int i = 0; i < num_proofs; i++) {
        jstring public_str = static_cast<jstring>(env->GetObjectArrayElement(public_paths, i));
        const char* public_cstr = env->GetStringUTFChars(public_str, nullptr);
        public_ptrs.push_back(public_cstr);
    }
    
    // Get zkey path
    JniString zkey(env, zkey_path);
    
    // Get error buffer
    char* error_buf = nullptr;
    jlong error_buf_size = 0;
    if (error_msg_buffer != nullptr) {
        error_buf = static_cast<char*>(env->GetDirectBufferAddress(error_msg_buffer));
        error_buf_size = env->GetDirectBufferCapacity(error_msg_buffer);
    }
    
    if (error_buf != nullptr) {
        // Clear the buffer before use
        error_buf[0] = '\0';
    }
    

    
    // Call the parallel_prove function
    const ProofResult* results = parallel_prove(
            witness_ptrs.data(),
            zkey.get(),
            proof_ptrs.data(),
            public_ptrs.data(),
            static_cast<unsigned long long>(num_proofs),
            error_buf,
            static_cast<unsigned long long>(error_buf_size),
            static_cast<DeviceType>(device_type),
            static_cast<unsigned long long>(max_batch_size)
    );
    
    // Clean up JNI string references
    for (int i = 0; i < num_proofs; i++) {
        jstring witness_str = static_cast<jstring>(env->GetObjectArrayElement(witness_paths, i));
        env->ReleaseStringUTFChars(witness_str, witness_ptrs[i]);
        
        jstring proof_str = static_cast<jstring>(env->GetObjectArrayElement(proof_paths, i));
        env->ReleaseStringUTFChars(proof_str, proof_ptrs[i]);
        
        jstring public_str = static_cast<jstring>(env->GetObjectArrayElement(public_paths, i));
        env->ReleaseStringUTFChars(public_str, public_ptrs[i]);
    }
    
    // Return the pointer to results (0 if error occurred)
    return reinterpret_cast<jlong>(results);
}

jint jni_get_prover_result_value(
        JNIEnv *env,
        jobject /* this */,
        jlong results_ptr,
        jint index
) {
    const ProofResult* results = reinterpret_cast<const ProofResult*>(results_ptr);
    if (results == nullptr) {
        return static_cast<jint>(PROVER_FAILURE);
    }
    return static_cast<jint>(results[index].value);
}

void jni_free_parallel_results(
        JNIEnv *env,
        jobject /* this */,
        jlong results_ptr,
        jint count
) {
    const ProofResult* results = reinterpret_cast<const ProofResult*>(results_ptr);
    if (results != nullptr) {
        free_parallel_results(results, static_cast<unsigned long long>(count));
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // Find the Java class that declares the native methods
    jclass clazz = env->FindClass("com/ingonyama/imp1/NativeBridge");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    // Define the mapping between the Java method and the C++ function
    static const JNINativeMethod methods[] = {
            {"proveNative", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/nio/ByteBuffer;I)I", (void*)jni_prove},
            {"verifyNative", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void*)jni_verify},
            {"parallelProveNative", "([Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;Ljava/nio/ByteBuffer;IJ)J", (void*)jni_parallel_prove},
            {"getProverResultValue", "(JI)I", (void*)jni_get_prover_result_value},
            {"freeParallelResultsNative", "(JI)V", (void*)jni_free_parallel_results}
    };

    // Register the methods
    int rc = env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod));
    if (rc != JNI_OK) {
        return rc;
    }

    return JNI_VERSION_1_6;
}