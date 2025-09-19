# Module pillarbox-cast-receiver

Provides a [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] implementation to handle GoogleCast TV receiver. Application needs to use [PillarboxCastReceiverPlayer][ch.srgssr.pillarbox.cast.receiver.PillarboxCastReceiverPlayer] instead of `PillarboxExoPlayer`.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-cast-receiver:<pillarbox_version>")
```

## Configuring Cast support

When a launch request is sent out by a sender application, an _Intent_ is created with an application namespace. 
Application is responsible for handling it and creating an instance of the [CastReceiverContext][cast-receiver-context] object when the TV app is launched. 
The CastReceiverContext object is needed to interact with Cast while the TV app is running. 
This object enables TV applications to accept Cast media messages coming from any connected senders.

### Android TV Setup

Add a new intent filter to the activity that you want to handle the launch intent from your sender app:

```xml
<activity android:name="com.example.activity">
  <intent-filter>
      <action android:name="com.google.android.gms.cast.tv.action.LAUNCH" />
      <category android:name="android.intent.category.DEFAULT" />
  </intent-filter>
</activity>
```

Application needs to implement a [ReceiverOptionsProvider][cast-receiver-options-provider] to provide [CastReceiverOptions][cast-receiver-options]:

```kotlin
class MyReceiverOptionsProvider : ReceiverOptionsProvider {
  override fun getOptions(context: Context?): CastReceiverOptions {
    return CastReceiverOptions.Builder(context)
            // If you don't set setStatusText, it is pulled automatically from android:label in your Android TV manifest.
          .setStatusText("My App")
          .build()
    }
}
```

Then specify the `ReceiverOptionsProvider` in the application `AndroidManifest`:

```xml
 <meta-data
    android:name="com.google.android.gms.cast.tv.RECEIVER_OPTIONS_PROVIDER_CLASS_NAME"
    android:value="com.example.mysimpleatvapplication.MyReceiverOptionsProvider" />
```

The `ReceiverOptionsProvider` is used to provide the `CastReceiverOptions` when `CastReceiverContext` is initialized.

### Cast receiver context

Initialize the [CastReceiverContext][cast-receiver-context] when your app is created:

```kotlin
override fun onCreate() {
    CastReceiverContext.initInstance(this)
}
```

Start the `CastReceiverContext` when your app moves to the foreground:

```kotlin
CastReceiverContext.getInstance().start()
```

Call [`stop()`][cast-receiver-context-stop] on the `CastReceiverContext` after the app goes into the background for video apps or apps that don't support background playback:

```kotlin
// Player has stopped.
CastReceiverContext.getInstance().stop()
```

> Additionally, if the application does support playing in the background, call `stop()` on the `CastReceiverContext` when it stops playing while in the background.

Google Cast strongly recommends you use the `LifecycleObserver` from the `androidx.lifecycle` library to manage calling `CastReceiverContext.start()` and `CastReceiverContext.stop()`, 
especially if your native app has multiple activities.
This avoids race conditions when you call `start()` and `stop()` from different activities.

```kotlin
// Create a LifecycleObserver class.
class MyLifecycleObserver : DefaultLifecycleObserver {
  override fun onStart(owner: LifecycleOwner) {
    // App prepares to enter foreground.
    CastReceiverContext.getInstance().start()
  }

  override fun onStop(owner: LifecycleOwner) {
    // App has moved to the background or has terminated.
    CastReceiverContext.getInstance().stop()
  }
}

// Add the observer when your application is being created.
class MyApplication : Application() {
  fun onCreate() {
    super.onCreate()

    // Initialize CastReceiverContext.
    CastReceiverContext.initInstance(this /* android.content.Context */)

    // Register LifecycleObserver
    ProcessLifecycleOwner.get().lifecycle.addObserver(
        MyLifecycleObserver())
  }
}
```

## Playback

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

Handle the cast intent in the `Activity` in `onNewIntent` and in `onCreate` method after linking the session with the CastReceiver.

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
- [Google Cast console setup][https://developers.google.com/cast/docs/android_tv_receiver/core_features#cast_developer_console_setup]

[ch.srgssr.pillarbox.player.PillarboxPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-player/index.html
[ch.srgssr.pillarbox.cast.receiver.PillarboxCastReceiverPlayer]: https://android.pillarbox.ch/api/pillarbox-cast-receiver/ch.srgssr.pillarbox.cast.receiver/-pillarbox-cast-receiver-player/index.html
[cast-receiver-context]: https://developers.google.com/android/reference/com/google/android/gms/cast/tv/CastReceiverContext
[cast-receiver-options-provider]: https://developers.google.com/android/reference/com/google/android/gms/cast/tv/ReceiverOptionsProvider
[cast-receiver-options]: https://developers.google.com/android/reference/com/google/android/gms/cast/tv/CastReceiverOptions
[cast-receiver-context-stop]: https://developers.google.com/android/reference/com/google/android/gms/cast/tv/CastReceiverContext#stop()
[cast-receiver-console-setup]: https://developers.google.com/cast/docs/android_tv_receiver/core_features#cast_developer_console_setup
