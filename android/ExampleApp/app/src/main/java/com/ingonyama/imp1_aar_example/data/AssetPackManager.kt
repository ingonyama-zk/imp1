package com.ingonyama.imp1_aar_example.data

import android.content.Context
import com.google.android.play.core.assetpacks.AssetPackLocation
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState as GoogleAssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import com.ingonyama.imp1_aar_example.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class AssetPackManager(private val context: Context) {
    
    private val assetPackManager: AssetPackManager = AssetPackManagerFactory.getInstance(context)
    private val assetPackNames: List<String> = com.ingonyama.imp1_aar_example.BuildConfig.ASSET_PACK_NAMES.toList()
    private val packDownloadProgress = mutableMapOf<String, Int>()
    private val packDownloadStatus = mutableMapOf<String, Int>()
    
    data class AssetPackState(
        val completedPacks: Int,
        val totalPacks: Int,
        val isDownloading: Boolean,
        val hasFailed: Boolean,
        val averageProgress: Int,
        val statusMessage: String,
        val isReady: Boolean
    )
    
    /**
     * Checks the status of all zkey asset packs and initiates downloads if needed.
     */
    suspend fun checkAssetPacksStatus(): Flow<AssetPackState> = callbackFlow {
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
        
        // Register the listener to handle updates
        lateinit var listener: AssetPackStateUpdateListener
        
        listener = AssetPackStateUpdateListener { state ->
            val packName = state.name()
            if (packName in assetPackNames) {
                packDownloadStatus[packName] = state.status()
                updatePackProgress(state)
                
                // Always send the current state
                trySend(createAssetPackState())
                
                when (state.status()) {
                    AssetPackStatus.COMPLETED -> {
                        // Check if all packs are completed
                        if (packDownloadStatus.values.all { it == AssetPackStatus.COMPLETED }) {
                            close()
                        }
                    }
                    AssetPackStatus.FAILED,
                    AssetPackStatus.CANCELED -> {
                        close()
                    }
                    else -> {
                        // Continue listening for updates
                    }
                }
            }
        }
        
        assetPackManager.registerListener(listener)
        
        if (packsToDownload.isNotEmpty()) {
            downloadAssetPacks(packsToDownload)
        } else {
            // All packs are already available, just send the state
            trySend(createAssetPackState())
        }
        
        awaitClose {
            try {
                assetPackManager.unregisterListener(listener)
            } catch (e: Exception) {
                // Listener might already be unregistered
            }
        }
    }
    
    /**
     * Downloads the specified asset packs.
     */
    private fun downloadAssetPacks(packNames: List<String>) {
        assetPackManager.fetch(packNames)
    }
    
    /**
     * Updates the progress for a specific pack.
     */
    private fun updatePackProgress(state: GoogleAssetPackState) {
        val progress = if (state.totalBytesToDownload() > 0) {
            ((100.0 * state.bytesDownloaded()) / state.totalBytesToDownload()).toInt()
        } else {
            0
        }
        packDownloadProgress[state.name()] = progress
    }
    
    /**
     * Creates the current asset pack state.
     */
    private fun createAssetPackState(): AssetPackState {
        val completedPacks = packDownloadStatus.values.count { it == AssetPackStatus.COMPLETED }
        val totalPacks = assetPackNames.size
        val isDownloading = packDownloadStatus.values.any { it == AssetPackStatus.DOWNLOADING }
        val hasFailed = packDownloadStatus.values.any { it == AssetPackStatus.FAILED }
        val averageProgress = packDownloadProgress.values.average().toInt()
        val isReady = completedPacks == totalPacks
        
        val statusMessage = when {
            isReady -> Constants.STATUS_ALL_PACKS_READY.format(completedPacks, totalPacks)
            isDownloading -> Constants.STATUS_DOWNLOADING_PACKS.format(averageProgress, completedPacks, totalPacks)
            hasFailed -> Constants.STATUS_DOWNLOADS_FAILED.format(completedPacks, totalPacks)
            else -> Constants.STATUS_CHECKING_PACKS.format(completedPacks, totalPacks)
        }
        
        return AssetPackState(
            completedPacks = completedPacks,
            totalPacks = totalPacks,
            isDownloading = isDownloading,
            hasFailed = hasFailed,
            averageProgress = averageProgress,
            statusMessage = statusMessage,
            isReady = isReady
        )
    }
    
    /**
     * Gets the location of a specific asset pack.
     */
    fun getPackLocation(packName: String): AssetPackLocation? {
        return assetPackManager.getPackLocation(packName)
    }
} 