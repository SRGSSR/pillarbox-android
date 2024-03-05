[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Player module

This module provides `PillarboxPlayer`, the _Exoplayer_ `Player` implementation of media playback on Android.

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-player:$LATEST_RELEASE_VERSION")
```

More information can be found on the [top level README](../docs/README.md)

## Documentation
- [Getting started](#getting-started)
- [Tracking](./MediaItemTracking.md)

## Known issues
- Playing DRM content on two instances of `PillarboxPlayer` is not supported on all devices.
  - Currently known device: Samsung Galaxy A13

## Getting started

### Create a MediaItem




```kotlin
val mediaItem = MediaItem.fromUri(videoUri)
```

### Create a PillarboxPlayer

```kotlin
val player = PillarboxPlayer(context = context)
// Make player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

### Start playing a content

Create a `MediaItem` with all media information needed by 'PillarboxPlayer' as you would do with Exoplayer.
More information about MediaItem creation can be found [here](https://developer.android.com/media/media3/exoplayer/media-items)

```kotlin
val itemToPlay = MediaItem.fromUri("https://sample.com/sample.mp4")
player.setMediaItem(itemToPlay)
```

### Attaching to UI

PillarboxPlayer can be used with views provided by Exoplayer without any modifications.

#### Exoplayer ui module

Add the following to your `gradle` :

```gradle
 implementation("androidx.media3:media3-ui:$media3_version")
```

#### Set the player to the view

After adding the player view to your layout, in your Fragment or Activity you can then :

```kotlin
@Override
fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    // ...
    playerView = findViewById(R.id.player_view)
    playerView.player = player
}
```

**_A player can be attached to only one view!_**

### Release the player

When you don't need the player anymore, you have to release it. It frees resources used by the player. **_The player can't be used anymore after
that_**.

```kotlin
player.release()
```

### Connect the player to the MediaSession

```kotlin
val mediaSession = MediaSession.Builder(application, player).build()
```

Don't forget to release the `MediaSession` when you no longer need it or when releasing the player with

```kotlin
mediaSession.release()
```

More information about `MediaSession` is available [here](https://developer.android.com/guide/topics/media/media3/getting-started/mediasession)

## System integration and Background Playback

Android Media3 library recommends to use `MediaSessionService` or `MediaLibraryService` to do background playback. `MediaLibraryService` is useful
when the application needs to be connected to _Android Auto_ or _Automotive_. Pillarbox provide a implementation of each service type to help
integrator to handle them.

### PillarboxMediaSessionService

In order to use that service you need to declare it inside the application manifest as follow :

```xml

<service android:name=".service.DemoMediaSessionService" android:exported="true" android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>
```

And enable foreground service in the top of the manifest:

```xml

<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

And since Android 14 (targetApiVersion = 34) a new permission have to be added:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
```

Then in the code you have to use `MediaController` to handle playback, not `PillarboxPlayer`. Pillarbox provide an easy way to retrieve that
`MediaController` with `MediaControllerConnection`.

```kotlin
val sessionToken = SessionToken(application, ComponentName(application, MyMediaSessionService::class.java))
val listenableFuture = MediaController.Builder(application, sessionToken).setListener(this).buildAsync()

listenableFuture.addListener({ setController(listenableFuture.get())}, MoreExecutors.directExecutor())
// or suspend function
setController(listenableFuture.await())

// ...
MediaController.release(listenableFuture)
```

### PillarboxMediaLibraryService

`PillarboxMediaLibraryService` have the same feature than `PillarboxMediaSessionService` but it allow the application to provider content with 
_MediaBrowser_. More information about [Android auto](https://developer.android.com/training/auto/audio/).

In order to use that service you need to declare it inside the application manifest as follow :

```xml
<service android:name=".service.DemoMediaLibraryService" android:enabled="true" android:exported="true" android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaLibraryService" />
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>
```

Declaring application as Android Auto :

```xml
    <meta-data android:name="com.google.android.gms.car.application" android:resource="@xml/automotive_app_desc" />
```

`automotive_app_desc`

```xml
<?xml version="1.0" encoding="utf-8"?>
<automotiveApp>
    <uses name="media" />
</automotiveApp>
```

And enable foreground service in the top of the manifest:

```xml
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Then in the code you have to use `MediaBrowser` to handle playback, not `PillarboxPlayer`. Pillarbox provide an easy way to retrieve that
`MediaBrowser` with `MediaBrowserConnection`.

```kotlin
val sessionToken = SessionToken(application, ComponentName(application, MyMediaSessionService::class.java))
val listenableFuture = MediaBrowser.Builder(application, sessionToken).setListener(this).buildAsync()

listenableFuture.addListener({ setMediaBrowser(listenableFuture.get())}, MoreExecutors.directExecutor())
// or suspend function
setMediaBrowser(listenableFuture.await())

// ...
MediaController.release(listenableFuture)
```

## Exoplayer

As `PillarboxPlayer` extending an _Exoplayer_ `Player`, all documentation related to Exoplayer is valid for Pillarbox.

- [HelloWorld](https://developer.android.com/media/media3/exoplayer/hello-world.html)
- [Player Events](https://developer.android.com/media/media3/exoplayer/listening-to-player-events)
- [MediaItem](https://developer.android.com/media/media3/exoplayer/media-items)
- [Playlist](https://developer.android.com/media/media3/exoplayer/playlists)
- [Track Selection](https://developer.android.com/media/media3/exoplayer/track-selection)
