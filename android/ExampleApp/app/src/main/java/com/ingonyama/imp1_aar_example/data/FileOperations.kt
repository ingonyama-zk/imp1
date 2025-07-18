package com.ingonyama.imp1_aar_example.data

import android.content.Context
import com.google.android.play.core.assetpacks.AssetPackLocation
import com.ingonyama.imp1_aar_example.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileOperations(private val context: Context) {
    
    /**
     * Copies a zkey file from the asset pack to internal storage.
     */
    @Throws(IOException::class)
    suspend fun copyZkeyFromAssetPack(packLocation: AssetPackLocation, zkeyAssetName: String, destinationFile: File) {
        val zkeyFile = File(packLocation.assetsPath(), zkeyAssetName)
        if (!zkeyFile.exists()) {
            throw IOException("ZKey file not found in asset pack: $zkeyAssetName")
        }
        
        FileInputStream(zkeyFile).use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
    
    /**
     * Copies a file from the app's assets folder to internal storage.
     * @param assetName The name of the file in the assets folder.
     * @param destinationFile The destination file.
     * @throws IOException if the file cannot be read or written.
     */
    @Throws(IOException::class)
    suspend fun copyAssetToFile(assetName: String, destinationFile: File) {
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
    
    /**
     * Clears previous output files from the working directory.
     */
    suspend fun clearPreviousOutputs(workingDir: File) {
        workingDir.listFiles()?.filter { 
            it.name.endsWith(Constants.PROOF_FILE_EXTENSION) || it.name.endsWith(Constants.PUBLIC_FILE_EXTENSION) 
        }?.forEach { it.delete() }
    }
    
    /**
     * Creates working directory files for a single proof test.
     */
    suspend fun createSingleProofFiles(workingDir: File): SingleProofFiles {
        return SingleProofFiles(
            witnessFile = File(workingDir, Constants.DEFAULT_WITNESS_FILE),
            zkeyFile = File(workingDir, Constants.DEFAULT_ZKEY_FILE),
            vkFile = File(workingDir, Constants.DEFAULT_VK_FILE),
            proofFile = File(workingDir, Constants.DEFAULT_PROOF_FILE),
            publicFile = File(workingDir, Constants.DEFAULT_PUBLIC_FILE)
        )
    }
    
    /**
     * Creates working directory files for parallel proof tests.
     */
    suspend fun createParallelProofFiles(workingDir: File, numProofs: Int): ParallelProofFiles {
        val witnessPaths = Array(numProofs) { i -> File(workingDir, Constants.PARALLEL_WITNESS_PATTERN.format(i)) }
        val proofPaths = Array(numProofs) { i -> File(workingDir, Constants.PARALLEL_PROOF_PATTERN.format(i)) }
        val publicPaths = Array(numProofs) { i -> File(workingDir, Constants.PARALLEL_PUBLIC_PATTERN.format(i)) }
        
        return ParallelProofFiles(
            witnessFile = File(workingDir, Constants.DEFAULT_WITNESS_FILE),
            zkeyFile = File(workingDir, Constants.DEFAULT_ZKEY_FILE),
            vkFile = File(workingDir, Constants.DEFAULT_VK_FILE),
            witnessPaths = witnessPaths,
            proofPaths = proofPaths,
            publicPaths = publicPaths
        )
    }
    
    /**
     * Copies witness file multiple times for parallel processing.
     */
    suspend fun copyWitnessForParallelProcessing(witnessFile: File, witnessPaths: Array<File>) {
        for (i in witnessPaths.indices) {
            witnessFile.copyTo(witnessPaths[i], overwrite = true)
        }
    }
    
    /**
     * Gets the cache directory for temporary files.
     */
    fun getCacheDir(): File = context.cacheDir
}

data class SingleProofFiles(
    val witnessFile: File,
    val zkeyFile: File,
    val vkFile: File,
    val proofFile: File,
    val publicFile: File
)

data class ParallelProofFiles(
    val witnessFile: File,
    val zkeyFile: File,
    val vkFile: File,
    val witnessPaths: Array<File>,
    val proofPaths: Array<File>,
    val publicPaths: Array<File>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParallelProofFiles

        if (witnessFile != other.witnessFile) return false
        if (zkeyFile != other.zkeyFile) return false
        if (vkFile != other.vkFile) return false
        if (!witnessPaths.contentEquals(other.witnessPaths)) return false
        if (!proofPaths.contentEquals(other.proofPaths)) return false
        if (!publicPaths.contentEquals(other.publicPaths)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = witnessFile.hashCode()
        result = 31 * result + zkeyFile.hashCode()
        result = 31 * result + vkFile.hashCode()
        result = 31 * result + witnessPaths.contentHashCode()
        result = 31 * result + proofPaths.contentHashCode()
        result = 31 * result + publicPaths.contentHashCode()
        return result
    }
} 