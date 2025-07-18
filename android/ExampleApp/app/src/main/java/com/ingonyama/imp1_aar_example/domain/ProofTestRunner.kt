package com.ingonyama.imp1_aar_example.domain

import com.ingonyama.imp1.DeviceType
import com.ingonyama.imp1.NativeBridge
import com.ingonyama.imp1.ProverException
import com.ingonyama.imp1.ProofResult
import com.ingonyama.imp1.VerifierResult
import com.ingonyama.imp1_aar_example.data.Example
import com.ingonyama.imp1_aar_example.data.FileOperations
import com.ingonyama.imp1_aar_example.data.SingleProofFiles
import com.ingonyama.imp1_aar_example.data.ParallelProofFiles
import com.google.android.play.core.assetpacks.AssetPackLocation
import java.io.File
import java.io.IOException
import kotlin.system.measureTimeMillis

class ProofTestRunner(
    private val fileOperations: FileOperations,
    private val logCallback: (String) -> Unit
) {
    
    /**
     * Runs a single proof test (prove + verify).
     */
    suspend fun runSingleProofTest(
        example: Example,
        packLocation: AssetPackLocation
    ): SingleProofResult {
        logCallback("Starting test for: ${example.name}")
        
        val workingDir = fileOperations.getCacheDir()
        fileOperations.clearPreviousOutputs(workingDir)
        
        val files = fileOperations.createSingleProofFiles(workingDir)
        
        try {
            // Step 1: Copy asset files to the app's private storage
            logCallback("Copying assets to device storage...")
            fileOperations.copyAssetToFile(example.witnessAsset, files.witnessFile)
            fileOperations.copyZkeyFromAssetPack(packLocation, example.zkeyAsset, files.zkeyFile)
            fileOperations.copyAssetToFile(example.vkAsset, files.vkFile)
            logCallback("...copying complete.")

            // Step 2: Run the prover
            logCallback("\nRunning Prover...")
            val proveTime = measureTimeMillis {
                NativeBridge.prove(
                    witnessPath = files.witnessFile.absolutePath,
                    zkeyPath = files.zkeyFile.absolutePath,
                    proofPath = files.proofFile.absolutePath,
                    publicPath = files.publicFile.absolutePath,
                    deviceType = DeviceType.Cpu
                )
            }
            logCallback("✅ Prove SUCCESSFUL")
            logCallback("   Time taken: $proveTime ms")

            // Step 3: Run the verifier
            logCallback("\nRunning Verifier...")
            val verifyTime = measureTimeMillis {
                val result = NativeBridge.verify(
                    proofPath = files.proofFile.absolutePath,
                    publicPath = files.publicFile.absolutePath,
                    vkPath = files.vkFile.absolutePath
                )

                if (result == VerifierResult.VerifierSuccess) {
                    logCallback("✅ Verify SUCCESSFUL")
                } else {
                    logCallback("❌ Verify FAILED")
                }
            }
            logCallback("   Time taken: $verifyTime ms")
            
            return SingleProofResult.Success(
                proveTime = proveTime,
                verifyTime = verifyTime,
                isVerified = true
            )

        } catch (e: ProverException) {
            logCallback("❌ Prove FAILED")
            logCallback("   Error: ${e.message}")
            return SingleProofResult.ProverFailed(e.message ?: "Unknown error")
        } catch (e: IOException) {
            logCallback("\n❌ CRITICAL ERROR: Could not copy asset files.")
            logCallback("   Make sure the filenames in the `examples` list are correct.")
            logCallback("   Error: ${e.message}")
            return SingleProofResult.FileError(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Runs parallel proof generation and verification.
     */
    suspend fun runParallelProofTest(
        example: Example,
        packLocation: AssetPackLocation,
        numParallelProofs: Int
    ): ParallelProofResult {
        logCallback("Starting parallel proof test for: ${example.name}")
        logCallback("Number of parallel proofs: $numParallelProofs")
        
        val workingDir = fileOperations.getCacheDir()
        fileOperations.clearPreviousOutputs(workingDir)
        
        val files = fileOperations.createParallelProofFiles(workingDir, numParallelProofs)
        
        try {
            // Step 1: Copy asset files to the app's private storage
            logCallback("Copying assets to device storage...")
            fileOperations.copyAssetToFile(example.witnessAsset, files.witnessFile)
            fileOperations.copyZkeyFromAssetPack(packLocation, example.zkeyAsset, files.zkeyFile)
            fileOperations.copyAssetToFile(example.vkAsset, files.vkFile)
            
            // Copy witness file multiple times for parallel processing
            fileOperations.copyWitnessForParallelProcessing(files.witnessFile, files.witnessPaths)
            logCallback("...copying complete.")

            // Step 2: Run the parallel prover
            logCallback("\nRunning Parallel Prover...")
            val proveTime = measureTimeMillis {
                // Create the arrays for JNI
                val witnessPathsArray = files.witnessPaths.map { it.absolutePath }.toTypedArray()
                val proofPathsArray = files.proofPaths.map { it.absolutePath }.toTypedArray()
                val publicPathsArray = files.publicPaths.map { it.absolutePath }.toTypedArray()
                
                val results = NativeBridge.parallelProve(
                    witnessPaths = witnessPathsArray,
                    zkeyPath = files.zkeyFile.absolutePath,
                    proofPaths = proofPathsArray,
                    publicPaths = publicPathsArray,
                    deviceType = DeviceType.Cpu,
                    maxBatchSize = 0L
                )
                
                // Process results
                val proofResults = mutableListOf<String>()
                for ((index, result) in results.withIndex()) {
                    when (result.value) {
                        0 -> {
                            proofResults.add("✅ Proof ${index + 1}: Success")
                            logCallback("✅ Proof ${index + 1}: Success")
                        }
                        1 -> {
                            proofResults.add("❌ Proof ${index + 1}: Failed")
                            logCallback("❌ Proof ${index + 1}: Failed")
                        }
                        else -> {
                            proofResults.add("❓ Proof ${index + 1}: Unknown result")
                            logCallback("❓ Proof ${index + 1}: Unknown result")
                        }
                    }
                }
            }
            logCallback("✅ Parallel Prove completed")
            logCallback("   Time taken: $proveTime ms")
            val proofRuntime = formatTimeInterval(proveTime.toDouble())
            logCallback("   Runtime: $proofRuntime")

            // Step 3: Run the verifier for each proof
            logCallback("\nRunning Verifier for all proofs...")
            val verifyTime = measureTimeMillis {
                val verificationResults = mutableListOf<String>()
                for (i in 0 until numParallelProofs) {
                    val proofPath = files.proofPaths[i]
                    val publicPath = files.publicPaths[i]
                    
                    val result = NativeBridge.verify(
                        proofPath = proofPath.absolutePath,
                        publicPath = publicPath.absolutePath,
                        vkPath = files.vkFile.absolutePath
                    )

                    when (result) {
                        VerifierResult.VerifierSuccess -> {
                            verificationResults.add("✅ Proof ${i + 1}: Verified")
                            logCallback("✅ Proof ${i + 1}: Verified")
                        }
                        VerifierResult.VerifierFailure -> {
                            verificationResults.add("❌ Proof ${i + 1}: Verification Failed")
                            logCallback("❌ Proof ${i + 1}: Verification Failed")
                        }
                    }
                }
            }
            logCallback("✅ Verification completed")
            logCallback("   Time taken: $verifyTime ms")
            val verificationRuntime = formatTimeInterval(verifyTime.toDouble())
            logCallback("   Runtime: $verificationRuntime")
            
            return ParallelProofResult.Success(
                proveTime = proveTime,
                verifyTime = verifyTime,
                numProofs = numParallelProofs
            )

        } catch (e: ProverException) {
            logCallback("❌ Parallel Prove FAILED")
            logCallback("   Error: ${e.message}")
            return ParallelProofResult.ProverFailed(e.message ?: "Unknown error")
        } catch (e: IOException) {
            logCallback("\n❌ CRITICAL ERROR: Could not copy asset files.")
            logCallback("   Make sure the filenames in the `examples` list are correct.")
            logCallback("   Error: ${e.message}")
            return ParallelProofResult.FileError(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Formats a time interval in milliseconds to a human-readable string.
     */
    private fun formatTimeInterval(timeMs: Double): String {
        return when {
            timeMs < 1000 -> "${String.format("%.1f", timeMs)} ms"
            timeMs < 60000 -> "${String.format("%.2f", timeMs / 1000)} seconds"
            else -> {
                val minutes = (timeMs / 60000).toInt()
                val seconds = (timeMs % 60000) / 1000
                "$minutes minutes ${String.format("%.2f", seconds)} seconds"
            }
        }
    }
}

sealed class SingleProofResult {
    data class Success(
        val proveTime: Long,
        val verifyTime: Long,
        val isVerified: Boolean
    ) : SingleProofResult()
    
    data class ProverFailed(val error: String) : SingleProofResult()
    data class FileError(val error: String) : SingleProofResult()
}

sealed class ParallelProofResult {
    data class Success(
        val proveTime: Long,
        val verifyTime: Long,
        val numProofs: Int
    ) : ParallelProofResult()
    
    data class ProverFailed(val error: String) : ParallelProofResult()
    data class FileError(val error: String) : ParallelProofResult()
} 