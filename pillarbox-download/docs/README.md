# Module pillarbox-download

Provides offline playback: download media with [PillarboxDownloadManager][ch.srgssr.pillarbox.download.PillarboxDownloadManager], and play
the downloaded content with Pillarbox.

It is based on Media3's [download support](https://developer.android.com/media/media3/exoplayer/downloading-media).

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-download:<pillarbox_version>")
```

## Getting started

### Create the download service

Downloads run in a foreground service. Extend [PillarboxDownloadService][ch.srgssr.pillarbox.download.PillarboxDownloadService] and return
your application wide [PillarboxDownloadManager][ch.srgssr.pillarbox.download.PillarboxDownloadManager]:

```kotlin
class MyDownloadService : PillarboxDownloadService() {
    override val pillarboxDownloadManager: PillarboxDownloadManager by lazy {
        MyDownloads.getDownloadManager(this)
    }
}
```

Declare it in your `AndroidManifest.xml`:

```xml
<service
    android:name=".MyDownloadService"
    android:exported="false"
    android:foregroundServiceType="dataSync">
    <intent-filter>
        <action android:name="androidx.media3.exoplayer.downloadService.action.RESTART" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</service>
```

> [!TIP]
> On Android 13+, request the `POST_NOTIFICATIONS` permission to display the download progress notification.

### Create the download manager

Create a single [PillarboxDownloadManager][ch.srgssr.pillarbox.download.PillarboxDownloadManager] for your application, from the main
thread. Add the same [AssetLoader][ch.srgssr.pillarbox.player.asset.AssetLoader]s as your player: they are used to resolve the stream to
download.

```kotlin
val downloadManager = PillarboxDownloadManager(context, MyDownloadService::class.java) {
    +SRGAssetLoader(context)

    // Optional customization
    downloadDirectory(File(context.filesDir, "downloads"))
    maxParallelDownloads(2)
    requirements(Requirements(Requirements.NETWORK_UNMETERED))
    trackSelectionParameters(
        DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS.buildUpon()
            .setMaxVideoSize(1280, 720)
            .build()
    )
    // SRG SSR streams may require an Akamai token
    dataSourceFactory(AkamaiTokenDataSource.Factory())
}
```

### Play downloaded media offline

Set the download manager as the player's `DownloadedMediaProvider`. Downloaded media are then played from the download cache, whether the
device is online or not. Other media are streamed as usual.

```kotlin
val player = PillarboxExoPlayer(context) {
    downloadedMediaProvider(downloadManager)
}
player.setMediaItem(SRGMediaItem("urn:rts:video:12345"))
```

A media item is matched with its download by its media id, or by its URI if it has no media id.

### Manage the downloads

```kotlin
coroutineScope.launch {
    downloadManager.download(mediaItem) // Throws DownloadNotSupportedException for DRM protected content and live streams
}

downloadManager.pause(id)
downloadManager.resume(id)
coroutineScope.launch { downloadManager.restart(id) }
downloadManager.remove(id) // Cancels the download if in progress, and deletes the downloaded data

downloadManager.downloads.collect { downloads ->
    // Each Media3 Download provides its state, progress and size; Pillarbox adds its title and whether it is paused
    downloads.forEach { download ->
        println("${download.title}: ${download.state}, ${download.percentDownloaded}%, paused=${download.isPaused}")
    }
}
```

The downloads and their state are persisted, and survive application restarts. Only the title of the media is stored with a download.

## Limitations

- DRM protected content and live streams can't be downloaded.
- The metadata of the media (artwork, chapters, credits, blocked time ranges) is not stored: during offline playback, only the metadata of
  the played `MediaItem` is available.
- Media item trackers (analytics) are not started during offline playback.
