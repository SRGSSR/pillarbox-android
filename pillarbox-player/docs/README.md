# Module pillarbox-player

Provides [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer], an AndroidX Media3
[Player](https://developer.android.com/reference/androidx/media3/common/Player) implementation for media playback on Android.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-player:<pillarbox_version>")
```

## Getting started

### Create the player

```kotlin
val player = PillarboxExoPlayer(context, Default)
// Make the player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

#### Playback monitoring

By default, [PillarboxExoPlayer][ch.srgssr.pillarbox.player.PillarboxExoPlayer] does not record any monitoring data. You can configure this behaviour
when creating the player:

```kotlin
val player = PillarboxExoPlayer(context, Default) {
    // Disable monitoring recording (default behavior)
    disableMonitoring()

    // Output each monitoring event to Logcat
    monitoring(Logcat)

    // Send each monitoring event to a remote server
    monitoring(Remote) {
        config(endpointUrl = "https://example.com/monitoring")
    }
}
```

### Create a `MediaItem`

```kotlin
val mediaUri = "https://example.com/media.mp4"
val mediaItem = MediaItem.fromUri(mediaUri)

player.setMediaItem(mediaItem)
```

More information about [MediaItem][androidx.media3.common.MediaItem] creation can be found in the `MediaItem`
[documentation][media-items-documentation].

### Display a `Player`

[PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] can be used with the [View][android.view.View]s provided by AndroidX Media3 without any
modifications.

To quickly get started, add the following to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("androidx.media3:media3-ui:<androidx_media3_version>")
```

Then link your player to a [PlayerView][androidx.media3.ui.PlayerView]:

```kotlin
@Override
fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val player = PillarboxExoPlayer(context, Default)
    val playerView: PlayerView = findViewById(R.id.player_view)
    // A player can only be attached to one View!
    playerView.player = player
}
```

For more detailed information, you can check [AndroidX Media3 UI][media3-ui-documentation].

**Tip:** for integration with Compose, you can use [pillarbox-ui][pillarbox-ui].

### Release a `Player`

When the player is not needed anymore, you have to release it. This will free resources allocated by the player.

```kotlin
player.release()
```

**Warning:** the player can't be used anymore after that.

## Custom `AssetLoader`

[AssetLoader][ch.srgssr.pillarbox.player.asset.AssetLoader] is used to load content that doesn't directly have a playable URL, for example, a resource
id or a URI. Its responsibility is to provide a [MediaSource][androidx.media3.exoplayer.source.MediaSource] that:

- Is playable by the player;
- Contains [tracking data][pillarbox-tracking-data];
- Provides optional media metadata.

```kotlin
class CustomAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfigruation?.uri?.scheme == "custom"
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val data = service.fetchData(mediaItem.localConfigruation!!.uri)
        val trackerData = MutableMediaItemTrackerData()
        trackerData[KEY] = FactoryData(CustomMediaItemTracker.Factory(), CustomTrackerData("CustomData"))

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(data.title)
            .setArtworkUri(data.imageUri)
            .setChapters(data.chapters)
            .setCredits(data.credits)
            .build()
        val mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(data.url))

        return Asset(
            mediaSource = mediaSource,
            trackersData = trackerData.toMediaItemTrackerData(),
            mediaMetadata = mediaMetadata,
            blockedTimeRanges = emptyList(),
        )
    }
}
```

Now pass your `CustomAssetLoader` to your player, so it can understand and play your custom data:

```kotlin
val player = PillarboxExoPlayer(context, Default) {
    +CustomAssetLoader(context)
}
player.prepare()
player.setMediaItem(MediaItem.fromUri("custom://video:1234"))
player.play()
```

### Chapters

Chapters represent the temporal segmentation of the playing media.

A [Chapter][ch.srgssr.pillarbox.player.asset.timeRange.Chapter] can be created like that:

```kotlin
val chapter = Chapter(
    id = "1",
    start = 0L,
    end = 12_000L,
    mediaMetadata = MediaMetadata.Builder().setTitle("Chapter 1").build(),
)
```

[PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] provides methods to observe and access chapters:

```kotlin
val player = PillarboxExoPlayer(context, Default)
player.addListener(object : Listener {
    override fun onChapterChanged(chapter: Chapter?) {
        if (chapter == null) {
            // Hide chapter information
        } else {
            // Display chapter information
        }
    }
})

