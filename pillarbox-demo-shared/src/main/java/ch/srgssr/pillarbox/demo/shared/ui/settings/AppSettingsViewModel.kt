/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App settings view model
 *
 * @param appSettingsRepository
 */
class AppSettingsViewModel(private val appSettingsRepository: AppSettingsRepository) : ViewModel() {

    /**
     * Current app settings
     */
    val currentAppSettings = appSettingsRepository.getAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AppSettings())

    /**
     * Set metrics overlay enabled
     *
     * @param enabled
     */
    fun setMetricsOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setMetricsOverlayEnabled(enabled)
        }
    }

    /**
     * Set metrics overlay text color
     *
     * @param textColor
     */
    fun setMetricsOverlayTextColor(textColor: AppSettings.TextColor) {
        viewModelScope.launch {
            appSettingsRepository.setMetricsOverlayTextColor(textColor)
        }
    }

    /**
     * Set metrics overlay text size
     *
     * @param textSize
     */
    fun setMetricsOverlayTextSize(textSize: AppSettings.TextSize) {
        viewModelScope.launch {
            appSettingsRepository.setMetricsOverlayTextSize(textSize)
        }
    }

    /**
     * Set receiver application ID
     */
    fun setReceiverApplicationId(receiverApplicationId: String) {
        viewModelScope.launch {
            appSettingsRepository.setReceiverApplicationId(receiverApplicationId)
        }
    }

    /**
     * Factory
     *
     * @param appSettingsRepository
     */
    class Factory(private val appSettingsRepository: AppSettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppSettingsViewModel(appSettingsRepository) as T
        }
    }
}
