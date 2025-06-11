/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings.TextColor
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings.TextSize
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
                AppSettings(
                    metricsOverlayTextSize = preferences.getEnum(PreferencesKeys.METRICS_OVERLAY_TEXT_SIZE, TextSize.Medium),
                    metricsOverlayTextColor = preferences.getEnum(PreferencesKeys.METRICS_OVERLAY_TEXT_COLOR, TextColor.Yellow),
                    metricsOverlayEnabled = preferences[PreferencesKeys.METRICS_OVERLAY_ENABLED] ?: false,
                    receiverApplicationId = preferences[PreferencesKeys.RECEIVER_APPLICATION_ID] ?: AppSettings.ReceiverId.Default,
                    smoothSeekingEnabled = preferences[PreferencesKeys.SMOOTH_SEEKING_ENABLED] ?: true,
                )
            }
    }

    /**
     * Set smooth seeking enabled
     *
     * @param enabled
     */
    suspend fun setSmoothSeekingEnabled(enabled: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.SMOOTH_SEEKING_ENABLED] = enabled
        }
    }

    /**
     * Set metrics overlay enabled
     *
     * @param enabled
     */
    suspend fun setMetricsOverlayEnabled(enabled: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.METRICS_OVERLAY_ENABLED] = enabled
        }
    }

    /**
     * Set metrics overlay text color
     *
     * @param textColor
     */
    suspend fun setMetricsOverlayTextColor(textColor: TextColor) {
        dataStore.edit {
            it[PreferencesKeys.METRICS_OVERLAY_TEXT_COLOR] = textColor.name
        }
    }

    /**
     * Set metrics overlay text size
     *
     * @param textSize
     */
    suspend fun setMetricsOverlayTextSize(textSize: TextSize) {
        dataStore.edit {
            it[PreferencesKeys.METRICS_OVERLAY_TEXT_SIZE] = textSize.name
        }
    }

    /**
     * Set receiver application id
     *
     * @param receiverApplicationId
     */
    suspend fun setReceiverApplicationId(receiverApplicationId: String) {
        dataStore.edit {
            it[PreferencesKeys.RECEIVER_APPLICATION_ID] = receiverApplicationId
        }
    }

    private object PreferencesKeys {
        val METRICS_OVERLAY_ENABLED = booleanPreferencesKey("metrics_overlay_enabled")
        val METRICS_OVERLAY_TEXT_COLOR = stringPreferencesKey("metrics_overlay_text_color")
        val METRICS_OVERLAY_TEXT_SIZE = stringPreferencesKey("metrics_overlay_text_size")
        val RECEIVER_APPLICATION_ID = stringPreferencesKey("receiver_application_id")
        val SMOOTH_SEEKING_ENABLED = booleanPreferencesKey("smooth_seeking_enabled")
    }

    private companion object {
        private const val TAG = "AppSettingsRepository"

        private inline fun <reified T : Enum<T>> Preferences.getEnum(
            key: Preferences.Key<String>,
            defaultValue: T,
        ): T {
            return try {
                get(key)?.let { enumValueOf<T>(it) } ?: defaultValue
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Can't parse enum value", e)
                defaultValue
            }
        }
    }
}
