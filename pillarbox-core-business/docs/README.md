[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![Last release](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android?label=Release)](https://github.com/SRGSSR/pillarbox-android/releases)
[![Android min SDK](https://img.shields.io/badge/Android-21%2B-34A853)](https://github.com/SRGSSR/pillarbox-android)
[![License](https://img.shields.io/github/license/SRGSSR/pillarbox-android?label=License)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Core Business module

Provides a custom [`MediaSource`][media-source-documentation] to Pillarbox, suited for handling SRG SSR media URN. It basically converts a
[`MediaComposition`][media-composition-source] from the integration layer to a playable [`MediaSource`][media-source-documentation].

Supported contents are :

- On demand Video and Audio.
- Live streams, with and without DVR.
- Token-protected content.
- DRM protected content.
- 360° content (see [`SphericalSurfaceShowcase`][spherical-surface-showcase]).

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-core-business:<pillarbox_version>")
```

More information can be found in the [top level README](https://github.com/SRGSSR/pillarbox-android#readme).

## Getting started

### Create the player

To play a URN content with [`PillarboxPlayer`][pillarbox-player-source], you have to create it like this:

```kotlin
val player = PillarboxExoPlayer(context)
// Make the player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play() 
```

### Create a `MediaItem` with URN

To tell [`PillarboxPlayer`][pillarbox-player-source] to load a specific [`MediaItem`][media-item-documentation], it has to be created with
[`SRGMediaItemBuilder`][srg-media-item-builder-source]:

```kotlin
val urn = "urn:rts:video:12345"
val mediaItem = SRGMediaItem(urn)
// or with Builder
val mediaItem = SRGMediaItemBuilder(urn).build()

// Content on stage
val mediaItemOnStage = SRGMediaItem(urn){
    setHost(IlHost.Stage)
}

// Content with TV Vector
val mediaItemWithVector = SRGMediaItem(urn){
    setVector(Vector.TV)
}

// Compute Vector from Context
val vector = context.getVector()
val mediaItemWithVector = SRGMediaItem(urn){
    setVector(vector)
}
```

### Handle error

All exceptions thrown by [`PillarboxMediaSource`][pillarbox-media-source-source] are caught by the player inside a
[`PlaybackException`][playback-exception-documentation].

[`PillarboxMediaSource`][pillarbox-media-source-source] can throw:

- [`BlockReasonException`][block-reason-exception-source] when the chapter has a block reason.
- [`ResourceNotFoundException`][resource-not-found-exception-source] when no "playable" resources are found in the chapter.
- `RemoteResult.Error`.`throwable`:
    - `HttpException`
    - `IOException`
    - Any custom `Exception`

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

[`PillarboxMediaSource`][pillarbox-media-source-source] factory can be created with a [`MediaCompositionService`][media-composition-service-source],
which can be used to retrieve a [`MediaComposition`][media-composition-source]. You can create and provide your own implementation:

```kotlin
class CustomMediaCompositionService : MediaCompositionService {
    private val mediaCompositionMap = mutableMapOf<Uri, MediaComposition>()

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
        return mediaCompositionMap[uri]?.let {
            Result.success(it)
        } ?: Result.failure(IOException("$uri not found"))
    }
}
```

Then, pass it to [`PillarboxExoPlayer`][pillarbox-exo-player-source]:

```kotlin
val player = PillarboxExoPlayer(context) {
    srgAssetLoader(context) {
        mediaCompositionService(CustomMediaCompositionService())
    }
}
```

[block-reason-exception-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/exception/BlockReasonException.kt
[media-composition-service-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/integrationlayer/service/MediaCompositionService.kt
[media-composition-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/integrationlayer/data/MediaComposition.kt
[media-item-documentation]: https://developer.android.com/reference/androidx/media3/common/MediaItem
[media-source-documentation]: https://developer.android.com/reference/androidx/media3/exoplayer/source/MediaSource
[pillarbox-exo-player-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/PillarboxSRG.kt
[pillarbox-media-source-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/source/PillarboxMediaSource.kt
[pillarbox-player-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxPlayer.kt
[playback-exception-documentation]: https://developer.android.com/reference/androidx/media3/common/PlaybackException
[resource-not-found-exception-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/exception/ResourceNotFoundException.kt
[spherical-surface-showcase]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-demo/src/main/java/ch/srgssr/pillarbox/demo/ui/showcases/misc/SphericalSurfaceShowcase.kt
[srg-media-item-builder-source]: https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-core-business/src/main/java/ch/srgssr/pillarbox/core/business/SRGMediaItemBuilder.kt
