/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import android.media.MediaDrm
import android.os.Build
import android.util.Log
import androidx.media3.common.C

/**
 * Describes the playback capabilities of the device, so that the integration layer only returns resources that the device is actually able to
 * play.
 *
 * Use [DeviceCapabilities.device] to get the capabilities of the current device. They are sent to the integration layer as query parameters of
 * the [IlUrl][ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl].
 *
 * The DRM capabilities are only sent to the integration layer when [drmSecurityLevel] is known.
 *
 * @property platform The platform requesting the media composition.
 * @property drmVendor The DRM vendor requesting the media composition.
 * @property drmSecurityLevel The Widevine security level of the device, or `null` if Widevine is unavailable.
 */
class DeviceCapabilities(
    val platform: String = PLATFORM_ANDROID,
    val drmVendor: String = VENDOR_ANDROID,
    val drmSecurityLevel: String? = null,
) {
    companion object {
        /**
         * The platform value identifying Android clients to the integration layer.
         */
        const val PLATFORM_ANDROID = "android"

        /**
         * The vendor value identifying Android clients to the integration layer.
         */
        const val VENDOR_ANDROID = "com.widevine.alpha"

        private const val TAG = "DeviceCapabilities"

        /**
         * Vendor defined [MediaDrm] property. Unlike [MediaDrm.PROPERTY_VENDOR], it has no constant in the framework.
         */
        private const val PROPERTY_SECURITY_LEVEL = "securityLevel"

        /**
         * The capabilities of the device Pillarbox is currently running on.
         *
         * The Widevine security level is read from [MediaDrm] once, the first time this property is accessed, as it cannot change during the
         * lifetime of the process. It is `null` on devices without Widevine support, in which case the integration layer falls back to
         * DRM-free resources.
         */
        val device: DeviceCapabilities by lazy { readDeviceCapabilities() }

        private fun readDeviceCapabilities(): DeviceCapabilities {
            var mediaDrm: MediaDrm? = null

            val capabilities = runCatching {
                val drm = MediaDrm(C.WIDEVINE_UUID)
                mediaDrm = drm

                DeviceCapabilities(
                    drmSecurityLevel = drm.getPropertyString(PROPERTY_SECURITY_LEVEL).takeIf { it.isNotBlank() },
                )
            }

            mediaDrm?.releaseCompat()

            return capabilities.getOrElse { throwable ->
                Log.w(TAG, "Could not read the Widevine capabilities of this device", throwable)
                DeviceCapabilities()
            }
        }

        private fun MediaDrm.releaseCompat() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                close()
            } else {
                @Suppress("DEPRECATION")
                release()
            }
        }
    }
}
