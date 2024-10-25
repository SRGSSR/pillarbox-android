# `MediaSession`

[`MediaSession`][media-session] from Media3 is needed in some cases:

- Intent to use the player on TV.
- Intent to use the player on Android Auto.
- When playing content in the background with a media notification.
- Handle actions button from headset or remote devices.

Pillarbox enhanced MediaSession with Pillarbox special features, every original Media3 classes have their equivalent in Pillarbox:

- `MediaSession` : [`PillarboxMediaSession`][pillarbox-media-session-source]
- `MediaSessionService` : [`PillarboxMediaSessionService`][pillarbox-media-session-service-source]
- `MediaController` : [`PillarboxMediaController`][pillarbox-media-controller-source]
- `MediaLibrarySession` : [`PillarboxMediaLibrarySession`][pillarbox-media-library-session-source]
- `MediaLibraryService` : [`PillarboxMediaLibraryService`][pillarbox-media-library-service-source]
- `MediaBrowser`: [`PillarboxMediaBrowser`][pillarbox-media-browser-source]

## Connect the player to the `MediaSession`

```kotlin
val mediaSession = PillarboxMediaSession.Builder(context, player).build()
```

Remember to release the [`MediaSession`][media-session-documentation] when you no longer need it, or when releasing the player, with:

```kotlin
mediaSession.release()
```

More information about [`MediaSession`][media-session-documentation] is available [here][media-session-guide].

## System integration and background playback

AndroidX Media3 library recommends to use [`MediaSessionService`][media-session-service-documentation] or
[`MediaLibraryService`][media-library-service-documentation] to do background playback. [`MediaLibraryService`][media-library-service-documentation]
is useful when the application needs to connect to _Android Auto_ or _Automotive_. Pillarbox provides an implementation of each service type to help
you handle them.

### `PillarboxMediaSessionService`

To use that service, you need to declare it inside your `AndroidManifest.xml`, as follows:

```xml
<service android:exported="true" android:foregroundServiceType="mediaPlayback" android:name=".service.DemoMediaSessionService">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

And enable foreground service at the top of the manifest:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- Only necessary if your target SDK version is 34 or newer -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

Then, in your code, you have to use [`PillarboxMediaController`][pillarbox-media-controller-source] to handle playback, instead of
[`PillarboxExoPlayer`][pillarbox-exo-player-source]. Pillarbox provides an easy way to retrieve the
[`MediaController`][media-controller-documentation] with [`PillarboxMediaController.Builder`][pillarbox-media-controller-source].

```kotlin
coroutineScope.launch {
    val mediaController: PillarboxPlayer = PillarboxMediaController.Builder(context, DemoMediaLibraryService::class.java).build()
    doSomethingWith(mediaController)
}
```

### `PillarboxMediaLibraryService`

[`PillarboxMediaLibraryService`][pillarbox-media-library-service-source] has the same features as
[`PillarboxMediaSessionService`][pillarbox-media-session-service-source], but it allows the application to provide content with
[`MediaBrowser`][media-browser-documentation]. More information about [Android Auto][android-auto-documentation].

To use that service, you need to declare it inside the application manifest as follows:

```xml
<service android:exported="true" android:foregroundServiceType="mediaPlayback" android:name=".service.DemoMediaLibraryService">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaLibraryService" />
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>
```

Declare the application as an Android Auto application:

```xml
<meta-data android:name="com.google.android.gms.car.application" android:resource="@xml/automotive_app_desc" />
```

In the `res/xml/automotive_app_desc.xml` file, add the following:

```xml
<?xml version="1.0" encoding="utf-8"?>
<automotiveApp>
    <uses name="media" />
</automotiveApp>
```

And enable foreground service at the top of the manifest:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Then, in your code, you have to use [`PillarboxMediaBrowser`][pillarbox-media-browser-source] to handle playback, instead of
[`PillarboxExoPlayer`][pillarbox-exo-player-source]. Pillarbox provides an easy way to retrieve the
[`MediaBrowser`][media-browser-documentation] with [`PillarboxMediaBrowser.Builder`][pillarbox-media-browser-source].

```kotlin
coroutineScope.launch {
    val mediaBrowser: PillarboxPlayer = PillarboxMediaBrowser.Builder(context, DemoMediaLibraryService::class.java).build()
    doSomethingWith(mediaBrowser)
}
```

[media-browser-documentation]: https://developer.android.com/media/media3/session/connect-to-media-app#browser
[media-controller-documentation]: https://developer.android.com/media/media3/session/connect-to-media-app
[media-session]: https://developer.android.com/media/media3/session/control-playback
[media-library-service-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaLibraryService
[media-session-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaSession
[media-session-guide]: https://developer.android.com/guide/topics/media/media3/getting-started/mediasession
[media-session-service-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaSessionService
[pillarbox-exo-player-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxExoPlayer.kt
[pillarbox-media-browser-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaBrowser.kt
[pillarbox-media-controller-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaController.kt
[pillarbox-media-library-service-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaLibraryService.kt
[pillarbox-media-library-session-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaLibrarySession.kt
[pillarbox-media-session-service-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaSessionService.kt
[pillarbox-media-session-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaSession.kt
[android-auto-documentation]: https://developer.android.com/training/auto/audio/
