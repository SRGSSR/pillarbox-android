/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PillarboxDownloadManagerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var context: Application
    private lateinit var databaseProvider: StandaloneDatabaseProvider
    private lateinit var downloadIndex: DefaultDownloadIndex
    private lateinit var downloadManager: PillarboxDownloadManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        context = ApplicationProvider.getApplicationContext()
        // Same database as the download manager, to add downloads to its index
        databaseProvider = StandaloneDatabaseProvider(context)
        downloadIndex = DefaultDownloadIndex(databaseProvider)
        downloadManager = createDownloadManager()
        // A download index creates its table on first access, dropping any existing one if its version isn't stored yet. The index of the
        // download manager is first accessed on Media3's background thread, so it could drop the downloads added by the tests. Accessing it
        // now creates the table before any download is added.
        downloadManager.media3DownloadManager.downloadIndex.getDownload(MEDIA_ID)
    }

    @AfterTest
    fun tearDown() {
        downloadManager.release()
        databaseProvider.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `builder applies the configuration`() {
        downloadManager.release()
        val downloadDirectory = temporaryFolder.newFolder()
        val requirements = Requirements(Requirements.NETWORK_UNMETERED)
        downloadManager = PillarboxDownloadManager.Builder(context, TestDownloadService::class.java)
            .setDownloadDirectory(downloadDirectory)
            .setMaxParallelDownloads(2)
            .setRequirements(requirements)
            .build()

        assertEquals(downloadDirectory, downloadManager.downloadDirectory)
        assertEquals(2, downloadManager.media3DownloadManager.maxParallelDownloads)
        assertEquals(requirements, downloadManager.media3DownloadManager.requirements)
    }

    @Test
    fun `builder rejects invalid max parallel downloads`() {
        val builder = PillarboxDownloadManager.Builder(context, TestDownloadService::class.java)

        assertFailsWith<IllegalArgumentException> { builder.setMaxParallelDownloads(0) }
    }

    @Test
    fun `download id is the media id`() {
        val mediaItem = MediaItem.Builder()
            .setMediaId("urn:rts:video:1234")
            .setUri("https://example.com/media.m3u8")
            .build()

        assertEquals("urn:rts:video:1234", downloadManager.getDownloadId(mediaItem))
    }

    @Test
    fun `download id is the URI without media id`() {
        val mediaItem = MediaItem.fromUri("https://example.com/media.m3u8")

        assertEquals("https://example.com/media.m3u8", downloadManager.getDownloadId(mediaItem))
    }

    @Test
    fun `download id without media id and URI`() {
        assertFailsWith<IllegalStateException> { downloadManager.getDownloadId(MediaItem.EMPTY) }
    }

    @Test
    fun `get download`() {
        addDownload(MEDIA_ID, Download.STATE_STOPPED)

        assertEquals(MEDIA_ID, downloadManager.getDownload(mediaItem(MEDIA_ID))?.request?.id)
        assertNull(downloadManager.getDownload(mediaItem("unknown")))
        assertNull(downloadManager.getDownload(MediaItem.EMPTY))
    }

    @Test
    fun `get download request of a completed download`() {
        addDownload(MEDIA_ID, Download.STATE_COMPLETED)

        assertEquals(MEDIA_ID, downloadManager.getDownloadRequest(mediaItem(MEDIA_ID))?.id)
    }

    @Test
    fun `get download request of an incomplete download`() {
        addDownload(MEDIA_ID, Download.STATE_STOPPED)

        assertNull(downloadManager.getDownloadRequest(mediaItem(MEDIA_ID)))
    }

    @Test
    fun `get download request of an unknown download`() {
        assertNull(downloadManager.getDownloadRequest(mediaItem(MEDIA_ID)))
    }

    @Test
    fun `downloads are sorted by most recent first`() = runTest {
        addDownload("old", Download.STATE_COMPLETED, startTimeMs = 1_000L)
        addDownload("recent", Download.STATE_FAILED, startTimeMs = 3_000L)
        addDownload("middle", Download.STATE_STOPPED, startTimeMs = 2_000L)

        downloadManager.downloads.test {
            assertEquals(listOf("recent", "middle", "old"), awaitItem().map { it.request.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pause sends the paused stop reason`() {
        downloadManager.pause(MEDIA_ID)

        val intent = nextStartedService()
        assertEquals(DownloadService.ACTION_SET_STOP_REASON, intent.action)
        assertEquals(MEDIA_ID, intent.getStringExtra(DownloadService.KEY_CONTENT_ID))
        assertEquals(PillarboxDownloadManager.STOP_REASON_PAUSED, intent.getIntExtra(DownloadService.KEY_STOP_REASON, -1))
    }

    @Test
    fun `resume clears the stop reason`() {
        downloadManager.resume(MEDIA_ID)

        val intent = nextStartedService()
        assertEquals(DownloadService.ACTION_SET_STOP_REASON, intent.action)
        assertEquals(MEDIA_ID, intent.getStringExtra(DownloadService.KEY_CONTENT_ID))
        assertEquals(Download.STOP_REASON_NONE, intent.getIntExtra(DownloadService.KEY_STOP_REASON, -1))
    }

    @Test
    fun `remove removes the download`() {
        downloadManager.remove(MEDIA_ID)

        val intent = nextStartedService()
        assertEquals(DownloadService.ACTION_REMOVE_DOWNLOAD, intent.action)
        assertEquals(MEDIA_ID, intent.getStringExtra(DownloadService.KEY_CONTENT_ID))
    }

    @Test
    fun `restart adds the download again`() = runTest {
        addDownload(MEDIA_ID, Download.STATE_FAILED)

        downloadManager.restart(MEDIA_ID)

        val intent = nextStartedService()
        assertEquals(DownloadService.ACTION_ADD_DOWNLOAD, intent.action)
        @Suppress("DEPRECATION")
        assertEquals(MEDIA_ID, intent.getParcelableExtra<DownloadRequest>(DownloadService.KEY_DOWNLOAD_REQUEST)?.id)
    }

    @Test
    fun `restart an unknown download`() = runTest {
        downloadManager.restart(MEDIA_ID)

        assertNull(shadowOf(context).nextStartedService)
    }

    @Test
    fun `download content protected with another DRM scheme than Widevine`() = runTest {
        val mediaItem = MediaItem.Builder()
            .setUri("https://example.com/media.mp4")
            .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.PLAYREADY_UUID).setLicenseUri(LICENSE_URI).build())
            .build()

        assertFailsWith<DownloadNotSupportedException> { downloadManager.download(mediaItem) }
        assertNull(shadowOf(context).nextStartedService)
    }

    @Test
    fun `download Widevine protected content without license URI`() = runTest {
        val mediaItem = MediaItem.Builder()
            .setUri("https://example.com/media.mp4")
            .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).build())
            .build()

        assertFailsWith<DownloadNotSupportedException> { downloadManager.download(mediaItem) }
        assertNull(shadowOf(context).nextStartedService)
    }

    @Test
    fun `download uses the asset loaders to resolve the stream`() = runTest {
        downloadManager.release()
        // The asset loader resolves the media to a stream protected with an unsupported DRM scheme, which proves it is used
        downloadManager = createDownloadManager(DrmAssetLoader(context))

        assertFailsWith<DownloadNotSupportedException> { downloadManager.download(mediaItem(DrmAssetLoader.MEDIA_ID)) }
    }

    private fun createDownloadManager(vararg assetLoaders: AssetLoader): PillarboxDownloadManager {
        return PillarboxDownloadManager.Builder(context, TestDownloadService::class.java)
            .setDownloadDirectory(temporaryFolder.newFolder())
            .apply { assetLoaders.forEach(::addAssetLoader) }
            .build()
    }

    private fun addDownload(id: String, state: Int, startTimeMs: Long = 0L) {
        val request = DownloadRequest.Builder(id, Uri.parse("https://example.com/$id.m3u8")).build()

        val failureReason = if (state == Download.STATE_FAILED) Download.FAILURE_REASON_UNKNOWN else Download.FAILURE_REASON_NONE

        downloadIndex.putDownload(
            Download(request, state, startTimeMs, startTimeMs, C.LENGTH_UNSET.toLong(), Download.STOP_REASON_NONE, failureReason)
        )
    }

    private fun nextStartedService(): Intent {
        return assertNotNull(shadowOf(context).nextStartedService)
    }

    private class DrmAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        override fun canLoadAsset(mediaItem: MediaItem): Boolean {
            return mediaItem.mediaId == MEDIA_ID
        }

        override suspend fun loadAsset(mediaItem: MediaItem): Asset {
            val streamMediaItem = MediaItem.Builder()
                .setUri("https://example.com/drm.mp4")
                .setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.PLAYREADY_UUID).setLicenseUri(LICENSE_URI).build())
                .build()

            return Asset(mediaSource = mediaSourceFactory.createMediaSource(streamMediaItem))
        }

        companion object {
            const val MEDIA_ID = "urn:drm"
        }
    }

    private class TestDownloadService : PillarboxDownloadService() {
        override val pillarboxDownloadManager: PillarboxDownloadManager
            get() = error("Not used in tests")
    }

    private companion object {
        private const val MEDIA_ID = "urn:rts:video:1234"
        private const val LICENSE_URI = "https://example.com/license"

        private fun mediaItem(mediaId: String): MediaItem {
            return MediaItem.Builder()
                .setMediaId(mediaId)
                .setUri("https://example.com/$mediaId.m3u8")
                .build()
        }
    }
}
