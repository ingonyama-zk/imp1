package com.ingonyama.imp1_aar_example

/**
 * Centralized constants for the IMP1 Android example app.
 * This file contains all magic numbers, strings, and configuration values
 * used throughout the application to improve maintainability.
 */
object Constants {
    
    // ============================================================================
    // PARALLEL PROOF CONFIGURATION
    // ============================================================================
    
    /** Minimum number of parallel proofs allowed */
    const val MIN_PARALLEL_PROOFS = 1
    
    /** Maximum number of parallel proofs allowed */
    const val MAX_PARALLEL_PROOFS = 50
    
    /** Default number of parallel proofs */
    const val DEFAULT_PARALLEL_PROOFS = 2
    
    // ============================================================================
    // FILE EXTENSIONS AND PATTERNS
    // ============================================================================
    
    /** File extension for proof files */
    const val PROOF_FILE_EXTENSION = ".proof"
    
    /** File extension for public input files */
    const val PUBLIC_FILE_EXTENSION = ".public"
    
    /** File extension for witness files */
    const val WITNESS_FILE_EXTENSION = ".wtns"
    
    /** File extension for zkey files */
    const val ZKEY_FILE_EXTENSION = ".zkey"
    
    /** File extension for verification key files */
    const val VK_FILE_EXTENSION = ".json"
    
    // ============================================================================
    // DEFAULT FILE NAMES
    // ============================================================================
    
    /** Default witness file name */
    const val DEFAULT_WITNESS_FILE = "witness.wtns"
    
    /** Default zkey file name */
    const val DEFAULT_ZKEY_FILE = "zkey.zkey"
    
    /** Default verification key file name */
    const val DEFAULT_VK_FILE = "vk.json"
    
    /** Default proof file name */
    const val DEFAULT_PROOF_FILE = "test.proof"
    
    /** Default public file name */
    const val DEFAULT_PUBLIC_FILE = "test.public"
    
    // ============================================================================
    // PARALLEL FILE NAMING PATTERNS
    // ============================================================================
    
    /** Pattern for parallel witness files */
    const val PARALLEL_WITNESS_PATTERN = "witness_%d.wtns"
    
    /** Pattern for parallel proof files */
    const val PARALLEL_PROOF_PATTERN = "proof_%d.proof"
    
    /** Pattern for parallel public files */
    const val PARALLEL_PUBLIC_PATTERN = "public_%d.public"
    
    
    // ============================================================================
    // LOG MESSAGES
    // ============================================================================
    
    /** Log message for starting test */
    const val LOG_STARTING_TEST = "Starting test for: %s"
    
    /** Log message for starting parallel test */
    const val LOG_STARTING_PARALLEL_TEST = "Starting parallel proof test for: %s"
    
    /** Log message for number of parallel proofs */
    const val LOG_NUM_PARALLEL_PROOFS = "Number of parallel proofs: %d"
    
    /** Log message for copying assets */
    const val LOG_COPYING_ASSETS = "Copying assets to device storage..."
    
    /** Log message for copying complete */
    const val LOG_COPYING_COMPLETE = "...copying complete."
    
    /** Log message for running prover */
    const val LOG_RUNNING_PROVER = "\nRunning Prover..."
    
    /** Log message for running parallel prover */
    const val LOG_RUNNING_PARALLEL_PROVER = "\nRunning Parallel Prover..."
    
    /** Log message for running verifier */
    const val LOG_RUNNING_VERIFIER = "\nRunning Verifier..."
    
    /** Log message for running verifier for all proofs */
    const val LOG_RUNNING_VERIFIER_ALL = "\nRunning Verifier for all proofs..."
    
    /** Log message for prove success */
    const val LOG_PROVE_SUCCESS = "✅ Prove SUCCESSFUL"
    
    /** Log message for parallel prove completed */
    const val LOG_PARALLEL_PROVE_COMPLETED = "✅ Parallel Prove completed"
    
    /** Log message for verify success */
    const val LOG_VERIFY_SUCCESS = "✅ Verify SUCCESSFUL"
    
    /** Log message for verify failed */
    const val LOG_VERIFY_FAILED = "❌ Verify FAILED"
    
