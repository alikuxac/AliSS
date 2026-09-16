package com.alikuxac.aliss.data

import android.content.Context
import android.content.SharedPreferences

data class S3Config(
    val endpoint: String = "",
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val isServiceEnabled: Boolean = false,
    val syncFolder: String = "",
    val isWifiOnly: Boolean = false
)

class S3ConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("s3_prefs", Context.MODE_PRIVATE)

    fun saveConfig(config: S3Config) {
        prefs.edit().apply {
            putString("endpoint", config.endpoint)
            putString("bucket", config.bucket)
            putString("access_key", config.accessKey)
            putString("secret_key", config.secretKey)
            putBoolean("is_service_enabled", config.isServiceEnabled)
            putString("sync_folder", config.syncFolder)
            putBoolean("is_wifi_only", config.isWifiOnly)
            apply()
        }
    }

    fun getConfig(): S3Config {
        return S3Config(
            endpoint = prefs.getString("endpoint", "") ?: "",
            bucket = prefs.getString("bucket", "") ?: "",
            accessKey = prefs.getString("access_key", "") ?: "",
            secretKey = prefs.getString("secret_key", "") ?: "",
            isServiceEnabled = prefs.getBoolean("is_service_enabled", false),
            syncFolder = prefs.getString("sync_folder", "") ?: "",
            isWifiOnly = prefs.getBoolean("is_wifi_only", false)
        )
    }
}
