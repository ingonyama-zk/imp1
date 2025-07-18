package com.ingonyama.imp1_aar_example

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ingonyama.imp1_aar_example.data.AssetPackManager
import com.ingonyama.imp1_aar_example.data.Examples
import com.ingonyama.imp1_aar_example.data.FileOperations
import com.ingonyama.imp1_aar_example.domain.ProofTestRunner
import com.ingonyama.imp1_aar_example.ui.ParallelProofManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Use the examples from the data layer
    private val examples = Examples.list

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
    private lateinit var minusButton: TextView
    private lateinit var plusButton: TextView
    private lateinit var parallelCountTextView: TextView
    
    // Managers
    private lateinit var assetPackManager: AssetPackManager
    private lateinit var fileOperations: FileOperations
    private lateinit var proofTestRunner: ProofTestRunner
    private lateinit var parallelProofManager: ParallelProofManager
    
    // State
    private var isParallelMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        initializeUIComponents()
        
        // Initialize managers
        initializeManagers()
        
        // Setup the spinner with the list of examples
        setupSpinner()
        
        // Set up parallel proof UI event handlers
        setupParallelProofUI()
        
        // Set the click listener for the run button
        setupRunButton()
        
        // Initially disable the run button until asset packs are ready
        runButton.isEnabled = false
        
        // Check asset pack status on startup
        checkAssetPacksStatus()
    }

    /**
     * Initializes UI components.
     */
    private fun initializeUIComponents() {
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
        minusButton = findViewById(R.id.minusButton)
        plusButton = findViewById(R.id.plusButton)
        parallelCountTextView = findViewById(R.id.parallelCountTextView)
    }
    
    /**
     * Initializes managers.
     */
    private fun initializeManagers() {
        assetPackManager = AssetPackManager(this)
        fileOperations = FileOperations(this)
        proofTestRunner = ProofTestRunner(fileOperations) { message ->
            lifecycleScope.launch(Dispatchers.Main) {
                logTextView.append("$message\n")
            }
        }
        parallelProofManager = ParallelProofManager(minusButton, plusButton, parallelCountTextView)
    }
    
    /**
     * Sets up the spinner with examples.
     */
    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, examples)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exampleSpinner.adapter = adapter
    }
    
    /**
     * Sets up the run button click listener.
     */
    private fun setupRunButton() {
        runButton.setOnClickListener {
            val selectedExample = exampleSpinner.selectedItem as com.ingonyama.imp1_aar_example.data.Example
            if (isParallelMode) {
                runParallelProofTest(selectedExample)
            } else {
                runSingleProofTest(selectedExample)
            }
        }
    }
    
    /**
     * Checks the status of all zkey asset packs and initiates downloads if needed.
     */
    private fun checkAssetPacksStatus() {
        lifecycleScope.launch {
            assetPackManager.checkAssetPacksStatus().collectLatest { state ->
                updateDownloadStatus(state)
            }
        }
    }
    
    /**
     * Updates the download status UI based on asset pack state.
     */
    private fun updateDownloadStatus(state: AssetPackManager.AssetPackState) {
        downloadStatusTextView.text = state.statusMessage
        downloadProgressBar.progress = state.averageProgress
        
        // Enable the run button if all packs are ready
        runButton.isEnabled = state.isReady
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
        
        // Set up parallel proof manager
        parallelProofManager.setupButtonHandlers { count ->
            // Handle count changes if needed
        }
    }
    
    /**
     * Updates the UI based on the selected mode.
     */
    private fun updateUIForMode() {
        if (isParallelMode) {
            parallelConfigLayout.visibility = View.VISIBLE
            runButton.text = Constants.BUTTON_TEXT_PARALLEL_PROOFS
        } else {
            parallelConfigLayout.visibility = View.GONE
            runButton.text = Constants.BUTTON_TEXT_SINGLE_PROOF
        }
    }

    /**
     * Runs a single proof test.
     */
    private fun runSingleProofTest(example: com.ingonyama.imp1_aar_example.data.Example) {
        lifecycleScope.launch(Dispatchers.IO + CoroutineName(Constants.COROUTINE_NAME_SINGLE_PROOF)) {
            withContext(Dispatchers.Main) {
                logTextView.text = ""
                progressBar.visibility = View.VISIBLE
                runButton.isEnabled = false
            }
            
            try {
                val packLocation = assetPackManager.getPackLocation(example.assetPack)
                if (packLocation == null) {
                    withContext(Dispatchers.Main) {
                        logTextView.append(Constants.ERROR_ASSET_PACK_NOT_AVAILABLE.format(example.assetPack) + "\n")
                        progressBar.visibility = View.GONE
                        runButton.isEnabled = true
                    }
                    return@launch
                }
                
                val result = proofTestRunner.runSingleProofTest(example, packLocation)
                
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = true
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logTextView.append(Constants.ERROR_PREFIX + e.message + "\n")
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = true
                }
            }
        }
    }

    /**
     * Runs parallel proof tests.
     */
    private fun runParallelProofTest(example: com.ingonyama.imp1_aar_example.data.Example) {
        lifecycleScope.launch(Dispatchers.IO + CoroutineName(Constants.COROUTINE_NAME_PARALLEL_PROOF)) {
            withContext(Dispatchers.Main) {
                logTextView.text = ""
                progressBar.visibility = View.VISIBLE
                runButton.isEnabled = false
            }
            
            try {
                val packLocation = assetPackManager.getPackLocation(example.assetPack)
                if (packLocation == null) {
                    withContext(Dispatchers.Main) {
                        logTextView.append(Constants.ERROR_ASSET_PACK_NOT_AVAILABLE.format(example.assetPack) + "\n")
                        progressBar.visibility = View.GONE
                        runButton.isEnabled = true
                    }
                    return@launch
                }
                
                val numParallelProofs = parallelProofManager.getNumParallelProofs()
                val result = proofTestRunner.runParallelProofTest(example, packLocation, numParallelProofs)
                
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = true
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logTextView.append(Constants.ERROR_PREFIX + e.message + "\n")
                    progressBar.visibility = View.GONE
                    runButton.isEnabled = true
                }
            }
        }
    }


    

}
