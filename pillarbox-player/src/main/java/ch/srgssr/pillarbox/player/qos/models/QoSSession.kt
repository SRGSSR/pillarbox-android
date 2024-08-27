/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.provider.Settings.Secure
import android.view.WindowManager
import ch.srgssr.pillarbox.player.BuildConfig
import ch.srgssr.pillarbox.player.qos.models.QoSDevice.DeviceType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a QoS session, which contains information about the device, current media, and player.
 *
 * @property device The information about the device.
 * @property media The information about the media being played.
 * @property operatingSystem The information about the operating system.
 * @property player The information about the player.
 * @property qoeTimings The metrics about the time needed to load the various media components, as experienced by the user.
 * @property qosTimings The metrics about the time needed to load the various media components, during the preload phase.
 * @property screen The information about the device screen.
 */
@Serializable
data class QoSSession(
    val device: QoSDevice,
    val media: QoSMedia,
    @SerialName("os") val operatingSystem: QoSOS = QoSOS(
        name = PLATFORM_NAME,
        version = OPERATING_SYSTEM_VERSION,
    ),
    val player: QoSPlayer = QoSPlayer(
        name = PLAYER_NAME,
        platform = PLATFORM_NAME,
        version = PLAYER_VERSION,
    ),
    @SerialName("qoe_timings") val qoeTimings: QoETimings = QoETimings(),
    @SerialName("qos_timings") val qosTimings: QoSTimings = QoSTimings(),
    val screen: QoSScreen,
) : QoSMessageData {
    constructor(
        context: Context,
        media: QoSMedia,
        qoeTimings: QoETimings,
        qosTimings: QoSTimings,
    ) : this(
        device = QoSDevice(
            id = getDeviceId(context),
            model = getDeviceModel(),
            type = context.getDeviceType(),
        ),
        media = media,
        qoeTimings = qoeTimings,
        qosTimings = qosTimings,
        screen = context.getWindowBounds().let { windowBounds ->
            QoSScreen(
                height = windowBounds.height(),
                width = windowBounds.width(),
            )
        },
    )

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

        private fun Context.getDeviceType(): DeviceType? {
            val configuration = resources.configuration
            return when (configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) {
                Configuration.UI_MODE_TYPE_CAR -> DeviceType.CAR
                Configuration.UI_MODE_TYPE_DESK -> DeviceType.DESKTOP
                Configuration.UI_MODE_TYPE_NORMAL -> {
                    val smallestWidthDp = configuration.smallestScreenWidthDp

                    if (smallestWidthDp >= PHONE_TABLET_WIDTH_THRESHOLD) {
                        DeviceType.TABLET
                    } else {
                        DeviceType.PHONE
                    }
                }

                Configuration.UI_MODE_TYPE_TELEVISION -> DeviceType.TV
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
