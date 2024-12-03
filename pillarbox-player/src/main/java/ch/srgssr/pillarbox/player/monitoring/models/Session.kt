/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.provider.Settings.Secure
import android.view.WindowManager
import ch.srgssr.pillarbox.player.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a monitoring session, which encapsulates information about the device, current media, player, and performance metrics.
 *
 * @property device Information about the device.
 * @property media Information about the media being played.
 * @property operatingSystem Information about the device's operating system.
 * @property player Information about the player.
 * @property qoeTimings Quality of Experience timings, representing user-perceived performance metrics related to media loading and playback.
 * @property qosTimings Quality of Service timings, representing pre-playback performance metrics gathered during resource loading.
 * @property screen Information about the device's screen.
 */
@Serializable
data class Session(
    val device: Device,
    val media: Media,
    @SerialName("os") val operatingSystem: OS = OS(
        name = PLATFORM_NAME,
        version = OPERATING_SYSTEM_VERSION,
    ),
    val player: Player = Player(
        name = PLAYER_NAME,
        platform = PLATFORM_NAME,
        version = PLAYER_VERSION,
    ),
    @SerialName("qoe_timings") val qoeTimings: Timings.QoE = Timings.QoE(),
    @SerialName("qos_timings") val qosTimings: Timings.QoS = Timings.QoS(),
    val screen: Screen,
) : MessageData {
    constructor(
        context: Context,
        media: Media,
        qoeTimings: Timings.QoE,
        qosTimings: Timings.QoS,
    ) : this(
        device = Device(
            id = getDeviceId(context),
            model = getDeviceModel(),
            type = context.getDeviceType(),
        ),
        media = media,
        qoeTimings = qoeTimings,
        qosTimings = qosTimings,
        screen = context.getWindowBounds().let { windowBounds ->
            Screen(
                height = windowBounds.height(),
                width = windowBounds.width(),
            )
        },
    )

    /**
     * Represents the information about a device's screen.
     *
     * @property height The height of the screen, in pixels.
     * @property width The width of the screen, in pixels.
     */
    @Serializable
    data class Screen(
        val height: Int,
        val width: Int,
    )

    /**
     * Represents information about the operating system.
     *
     * @property name The name of the operating system.
     * @property version The version of the operating system.
     */
    @Serializable
    data class OS(
        val name: String,
        val version: String,
    )

    /**
     * Represents information about the player.
     *
     * @property name The name of the player.
     * @property platform The platform the player is using.
     * @property version The version of the player.
     */
    @Serializable
    data class Player(
        val name: String,
        val platform: String,
        val version: String,
    )

    /**
     * Represents information about the media being played.
     *
     * @property assetUrl The URL of the asset.
     * @property id The id of the media.
     * @property metadataUrl The URL of the metadata.
     * @property origin The origin of the media.
     */
    @Serializable
    data class Media(
        @SerialName("asset_url") val assetUrl: String,
        val id: String,
        @SerialName("metadata_url") val metadataUrl: String,
        val origin: String,
    )

    /**
     * Represents information about the device.
     *
     * @property id The unique identifier of the device.
     * @property model The model of the device.
     * @property type The type of device.
     */
    @Serializable
    data class Device(
        val id: String,
        val model: String,
        val type: Type?,
    ) {
        /**
         * Represents the type of a device.
         */
        @Suppress("UndocumentedPublicProperty")
        enum class Type {
            @SerialName("Car")
            CAR,

            @SerialName("Desktop")
            DESKTOP,

            @SerialName("Phone")
            PHONE,

            @SerialName("Tablet")
            TABLET,
            TV,
        }
    }

    private companion object {
        private val OPERATING_SYSTEM_VERSION = Build.VERSION.RELEASE
        private const val PHONE_TABLET_WIDTH_THRESHOLD = 600
        private const val PLATFORM_NAME = "Android"
        private const val PLAYER_NAME = "Pillarbox"
        private const val PLAYER_VERSION = BuildConfig.VERSION_NAME

        @SuppressLint("HardwareIds")
        private fun getDeviceId(context: Context): String {
            return Secure.getString(
                context.contentResolver,
                Secure.ANDROID_ID
            ) ?: ""
        }

        private fun getDeviceModel(): String {
            return Build.MANUFACTURER + " " + Build.MODEL
        }

        private fun Context.getDeviceType(): Device.Type? {
            val configuration = resources.configuration
            return when (configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) {
                Configuration.UI_MODE_TYPE_CAR -> Device.Type.CAR
                Configuration.UI_MODE_TYPE_DESK -> Device.Type.DESKTOP
                Configuration.UI_MODE_TYPE_NORMAL -> {
                    val smallestWidthDp = configuration.smallestScreenWidthDp

                    if (smallestWidthDp >= PHONE_TABLET_WIDTH_THRESHOLD) {
                        Device.Type.TABLET
                    } else {
                        Device.Type.PHONE
                    }
                }

                Configuration.UI_MODE_TYPE_TELEVISION -> Device.Type.TV
                else -> null
            }
        }

        private fun Context.getWindowBounds(): Rect {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                Rect().also {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay.getRectSize(it)
                }
            } else {
                windowManager.maximumWindowMetrics.bounds
            }
        }
    }
}
