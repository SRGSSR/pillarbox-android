[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Core Business module

Provides SRG SSR media URN `MediaSource` to Pillarbox. It basically converts an integration layer `MediaComposition` to a
playable `MediaSource`.

Supported contents are :

- Video and Audio on demand
- Live streams (with and without DVR)
- Token protected
- DRM protected
- 360° content (Need to used the correct view)

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-core-business:$LATEST_RELEASE_VERSION")
```

More information can be found on the [top level README](../docs/README.md)

## Getting started

### Create the player

In order to play an urn content with PillarboxPlayer, you have to create it like this :

```kotlin
val player = PillarboxPlayer(
    context = context,
    mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
        addAssetLoader(SRGAssetLoader(context))
    },
    mediaItemTrackerProvider = DefaultMediaItemTrackerRepository()
)
```

### Create MediaItem with URN

In order to tell `PillarboxPlayer` to load a specific `MediaItem` with `PillarboxMediaSourceFactory`, the `MediaItem` has to be created with 
`SRGMediaItemBuilder` :

```kotlin
val urnToPlay = "urn:rts:video:12345"
val itemToPlay = SRGMediaItemBuilder(urnToPlay).build()

player.setMediaItem(itemToPlay)
```

### Handle error

All exceptions thrown by `MediaCompositionMediaItemSource` are caught by the player inside a `PlaybackException`.

`MediaCompositionMediaItemSource` can throw

- BlockReasonException when Chapter has a block reason
- ResourceNotFoundException when no "playable" resource are found in the Chapter
- RemoteResult.Error.throwable
    - HttpException
    - IOException
    - Any custom Exception

```kotlin
 player.addListener(object : Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        when (val cause = error.cause) {
            is BlockReasonException -> Log.e("Demo", "Content blocked by ${cause.blockReason}")
            is ResourceNotFoundException -> Log.e("Demo", "No Resources found in the Chapter")
            else -> Log.d("Demo", "Error occurred", cause)
        }
    }
})
```

## Going further

`PillarboxMediaSource` factory can be created with a `MediaCompositionService`, which can be used to retrieve a `MediaComposition`.  You can create 
you own `MediaCompositionService` to load the `MediaComposition` :

```kotlin
class MediaCompositionMapDataSource : MediaCompositionService {
    private val mediaCompositionMap = mutableMapOf<Uri, MediaComposition>()

    override suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition> {
        return mediaCompositionMap[uri]?.let {
            Result.success(it)
        } ?: Result.failure(IOException("$uri not found"))
    }
}
```