    /** Log message for verification completed */
    const val LOG_VERIFICATION_COMPLETED = "✅ Verification completed"
    
    /** Log message for prove failed */
    const val LOG_PROVE_FAILED = "❌ Prove FAILED"
    
    /** Log message for parallel prove failed */
    const val LOG_PARALLEL_PROVE_FAILED = "❌ Parallel Prove FAILED"
    
    /** Log message for critical file error */
    const val LOG_CRITICAL_FILE_ERROR = "\n❌ CRITICAL ERROR: Could not copy asset files."
    
    /** Log message for file error instructions */
    const val LOG_FILE_ERROR_INSTRUCTIONS = "   Make sure the filenames in the `examples` list are correct."
    
    /** Log message for time taken */
    const val LOG_TIME_TAKEN = "   Time taken: %d ms"
    
    /** Log message for runtime */
    const val LOG_RUNTIME = "   Runtime: %s"
    
    /** Log message for error details */
    const val LOG_ERROR_DETAILS = "   Error: %s"
    
    /** Log message for proof success */
    const val LOG_PROOF_SUCCESS = "✅ Proof %d: Success"
    
    /** Log message for proof failed */
    const val LOG_PROOF_FAILED = "❌ Proof %d: Failed"
    
    /** Log message for proof unknown result */
    const val LOG_PROOF_UNKNOWN = "❓ Proof %d: Unknown result"
    
    /** Log message for proof verified */
    const val LOG_PROOF_VERIFIED = "✅ Proof %d: Verified"
    
    /** Log message for proof verification failed */
    const val LOG_PROOF_VERIFICATION_FAILED = "❌ Proof %d: Verification Failed"
    
    // ============================================================================
    // ASSET PACK STATUS MESSAGES
    // ============================================================================
    
    /** Status message for all packs ready */
    const val STATUS_ALL_PACKS_READY = "All asset packs ready (%d/%d)"
    
    /** Status message for downloading packs */
    const val STATUS_DOWNLOADING_PACKS = "Downloading asset packs: %d%% (%d/%d ready)"
    
    /** Status message for failed downloads */
    const val STATUS_DOWNLOADS_FAILED = "Some asset pack downloads failed (%d/%d ready)"
    
    /** Status message for checking packs */
    const val STATUS_CHECKING_PACKS = "Checking asset packs... (%d/%d ready)"
    
    // ============================================================================
    // TIME FORMATTING
    // ============================================================================
    
    /** Time threshold for milliseconds display (ms) */
    const val TIME_MS_THRESHOLD = 1000.0
    
    
    /** Time threshold for seconds display (ms) */
    const val TIME_SECONDS_THRESHOLD = 60000.0
    
    /** Milliseconds per second */
    const val MS_PER_SECOND = 1000.0
    
    /** Milliseconds per minute */
    const val MS_PER_MINUTE = 60000.0
    
    /** Time format for milliseconds */
    const val TIME_FORMAT_MS = "%.1f ms"
    
    /** Time format for seconds */
    const val TIME_FORMAT_SECONDS = "%.2f seconds"
    
    /** Time format for minutes and seconds */
    const val TIME_FORMAT_MINUTES_SECONDS = "%d minutes %.2f seconds"
    
    // ============================================================================
    // JNI RESULT VALUES
    // ============================================================================
    
    /** JNI success result value */
    const val JNI_SUCCESS = 0
    
    /** JNI failure result value */
    const val JNI_FAILURE = 1
    
    // ============================================================================
    // UI ALPHA VALUES
    // ============================================================================
    
    /** Alpha value for enabled UI elements */
    const val UI_ALPHA_ENABLED = 1.0f
    
    /** Alpha value for disabled UI elements */
    const val UI_ALPHA_DISABLED = 0.3f
    
    // ============================================================================
    // COROUTINE NAMES
    // ============================================================================
    
    /** Coroutine name for single proof operations */
    const val COROUTINE_NAME_SINGLE_PROOF = "IMP1 Single Proof"
    
    /** Coroutine name for parallel proof operations */
    const val COROUTINE_NAME_PARALLEL_PROOF = "IMP1 Parallel Proof"
} 