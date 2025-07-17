package com.ingonyama.imp1_aar_example

import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.assetpacks.AssetPackLocation
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import com.ingonyama.imp1.DeviceType
import com.ingonyama.imp1.NativeBridge
import com.ingonyama.imp1.ProverException
import com.ingonyama.imp1.ProofResult
import com.ingonyama.imp1.VerifierResult
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    // Define a data class to hold the filenames for each test case.
    // The name will be displayed in the Spinner.
    data class Example(
        val name: String,
        val witnessAsset: String,
        val zkeyAsset: String,
        val vkAsset: String,
        val assetPack: String  // Which asset pack contains the zkey
    ) {
        // This makes the spinner show the 'name' property
        override fun toString(): String = name
    }

    // List of examples with their corresponding asset packs.
    private val examples = listOf(
        Example("100k", "100k_witness.wtns", "100k_circuit_final.zkey", "100k_verification_key.json", "zkey_pack_0"),
        Example("200k", "200k_witness.wtns", "200k_circuit_final.zkey", "200k_verification_key.json", "zkey_pack_0"),
        Example("400k", "400k_witness.wtns", "400k_circuit_final.zkey", "400k_verification_key.json", "zkey_pack_0"),
        Example("800k", "800k_witness.wtns", "800k_circuit_final.zkey", "800k_verification_key.json", "zkey_pack_1"),
        Example("1600k", "1600k_witness.wtns", "1600k_circuit_final.zkey", "1600k_verification_key.json", "zkey_pack_1"),
        Example("Sha256", "sha256_witness.wtns", "sha256_circuit_final.zkey", "sha256_verification_key.json", "zkey_pack_1"),
        Example("Rarimo", "rarimo_witness.wtns", "rarimo_circuit_final.zkey", "rarimo_verification_key.json", "zkey_pack_rarimo"),
        Example("Keccak", "keccak_witness.wtns", "keccak_circuit_final.zkey", "keccak_verification_key.json", "zkey_pack_1"),
        Example("aes-128", "aes_128_ctr_witness.wtns", "aes_128_ctr_circuit_final.zkey", "aes_128_ctr_verification_key.json", "zkey_pack_zkp2p"),
        Example("aes-256", "aes_256_ctr_witness.wtns", "aes_256_ctr_circuit_final.zkey", "aes_256_ctr_verification_key.json", "zkey_pack_zkp2p"),
        Example("chacha20", "chacha20_witness.wtns", "chacha20_circuit_final.zkey", "chacha20_verification_key.json", "zkey_pack_zkp2p"),
    )

    private lateinit var exampleSpinner: Spinner
    private lateinit var runButton: Button
    private lateinit var logTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var downloadProgressBar: ProgressBar
    private lateinit var downloadStatusTextView: TextView
    
    // Parallel proof UI components
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var singleModeRadio: RadioButton
    private lateinit var parallelModeRadio: RadioButton
    private lateinit var parallelConfigLayout: LinearLayout
    private lateinit var parallelCountSeekBar: SeekBar
    private lateinit var parallelCountTextView: TextView
    
    // Parallel proof state
    private var isParallelMode = false
    private var numParallelProofs = 2
    private var proofResults = mutableListOf<String>()
    private var verificationResults = mutableListOf<String>()
    private var proofRuntime = ""
    private var verificationRuntime = ""
    
    private lateinit var assetPackManager: AssetPackManager
    private val assetPackNames = BuildConfig.ASSET_PACK_NAMES.toList()
    private val packDownloadProgress = mutableMapOf<String, Int>()
    private val packDownloadStatus = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        exampleSpinner = findViewById(R.id.exampleSpinner)
        runButton = findViewById(R.id.runButton)
        logTextView = findViewById(R.id.logTextView)
        progressBar = findViewById(R.id.progressBar)
        downloadProgressBar = findViewById(R.id.downloadProgressBar)
        downloadStatusTextView = findViewById(R.id.downloadStatusTextView)
        
        // Initialize parallel proof UI components
        modeRadioGroup = findViewById(R.id.modeRadioGroup)
        singleModeRadio = findViewById(R.id.singleModeRadio)
        parallelModeRadio = findViewById(R.id.parallelModeRadio)
        parallelConfigLayout = findViewById(R.id.parallelConfigLayout)
        parallelCountSeekBar = findViewById(R.id.parallelCountSeekBar)
        parallelCountTextView = findViewById(R.id.parallelCountTextView)

        // Initialize Asset Pack Manager
        assetPackManager = AssetPackManagerFactory.getInstance(this)

        // Setup the spinner with the list of examples
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, examples)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exampleSpinner.adapter = adapter

        // Set up parallel proof UI event handlers
        setupParallelProofUI()
        
        // Set the click listener for the run button
        runButton.setOnClickListener {
            val selectedExample = exampleSpinner.selectedItem as Example
            if (isParallelMode) {
                runParallelProofTest(selectedExample)
            } else {
                runFullTest(selectedExample)
            }
        }
        
        // Initially disable the run button until asset packs are ready
        runButton.isEnabled = false
        
        // Check asset pack status on startup
        checkAssetPacksStatus()
    }

    /**
     * Checks the status of all zkey asset packs and initiates downloads if needed.
     */
    private fun checkAssetPacksStatus() {
        val packsToDownload = mutableListOf<String>()
        
        for (packName in assetPackNames) {
            val packLocation = assetPackManager.getPackLocation(packName)
            if (packLocation == null) {
                // Pack is not available, needs to be downloaded
                packsToDownload.add(packName)
                packDownloadStatus[packName] = AssetPackStatus.NOT_INSTALLED
            } else {
                // Pack is available
                packDownloadStatus[packName] = AssetPackStatus.COMPLETED
                packDownloadProgress[packName] = 100
            }
        }
        
        // Always register the listener to handle updates
        registerDownloadListener()
        
        if (packsToDownload.isNotEmpty()) {
            downloadAssetPacks(packsToDownload)
        } else {
            // All packs are already available, just update the UI
            updateOverallDownloadStatus()
        }
    }

    /**
     * Registers the download progress listener.
     */
    private fun registerDownloadListener() {
        lateinit var listener: AssetPackStateUpdateListener
        
        listener = AssetPackStateUpdateListener { state ->
            val packName = state.name()
            if (packName in assetPackNames) {
                packDownloadStatus[packName] = state.status()
                updatePackProgress(state)
                
                when (state.status()) {
                    AssetPackStatus.COMPLETED -> {
                        packDownloadProgress[packName] = 100
                        runOnUiThread { updateOverallDownloadStatus() }
                        
                        // Check if all packs are completed
                        if (packDownloadStatus.values.all { it == AssetPackStatus.COMPLETED }) {
                            assetPackManager.unregisterListener(listener)
                        }
                    }
                    AssetPackStatus.FAILED -> {
                        runOnUiThread { updateOverallDownloadStatus() }
                        assetPackManager.unregisterListener(listener)
                    }
                    AssetPackStatus.CANCELED -> {
                        runOnUiThread { updateOverallDownloadStatus() }
                        assetPackManager.unregisterListener(listener)
                    }
                    else -> {
                        runOnUiThread { updateOverallDownloadStatus() }
                    }
                }
            }
        }
        
        assetPackManager.registerListener(listener)
    }

    /**
     * Downloads the specified asset packs.
     */
    private fun downloadAssetPacks(packNames: List<String>) {
        val request = assetPackManager.fetch(packNames)
        
        request.addOnSuccessListener {
            downloadStatusTextView.text = "Asset pack downloads started"
        }.addOnFailureListener { exception ->
            downloadStatusTextView.text = "Failed to start downloads: ${exception.message}"
        }
    }

    /**
     * Updates the progress for a specific pack.
     */
    private fun updatePackProgress(state: AssetPackState) {
        val progress = if (state.totalBytesToDownload() > 0) {
            ((100.0 * state.bytesDownloaded()) / state.totalBytesToDownload()).toInt()
        } else {
            0
        }
        packDownloadProgress[state.name()] = progress
    }

    /**
     * Updates the overall download status UI.
     */
    private fun updateOverallDownloadStatus() {
        val completedPacks = packDownloadStatus.values.count { it == AssetPackStatus.COMPLETED }
        val totalPacks = assetPackNames.size
        
        when {
            completedPacks == totalPacks -> {
                downloadStatusTextView.text = "All asset packs ready ($completedPacks/$totalPacks)"
                downloadProgressBar.visibility = View.GONE
                runButton.isEnabled = true
            }
            packDownloadStatus.values.any { it == AssetPackStatus.DOWNLOADING } -> {
                val avgProgress = packDownloadProgress.values.average().toInt()
                downloadProgressBar.progress = avgProgress
                downloadProgressBar.visibility = View.VISIBLE
                downloadStatusTextView.text = "Downloading asset packs: $avgProgress% ($completedPacks/$totalPacks ready)"
                runButton.isEnabled = false
            }
            packDownloadStatus.values.any { it == AssetPackStatus.FAILED } -> {
                downloadStatusTextView.text = "Some asset pack downloads failed ($completedPacks/$totalPacks ready)"
                downloadProgressBar.visibility = View.GONE
                runButton.isEnabled = false
            }
            else -> {
                downloadStatusTextView.text = "Checking asset packs... ($completedPacks/$totalPacks ready)"
                downloadProgressBar.visibility = View.VISIBLE
                runButton.isEnabled = false
            }
        }
    }

    /**
     * Runs the full prove and verify test in a background coroutine.
     */
    private fun runFullTest(example: Example) {
        // Use lifecycleScope to launch a coroutine that is automatically
        // cancelled when the Activity is destroyed.
        lifecycleScope.launch(Dispatchers.IO + CoroutineName("IMP1 Prove+Verify")) {
            // Show progress and disable UI on the Main thread
            withContext(Dispatchers.Main) {
                logTextView.text = ""
                progressBar.visibility = View.VISIBLE
                runButton.isEnabled = false
                log("Starting test for: ${example.name}")
            }

            // Check if the required asset pack is available
            val packLocation = assetPackManager.getPackLocation(example.assetPack)
            if (packLocation == null) {
                log("❌ Asset pack '${example.assetPack}' not available. Please wait for download to complete.")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = false
                }
                return@launch
            }

            // The cache directory is a good place for temporary files.
            val workingDir = cacheDir
            // Clear previous output files if they exist
            workingDir.listFiles()?.filter { it.name.endsWith(".proof") || it.name.endsWith(".public") }?.forEach { it.delete() }

            // Define file paths for inputs and outputs
            val witnessFile = File(workingDir, "witness.wtns")
            val zkeyFile = File(workingDir, "zkey.zkey")
            val vkFile = File(workingDir, "vk.json")
            val proofFile = File(workingDir, "test.proof")
            val publicFile = File(workingDir, "test.public")

            try {
                // Step 1: Copy asset files to the app's private storage, where they can be accessed by path.
                log("Copying assets to device storage...")
                copyAssetToFile(example.witnessAsset, witnessFile)
                copyZkeyFromAssetPack(packLocation, example.zkeyAsset, zkeyFile)
                copyAssetToFile(example.vkAsset, vkFile)
                log("...copying complete.")

                // Step 2: Run the prover
                log("\nRunning Prover...")
                try {
                    val proveTime = measureTimeMillis {
                        NativeBridge.prove(
                            witnessPath = witnessFile.absolutePath,
                            zkeyPath = zkeyFile.absolutePath,
                            proofPath = proofFile.absolutePath,
                            publicPath = publicFile.absolutePath,
                            deviceType = DeviceType.Cpu
                        )
                    }
                    log("✅ Prove SUCCESSFUL")
                    log("   Time taken: $proveTime ms")
                } catch (e: ProverException) {
                    log("❌ Prove FAILED")
                    log("   Error: ${e.message}")
                    // If prove fails, we can't continue to verify.
                    return@launch
                } finally {
                    // Update UI on the Main thread after this block is done
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        runButton.isEnabled = true
                    }
                }

                // Step 3: Run the verifier
                log("\nRunning Verifier...")
                val verifyTime = measureTimeMillis {
                    val result = NativeBridge.verify(
                        proofPath = proofFile.absolutePath,
                        publicPath = publicFile.absolutePath,
                        vkPath = vkFile.absolutePath
                    )

                    if(result == VerifierResult.VerifierSuccess) {
                        log("✅ Verify SUCCESSFUL")
                    } else {
                        log("❌ Verify FAILED")
                    }
                }
                log("   Time taken: $verifyTime ms")

            } catch (e: IOException) {
                log("\n❌ CRITICAL ERROR: Could not copy asset files.")
                log("   Make sure the filenames in the `examples` list are correct.")
                log("   Error: ${e.message}")
            } finally {
                // Final UI update, re-enabling the button and hiding the progress bar.
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = true
                }
            }
        }
    }

    /**
     * Copies a zkey file from the asset pack to internal storage.
     */
    @Throws(IOException::class)
    private fun copyZkeyFromAssetPack(packLocation: AssetPackLocation, zkeyAssetName: String, destinationFile: File) {
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
    private fun copyAssetToFile(assetName: String, destinationFile: File) {
        assets.open(assetName).use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /**
     * Sets up the parallel proof UI event handlers.
     */
    private fun setupParallelProofUI() {
        // Mode toggle handler
        modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            isParallelMode = checkedId == R.id.parallelModeRadio
            updateUIForMode()
        }
        
        // Parallel count seekbar handler
        parallelCountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                numParallelProofs = progress + 1 // SeekBar starts at 0, we want 1-50
                parallelCountTextView.text = numParallelProofs.toString()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    /**
     * Updates the UI based on the selected mode.
     */
    private fun updateUIForMode() {
        if (isParallelMode) {
            parallelConfigLayout.visibility = View.VISIBLE
            runButton.text = "Run Parallel Proofs"
        } else {
            parallelConfigLayout.visibility = View.GONE
            runButton.text = "Run Single Proof"
        }
    }
    
    /**
     * Runs parallel proof generation and verification.
     */
    private fun runParallelProofTest(example: Example) {
        lifecycleScope.launch(Dispatchers.IO + CoroutineName("IMP1 Parallel Prove+Verify")) {
            // Show progress and disable UI on the Main thread
            withContext(Dispatchers.Main) {
                logTextView.text = ""
                progressBar.visibility = View.VISIBLE
                runButton.isEnabled = false
                log("Starting parallel proof test for: ${example.name}")
                log("Number of parallel proofs: $numParallelProofs")
            }
            
            Log.d("IMP1_DEBUG", "Entered runParallelProofTest function")
            Log.d("IMP1_DEBUG", "Example name: ${example.name}")
            Log.d("IMP1_DEBUG", "Example witness asset: ${example.witnessAsset}")
            Log.d("IMP1_DEBUG", "Example zkey asset: ${example.zkeyAsset}")
            Log.d("IMP1_DEBUG", "Example vk asset: ${example.vkAsset}")
            Log.d("IMP1_DEBUG", "Example asset pack: ${example.assetPack}")
            
            log("DEBUG: Entered runParallelProofTest function")
            log("DEBUG: Example name: ${example.name}")
            log("DEBUG: Example witness asset: ${example.witnessAsset}")
            log("DEBUG: Example zkey asset: ${example.zkeyAsset}")
            log("DEBUG: Example vk asset: ${example.vkAsset}")
            log("DEBUG: Example asset pack: ${example.assetPack}")

            // Check if the required asset pack is available
            Log.d("IMP1_DEBUG", "Checking asset pack availability...")
            log("DEBUG: Checking asset pack availability...")
            val packLocation = assetPackManager.getPackLocation(example.assetPack)
            Log.d("IMP1_DEBUG", "Pack location: $packLocation")
            log("DEBUG: Pack location: $packLocation")
            if (packLocation == null) {
                Log.d("IMP1_DEBUG", "Asset pack not available: ${example.assetPack}")
                log("❌ Asset pack '${example.assetPack}' not available. Please wait for download to complete.")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = true
                }
                return@launch
            }
            Log.d("IMP1_DEBUG", "Asset pack is available")
            log("DEBUG: Asset pack is available")

            // The cache directory is a good place for temporary files.
            val workingDir = cacheDir
            // Clear previous output files if they exist
            workingDir.listFiles()?.filter { it.name.endsWith(".proof") || it.name.endsWith(".public") }?.forEach { it.delete() }

            // Define file paths for inputs and outputs
            val witnessFile = File(workingDir, "witness.wtns")
            val zkeyFile = File(workingDir, "zkey.zkey")
            val vkFile = File(workingDir, "vk.json")
            
            // Arrays for parallel proof paths
            val witnessPaths = Array(numParallelProofs) { i -> File(workingDir, "witness_$i.wtns") }
            val proofPaths = Array(numParallelProofs) { i -> File(workingDir, "proof_$i.proof") }
            val publicPaths = Array(numParallelProofs) { i -> File(workingDir, "public_$i.public") }

            try {
                // Step 1: Copy asset files to the app's private storage
                Log.d("IMP1_DEBUG", "Starting to copy assets...")
                log("Copying assets to device storage...")
                Log.d("IMP1_DEBUG", "Copying witness file: ${example.witnessAsset}")
                copyAssetToFile(example.witnessAsset, witnessFile)
                Log.d("IMP1_DEBUG", "Copying zkey file: ${example.zkeyAsset}")
                copyZkeyFromAssetPack(packLocation, example.zkeyAsset, zkeyFile)
                Log.d("IMP1_DEBUG", "ZKey file copied. Exists: ${zkeyFile.exists()}, Size: ${zkeyFile.length()} bytes")
                Log.d("IMP1_DEBUG", "Copying vk file: ${example.vkAsset}")
                copyAssetToFile(example.vkAsset, vkFile)
                
                // Copy witness file multiple times for parallel processing
                for (i in 0 until numParallelProofs) {
                    witnessFile.copyTo(witnessPaths[i], overwrite = true)
                }
                log("...copying complete.")
                
                // Debug: Check if files exist
                log("Debug: Checking file existence...")
                log("  Witness file exists: ${witnessFile.exists()}")
                log("  ZKey file exists: ${zkeyFile.exists()}")
                log("  VK file exists: ${vkFile.exists()}")
                log("  VK file path: ${vkFile.absolutePath}")
                log("  VK file size: ${vkFile.length()} bytes")

                // Step 2: Run the parallel prover
                Log.d("IMP1_DEBUG", "About to start parallel prove...")
                log("\nRunning Parallel Prover...")
                Log.d("IMP1_DEBUG", "About to call NativeBridge.parallelProve")
                log("DEBUG: About to call NativeBridge.parallelProve")
                Log.d("IMP1_DEBUG", "Witness paths count: ${witnessPaths.size}")
                log("DEBUG: Witness paths count: ${witnessPaths.size}")
                Log.d("IMP1_DEBUG", "Proof paths count: ${proofPaths.size}")
                log("DEBUG: Proof paths count: ${proofPaths.size}")
                Log.d("IMP1_DEBUG", "Public paths count: ${publicPaths.size}")
                log("DEBUG: Public paths count: ${publicPaths.size}")
                Log.d("IMP1_DEBUG", "ZKey path: ${zkeyFile.absolutePath}")
                log("DEBUG: ZKey path: ${zkeyFile.absolutePath}")
                try {
                                    Log.d("IMP1_DEBUG", "Calling NativeBridge.parallelProve...")
                Log.d("IMP1_DEBUG", "Witness paths: ${witnessPaths.map { it.absolutePath }}")
                Log.d("IMP1_DEBUG", "Proof paths: ${proofPaths.map { it.absolutePath }}")
                Log.d("IMP1_DEBUG", "Public paths: ${publicPaths.map { it.absolutePath }}")
                Log.d("IMP1_DEBUG", "ZKey path: ${zkeyFile.absolutePath}")
                
                // Create the arrays for JNI
                val witnessPathsArray = witnessPaths.map { it.absolutePath }.toTypedArray()
                val proofPathsArray = proofPaths.map { it.absolutePath }.toTypedArray()
                val publicPathsArray = publicPaths.map { it.absolutePath }.toTypedArray()
                
                // Debug: Check array contents before JNI call
                Log.d("IMP1_DEBUG", "JNI Arrays before call:")
                for (i in 0 until witnessPathsArray.size) {
                    Log.d("IMP1_DEBUG", "  witnessPathsArray[$i]: ${witnessPathsArray[i]}")
                    Log.d("IMP1_DEBUG", "  proofPathsArray[$i]: ${proofPathsArray[i]}")
                    Log.d("IMP1_DEBUG", "  publicPathsArray[$i]: ${publicPathsArray[i]}")
                }
                
                val proveTime = measureTimeMillis {
                    val results = NativeBridge.parallelProve(
                            witnessPaths = witnessPathsArray,
                            zkeyPath = zkeyFile.absolutePath,
                            proofPaths = proofPathsArray,
                            publicPaths = publicPathsArray,
                            deviceType = DeviceType.Cpu,
                            maxBatchSize = 0L
                        )
                        
                        // Process results
                        proofResults.clear()
                        for ((index, result) in results.withIndex()) {
                            when (result.value) {
                                0 -> {
                                    proofResults.add("✅ Proof ${index + 1}: Success")
                                    log("✅ Proof ${index + 1}: Success")
                                }
                                1 -> {
                                    proofResults.add("❌ Proof ${index + 1}: Failed")
                                    log("❌ Proof ${index + 1}: Failed")
                                }
                                else -> {
                                    proofResults.add("❓ Proof ${index + 1}: Unknown result")
                                    log("❓ Proof ${index + 1}: Unknown result")
                                }
                            }
                        }
                    }
                    log("✅ Parallel Prove completed")
                    log("   Time taken: $proveTime ms")
                    proofRuntime = formatTimeInterval(proveTime.toDouble())
                    log("   Runtime: $proofRuntime")
                } catch (e: ProverException) {
                    log("❌ Parallel Prove FAILED")
                    log("   Error: ${e.message}")
                    return@launch
                } finally {
                    // Update UI on the Main thread after this block is done
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        runButton.isEnabled = true
                    }
                }

                // Step 3: Run the verifier for each proof
                log("\nRunning Verifier for all proofs...")
                verificationResults.clear()
                val verifyTime = measureTimeMillis {
                    for (i in 0 until numParallelProofs) {
                        val proofPath = proofPaths[i]
                        val publicPath = publicPaths[i]
                        
                        // Debug: Check files before verification
                        log("Debug: Verifying proof ${i + 1}...")
                        log("  Proof file exists: ${proofPath.exists()}")
                        log("  Public file exists: ${publicPath.exists()}")
                        log("  VK file exists: ${vkFile.exists()}")
                        log("  Proof path: ${proofPath.absolutePath}")
                        log("  Public path: ${publicPath.absolutePath}")
                        log("  VK path: ${vkFile.absolutePath}")
                        
                        val result = NativeBridge.verify(
                            proofPath = proofPath.absolutePath,
                            publicPath = publicPath.absolutePath,
                            vkPath = vkFile.absolutePath
                        )

                        when (result) {
                            VerifierResult.VerifierSuccess -> {
                                verificationResults.add("✅ Proof ${i + 1}: Verified")
                                log("✅ Proof ${i + 1}: Verified")
                            }
                            VerifierResult.VerifierFailure -> {
                                verificationResults.add("❌ Proof ${i + 1}: Verification Failed")
                                log("❌ Proof ${i + 1}: Verification Failed")
                            }
                        }
                    }
                }
                log("✅ Verification completed")
                log("   Time taken: $verifyTime ms")
                verificationRuntime = formatTimeInterval(verifyTime.toDouble())
                log("   Runtime: $verificationRuntime")

            } catch (e: IOException) {
                log("\n❌ CRITICAL ERROR: Could not copy asset files.")
                log("   Make sure the filenames in the `examples` list are correct.")
                log("   Error: ${e.message}")
            } finally {
                // Final UI update, re-enabling the button and hiding the progress bar.
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = true
                }
            }
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

    /**
     * Helper function to append messages to the on-screen log from any thread.
     */
    private suspend fun log(message: String) {
        withContext(Dispatchers.Main) {
            logTextView.append("$message\n")
        }
    }
}
