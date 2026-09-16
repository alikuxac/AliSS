package com.alikuxac.aliss.presentation.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alikuxac.aliss.data.MediaSyncService
import com.alikuxac.aliss.domain.model.S3Config
import com.alikuxac.aliss.domain.usecase.GetConfigUseCase
import com.alikuxac.aliss.domain.usecase.SaveConfigUseCase

class SettingsViewModel(
    private val getConfigUseCase: GetConfigUseCase,
    private val saveConfigUseCase: SaveConfigUseCase
) : ViewModel() {

    private val _config = MutableLiveData<S3Config>().apply {
        value = getConfigUseCase()
    }
    val config: LiveData<S3Config> = _config

    fun saveConfig(
        endpoint: String,
        bucket: String,
        accessKey: String,
        secretKey: String,
        isEnabled: Boolean,
        context: Context
    ) {
        val newConfig = S3Config(endpoint, bucket, accessKey, secretKey, isEnabled)
        saveConfigUseCase(newConfig)
        _config.value = newConfig

        val intent = Intent(context, MediaSyncService::class.java)
        if (isEnabled) {
            context.startForegroundService(intent)
        } else {
            context.stopService(intent)
        }
    }
}
