# Module pillarbox-cast

Provides a [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] implementation that controls a Cast receiver app.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-cast:<pillarbox_version>")
```

The main goal of this module is to be able to build a user interface that can be used with a [PillarboxExoPlayer][ch.srgssr.pillarbox.player.PillarboxExoPlayer] or [PillarboxCastPlayer][ch.srgssr.pillarbox.cast.PillarboxCastPlayer]. Both
implementations are based on the [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] interface.

## Getting started

### Setup [CastContext][cast-context]

The framework has a global singleton object, the `CastContext`, that coordinates all the framework's interactions.

Application must implement the [OptionsProvider][cast-options-provider] interface to supply options needed to initialize the `CastContext` singleton. 
OptionsProvider provides an instance of [CastOptions][cast-options] which contains options that affect the behavior of the framework. 
The most important of these is the Web Receiver **application ID**,
which is used to filter discovery results and to launch the Web Receiver app when a Cast session is started or the Android TV receiver.

```kotlin
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val launchOptions: LaunchOptions = LaunchOptions.Builder()
            .setAndroidReceiverCompatible(true) // true if the sender application is compatible with the Android TV receiver.
            .build()
        return Builder()
            .setReceiverApplicationId(RECEIVER_APP_ID)
            .setLaunchOptions(launchOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}
```
You must declare the fully qualified name of the implemented OptionsProvider as a metadata field in the AndroidManifest.xml file of the sender app:

```xml
<application>
    ...
    <meta-data
        android:name=
            "com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
        android:value="com.foo.CastOptionsProvider" />
</application>
```

CastContext is lazily initialized when the `CastContext.getSharedInstance()` is called.
Pillarbox provides an extension to easily handle the initialization.

```kotlin
val castContext = context.getCastContext()
```

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
- [MediaMaestro][androidx-mediarouter-compose], which works best with Compose.

## Local to remote playback

[CastPlayerSynchronizer][ch.srgssr.pillarbox.cast.CastPlayerSynchronizer] provide an easy to use local to remote management that synchronized player state when needed.

When using [CastPlayerSynchronizer][ch.srgssr.pillarbox.cast.CastPlayerSynchronizer] state transition is handeled when it is needed. By default the following states are synchronized:
- MediaItems
- Playback position
- Repeat mode
- PlayWhenReady
- Track selection

> 
> Track selection restoration does best effort, by default it tries to select the first track that matches the language. So if multiple tracks with the same language are present, it may not choose the one that is actually selected.

```kotlin
val localPlayer = PillarboxExoPlayer(context, Default)
val castPlayer = PillarboxCastPlayer(context, Default)

val castSynchronizer = CastPlayerSynchronizer(
    castContext = castContext,
    coroutineScope = coroutineScope,
    castPlayer = castPlayer,
    localPlayer = localPlayer,
)
var currentPlayer: StateFlow<PillarboxPlayer> = castSynchronizer.currentPlayer

....

PlayerView(currentPlayer)
```

The default behavior can be modified by overridden [DefaultPlayerSynchronizer][ch.srgssr.pillarbox.cast.CastPlayerSynchronizer.DefaultPlayerSynchronizer] or by creating a new [PlayerSynchronizer][ch.srgssr.pillarbox.cast.CastPlayerSynchronizer] implementation.

```kotlin
class CustomPlayerSynchronizer : CastPlayerSynchronizer.DefaultPlayerSynchronizer() {

    override fun onTracksChanged(
        newTracks: Tracks,
        selectedAudioTrack: AudioTrack?,
        selectedTextTrack: TextTrack?
    ): CastPlayerSynchronizer.Selection {
        // An example to disable text track
        val selection = super.onTracksChanged(newTracks, selectedAudioTrack, selectedTextTrack)
        return CastPlayerSynchronizer.Selection(selection.audioTrack, null)
    }

    override fun onPlayerChanged(oldPlayer: PillarboxPlayer, newPlayer: PillarboxPlayer) {
        super.onPlayerChanged(oldPlayer, newPlayer)
        // Update newPlayer with some state and handle the state of the oldPlayer.
        newPlayer.prepare()
        oldPlayer.stop()
        oldPlayer.clearMediaItems()
        // Don't call release in otherwise the player can't come back after.
    }
}

val castSynchronizer = CastPlayerSynchronizer(
    castContext = castContext,
    coroutineScope = coroutineScope,
    castPlayer = castPlayer,
    localPlayer = localPlayer,
    playerSynchronizer = CustomPlayerSynchronizer()
)
```

## Road map

- Handle Pillarbox metadata such as Chapters, blocked time range and credits.

## Additional resources

- [Google Cast SDK](https://developers.google.com/cast/docs/android_sender)

[ch.srgssr.pillarbox.player.PillarboxPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-player/index.html
[ch.srgssr.pillarbox.player.PillarboxExoPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-exo-player.html
[ch.srgssr.pillarbox.cast.PillarboxCastPlayer]: https://android.pillarbox.ch/api/pillarbox-cast/ch.srgssr.pillarbox.cast/-pillarbox-cast-player/index.html
[ch.srgssr.pillarbox.cast.CastPlayerSynchronizer]: https://android.pillarbox.ch/api/pillarbox-cast/ch.srgssr.pillarbox.cast/-cast-player-synchronizer/index.html
[ch.srgssr.pillarbox.cast.CastPlayerSynchronizer.DefaultPlayerSynchronizer]: https://android.pillarbox.ch/api/pillarbox-cast/ch.srgssr.pillarbox.cast/-cast-player-synchronizer/-default-player-synchronizer/index.html
[ch.srgssr.pillarbox.cast.CastPlayerSynchronizer.PlayerSynchronizer]: https://android.pillarbox.ch/api/pillarbox-cast/ch.srgssr.pillarbox.cast/-cast-player-synchronizer/-player-synchronizer/index.html
[androidx.media3.cast.MediaItemConverter]: https://developer.android.com/reference/androidx/media3/cast/MediaItemConverter
[androidx-mediarouter-compose]: https://srgssr.github.io/MediaMaestro/
[media-route-button]: https://developer.android.com/reference/androidx/mediarouter/app/MediaRouteButton
[cast-context]: https://developers.google.com/android/reference/com/google/android/gms/cast/framework/CastContext
[cast-options]: https://developers.google.com/android/reference/com/google/android/gms/cast/framework/CastOptions
[cast-options-provider]: https://developers.google.com/android/reference/com/google/android/gms/cast/framework/OptionsProvider
