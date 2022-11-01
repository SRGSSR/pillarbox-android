[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Player module

This module provides `PillarboxPlayer`, the `Player` implementation media playback on Android.

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-player:$LATEST_RELEASE_VERSION")
```

More information can be found on the [top level README](../docs/README.md)

## Getting started

### 1 Create a MediaItemSource

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

### 2 Create a PillarboxPlayer

```kotlin
val player = PillarboxPlayer(context = context, mediaItemSource = DefaultMediaItemSource())
// Make player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

### 3 Start playing a video

```kotlin
val itemToPlay = MediaItem.fromUri("https://sample.com/sample.mp4")
player.setMediaItem(itemToPlay)
```

### 4 Attaching to UI

PillarboxPlayer can be used with views provided by Exoplayer without any modificatons.

#### Exoplayer ui module

Add the following to your `gradle` :

```gradle
 implementation("androidx.media3:media3-ui:$media3_version")
```

#### Set the player to the view

After adding the player view to your layout, in your Fragment or Activity you can when :

```kotlin
@Override
fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState);
    // ...
    playerView = findViewById(R.id.player_view)
    playerView.player = player
}
```

**_A player can be attach to only one view!_**

### 5 Release the player

When you don't need anymore the player, you have to release it. It free's resources used by the player. **_The player can't be use anymore after
that_**.

```kotlin
player.release()
```

## Exoplayer

As `PillarboxPlayer` extending an _Exoplayer_ `Player`, all documentation related to Exoplayer is valid for Pillarbox.

- [HelloWorld](https://exoplayer.dev/hello-world.html)
- [Player Events](https://exoplayer.dev/listening-to-player-events.html)
- [MediaItem](https://exoplayer.dev/media-items.html)
- [Playlist](https://exoplayer.dev/playlists.html)
- [Track Selection](https://exoplayer.dev/track-selection.html)
