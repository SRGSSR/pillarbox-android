/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * App settings
 *
 * @property metricsOverlayEnabled
 * @property metricsOverlayOptions
 */
class AppSettings(
    val metricsOverlayEnabled: Boolean = false,
    val metricsOverlayOptions: MetricsOverlayOptions = MetricsOverlayOptions()
)

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * App settings repository
 * @param context The context.
 */
class AppSettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    /**
     * Get app settings
     *
     * @return
     */
    fun getAppSettings(): Flow<AppSettings> {
        return dataStore.data
            .catch {
                if (it is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { preferences ->
                AppSettings(metricsOverlayEnabled = preferences[PreferencesKeys.ENABLE_METRICS_OVERLAY] ?: false)
            }
    }

    /**
     * Set metrics overlay enabled
     *
     * @param enabled
     */
    suspend fun setMetricsOverlayEnabled(enabled: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.ENABLE_METRICS_OVERLAY] = enabled
        }
    }

    private object PreferencesKeys {
        val ENABLE_METRICS_OVERLAY = booleanPreferencesKey("enable_metrics_overlay")
    }
}