val chapters = player.getCurrentChapters()
val currentChapter = player.getChapterAtPosition()
val chapterAtPosition = player.getChapterAtPosition(10_000L)
```

Chapters can be added to a [MediaItem][androidx.media3.common.MediaItem] via its metadata:

```kotlin
val mediaMetadata = MediaMetadata.Builder()
    .setChapters(listOf(chapter))
    .build()
val mediaItem = MediaItem.Builder()
    .setMediaMetadata(mediaMetadata)
    .build()
```

### Credits

Credits represent a point in the player timeline where opening credits or closing credits should be displayed.

A [Credit][ch.srgssr.pillarbox.player.asset.timeRange.Credit] can be created like that:

```kotlin
val openingCredits = Credit.Opening(start = 5_000L, end = 10_000L)
val closingCredits = Credit.Closing(start = 20_000L, end = 30_000L)
```

[PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] provides methods to observe and access credits:

```kotlin
val player = PillarboxExoPlayer(context, Default)
player.addListener(object : Listener {
    override fun onCreditChanged(credit: Credit?) {
        when (credit) {
            is Credit.Opening -> Unit // Show "Skip intro" button
            is Credit.Closing -> Unit // Show "Skip credits" button
            else -> Unot // Hide button
        }
    }
})

val credits = player.getCurrentCredits()
val currentCredit = player.getCreditAtPosition()
val creditAtPosition = player.getCreditAtPosition(5_000L)
```

Chapters can be added to a [MediaItem][androidx.media3.common.MediaItem] via its metadata:

```kotlin
val mediaMetadata = MediaMetadata.Builder()
    .setCredits(listOf(openingCredits, closingCredits))
    .build()
val mediaItem = MediaItem.Builder()
    .setMediaMetadata(mediaMetadata)
    .build()
```

## Known issues

- Playing DRM content on two instances of [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] is not supported on all devices.
  - Known affected devices: Samsung Galaxy A13, Huawei Nova 5i Pro, Huawei P40 Lite.
  - Related issue: [androidx/media#1877](https://github.com/androidx/media/issues/1877).

## Further reading

As [PillarboxExoPlayer][ch.srgssr.pillarbox.player.PillarboxExoPlayer] extends from [ExoPlayer][androidx.media3.exoplayer.ExoPlayer], all
documentation related to ExoPlayer is also valid for Pillarbox. Here are some useful links to get more information about ExoPlayer:

- [Getting started with ExoPlayer](https://developer.android.com/media/media3/exoplayer/hello-world.html)
- [Player events](https://developer.android.com/media/media3/exoplayer/listening-to-player-events)
- [Media items](https://developer.android.com/media/media3/exoplayer/media-items)
- [Playlists](https://developer.android.com/media/media3/exoplayer/playlists)
- [Track selection](https://developer.android.com/media/media3/exoplayer/track-selection)

You can check the following pages for a deeper understanding of Pillarbox concepts:

- [Media item tracking][pillarbox-tracking-data]
- [Media session][pillarbox-media-session]

[android.view.View]: https://developer.android.com/reference/android/view/View
[androidx.media3.common.MediaItem]: https://developer.android.com/reference/androidx/media3/common/MediaItem
[androidx.media3.exoplayer.ExoPlayer]: https://developer.android.com/reference/androidx/media3/exoplayer/ExoPlayer
[androidx.media3.exoplayer.source.MediaSource]: https://developer.android.com/reference/androidx/media3/exoplayer/source/MediaSource
[androidx.media3.ui.PlayerView]: https://developer.android.com/reference/androidx/media3/ui/PlayerView
[ch.srgssr.pillarbox.player.PillarboxExoPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-exo-player.html
[ch.srgssr.pillarbox.player.PillarboxPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-player/index.html
[ch.srgssr.pillarbox.player.asset.AssetLoader]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player.asset/-asset-loader/index.html
[ch.srgssr.pillarbox.player.asset.timeRange.Chapter]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player.asset.timeRange/-chapter/index.html
[ch.srgssr.pillarbox.player.asset.timeRange.Credit]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player.asset.timeRange/-credit/index.html
[media-items-documentation]: https://developer.android.com/media/media3/exoplayer/media-items
[media3-ui-documentation]: https://developer.android.com/media/media3/ui/playerview
[pillarbox-media-session]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/docs/MediaSession.md
[pillarbox-ui]: https://android.pillarbox.ch/api/pillarbox-ui/index.html
[pillarbox-tracking-data]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/docs/MediaItemTracking.md

[exo-player-documentation]: https://developer.android.com/media/media3/exoplayer
