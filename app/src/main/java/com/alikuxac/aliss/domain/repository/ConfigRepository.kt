package com.alikuxac.aliss.domain.repository

import com.alikuxac.aliss.domain.model.S3Config

interface ConfigRepository {
    fun getConfig(): S3Config
    fun saveConfig(config: S3Config)
}
