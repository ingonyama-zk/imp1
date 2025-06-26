#include <jni.h>
#include <string>
#include <vector>
#include <memory>

enum DeviceType {
    Cpu = 1,
};

enum ProverResult {
    ProverSuccess = 0,
    ProverFailure = 1,
};

enum VerifierResult {
    VerifierSuccess = 0,
    VerifierFailure = 1,
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
            {"verifyNative", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void*)jni_verify}
    };

    // Register the methods
    int rc = env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(JNINativeMethod));
    if (rc != JNI_OK) {
        return rc;
    }

    return JNI_VERSION_1_6;
}