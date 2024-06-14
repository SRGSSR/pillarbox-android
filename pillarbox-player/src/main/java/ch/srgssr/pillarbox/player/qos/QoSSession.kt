/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.view.WindowManager
import ch.srgssr.pillarbox.player.BuildConfig

/**
 * Represents a QoS session, which contains information about the device, current media, and player.
 *
 * @property deviceId The unique identifier of the device.
 * @property deviceModel The model of the device.
 * @property deviceType The type of device.
 * @property mediaId The identifier of the media being played.
 * @property mediaSource The source URL of the media being played.
 * @property operatingSystemName The name of the operating system.
 * @property operatingSystemVersion The version of the operating system.
 * @property origin The origin of the player.
 * @property playerName The name of the player.
 * @property playerPlatform The platform of the player.
 * @property playerVersion The version of the player.
 * @property screenHeight The height of the screen in pixels.
 * @property screenWidth The width of the screen in pixels.
 * @property timings The timing until the current media started to play.
 */
data class QoSSession(
    val deviceId: String,
    val deviceModel: String = getDeviceModel(),
    val deviceType: DeviceType,
    val mediaId: String,
    val mediaSource: String,
    val operatingSystemName: String = PLATFORM_NAME,
    val operatingSystemVersion: String = OPERATING_SYSTEM_VERSION,
    val origin: String,
    val playerName: String = PLAYER_NAME,
    val playerPlatform: String = PLATFORM_NAME,
    val playerVersion: String = PLAYER_VERSION,
    val screenHeight: Int,
    val screenWidth: Int,
    val timings: QoSSessionTimings,
) {
    /**
     * The type of device.
     */
    enum class DeviceType {
        CAR,
        PHONE,
        TABLET,
        TV,
    }

    constructor(
        context: Context,
        mediaId: String,
        mediaSource: String,
        timings: QoSSessionTimings,
    ) : this(
        deviceId = getDeviceId(),
        deviceModel = getDeviceModel(),
        deviceType = context.getDeviceType(),
        mediaId = mediaId,
        mediaSource = mediaSource,
        operatingSystemName = PLATFORM_NAME,
        operatingSystemVersion = OPERATING_SYSTEM_VERSION,
        origin = context.packageName,
        playerName = PLAYER_NAME,
        playerPlatform = PLATFORM_NAME,
        playerVersion = PLAYER_VERSION,
        screenHeight = context.getWindowBounds().height(),
        screenWidth = context.getWindowBounds().width(),
        timings = timings,
    )

    private companion object {
        private val OPERATING_SYSTEM_VERSION = Build.VERSION.RELEASE
        private const val PHONE_TABLET_WIDTH_THRESHOLD = 600
        private const val PLATFORM_NAME = "android"
        private const val PLAYER_NAME = "pillarbox"
        private const val PLAYER_VERSION = BuildConfig.VERSION_NAME

        private fun getDeviceId(): String {
            // TODO Define this somehow
            return ""
        }

        private fun getDeviceModel(): String {
            return Build.MANUFACTURER + " " + Build.MODEL
        }

        private fun Context.getDeviceType(): DeviceType {
            val configuration = resources.configuration
            return when (configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) {
                Configuration.UI_MODE_TYPE_CAR -> DeviceType.CAR
                Configuration.UI_MODE_TYPE_NORMAL -> {
                    val smallestWidthDp = configuration.smallestScreenWidthDp

                    if (smallestWidthDp >= PHONE_TABLET_WIDTH_THRESHOLD) {
                        DeviceType.TABLET
                    } else {
                        DeviceType.PHONE
                    }
                }

                Configuration.UI_MODE_TYPE_TELEVISION -> DeviceType.TV
                else -> DeviceType.PHONE // TODO Do we assume PHONE by default? Or do we throw an exception?
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
