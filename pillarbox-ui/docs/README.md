[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox UI module

Provides UI Compose components :

- PlayerSurface
- Exoplayer views compose wrappers

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-ui:$LATEST_RELEASE_VERSION")
```

More information can be found on the [top level README](../docs/README.md)

## Getting started

### Drawing a simple video surface

```kotlin
    Box(modifier = Modifier) {
    PlayerSurface(player = player)
}
```

### Create a simple player with controls and subtitles

In this example we are drawing controls and subtitles on top of the player surface. To add subtitles use `ExoPlayerSubtitleView` and for controls
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
            defaultAspectRatio = defaultAspectRatio
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
- `ScaleMode.Zoom`: Like _Crop_ but doesn't clip content to the parent container. Useful for fullscreen mode

## Compose

To learn more about compose, you can read the [Official documentation](https://developer.android.com/jetpack/compose)


