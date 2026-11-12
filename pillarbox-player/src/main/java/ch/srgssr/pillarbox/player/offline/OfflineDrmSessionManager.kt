/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback

/**
 * [DrmSessionManager] playing a downloaded media with its Widevine [OfflineLicense]s.
 *
 * Media3's [DefaultDrmSessionManager] plays with a single offline license. When the tracks of a media are encrypted with different keys,
 * for example audio and video, each key is in its own license: this manager then uses one [DefaultDrmSessionManager] per license, and
 * acquires the session of each track from the manager of the license matching its PSSH box.
 *
 * @param licenses The offline licenses of the media. Must not be empty.
 */
@UnstableApi
internal class OfflineDrmSessionManager(private val licenses: List<OfflineLicense>) : DrmSessionManager {
    private val managers = licenses.map { license ->
        // The license server is only used to renew a license that is about to expire, which isn't possible without its URL
        val drmCallback = HttpMediaDrmCallback(null, DefaultHttpDataSource.Factory())
        DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .setMultiSession(true)
            .build(drmCallback)
            .apply { setMode(DefaultDrmSessionManager.MODE_PLAYBACK, license.keySetId) }
    }

    init {
        require(licenses.isNotEmpty()) { "At least one offline license is required" }
    }

    override fun setPlayer(playbackLooper: Looper, playerId: PlayerId) {
        managers.forEach { it.setPlayer(playbackLooper, playerId) }
    }

    override fun prepare() {
        managers.forEach { it.prepare() }
    }

    override fun release() {
        managers.forEach { it.release() }
    }

    override fun getCryptoType(format: Format): @C.CryptoType Int {
        return managerFor(format).getCryptoType(format)
    }

    override fun preacquireSession(
        eventDispatcher: DrmSessionEventListener.EventDispatcher?,
        format: Format,
    ): DrmSessionManager.DrmSessionReference {
        return managerFor(format).preacquireSession(eventDispatcher, format)
    }

    override fun acquireSession(eventDispatcher: DrmSessionEventListener.EventDispatcher?, format: Format): DrmSession? {
        return managerFor(format).acquireSession(eventDispatcher, format)
    }

    /**
     * Returns the index of the license whose PSSH box is the one of [format], or of the first one if none matches, for example for clear
     * tracks or a license decrypting all the tracks.
     */
    fun getLicenseIndex(format: Format): Int {
        val drmInitData = format.drmInitData
        val psshDatas = if (drmInitData == null) {
            emptyList()
        } else {
            (0 until drmInitData.schemeDataCount)
                .map(drmInitData::get)
                .filter { it.matches(C.WIDEVINE_UUID) }
                .mapNotNull { it.data }
        }
        val index = licenses.indexOfFirst { license ->
            psshDatas.any { it.contentEquals(license.psshData) }
        }

        return index.coerceAtLeast(0)
    }

    private fun managerFor(format: Format): DrmSessionManager {
        return managers[getLicenseIndex(format)]
    }
}

/**
 * [DrmSessionManagerProvider] providing an [OfflineDrmSessionManager] for the [MediaItem]s whose
 * [tag][MediaItem.LocalConfiguration.tag] contains their [OfflineLicense]s, see [OfflineLicenses], and delegating to [delegate] otherwise.
 *
 * @param delegate The [DrmSessionManagerProvider] used for the other [MediaItem]s.
 */
@UnstableApi
internal class OfflineDrmSessionManagerProvider(private val delegate: DrmSessionManagerProvider) : DrmSessionManagerProvider {
    override fun get(mediaItem: MediaItem): DrmSessionManager {
        val offlineLicenses = mediaItem.localConfiguration?.tag as? OfflineLicenses

        return offlineLicenses?.let { OfflineDrmSessionManager(it.licenses) } ?: delegate.get(mediaItem)
    }
}

/**
 * The [OfflineLicense]s of a downloaded media, set as the [tag][MediaItem.LocalConfiguration.tag] of the [MediaItem] played.
 */
internal class OfflineLicenses(val licenses: List<OfflineLicense>)
