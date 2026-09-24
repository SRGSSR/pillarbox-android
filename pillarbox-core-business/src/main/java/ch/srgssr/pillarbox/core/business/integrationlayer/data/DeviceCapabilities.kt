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
 * Use [DeviceCapabilities.device] to get the capabilities of the current device, and pass a custom instance to
 * [HttpMediaCompositionService][ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService] to advertise something else.
 *
 * The DRM capabilities are only sent to the integration layer when both [drmVendor] and [drmSecurityLevel] are known.
 *
 * @property platform The platform requesting the media composition.
 * @property drmVendor The Widevine vendor of the device, or `null` if Widevine is unavailable.
 * @property drmSecurityLevel The Widevine security level of the device, or `null` if Widevine is unavailable.
 */
class DeviceCapabilities(
    val platform: String = PLATFORM_ANDROID,
    val drmVendor: String? = null,
    val drmSecurityLevel: String? = null,
) {
    companion object {
        /**
         * The platform value identifying Android clients to the integration layer.
         */
        const val PLATFORM_ANDROID = "android"

        private const val TAG = "DeviceCapabilities"

        /**
         * Vendor defined [MediaDrm] property. Unlike [MediaDrm.PROPERTY_VENDOR], it has no constant in the framework.
         */
        private const val PROPERTY_SECURITY_LEVEL = "securityLevel"

        /**
         * The capabilities of the device Pillarbox is currently running on.
         *
         * The Widevine properties are read from [MediaDrm] once, the first time this property is accessed, as they cannot change during the
         * lifetime of the process. They are `null` on devices without Widevine support, in which case the integration layer falls back to
         * DRM-free resources.
         */
        val device: DeviceCapabilities by lazy { readDeviceCapabilities() }

        private fun readDeviceCapabilities(): DeviceCapabilities {
            var mediaDrm: MediaDrm? = null

            // Probing Widevine is best effort: any failure, be it a device without Widevine or a vendor specific MediaDrm error, must degrade
            // to unknown capabilities rather than break playback, including for DRM-free content.
            val capabilities = runCatching {
                val drm = MediaDrm(C.WIDEVINE_UUID)
                mediaDrm = drm

                DeviceCapabilities(
                    drmVendor = drm.getPropertyString(MediaDrm.PROPERTY_VENDOR).takeIf { it.isNotBlank() },
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
