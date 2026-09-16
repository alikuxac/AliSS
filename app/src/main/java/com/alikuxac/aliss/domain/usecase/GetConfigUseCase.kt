package com.alikuxac.aliss.domain.usecase

import com.alikuxac.aliss.domain.model.S3Config
import com.alikuxac.aliss.domain.repository.ConfigRepository

class GetConfigUseCase(private val repository: ConfigRepository) {
    operator fun invoke(): S3Config {
        return repository.getConfig()
    }
}
