package com.ingonyama.imp1_aar_example.ui

import android.widget.TextView
import com.ingonyama.imp1_aar_example.Constants
import com.ingonyama.imp1_aar_example.R

/**
 * Manages the parallel proof UI state and interactions.
 */
class ParallelProofManager(
    private val minusButton: TextView,
    private val plusButton: TextView,
    private val parallelCountTextView: TextView
) {
    
    private var numParallelProofs: Int = Constants.DEFAULT_PARALLEL_PROOFS
    
    /**
     * Sets up the plus/minus button handlers.
     */
    fun setupButtonHandlers(
        onCountChanged: (Int) -> Unit
    ) {
        minusButton.setOnClickListener {
            if (numParallelProofs > Constants.MIN_PARALLEL_PROOFS) {
                decrementParallelProofs()
                onCountChanged(numParallelProofs)
            }
        }
        
        plusButton.setOnClickListener {
            if (numParallelProofs < Constants.MAX_PARALLEL_PROOFS) {
                incrementParallelProofs()
                onCountChanged(numParallelProofs)
            }
        }
        
        // Initialize button states
        updateButtonStates()
    }
    
    /**
     * Updates the enabled/disabled state of plus/minus buttons.
     */
    private fun updateButtonStates() {
        minusButton.isEnabled = numParallelProofs > Constants.MIN_PARALLEL_PROOFS
        plusButton.isEnabled = numParallelProofs < Constants.MAX_PARALLEL_PROOFS
        
        // Update button alpha for visual feedback
        minusButton.alpha = if (numParallelProofs > Constants.MIN_PARALLEL_PROOFS) Constants.UI_ALPHA_ENABLED else Constants.UI_ALPHA_DISABLED
        plusButton.alpha = if (numParallelProofs < Constants.MAX_PARALLEL_PROOFS) Constants.UI_ALPHA_ENABLED else Constants.UI_ALPHA_DISABLED
        
        // Update text color for better visibility
        val enabledColor = minusButton.resources.getColor(android.R.color.holo_blue_dark, null)
        val disabledColor = minusButton.resources.getColor(android.R.color.darker_gray, null)
        
        minusButton.setTextColor(if (numParallelProofs > Constants.MIN_PARALLEL_PROOFS) enabledColor else disabledColor)
        plusButton.setTextColor(if (numParallelProofs < Constants.MAX_PARALLEL_PROOFS) enabledColor else disabledColor)
    }
    
    /**
     * Gets the current number of parallel proofs.
     */
    fun getNumParallelProofs(): Int = numParallelProofs
    
    /**
     * Sets the number of parallel proofs.
     */
    fun setNumParallelProofs(count: Int) {
        numParallelProofs = count.coerceIn(Constants.MIN_PARALLEL_PROOFS, Constants.MAX_PARALLEL_PROOFS)
        parallelCountTextView.text = numParallelProofs.toString()
        updateButtonStates()
    }
    
    /**
     * Increments the number of parallel proofs.
     */
    private fun incrementParallelProofs() {
        if (numParallelProofs < Constants.MAX_PARALLEL_PROOFS) {
            numParallelProofs++
            parallelCountTextView.text = numParallelProofs.toString()
            updateButtonStates()
        }
    }
    
    /**
     * Decrements the number of parallel proofs.
     */
    private fun decrementParallelProofs() {
        if (numParallelProofs > Constants.MIN_PARALLEL_PROOFS) {
            numParallelProofs--
            parallelCountTextView.text = numParallelProofs.toString()
            updateButtonStates()
        }
    }
} 