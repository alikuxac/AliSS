package com.alikuxac.aliss.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.alikuxac.aliss.domain.model.S3Config
import com.alikuxac.aliss.domain.repository.ConfigRepository

class ConfigRepositoryImpl(context: Context) : ConfigRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("s3_prefs", Context.MODE_PRIVATE)

    override fun getConfig(): S3Config {
        return S3Config(
            endpoint = prefs.getString("endpoint", "") ?: "",
            bucket = prefs.getString("bucket", "") ?: "",
            accessKey = prefs.getString("access_key", "") ?: "",
            secretKey = prefs.getString("secret_key", "") ?: "",
            isServiceEnabled = prefs.getBoolean("is_service_enabled", false)
        )
    }

    override fun saveConfig(config: S3Config) {
        prefs.edit().apply {
            putString("endpoint", config.endpoint)
            putString("bucket", config.bucket)
            putString("access_key", config.accessKey)
            putString("secret_key", config.secretKey)
            putBoolean("is_service_enabled", config.isServiceEnabled)
            apply()
        }
    }
}
