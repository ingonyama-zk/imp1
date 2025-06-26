package com.ingonyama.imp1

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

enum class DeviceType(val value: Int) {
    Cpu(1),
}

enum class ProverResult(val value: Int) {
    ProverSuccess(0),
    ProverFailure(1);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}

enum class VerifierResult(val value: Int) {
    VerifierSuccess(0),
    VerifierFailure(1);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}

class ProverException(message: String) : Exception(message)

object NativeBridge {
    init {
        System.loadLibrary("c++_shared")
        System.loadLibrary("imp1")
    }

    /**
     * Generates a proof.
     *
     * @param witnessPath Path to the witness file.
     * @param zkeyPath Path to the zkey file.
     * @param proofPath Path where the output proof will be saved.
     * @param publicPath Path where the output public signals will be saved.
     * @param deviceType The type of device to use for proving.
     * @throws ProverException if the Rust prove function returns a failure.
     */
    fun prove(
        witnessPath: String,
        zkeyPath: String,
        proofPath: String,
        publicPath: String,
        deviceType: DeviceType
    ) {
        val errorMsgMaxSize = 256
        val errorBuffer = ByteBuffer.allocateDirect(errorMsgMaxSize).order(ByteOrder.nativeOrder())

        val result = ProverResult.fromInt(
            proveNative(witnessPath, zkeyPath, proofPath, publicPath, errorBuffer, deviceType.value)
        )

        if (result == ProverResult.ProverFailure) {
            val errorBytes = ByteArray(errorBuffer.position())
            errorBuffer.rewind()
            errorBuffer.get(errorBytes)
            val errorMessage = String(errorBytes, StandardCharsets.UTF_8).trim()
            throw ProverException(errorMessage)
        }
    }

    /**
     * Verifies a proof.
     *
     * @param proofPath Path to the proof file.
     * @param publicPath Path to the public signals file.
     * @param vkPath Path to the verification key file.
     * @return The result of the verification.
     */
    fun verify(
        proofPath: String,
        publicPath: String,
        vkPath: String
    ): VerifierResult {
        return VerifierResult.fromInt(
            verifyNative(proofPath, publicPath, vkPath)
        )
    }

    // Private external functions that link to the JNI bridge
    private external fun proveNative(
        witnessPath: String,
        zkeyPath: String,
        proofPath: String,
        publicPath: String,
        errorMsgBuffer: ByteBuffer,
        deviceType: Int
    ): Int

    private external fun verifyNative(
        proofPath: String,
        publicPath: String,
        vkPath: String
    ): Int
}