package com.alikuxac.aliss.domain.usecase

import com.alikuxac.aliss.domain.model.S3Config
import com.alikuxac.aliss.domain.repository.ConfigRepository

class SaveConfigUseCase(private val repository: ConfigRepository) {
    operator fun invoke(config: S3Config) {
        repository.saveConfig(config)
    }
}
