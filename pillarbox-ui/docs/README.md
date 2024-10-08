[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![Last release](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android?label=Release)](https://github.com/SRGSSR/pillarbox-android/releases)
[![Android min SDK](https://img.shields.io/badge/Android-21%2B-34A853)](https://github.com/SRGSSR/pillarbox-android)
[![License](https://img.shields.io/github/license/SRGSSR/pillarbox-android?label=License)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox UI module

Provides UI Compose components:

- PlayerSurface
- Exoplayer views compose wrappers
- ProgressTrackers to connect the player to a progress bar or slider.

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-ui:$LATEST_RELEASE_VERSION")
```

More information can be found on the [top level README](../docs/README.md)

## Getting started

### Drawing a simple video surface

```kotlin
@Composable
fun SimplePlayer(player: Player) {
    Box(modifier = Modifier) {
        PlayerSurface(player = player)
    }
}
```

### Create a simple player with controls and subtitles

In this example, we are drawing controls and subtitles on top of the player surface. To add subtitles use `ExoPlayerSubtitleView` and for controls
you can use the Exoplayer version, `ExoPlayerControlView`.

```kotlin
@Composable
fun MyPlayer(player: Player) {
    val defaultAspectRatio = 1.0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        PlayerSurface(
            modifier = Modifier,
            player = player,
            scaleMode = ScaleMode.Fit,
            defaultAspectRatio = defaultAspectRatio,
            surfaceType = SurfaceType.Surface, // By default
        )
        ExoPlayerControlView(modifier = Modifier.matchParentSize(), player = player)
        ExoPlayerSubtitleView(modifier = Modifier.matchParentSize(), player = player)
    }
}
```

The `defaultAspectRatio` is used while the video is loading or if the player doesn't play a video.

In this example we use `ScaleMode.Fit` to fit the content to the parent container but there are more scales modes :

- `ScaleMode.Fit` : Fit player content to the parent container and keep aspect ratio.
- `ScaleMode.Fill` : Fill player content to the parent container.
- `ScaleMode.Crop` : Crop player content inside the parent container and keep aspect ratio. Content outside the parent container will be clipped.

### Surface types

`PlayerSurface` allows choosing between multiple types of surface:

- `SurfaceType.Surface` (default): the player is linked to a `SurfaceView`. This option is the most optimized version, and supports playing any
  content including DRM protected content.
- `SurfaceType.Texture`: the player is linked to a `TextureView`. This option may be interesting when dealing with animation, and the `Surface`
  option doesn't work as expected.
- `SurfaceType.Spherical`: the player is linked to a `SphericalGLSurfaceView`. This surface type is suited when playing 360° video content.

> [!NOTE]
> The last two surface types are not suited when playing DRM protected content.

### Listen to player states

To listen to player states _Pillarbox_ provides some extensions `PlayerCallbackFlow.kt` and some Compose extensions `ComposablePlayer.kt`.

```kotlin

@Composable
fun MyPlayerView(player: Player) {
    val defaultAspectRatio = 1.0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        PlayerSurface(
            modifier = Modifier,
            player = player,
            scaleMode = ScaleMode.Fit,
            defaultAspectRatio = defaultAspectRatio
        )
        // Displays current position periodically
        val currentPosition = player.currentPositionAsState()
        Text(text = "Position = $currentPosition ms", modifier = Modifier.align(Alignment.TopStart))

        val duration = player.durationAsState()
        Text(text = "Duration = $duration ms", modifier = Modifier.align(Alignment.TopEnd))

        val isPlaying = player.isPlayingAsState()
        Button(modifier = Modififer.align(Alignement.Center), onClick = { togglePlayingBack() }) {
            Text(text = if (isPlaying) "Pause" else "Play")
        }
    }
}
```

## Compose

To learn more about compose, you can read the [Official documentation](https://developer.android.com/jetpack/compose)


