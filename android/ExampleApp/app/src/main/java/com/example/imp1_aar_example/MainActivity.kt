package com.example.imp1_aar_example // Make sure this matches your package name

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ingonyama.imp1.DeviceType
import com.ingonyama.imp1.NativeBridge
import com.ingonyama.imp1.ProverException
import com.ingonyama.imp1.VerifierResult
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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
        val vkAsset: String
    ) {
        // This makes the spinner show the 'name' property
        override fun toString(): String = name
    }

    // List of examples.
    private val examples = listOf(
        Example("Sha256", "sha256_witness.wtns", "sha256_circuit_final.zkey", "sha256_verification_key.json"),
    )

    private lateinit var exampleSpinner: Spinner
    private lateinit var runButton: Button
    private lateinit var logTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        exampleSpinner = findViewById(R.id.exampleSpinner)
        runButton = findViewById(R.id.runButton)
        logTextView = findViewById(R.id.logTextView)
        progressBar = findViewById(R.id.progressBar)

        // Setup the spinner with the list of examples
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, examples)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exampleSpinner.adapter = adapter

        // Set the click listener for the run button
        runButton.setOnClickListener {
            val selectedExample = exampleSpinner.selectedItem as Example
            runFullTest(selectedExample)
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
                copyAssetToFile(example.zkeyAsset, zkeyFile)
                copyAssetToFile(example.vkAsset, vkFile)
                log("...copying complete.")

                // Step 2: Run the prover
                log("\nRunning Prover...")
                var proveTime = -1L
                try {
                    proveTime = measureTimeMillis {
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
     * Helper function to append messages to the on-screen log from any thread.
     */
    private suspend fun log(message: String) {
        withContext(Dispatchers.Main) {
            logTextView.append("$message\n")
        }
    }
}
