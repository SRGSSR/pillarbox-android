# Module pillarbox-cast

Provides a [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] implementation based on Media 3 [CastPlayer][androidx.media3.cast.CastPlayer] that controls a Cast receiver 
app.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-cast:<pillarbox_version>")
```

The main goal of this module is to be able to build a user interface that can be used with a [PillarboxExoPlayer][ch.srgssr.pillarbox.player.PillarboxExoPlayer] or [PillarboxCastPlayer][ch.srgssr.pillarbox.cast.PillarboxCastPlayer]. Both
implementations are based on the [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] interface.

## Getting started

### Create the player

```kotlin
val player = PillarboxCastPlayer(context, Default)
player.setSessionAvailabilityListener(object : SessionAvailabilityListener {
    override fun onCastSessionAvailable() {
        // The cast session is connected.
        player.setMediaItems(mediaItems)
        player.play()
        player.prepare()
    }

    override fun onCastSessionUnavailable() {
        // The cast session has been disconnected.
    }

})
// When the player is not needed anymore.
player.release()
```

The `SessionAvailabilityListener` can also be created this way:

```kotlin
val player = PillarboxCastPlayer(context, Default) {
    onCastSessionAvailable {
        // The cast session is connected.
        setMediaItems(mediaItems)
        play()
        prepare()
    }

    onCastSessionUnavailable {
        // The cast session has been disconnected.
    }
}
```

### Configure [MediaItemConverter][androidx.media3.cast.MediaItemConverter]

```kotlin
val player = PillarboxCastPlayer(context, Default) {
    mediaItemConverter(DefaultMediaItemConverter()) // By default
}
```

### Display a Cast button

Somewhere in your application, a Cast button has to be displayed to allow the user to connect to a Cast device.

To do this, you can use either:
- The official [MediaRouteButton][media-route-button], best suited for application using AppCompat and XML `View`s.
- [androidx-mediarouter-compose][androidx-mediarouter-compose], which works best with Compose.

## Local to remote playback

With `PillarboxCastPlayer`, it is easy to switch from local to remote, and back to local playback.

When switching to remote playback, the local playback has to be stop manually and the current state of the player has to be configured on the 
remote player.

```kotlin
val localPlayer = PillarboxExoPlayer(context, Default)
val remotePlayer = PillarboxCastPlayer(context, Default)
var currentPlayer: PillarboxPlayer = if (remotePlayer.isCastSessionAvailable()) remotePlayer else localPlayer

player.setSessionAvailabilityListener(object : SessionAvailabilityListener {
    override fun onCastSessionAvailable() {
        setCurrentPlayer(remotePlayer)
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(localPlayer)
    }
})

PlayerView(currentPlayer)
```

## Additional resources

- [Google Cast SDK](https://developers.google.com/cast/docs/android_sender)

[ch.srgssr.pillarbox.player.PillarboxPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-player/index.html
[ch.srgssr.pillarbox.player.PillarboxExoPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-exo-player.html
[ch.srgssr.pillarbox.cast.PillarboxCastPlayer]: https://android.pillarbox.ch/api/ch.srgssr.pillarbox.cast/-pillarbox-cast-player/index.html
[androidx.media3.cast.CastPlayer]: https://developer.android.com/reference/androidx/media3/cast/CastPlayer
[androidx.media3.cast.MediaItemConverter]: https://developer.android.com/reference/androidx/media3/cast/MediaItemConverter
[androidx-mediarouter-compose]: https://srgssr.github.io/androidx-mediarouter-compose/index.html
[media-route-button]: https://developer.android.com/reference/androidx/mediarouter/app/MediaRouteButton
