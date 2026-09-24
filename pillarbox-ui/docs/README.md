# Module pillarbox-ui

Provides UI Compose components and helpers.

This includes:

- [PillarboxPlayerSurface][ch.srgssr.pillarbox.ui.widget.player.PillarboxPlayerSurface], to display a player on a surface, texture, or spherical surface.
- Compose wrapper for ExoPlayer `View`s.
- [PlayerFrame][ch.srgssr.pillarbox.ui.widget.player.PlayerFrame] to handle player component such as subtitles, overlays and surface content.
- [ProgressTracker][ch.srgssr.pillarbox.ui.ProgressTrackerState] to connect the player to a progress bar or slider.
- [PipManager][ch.srgssr.pillarbox.ui.state.PipManager] to manage the Picture-in-Picture mode of an `Activity`.

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

### Picture-in-Picture

[PipManager][ch.srgssr.pillarbox.ui.state.PipManager] manages the Picture-in-Picture mode of the local [Activity][android.app.Activity]. Get an
instance with [rememberPipManager][ch.srgssr.pillarbox.ui.state.rememberPipManager], and pass it the [Player][androidx.media3.common.Player] so
that the Picture-in-Picture window uses the aspect ratio of the content being played.

The [Activity][android.app.Activity] must declare that it supports Picture-in-Picture, and handle configuration changes itself:

```xml
<activity
    android:name=".MyPlayerActivity"
    android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|smallestScreenSize"
    android:supportsPictureInPicture="true" />
```

```kotlin
@Composable
fun MyPlayer(player: Player) {
    // Enters Picture-in-Picture automatically when the user leaves the Activity, on Android S and above.
    val pipManager = rememberPipManager(player = player, autoEnterEnabled = true)

    Box {
        PillarboxPlayerSurface(player = player)

        // Only display the controls when the Activity is neither in Picture-in-Picture, nor animating into it.
        if (!pipManager.isInPictureInPicture && !pipManager.isTransitioning) {
            MyControls(
                // Only display the button when Picture-in-Picture is available.
                isPictureInPictureEnabled = pipManager.isSupported && pipManager.isAllowed,
                onPictureInPictureClick = pipManager::enter,
            )
        }
    }
}
```

- [isSupported][ch.srgssr.pillarbox.ui.state.PipManager.isSupported] tells whether the device and the [Activity][android.app.Activity] support
  Picture-in-Picture, and [isAllowed][ch.srgssr.pillarbox.ui.state.PipManager.isAllowed] whether the user allows it for the application. Use both
  to decide whether to display a Picture-in-Picture button.
- [enter()][ch.srgssr.pillarbox.ui.state.PipManager.enter] enters Picture-in-Picture mode, for example from that button.
- [isInPictureInPicture][ch.srgssr.pillarbox.ui.state.PipManager.isInPictureInPicture] tells whether the [Activity][android.app.Activity] is
  currently in Picture-in-Picture mode, and [isTransitioning][ch.srgssr.pillarbox.ui.state.PipManager.isTransitioning] whether it is animating
  into it. Hide the controls in both cases, so that they don't show up in the Picture-in-Picture window or during the transition.
- [ratio][ch.srgssr.pillarbox.ui.state.PipManager.ratio] overrides the aspect ratio of the Picture-in-Picture window, which is otherwise the
  aspect ratio of the video being played.

As long as the [PipManager][ch.srgssr.pillarbox.ui.state.PipManager] is in composition, it keeps the [Activity][android.app.Activity]'s
Picture-in-Picture parameters up to date, so that the system can animate the transition even when Picture-in-Picture is entered by the user
leaving the [Activity][android.app.Activity] rather than through [enter()][ch.srgssr.pillarbox.ui.state.PipManager.enter].

#### Animate the transition from the video

By default, the system animates the Picture-in-Picture transition from the whole [Activity][android.app.Activity]. To animate it from the video
instead, report the bounds of the video surface to [sourceRect][ch.srgssr.pillarbox.ui.state.PipManager.sourceRect]. The `surface` slot of
[PlayerFrame][ch.srgssr.pillarbox.ui.widget.player.PlayerFrame] is laid out exactly like the video, which makes it a good place to measure them:

```kotlin
PlayerFrame(
    player = player,
    surface = {
        Box(
            modifier = Modifier
                .matchParentSize()
                .onGloballyPositioned {
                    pipManager.sourceRect = it.boundsInWindow().roundToIntRect().toAndroidRect()
                },
        )
    },
)
```

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

[android.app.Activity]: https://developer.android.com/reference/android/app/Activity
[android.view.SurfaceView]: https://developer.android.com/reference/android/view/SurfaceView
[android.view.TextureView]: https://developer.android.com/reference/android/view/TextureView
[androidx.compose.runtime.State]: https://developer.android.com/reference/kotlin/androidx/compose/runtime/State.html
[androidx.media3.common.Player]: https://developer.android.com/reference/androidx/media3/common/Player
[androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView]: https://developer.android.com/reference/androidx/media3/exoplayer/video/spherical/SphericalGLSurfaceView
[ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerControlView]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.exoplayer/-exo-player-control-view.html
[ch.srgssr.pillarbox.ui.extension]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.extension/index.html
[ch.srgssr.pillarbox.ui.state.PipManager]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.state/-pip-manager/index.html
[ch.srgssr.pillarbox.ui.state.rememberPipManager]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.state/remember-pip-manager.html
[ch.srgssr.pillarbox.ui.widget.player.PillarboxPlayerSurface]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-pillarbox-player-surface.html
[ch.srgssr.pillarbox.ui.widget.player.PlayerFrame]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-player-frame.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Spherical]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-spherical/index.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Surface]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-surface/index.html
[ch.srgssr.pillarbox.ui.widget.player.SurfaceType.Texture]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui.widget.player/-surface-type/-texture/index.html
[ch.srgssr.pillarbox.ui.ProgressTrackerState]: https://android.pillarbox.ch/api/pillarbox-ui/ch.srgssr.pillarbox.ui/-progress-tracker-state/index.html
