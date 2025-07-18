package com.ingonyama.imp1_aar_example.domain

import com.ingonyama.imp1.DeviceType
import com.ingonyama.imp1.NativeBridge
import com.ingonyama.imp1.ProverException
import com.ingonyama.imp1.ProofResult
import com.ingonyama.imp1.VerifierResult
import com.ingonyama.imp1_aar_example.Constants
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
        logCallback(Constants.LOG_STARTING_TEST.format(example.name))
        
        val workingDir = fileOperations.getCacheDir()
        fileOperations.clearPreviousOutputs(workingDir)
        
        val files = fileOperations.createSingleProofFiles(workingDir)
        
        try {
            // Step 1: Copy asset files to the app's private storage
            logCallback(Constants.LOG_COPYING_ASSETS)
            fileOperations.copyAssetToFile(example.witnessAsset, files.witnessFile)
            fileOperations.copyZkeyFromAssetPack(packLocation, example.zkeyAsset, files.zkeyFile)
            fileOperations.copyAssetToFile(example.vkAsset, files.vkFile)
            logCallback(Constants.LOG_COPYING_COMPLETE)

            // Step 2: Run the prover
            logCallback(Constants.LOG_RUNNING_PROVER)
            val proveTime = measureTimeMillis {
                            NativeBridge.prove(
                witnessPath = files.witnessFile.absolutePath,
                zkeyPath = files.zkeyFile.absolutePath,
                proofPath = files.proofFile.absolutePath,
                publicPath = files.publicFile.absolutePath,
                deviceType = DeviceType.CPU
            )
            }
            logCallback(Constants.LOG_PROVE_SUCCESS)
            logCallback(Constants.LOG_TIME_TAKEN.format(proveTime))

            // Step 3: Run the verifier
            logCallback(Constants.LOG_RUNNING_VERIFIER)
            val verifyTime = measureTimeMillis {
                val result = NativeBridge.verify(
                    proofPath = files.proofFile.absolutePath,
                    publicPath = files.publicFile.absolutePath,
                    vkPath = files.vkFile.absolutePath
                )

                if (result == VerifierResult.SUCCESS) {
                    logCallback(Constants.LOG_VERIFY_SUCCESS)
                } else {
                    logCallback(Constants.LOG_VERIFY_FAILED)
                }
            }
            logCallback(Constants.LOG_TIME_TAKEN.format(verifyTime))
            
            return SingleProofResult.Success(
                proveTime = proveTime,
                verifyTime = verifyTime,
                isVerified = true
            )

        } catch (e: ProverException) {
            logCallback(Constants.LOG_PROVE_FAILED)
            logCallback(Constants.LOG_ERROR_DETAILS.format(e.message))
            return SingleProofResult.ProverFailed(e.message ?: "Unknown error")
        } catch (e: IOException) {
            logCallback(Constants.LOG_CRITICAL_FILE_ERROR)
            logCallback(Constants.LOG_FILE_ERROR_INSTRUCTIONS)
            logCallback(Constants.LOG_ERROR_DETAILS.format(e.message))
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
        logCallback(Constants.LOG_STARTING_PARALLEL_TEST.format(example.name))
        logCallback(Constants.LOG_NUM_PARALLEL_PROOFS.format(numParallelProofs))
        
        val workingDir = fileOperations.getCacheDir()
        fileOperations.clearPreviousOutputs(workingDir)
        
        val files = fileOperations.createParallelProofFiles(workingDir, numParallelProofs)
        
        try {
            // Step 1: Copy asset files to the app's private storage
            logCallback(Constants.LOG_COPYING_ASSETS)
            fileOperations.copyAssetToFile(example.witnessAsset, files.witnessFile)
            fileOperations.copyZkeyFromAssetPack(packLocation, example.zkeyAsset, files.zkeyFile)
            fileOperations.copyAssetToFile(example.vkAsset, files.vkFile)
            
            // Copy witness file multiple times for parallel processing
            fileOperations.copyWitnessForParallelProcessing(files.witnessFile, files.witnessPaths)
            logCallback(Constants.LOG_COPYING_COMPLETE)

            // Step 2: Run the parallel prover
            logCallback(Constants.LOG_RUNNING_PARALLEL_PROVER)
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
                    deviceType = DeviceType.CPU,
                    maxBatchSize = 0L
                )
                
                // Process results
                val proofResults = mutableListOf<String>()
                for ((index, result) in results.withIndex()) {
                    when (result.value) {
                        Constants.JNI_SUCCESS -> {
                            proofResults.add(Constants.LOG_PROOF_SUCCESS.format(index + 1))
                            logCallback(Constants.LOG_PROOF_SUCCESS.format(index + 1))
                        }
                        Constants.JNI_FAILURE -> {
                            proofResults.add(Constants.LOG_PROOF_FAILED.format(index + 1))
                            logCallback(Constants.LOG_PROOF_FAILED.format(index + 1))
                        }
                        else -> {
                            proofResults.add(Constants.LOG_PROOF_UNKNOWN.format(index + 1))
                            logCallback(Constants.LOG_PROOF_UNKNOWN.format(index + 1))
                        }
                    }
                }
            }
            logCallback(Constants.LOG_PARALLEL_PROVE_COMPLETED)
            logCallback(Constants.LOG_TIME_TAKEN.format(proveTime))
            val proofRuntime = formatTimeInterval(proveTime.toDouble())
            logCallback(Constants.LOG_RUNTIME.format(proofRuntime))

            // Step 3: Run the verifier for each proof
            logCallback(Constants.LOG_RUNNING_VERIFIER_ALL)
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
                        VerifierResult.SUCCESS -> {
                            verificationResults.add(Constants.LOG_PROOF_VERIFIED.format(i + 1))
                            logCallback(Constants.LOG_PROOF_VERIFIED.format(i + 1))
                        }
                        VerifierResult.FAILURE -> {
                            verificationResults.add(Constants.LOG_PROOF_VERIFICATION_FAILED.format(i + 1))
                            logCallback(Constants.LOG_PROOF_VERIFICATION_FAILED.format(i + 1))
                        }
                    }
                }
            }
            logCallback(Constants.LOG_VERIFICATION_COMPLETED)
            logCallback(Constants.LOG_TIME_TAKEN.format(verifyTime))
            val verificationRuntime = formatTimeInterval(verifyTime.toDouble())
            logCallback(Constants.LOG_RUNTIME.format(verificationRuntime))
            
            return ParallelProofResult.Success(
                proveTime = proveTime,
                verifyTime = verifyTime,
                numProofs = numParallelProofs
            )

        } catch (e: ProverException) {
            logCallback(Constants.LOG_PARALLEL_PROVE_FAILED)
            logCallback(Constants.LOG_ERROR_DETAILS.format(e.message))
            return ParallelProofResult.ProverFailed(e.message ?: "Unknown error")
        } catch (e: IOException) {
            logCallback(Constants.LOG_CRITICAL_FILE_ERROR)
            logCallback(Constants.LOG_FILE_ERROR_INSTRUCTIONS)
            logCallback(Constants.LOG_ERROR_DETAILS.format(e.message))
            return ParallelProofResult.FileError(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Formats a time interval in milliseconds to a human-readable string.
     */
    private fun formatTimeInterval(timeMs: Double): String {
        return when {
            timeMs < Constants.TIME_MS_THRESHOLD -> String.format(Constants.TIME_FORMAT_MS, timeMs)
            timeMs < Constants.TIME_SECONDS_THRESHOLD -> String.format(Constants.TIME_FORMAT_SECONDS, timeMs / Constants.MS_PER_SECOND)
            else -> {
                val minutes = (timeMs / Constants.MS_PER_MINUTE).toInt()
                val seconds = (timeMs % Constants.MS_PER_MINUTE) / Constants.MS_PER_SECOND
                String.format(Constants.TIME_FORMAT_MINUTES_SECONDS, minutes, seconds)
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