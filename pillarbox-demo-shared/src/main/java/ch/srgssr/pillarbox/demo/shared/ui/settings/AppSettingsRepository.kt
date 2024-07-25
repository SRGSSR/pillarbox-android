/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

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
                val textColor = try {
                    AppSettings.TextColor.valueOf(
                        preferences[PreferencesKeys.COLOR_METRICS_OVERLAY] ?: AppSettings.TextColor.Yellow.name
                    )
                } catch (e: IllegalArgumentException) {
                    Log.w(this::class.simpleName, "Can't parse text color", e)
                    AppSettings.TextColor.Yellow
                }

                val textSize = try {
                    AppSettings.TextSize.valueOf(
                        preferences[PreferencesKeys.FONT_SIZE_METRICS_OVERLAY] ?: AppSettings.TextSize.Medium.name
                    )
                } catch (e: IllegalArgumentException) {
                    Log.w(this::class.simpleName, "Can't parse text size", e)
                    AppSettings.TextSize.Medium
                }
                AppSettings(
                    metricsOverlayTextSize = textSize,
                    metricsOverlayTextColor = textColor,
                    metricsOverlayEnabled = preferences[PreferencesKeys.ENABLE_METRICS_OVERLAY] ?: false,
                )
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

    /**
     * Set metrics overlay text color
     *
     * @param color
     */
    suspend fun setMetricsOverlayTextColor(color: AppSettings.TextColor) {
        dataStore.edit {
            it[PreferencesKeys.COLOR_METRICS_OVERLAY] = color.name
        }
    }

    /**
     * Set metrics overlay text size
     *
     * @param fontSize
     */
    suspend fun setMetricsOverlayTextSize(fontSize: AppSettings.TextSize) {
        dataStore.edit {
            it[PreferencesKeys.FONT_SIZE_METRICS_OVERLAY] = fontSize.name
        }
    }

    private object PreferencesKeys {
        val ENABLE_METRICS_OVERLAY = booleanPreferencesKey("enable_metrics_overlay")
        val COLOR_METRICS_OVERLAY = stringPreferencesKey("color_metrics_overlay")
        val FONT_SIZE_METRICS_OVERLAY = stringPreferencesKey("font_size_metrics_overlay")
    }
}
