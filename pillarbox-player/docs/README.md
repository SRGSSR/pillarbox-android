[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![Last release](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android?label=Release)](https://github.com/SRGSSR/pillarbox-android/releases)
[![Android min SDK](https://img.shields.io/badge/Android-21%2B-34A853)](https://github.com/SRGSSR/pillarbox-android)
[![License](https://img.shields.io/github/license/SRGSSR/pillarbox-android?label=License)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Player module

Provides [`PillarboxPlayer`][pillarbox-player-source], an AndroidX Media3 [`Player`][player-documentation] implementation for media playback on
Android.

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-player:<pillarbox_version>")
```

More information can be found in the [top level README](https://github.com/SRGSSR/pillarbox-android#readme).

## Documentation

- [Getting started](#getting-started)
- [Tracking](./MediaItemTracking.md)

## Known issues

- Playing DRM content on two instances of [`PillarboxPlayer`][pillarbox-player-source] is not supported on all devices.
    - Known affected devices: Samsung Galaxy A13.

## Getting started

### Create the player

```kotlin
val player = PillarboxExoPlayer(context)
// Make the player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

#### Monitoring playback

By default, [`PillarboxExoPlayer`][pillarbox-exo-player-source] does not record any monitoring data. You can configure this when creating the player:

```kotlin
val player = PillarboxExoPlayer(context) {
    monitoring(Logcat)
}
```

Multiple implementations are provided out of the box, but you can also provide your own
[`MonitoringMessageHandler`][monitoring-message-handler-source]:

- `NoOp()` (default): does nothing.
- `Logcat { config(...) }`: prints each message to Logcat.
- `Remote { config(...) }`: sends each message to a remote server.

### Create a `MediaItem`

More information about [`MediaItem`][media-item-documentation] creation can be found [here][media-item-creation-documentation].

```kotlin
val mediaUri = "https://sample.com/sample.mp4"
val mediaItem = MediaItem.fromUri(mediaUri)

player.setMediaItem(mediaItem)
```

### Attaching to UI

[`PillarboxPlayer`][pillarbox-player-source] can be used with views provided by [Exoplayer][exo-player-documentation] without any modifications.

#### ExoPlayer UI module

Add the following to your module's `build.gradle`/`build.gradle.kts` file:

```gradle
implementation("androidx.media3:media3-ui:<androidx_media3_version>")
```

#### Set the player to the view

After adding the [`PlayerView`][player-view-documentation] to your layout, you can then do the following:

```kotlin
@Override
fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val playerView: PlayerView = findViewById(R.id.player_view)
    playerView.player = player
}
```

> [!WARNING]
> A player can be attached to only one [`View`][view-documentation]!

### Release the player

When you don't need the player anymore, you have to release it. It frees resources used by the player.

```kotlin
player.release()
```

> [!WARNING]
> The player can't be used anymore after that.

### Connect the player to the `MediaSession`

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
[`MediaBrowser`][media-browser-documentation]. More information about [Android auto][android-auto-documentation].

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

## ExoPlayer

As [`PillarboxExoPlayer`][pillarbox-exo-player-source] extends from [ExoPlayer][exo-player-documentation], all documentation related to ExoPlayer is 
also valid for Pillarbox. Here are some useful links to get more information about ExoPlayer:

- [Getting started with ExoPlayer](https://developer.android.com/media/media3/exoplayer/hello-world.html)
- [Player events](https://developer.android.com/media/media3/exoplayer/listening-to-player-events)
- [Media items](https://developer.android.com/media/media3/exoplayer/media-items)
- [Playlists](https://developer.android.com/media/media3/exoplayer/playlists)
- [Track selection](https://developer.android.com/media/media3/exoplayer/track-selection)

[android-auto-documentation]: https://developer.android.com/training/auto/audio/
[exo-player-documentation]: https://developer.android.com/media/media3/exoplayer
[media-browser-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaBrowser
[media-controller-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaController
[media-item-creation-documentation]: https://developer.android.com/media/media3/exoplayer/media-items
[media-item-documentation]: https://developer.android.com/reference/androidx/media3/common/MediaItem
[media-library-service-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaLibraryService
[media-session-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaSession
[media-session-guide]: https://developer.android.com/guide/topics/media/media3/getting-started/mediasession
[media-session-service-documentation]: https://developer.android.com/reference/androidx/media3/session/MediaSessionService
[monitoring-message-handler-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/monitoring/MonitoringMessageHandler.kt
[pillarbox-exo-player-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxExoPlayer.kt
[pillarbox-media-browser-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaBrowser.kt
[pillarbox-media-controller-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaController.kt
[pillarbox-media-library-service-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaLibraryService.kt
[pillarbox-media-session-service-source]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/session/PillarboxMediaSessionService.kt
[pillarbox-player-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxPlayer.kt
[player-documentation]: https://developer.android.com/reference/androidx/media3/common/Player
[player-view-documentation]: https://developer.android.com/reference/androidx/media3/ui/PlayerView
[view-documentation]: https://developer.android.com/reference/android/view/View.html
