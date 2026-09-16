package com.alikuxac.aliss.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alikuxac.aliss.domain.usecase.GetConfigUseCase
import com.alikuxac.aliss.domain.usecase.SaveConfigUseCase

class SettingsViewModelFactory(
    private val getConfigUseCase: GetConfigUseCase,
    private val saveConfigUseCase: SaveConfigUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(getConfigUseCase, saveConfigUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
