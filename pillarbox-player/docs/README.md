[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![Last release](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android?label=Release)](https://github.com/SRGSSR/pillarbox-android/releases)
[![Android min SDK](https://img.shields.io/badge/Android-21%2B-34A853)](https://github.com/SRGSSR/pillarbox-android)
[![License](https://img.shields.io/github/license/SRGSSR/pillarbox-android?label=License)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Player module

Provides [`PillarboxPlayer`][pillarbox-player-source], an AndroidX Media3 [`Player`][player-documentation] implementation for media playback on
Android.

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-player:<pillarbox_version>")
```

More information can be found in the [top level README](https://github.com/SRGSSR/pillarbox-android#readme).

## Documentation

- [Getting started](#getting-started)
- [MediaSession](./MediaSession.md)
- [Tracking](./MediaItemTracking.md)

## Known issues

- Playing DRM content on two instances of [`PillarboxPlayer`][pillarbox-player-source] is not supported on all devices.
    - Known affected devices: Samsung Galaxy A13.

## Getting started

### Create the player

```kotlin
val player = PillarboxExoPlayer(context)
// Make the player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

#### Monitoring playback

By default, [`PillarboxExoPlayer`][pillarbox-exo-player-source] does not record any monitoring data. You can configure this when creating the player:

```kotlin
val player = PillarboxExoPlayer(context) {
    monitoring(Logcat)
}
```

Multiple implementations are provided out of the box, but you can also provide your own
[`MonitoringMessageHandler`][monitoring-message-handler-source]:

- `NoOp()` (default): does nothing.
- `Logcat { config(...) }`: prints each message to Logcat.
- `Remote { config(...) }`: sends each message to a remote server.

### Create a `MediaItem`

More information about [`MediaItem`][media-item-documentation] creation can be found [here][media-item-creation-documentation].

```kotlin
val mediaUri = "https://sample.com/sample.mp4"
val mediaItem = MediaItem.fromUri(mediaUri)

player.setMediaItem(mediaItem)
```

### Attaching to UI

[`PillarboxPlayer`][pillarbox-player-source] can be used with views provided by [Exoplayer][exo-player-documentation] without any modifications.

#### ExoPlayer UI module

Add the following to your module's `build.gradle`/`build.gradle.kts` file:

```gradle
implementation("androidx.media3:media3-ui:<androidx_media3_version>")
```

#### Set the player to the view

After adding the [`PlayerView`][player-view-documentation] to your layout, you can then do the following:

```kotlin
@Override
fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val playerView: PlayerView = findViewById(R.id.player_view)
    playerView.player = player
}
```

> [!WARNING]
> A player can be attached to only one [`View`][view-documentation]!

### Release the player

When you don't need the player anymore, you have to release it. It frees resources used by the player.

```kotlin
player.release()
```

> [!WARNING]
> The player can't be used anymore after that.

## Custom `AssetLoader`

`AssetLoader` is used to load content that doesn't directly have a playable URL, for example, a resource id or a URI. 
Its responsibility is to provide a `MediaSource` that is playable by the player, [tracking data](./MediaItemTracking.md) and optionally media 
metadata.

```kotlin
class DemoAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfigruation?.uri.toString().startsWith("demo://")
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val data = someService.fetchData(mediaItem.localConfigruation!!.uri)
        val trackerData = MutableMediaItemTrackerData()
        trackerData[key] = FactoryData(DemoMediaItemTracker.Factory(), DemoTrackerData("Data1"))
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(data.title)
            .setArtworkUri(data.imageUri)
            .setChapters(data.chapters)
            .setCredits(data.credits)
            .build()
        val mediaSource: MediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(data.url))
        return Asset(
            mediaSource = mediaSource,
            trackersData = trackerData.toMediaItemTrackerData(),
            mediaMetadata = mediaMetadata,
            blockedTimeRanges = emptyList(),
        )
    }
}
```

To play custom content defined above, the custom `AssetLoader` has to be added to [`PillarboxPlayer`][pillarbox-player-source] with the following code:

```kotlin
val player = PillarboxExoPlayer(context) {
    +DemoAssetLoader()
}
player.prepare()
player.setMediaItem(MediaItem.fromUri("demo://video:1234"))
player.play()
```

### Chapters

Chapters represent the temporal segmentation of the playing media.

A Chapter can be created like that:

```kotlin
val chapter = Chapter(id = "1", start = 0L, end = 12_000L, mediaMetadata = MediaMetadata.Builder().setTitle("Chapter 1").build())
```

[`PillarboxPlayer`][pillarbox-player-source] will automatically keep tracks of Chapters change during playback through [`PillarboxPlayer.Listener.onChapterChanged`][pillarbox-player-listener-source].

```kotlin
val chapterList: List<Chapter> = player.getCurrentChapters()

val currentChapter: Chapter? = player.getChapterAtPosition()

val chapterAt: Chapter? = player.getChapterAtPosition(10_000L)
```

Chapters can be added at anytime to the player inside `MediaItem.mediaMetadata`:

```kotlin
val mediaMetadata = MediaMetadata.Builder()
    .setChapters(listOf(chapter))
    .build()
val mediaItem = MediaItem.Builder()
    .setMediaMetadata(mediaMetadata)
    .build()
```

### Credits

Credits represent point in the player timeline where opening credits and closing credits should be displayed. 
It can be used to display a "skip button" to allow users to not show credits.

```kotlin
val opening: Credit = Credit.Opening(start = 5_000L, end = 10_000L)
val closing: Credit = Credit.Closing(start = 20_000L, end = 30_000L)
```

[`PillarboxPlayer`][pillarbox-player-source] will automatically keep tracks of Credits change during playback through [`PillarboxPlayer.Listener.onCreditChanged`][pillarbox-player-listener-source].

```kotlin
val creditList: List<Credit> = player.getCurrentCredits()

val currentCredit : Credit? = player.getCreditAtPosition()

val creditAt : Credit? = player.getCreditAtPosition(5_000L)
```

Credits can be added at anytime to the player inside `MediaItem.mediaMetadata`:

```kotlin
val mediaMetadata = MediaMetadata.Builder()
    .setCredits(listOf(opening, closing))
    .build()
val mediaItem = MediaItem.Builder()
    .setMediaMetadata(mediaMetadata)
    .build()
```

## ExoPlayer

As [`PillarboxExoPlayer`][pillarbox-exo-player-source] extends from [ExoPlayer][exo-player-documentation], all documentation related to ExoPlayer is 
also valid for Pillarbox. Here are some useful links to get more information about ExoPlayer:

- [Getting started with ExoPlayer](https://developer.android.com/media/media3/exoplayer/hello-world.html)
- [Player events](https://developer.android.com/media/media3/exoplayer/listening-to-player-events)
- [Media items](https://developer.android.com/media/media3/exoplayer/media-items)
- [Playlists](https://developer.android.com/media/media3/exoplayer/playlists)
- [Track selection](https://developer.android.com/media/media3/exoplayer/track-selection)

[exo-player-documentation]: https://developer.android.com/media/media3/exoplayer
[media-item-creation-documentation]: https://developer.android.com/media/media3/exoplayer/media-items
[media-item-documentation]: https://developer.android.com/reference/androidx/media3/common/MediaItem
[monitoring-message-handler-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/monitoring/MonitoringMessageHandler.kt
[pillarbox-exo-player-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxExoPlayer.kt
[pillarbox-player-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxPlayer.kt
[player-documentation]: https://developer.android.com/reference/androidx/media3/common/Player
[player-view-documentation]: https://developer.android.com/reference/androidx/media3/ui/PlayerView
[view-documentation]: https://developer.android.com/reference/android/view/View.html
[pillarbox-player-listener-source]:https://github.com/SRGSSR/pillarbox-android/blob/571-update-pillarbox-documentation/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxPlayer.kt
