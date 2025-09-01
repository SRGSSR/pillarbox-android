# Module pillarbox-cast-receiver

Provides a [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] implementation of a GoogleCast TV receiver. Application needs to use [PillarboxCastReceiverPlayer][ch.srgssr.pillarbox.cast.receiver.PillarboxCastReceiverPlayer] instead of `PillarboxExoPlayer`.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-cast-receiver:<pillarbox_version>")
```

## Configuring Cast support

[Configure cast support](https://developers.google.com/cast/docs/android_tv_receiver/core_features#configuring_cast_support) like it is describes following your application needs and configuration.

## Getting started

Get the `CastReceiverContext`

```kotlin
val castReceiverContext = CastReceiverContext.getInstance()
```

Create the [PillarboxCastReceiverPlayer][ch.srgssr.pillarbox.cast.receiver.PillarboxCastReceiverPlayer] that implements [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] and handles all incoming events from the cast senders.

```kotlin
val player : PillarboxPlayer = PillarboxCastReceiverPlayer(
    player = PillarboxExoPlayer(this),
    mediaItemConverter = SRGMediaItemConverter(), // It should be the same converter that is used by the Android senders.
    castReceiverContext = castReceiverContext,
)

val mediaSession = PillarboxMediaSession.Builder(this, player).build()
```

Link the player `MediaSession` with the `CastReceiverContext`

```kotlin
castReceiverContext.mediaManager.setSessionTokenFromPillarboxMediaSession(mediaSession)
```

Handle the cast intent in the `Activity` where the player live in the `onNewIntent` and in the `onCreate` method after linking the session with the CastReceiver.

```kotlin
private fun handleIntent(intent: Intent) {
    val mediaManager = CastReceiverContext.getInstance().mediaManager
    if (mediaManager.onNewIntent(intent)) {
        return
    } else {
        // Cast doesn't handle this intent do the stuff you application have to do.
    }
}
````

## Additional resources

- [Google Cast receiver SDK](https://developers.google.com/cast/docs/android_tv_receiver)
- [Google Cast sender SDK](https://developers.google.com/cast/docs/android_sender)

[ch.srgssr.pillarbox.player.PillarboxPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-player/index.html
[ch.srgssr.pillarbox.cast.receiver.PillarboxCastReceiverPlayer]: https://android.pillarbox.ch/api/pillarbox-cast-receiver/ch.srgssr.pillarbox.cast.receiver/-pillarbox-cast-receiver-player/index.html