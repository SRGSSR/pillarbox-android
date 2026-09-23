# Module pillarbox-ui

Provides UI Compose components and helpers.

This includes:

- [PillarboxPlayerSurface][ch.srgssr.pillarbox.ui.widget.player.PillarboxPlayerSurface], to display a player on a surface, texture, or spherical surface.
- Compose wrapper for ExoPlayer `View`s.
- [PlayerFrame][ch.srgssr.pillarbox.ui.widget.player.PlayerFrame] to handle player component such as subtitles, overlays and surface content.
- [ProgressTracker][ch.srgssr.pillarbox.ui.ProgressTrackerState] to connect the player to a progress bar or slider.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

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
    PillarboxPlayerSurface(
        player = player,
        modifier = modifier,
    )
}
```

### Create a `Player` with controls and subtitles

In this example, we are drawing controls and subtitles on top of the [Player][androidx.media3.common.Player]. To add controls, you can use
[ExoPlayerControlView][ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerControlView].

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
        val presentationState: PresentationState = rememberPresentationState(player = player, keepContentOnReset = false)
        PlayerFrame(
            player = player,
            presentationState = presentationState,
            contentScale = ContentScale.Fit,
            subtitle = {
                PlayerSubitle(player)
            },
            shutter = {
                // Draw when no video is playing or when the player is loading
                DrawShutter(player)
            }
        ) {
            ExoPlayerControlView(
                player = player,
                modifier = Modifier.matchParentSize(),
            )   
        }
    }
}
```

### Surface type

[PlayerSurface][ch.srgssr.pillarbox.ui.widget.player.PlayerSurface] lets you set the type of surface used to render its content, using its 
`surfaceType` argument.

```kotlin
PillarboxPlayerSurface(
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
[ch.srgssr.pillarbox.ui.extension]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.extension/index.html
[ch.srgssr.pillarbox.ui.widget.player.PillarboxPlayerSurface]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-pillarbox-player-surface.html
[ch.srgssr.pillarbox.ui.widget.player.PlayerFrame]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-player-frame.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Spherical]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-spherical/index.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Surface]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-surface/index.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Texture]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-texture/index.html
[ch.srgssr.pillarbox.ui.ProgressTrackerState]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui/-progress-tracker-state/index.html
