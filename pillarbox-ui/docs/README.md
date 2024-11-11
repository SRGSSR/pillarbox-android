# Module pillarbox-ui

Provides UI Compose components and helpers.

This includes:

- [PlayerSurface][ch.srgssr.pillarbox.ui.widget.player.PlayerSurface], to display a player on a surface, texture, or spherical surface.
- Compose wrapper for ExoPlayer `View`s.
- [ProgressTracker][ch.srgssr.pillarbox.ui.ProgressTrackerState] to connect the player to a progress bar or slider.

## Integration

To use this module, add the following dependency to your project's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-ui:<pillarbox_version>")
```

## Getting started

### Display a `Player`

```kotlin
@Composable
fun SimplePlayer(
    player: Player,
    modifier: Modifier = Modifier,
) {
    PlayerSurface(
        player = player,
        modifier = modifier,
    )
}
```

### Create a `Player` with controls and subtitles

In this example, we are drawing controls and subtitles on top of the [Player][androidx.media3.common.Player]. To add controls, you can use
[ExoPlayerControlView][ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerControlView]. And for subtitles, you can use
[ExoPlayerSubtitleView][ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView].

```kotlin
@Composable
fun MyPlayer(
    player: Player,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        PlayerSurface(
            player = player,
            defaultAspectRatio = 1f,
        )

        ExoPlayerControlView(
            player = player,
            modifier = Modifier.matchParentSize(),
        )

        ExoPlayerSubtitleView(
            player = player,
            modifier = Modifier.matchParentSize(),
        )
    }
}
```

The `defaultAspectRatio` is used while the video is loading or if the [Player][androidx.media3.common.Player] doesn't play a video.

### Scale mode

You can customize how the [Player][androidx.media3.common.Player] scales in the [PlayerSurface][ch.srgssr.pillarbox.ui.widget.player.PlayerSurface],
by setting the `scaleMode` argument.

```kotlin
PlayerSurface(
    player = player,
    scaleMode = ScaleMode.Fit,
)
```

- [ScaleMode.Fit][ch.srgssr.pillarbox.ui.ScaleMode.Fit] (default): resizes the [Player][androidx.media3.common.Player] to fit within its parent while
  maintaining its aspect ratio.
- [ScaleMode.Fill][ch.srgssr.pillarbox.ui.ScaleMode.Fill]: stretches the [Player][androidx.media3.common.Player] to fill its parent, ignoring the
  defined aspect ratio.
- [ScaleMode.Crop][ch.srgssr.pillarbox.ui.ScaleMode.Crop]: trims the [Player][androidx.media3.common.Player] to fill its parent while maintaining its
  aspect ratio.

### Surface type

[PlayerSurface][ch.srgssr.pillarbox.ui.widget.player.PlayerSurface] lets you set the type of surface used to render its content, using its 
`surfaceType` argument.

```kotlin
PlayerSurface(
    player = player,
    surfaceType = SurfaceType.Surface,
)
```

- [SurfaceType.Surface][ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Surface] (default): the [Player][androidx.media3.common.Player] is attached
  to a [SurfaceView][android.view.SurfaceView]. This is the most optimized option, and supports playing any content including DRM protected content.
- [SurfaceType.Texture][ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Texture]: the [Player][androidx.media3.common.Player] is attached to
  a [TextureView][android.view.TextureView]. This option may be interesting when dealing with animation, and
  the [SurfaceType.Surface][ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Surface] option doesn't work as expected. This does not work with DRM 
  content.
- [SurfaceType.Spherical][ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Spherical]: the [Player][androidx.media3.common.Player] is attached to
  a [SphericalGLSurfaceView][androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView]. This option is suited when playing 360° video 
  content. This does not work with DRM content.

### Observe `Player` states

The [ch.srgssr.pillarbox.ui.extension][ch.srgssr.pillarbox.ui.extension] package provides a collection of extensions to observe a
[Player][androidx.media3.common.Player]'s state through Compose's [State][androidx.compose.runtime.State] instances.

```kotlin
@Composable
fun MyPlayer(player: Player) {
    val currentPosition: Long by player.currentPositionAsState()
    val duration: Long by player.durationAsState()
    val isPlaying: Boolean by player.isPlayingAsState()
}
```

[android.view.SurfaceView]: https://developer.android.com/reference/android/view/SurfaceView
[android.view.TextureView]: https://developer.android.com/reference/android/view/TextureView
[androidx.compose.runtime.State]: https://developer.android.com/reference/kotlin/androidx/compose/runtime/State.html
[androidx.media3.common.Player]: https://developer.android.com/reference/androidx/media3/common/Player
[androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView]: https://developer.android.com/reference/androidx/media3/exoplayer/video/spherical/SphericalGLSurfaceView
[ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerControlView]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.exoplayer/-exo-player-control-view.html
[ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.exoplayer/-exo-player-subtitle-view.html
[ch.srgssr.pillarbox.ui.extension]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.extension/index.html
[ch.srgssr.pillarbox.ui.widget.player.PlayerSurface]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-player-surface.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Spherical]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-spherical/index.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Surface]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-surface/index.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Texture]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-texture/index.html
[ch.srgssr.pillarbox.ui.ProgressTrackerState]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui/-progress-tracker-state/index.html
[ch.srgssr.pillarbox.ui.ScaleMode]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui/-scale-mode/index.html
[ch.srgssr.pillarbox.ui.ScaleMode.Crop]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui/-scale-mode/-crop/index.html
[ch.srgssr.pillarbox.ui.ScaleMode.Fill]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui/-scale-mode/-fill/index.html
[ch.srgssr.pillarbox.ui.ScaleMode.Fit]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui/-scale-mode/-fit/index.html
