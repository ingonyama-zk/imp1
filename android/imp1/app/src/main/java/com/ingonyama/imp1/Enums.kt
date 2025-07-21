package com.ingonyama.imp1

/**
 * Unified enum system for JNI communication.
 * This consolidates all enums and structs to eliminate redundancy.
 */
object Enums {
    
    /**
     * Device types for proof generation.
     */
    enum class DeviceType(val value: Int) {
        CPU(1);
        
        companion object {
            fun fromInt(value: Int): DeviceType = entries.firstOrNull { it.value == value } ?: CPU
        }
    }
    
    /**
     * Results from proof generation operations.
     */
    enum class ProverResult(val value: Int) {
        SUCCESS(0),
        FAILURE(1);
        
        companion object {
            fun fromInt(value: Int): ProverResult = entries.firstOrNull { it.value == value } ?: FAILURE
        }
    }
    
    /**
     * Results from proof verification operations.
     */
    enum class VerifierResult(val value: Int) {
        SUCCESS(0),
        FAILURE(1);
        
        companion object {
            fun fromInt(value: Int): VerifierResult = entries.firstOrNull { it.value == value } ?: FAILURE
        }
    }
    
    /**
     * Wrapper for proof results that maintains compatibility with existing code.
     */
    data class ProofResult(val value: Int) {
        companion object {
            val ProverSuccess = ProofResult(ProverResult.SUCCESS.value)
            val ProverFailure = ProofResult(ProverResult.FAILURE.value)
            
            fun fromInt(value: Int): ProofResult = ProofResult(value)
        }
        
        val isSuccess: Boolean get() = value == ProverResult.SUCCESS.value
        val isFailure: Boolean get() = value == ProverResult.FAILURE.value
    }
} 