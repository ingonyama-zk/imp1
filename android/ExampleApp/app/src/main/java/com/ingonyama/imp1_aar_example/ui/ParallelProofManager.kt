package com.ingonyama.imp1_aar_example.ui

import android.widget.TextView
import com.ingonyama.imp1_aar_example.R

/**
 * Manages the parallel proof UI state and interactions.
 */
class ParallelProofManager(
    private val minusButton: TextView,
    private val plusButton: TextView,
    private val parallelCountTextView: TextView
) {
    
    private var numParallelProofs: Int = 2
    
    /**
     * Sets up the plus/minus button handlers.
     */
    fun setupButtonHandlers(
        onCountChanged: (Int) -> Unit
    ) {
        minusButton.setOnClickListener {
            if (numParallelProofs > MIN_PARALLEL_PROOFS) {
                numParallelProofs--
                parallelCountTextView.text = numParallelProofs.toString()
                updateButtonStates()
                onCountChanged(numParallelProofs)
            }
        }
        
        plusButton.setOnClickListener {
            if (numParallelProofs < MAX_PARALLEL_PROOFS) {
                numParallelProofs++
                parallelCountTextView.text = numParallelProofs.toString()
                updateButtonStates()
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
        minusButton.isEnabled = numParallelProofs > MIN_PARALLEL_PROOFS
        plusButton.isEnabled = numParallelProofs < MAX_PARALLEL_PROOFS
        
        // Update button alpha for visual feedback
        minusButton.alpha = if (numParallelProofs > MIN_PARALLEL_PROOFS) 1.0f else 0.3f
        plusButton.alpha = if (numParallelProofs < MAX_PARALLEL_PROOFS) 1.0f else 0.3f
        
        // Update text color for better visibility
        val enabledColor = minusButton.resources.getColor(android.R.color.holo_blue_dark, null)
        val disabledColor = minusButton.resources.getColor(android.R.color.darker_gray, null)
        
        minusButton.setTextColor(if (numParallelProofs > MIN_PARALLEL_PROOFS) enabledColor else disabledColor)
        plusButton.setTextColor(if (numParallelProofs < MAX_PARALLEL_PROOFS) enabledColor else disabledColor)
    }
    
    /**
     * Gets the current number of parallel proofs.
     */
    fun getNumParallelProofs(): Int = numParallelProofs
    
    /**
     * Sets the number of parallel proofs.
     */
    fun setNumParallelProofs(count: Int) {
        numParallelProofs = count.coerceIn(MIN_PARALLEL_PROOFS, MAX_PARALLEL_PROOFS)
        parallelCountTextView.text = numParallelProofs.toString()
        updateButtonStates()
    }
    
    companion object {
        const val MIN_PARALLEL_PROOFS = 1
        const val MAX_PARALLEL_PROOFS = 50
    }
} 