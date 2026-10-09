# Module pillarbox-core-business

Provides a [MediaSource][androidx.media3.exoplayer.source.MediaSource] for handling SRG SSR media URNs to Pillarbox. It basically converts an
integration layer [MediaComposition][ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition] to a playable
[MediaSource][androidx.media3.exoplayer.source.MediaSource].

The supported contents are:

- On demand video and audio.
- Live streams, with and without DRM.
- Token-protected content.
- DRM protected content.
- 360° content (see [SphericalSurfaceShowcase][spherical-surface-showcase]).

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-core-business:<pillarbox_version>")
```

## Getting started

### Create the player

To play a URN content with [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer], you have to create it like this:

```kotlin
val player = PillarboxExoPlayer(context)
// Make the player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

### Create a `MediaItem` with URN

To tell [PillarboxPlayer][ch.srgssr.pillarbox.player.PillarboxPlayer] to load a specific [MediaItem][androidx.media3.common.MediaItem], it has to be
created with [SRGMediaItem][ch.srgssr.pillarbox.core.business.SRGMediaItem]:

```kotlin
val urn = "urn:rts:video:12345"

// MediaItem created on Prod with Vector.MOBILE
val mediaItem: MediaItem = SRGMediaItem(urn)

// Optionally customize the MediaItem
val customMediaItem: MediaItem = SRGMediaItem(urn) {
    setHost(IlHost.Stage)
    setVector(Vector.TV)
    setVector(context.getVector())
}

// Give the MediaItem to the player so it can be played
player.setMediaItem(mediaItem)
```

### Add Commanders Act specific data linked to a MediaItem

[CommandersActSource][ch.srgssr.pillarbox.analytics.commandersact.CommandersActSource] can be passed to `SRGMediaItem` to forward labels to Commanders Act.

```kotlin
val mediaItem: MediaItem = SRGMediaItem("urn:rts:video:12345") {
    commandersActSource(CommandersActSource(pageId = "pageId", sectionId = "sectionId"))
}

// Give the MediaItem to the player so it can be played
player.setMediaItem(mediaItem)

// Read the CommandersActSource back from the MediaItem
val source: CommandersActSource? = mediaItem.commandersActSource
```

> [!NOTE]
> The CommandersActSource is stored in the [MediaMetadata.extras][androidx.media3.common.MediaMetadata.extras] using only platform types. 
> Those extras are shared with other processes through the media session (e.g. Bluetooth), which can't load custom Parcelable classes. 
> If you add your own data to the extras, only use platform types, otherwise system processes may crash on Android 12 and lower.

### Handle error

All exceptions thrown by [PillarboxMediaSource][ch.srgssr.pillarbox.player.source.PillarboxMediaSource] are caught by the player inside a
[PlaybackException][androidx.media3.common.PlaybackException].

[PillarboxMediaSource][ch.srgssr.pillarbox.player.source.PillarboxMediaSource] can throw:

- [BlockReasonException][ch.srgssr.pillarbox.core.business.exception.BlockReasonException] when the chapter has a block reason.
- [ResourceNotFoundException][ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException] when the chapter contains no resources.
- `RemoteResult.Error`.`throwable`:
    - `HttpException`.
    - `IOException`.
    - Any custom [Exception][kotlin.Exception].

```kotlin
player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (val cause = error.cause) {
            is BlockReasonException.StartDate -> Log.d("Pillarbox", "Content is blocked until ${cause.instant}")
            is BlockReasonException -> Log.d("Pillarbox", "Content is blocked", cause)
            is ResourceNotFoundException -> Log.d("Pillarbox", "No resources found in the chapter")
            else -> Log.d("Pillarbox", "An error occurred", cause)
        }
    }
})
```

## Going further

### Device capabilities

The URL of a [MediaItem][androidx.media3.common.MediaItem] created with [SRGMediaItem][ch.srgssr.pillarbox.core.business.SRGMediaItem] contains the
[DeviceCapabilities][ch.srgssr.pillarbox.core.business.integrationlayer.data.DeviceCapabilities] of the device, as query parameters:

- `playerPlatform`: always `android`.
- `drmPlayerCapabilities`: the Widevine vendor and security level, for example `com.widevine.alpha;L1`. It is only sent when the security level of
  the device can be read.

The integration layer then returns the resources the device is able to play, ordered by preference, and the first one is played. If no resources
are returned, a [ResourceNotFoundException][ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException] is thrown.

### Custom MediaCompositionService

[PillarboxMediaSource][ch.srgssr.pillarbox.player.source.PillarboxMediaSource] factory can be created with a
[MediaCompositionService][ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService], which can be used to retrieve a
[MediaComposition][ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition], wrapped in a
[MediaCompositionResponse][ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaCompositionResponse] alongside the response headers. You can
create and provide your own implementation.

The simplest way is to delegate the network request to
[HttpMediaCompositionService][ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService]:

```kotlin
class CachedMediaCompositionService(
    private val httpMediaCompositionService: MediaCompositionService = HttpMediaCompositionService(),
) : MediaCompositionService {
    private val mediaCompositionCache = mutableMapOf<Uri, MediaCompositionResponse>()

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaCompositionResponse> {
        if (uri in mediaCompositionCache) {
            return Result.success(mediaCompositionCache.getValue(uri))
        }

        return httpMediaCompositionService.fetchMediaComposition(uri)
            .onSuccess { mediaCompositionCache[uri] = it }
    }
}
```

Then, pass it to [PillarboxExoPlayer][ch.srgssr.pillarbox.player.PillarboxExoPlayer]:

```kotlin
val player = PillarboxExoPlayer(context) {
    srgAssetLoader(context) {
        mediaCompositionService(CachedMediaCompositionService())
    }
}
```

[android.os.Parcelable]: https://developer.android.com/reference/android/os/Parcelable
[androidx.media3.common.MediaItem]: https://developer.android.com/reference/androidx/media3/common/MediaItem
[androidx.media3.common.MediaMetadata.extras]: https://developer.android.com/reference/androidx/media3/common/MediaMetadata#extras()
[androidx.media3.common.PlaybackException]: https://developer.android.com/reference/androidx/media3/common/PlaybackException
[androidx.media3.exoplayer.source.MediaSource]: https://developer.android.com/reference/androidx/media3/exoplayer/source/MediaSource
[ch.srgssr.pillarbox.core.business.exception.BlockReasonException]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/exception/BlockReasonException.kt
[ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/exception/ResourceNotFoundException.kt
[ch.srgssr.pillarbox.core.business.integrationlayer.data.DeviceCapabilities]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business.integrationlayer.data/-device-capabilities/index.html
[ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business.integrationlayer.data/-media-composition/index.html
[ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business.integrationlayer.service/-http-media-composition-service/index.html
[ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business.integrationlayer.service/-media-composition-service/index.html
[ch.srgssr.pillarbox.core.business.SRGMediaItem]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business/-s-r-g-media-item.html
[ch.srgssr.pillarbox.player.PillarboxExoPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-exo-player/index.html
[ch.srgssr.pillarbox.player.PillarboxPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-player/index.html
[ch.srgssr.pillarbox.player.source.PillarboxMediaSource]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player.source/-pillarbox-media-source/index.html
[kotlin.Exception]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/
[spherical-surface-showcase]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-demo/src/main/java/ch/srgssr/pillarbox/demo/ui/showcases/misc/SphericalSurfaceShowcase.kt
[ch.srgssr.pillarbox.analytics.commandersact.CommandersActSource]: https://android.pillarbox.ch/api/pillarbox-analytics/ch.srgssr.pillarbox.analytics.commandersact/-commanders-act-source/index.html
