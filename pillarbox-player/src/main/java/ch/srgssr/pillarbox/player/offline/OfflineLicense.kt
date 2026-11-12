/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

/**
 * A Widevine offline license, used to play a downloaded media without network access.
 *
 * The tracks of a media may be encrypted with different keys, for example audio and video. Each key is then delivered in its own license,
 * requested with the PSSH box of the tracks it decrypts.
 *
 * @property psshData The Widevine PSSH box of the tracks decrypted by this license, as found in their
 * [DrmInitData][androidx.media3.common.DrmInitData], or `null` if this license decrypts all the tracks.
 * @property keySetId The key set id of the license, returned by [MediaDrm.provideKeyResponse][android.media.MediaDrm.provideKeyResponse].
 */
class OfflineLicense(
    val psshData: ByteArray?,
    val keySetId: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OfflineLicense) return false
        return psshData.contentEquals(other.psshData) && keySetId.contentEquals(other.keySetId)
    }

    override fun hashCode(): Int {
        return 31 * psshData.contentHashCode() + keySetId.contentHashCode()
    }

    override fun toString(): String {
        return "OfflineLicense(psshData=${psshData?.size} bytes, keySetId=${keySetId.size} bytes)"
    }
}
