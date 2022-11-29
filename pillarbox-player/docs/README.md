[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Player module

This module provides `PillarboxPlayer`, the _Exoplayer_ `Player` implementation of media playback on Android.

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-player:$LATEST_RELEASE_VERSION")
```

More information can be found on the [top level README](../docs/README.md)

## Getting started

### Create a MediaItemSource

`MediaItemSource` create a `MediaItem` with all media information needed by 'PillarboxPlayer'. More information about MediaItem creation can be
found [here](https://exoplayer.dev/media-items.html)

```kotlin
/**
 * Goal : Get a MediaItem with a mediaUri set.
 */
class DefaultMediaItemSource : MediaItemSource {
    /**
     * Suspend function to allow network call to fill MediaItem metadata and mediaUri if needed.
     */
    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        return mediaItem
    }
}
```

### Create a PillarboxPlayer

```kotlin
val player = PillarboxPlayer(context = context, mediaItemSource = DefaultMediaItemSource())
// Make player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

### Start playing a video

```kotlin
val itemToPlay = MediaItem.fromUri("https://sample.com/sample.mp4")
player.setMediaItem(itemToPlay)
```

### Attaching to UI

PillarboxPlayer can be used with views provided by Exoplayer without any modifications.

#### Exoplayer ui module

Add the following to your `gradle` :

```gradle
 implementation("androidx.media3:media3-ui:$media3_version")
```

#### Set the player to the view

After adding the player view to your layout, in your Fragment or Activity you can then :

```kotlin
@Override
fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    // ...
    playerView = findViewById(R.id.player_view)
    playerView.player = player
}
```

**_A player can be attached to only one view!_**

### Release the player

When you don't need the player anymore, you have to release it. It frees resources used by the player. **_The player can't be used anymore 
after
that_**.

```kotlin
player.release()
```

### Connect the player to the MediaSession

```kotlin
val mediaSession = MediaSession.Builder(application, player).build()
```

Don't forget to release the `MediaSession` when you no longer need it or when releasing the player with

```kotlin
mediaSession.release()
```

More information about `MediaSession` is available [here](https://developer.android.com/guide/topics/media/media3/getting-started/mediasession)

## Exoplayer

As `PillarboxPlayer` extending an _Exoplayer_ `Player`, all documentation related to Exoplayer is valid for Pillarbox.

- [HelloWorld](https://exoplayer.dev/hello-world.html)
- [Player Events](https://exoplayer.dev/listening-to-player-events.html)
- [MediaItem](https://exoplayer.dev/media-items.html)
- [Playlist](https://exoplayer.dev/playlists.html)
- [Track Selection](https://exoplayer.dev/track-selection.html)
