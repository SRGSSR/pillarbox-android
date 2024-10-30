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

To use this module, add the following dependency to your project's `build.gradle`/`build.gradle.kts` file:

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
val mediaItem: MediaItem = SRGMediaItem(urn)

// Content on stage
val mediaItemOnStage: MediaItem = SRGMediaItem(urn) {
    setHost(IlHost.Stage)
}

// Content with TV Vector
val mediaItemWithVector: MediaItem = SRGMediaItem(urn) {
    setVector(Vector.TV)
}

// Compute Vector from Context
val vector = context.getVector()
val mediaItemWithVector: MediaItem = SRGMediaItem(urn) {
    setVector(vector)
}

// Give the MediaItem to the player so it can be played
player.setMediaItem(mediaItem)
```

### Handle error

All exceptions thrown by [PillarboxMediaSource][ch.srgssr.pillarbox.player.source.PillarboxMediaSource] are caught by the player inside a
[PlaybackException][androidx.media3.common.PlaybackException].

[PillarboxMediaSource][ch.srgssr.pillarbox.player.source.PillarboxMediaSource] can throw:

- [BlockReasonException][ch.srgssr.pillarbox.core.business.exception.BlockReasonException] when the chapter has a block reason.
- [ResourceNotFoundException][ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException] when no "playable" resources are found in the
  chapter.
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

[PillarboxMediaSource][ch.srgssr.pillarbox.player.source.PillarboxMediaSource] factory can be created with a
[MediaCompositionService][ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService], which can be used to retrieve a
[MediaComposition][ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition]. You can create and provide your own implementation:

```kotlin
class CachedMediaCompositionService : MediaCompositionService {
    private val mediaCompositionCache = mutableMapOf<Uri, MediaComposition>()

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
        if (uri in mediaCompositionCache) {
            return Result.success(mediaCompositionCache.getValue(uri))
        }

        val mediaComposition = fetchMediaCompositionFromBackend(uri)
        if (mediaComposition != null) {
            mediaCompositionCache[uri] = mediaComposition

            return Result.success(mediaComposition)
        } else {
            return Result.failure(IOException("$uri not found"))
        }
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

[androidx.media3.common.MediaItem]: https://developer.android.com/reference/androidx/media3/common/MediaItem
[androidx.media3.common.PlaybackException]: https://developer.android.com/reference/androidx/media3/common/PlaybackException
[androidx.media3.exoplayer.source.MediaSource]: https://developer.android.com/reference/androidx/media3/exoplayer/source/MediaSource
[ch.srgssr.pillarbox.core.business.exception.BlockReasonException]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/exception/BlockReasonException.kt
[ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/exception/ResourceNotFoundException.kt
[ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business.integrationlayer.data/-media-composition/index.html
[ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business.integrationlayer.service/-media-composition-service/index.html
[ch.srgssr.pillarbox.core.business.SRGMediaItem]: https://android.pillarbox.ch/api/pillarbox-core-business/ch.srgssr.pillarbox.core.business/-s-r-g-media-item.html
[ch.srgssr.pillarbox.player.PillarboxExoPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-exo-player/index.html
[ch.srgssr.pillarbox.player.PillarboxPlayer]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player/-pillarbox-player/index.html
[ch.srgssr.pillarbox.player.source.PillarboxMediaSource]: https://android.pillarbox.ch/api/pillarbox-player/ch.srgssr.pillarbox.player.source/-pillarbox-media-source/index.html
[kotlin.Exception]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/
[spherical-surface-showcase]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-demo/src/main/java/ch/srgssr/pillarbox/demo/ui/showcases/misc/SphericalSurfaceShowcase.kt
