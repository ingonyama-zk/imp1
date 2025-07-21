package com.ingonyama.imp1

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

// Use unified enum system
typealias DeviceType = Enums.DeviceType
typealias ProverResult = Enums.ProverResult
typealias VerifierResult = Enums.VerifierResult
typealias ProofResult = Enums.ProofResult

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

        if (result == ProverResult.FAILURE) {
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

    /**
     * Generates multiple proofs in parallel.
     *
     * @param witnessPaths Array of paths to witness files.
     * @param zkeyPath Path to the zkey file (shared for all proofs).
     * @param proofPaths Array of paths where output proofs will be saved.
     * @param publicPaths Array of paths where output public signals will be saved.
     * @param deviceType The type of device to use for proving.
     * @param maxBatchSize Maximum batch size for processing (0 for default).
     * @return Array of ProofResult indicating success/failure for each proof.
     * @throws ProverException if the Rust parallel_prove function returns a failure.
     */
    fun parallelProve(
        witnessPaths: Array<String>,
        zkeyPath: String,
        proofPaths: Array<String>,
        publicPaths: Array<String>,
        deviceType: DeviceType,
        maxBatchSize: Long = 0
    ): Array<ProofResult> {
        require(witnessPaths.size == proofPaths.size && proofPaths.size == publicPaths.size) {
            "All path arrays must have the same size"
        }
        
        val errorMsgMaxSize = 256
        val errorBuffer = ByteBuffer.allocateDirect(errorMsgMaxSize).order(ByteOrder.nativeOrder())
        
        val resultsPtr = parallelProveNative(
            witnessPaths,
            zkeyPath,
            proofPaths,
            publicPaths,
            errorBuffer,
            deviceType.value,
            maxBatchSize
        )
        
        if (resultsPtr == 0L) {
            // Error occurred
            val errorBytes = ByteArray(errorBuffer.position())
            errorBuffer.rewind()
            errorBuffer.get(errorBytes)
            val errorMessage = String(errorBytes, StandardCharsets.UTF_8).trim()
            throw ProverException(errorMessage)
        }
        
        // Convert results pointer to array
        val results = Array(witnessPaths.size) { index ->
            val resultValue = getProverResultValue(resultsPtr, index)
            ProofResult.fromInt(resultValue)
        }
        
        // Free the native memory
        freeParallelResultsNative(resultsPtr, witnessPaths.size)
        
        return results
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

    private external fun parallelProveNative(
        witnessPaths: Array<String>,
        zkeyPath: String,
        proofPaths: Array<String>,
        publicPaths: Array<String>,
        errorMsgBuffer: ByteBuffer,
        deviceType: Int,
        maxBatchSize: Long
    ): Long

    private external fun getProverResultValue(resultsPtr: Long, index: Int): Int

    private external fun freeParallelResultsNative(resultsPtr: Long, count: Int)
}