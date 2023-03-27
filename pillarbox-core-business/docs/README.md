[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Core Business module

Provides SRG SSR media URN `MediaItemSource` to Pillarbox. It basically converts an integration layer `MediaComposition` to a
playable `MediaItem`.

Supported contents are :

- Video and Audio on demand
- Live streams (with and without DVR)
- Token protected
- DRM protected

Unsupported contents are :

- 360° content

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
    mediaItemSource = MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(application, IlHost.PROD)),
    /**
     * Can be skipped if you never play token-protected content.
     */
    dataSourceFactory = AkamaiTokenDataSource.Factory()
)
```

`MediaCompositionDataSourceImpl` retrieves a `MediaComposition` from the integration layer web service.

### Create MediaItem with URN

```kotlin
val urnToPlay = "urn:rts:video:12345"
val itemToPlay = MediaItem.Builder().setMediaId(urnToPlay).build()

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

## Add custom trackers

`MediaItemTracker` can be added to the player. The data related to tracker have to be added during `MediaItem` creation inside
`MediaCompositionMediaItemSource`. `TrackerDataProvider` allow to add data for specific tracker.

### Create custom MediaItemTracker

```kotlin
class CustomTracker : MediaItemTracker {

    data class Data(val mediaComposition: MediaComposition)

    // implements here functions
}
```

### Create and add required custom tracker data

```kotlin
val mediaItemSource = MediaCompositionMediaItemSource(
    mediaCompositionDataSource = mediaCompositionDataSource,
    trackerDataProvider = object : TrackerDataProvider {
        override fun update(trackerData: MediaItemTrackerData, resource: Resource, chapter: Chapter, mediaComposition: MediaComposition) {
            trackerData.putData(CustomTracker::class.java, CustomTracker.Data(mediaComposition))
        }
    })
```

### Inject custom tracker to the player

```kotlin
val player = PillarboxPlayer(context = context,
    mediaItemSource = mediaItemSource,
    mediaItemTrackerProvider = DefaultMediaItemTrackerRepository().apply {
        registerFactory(CustomTracker::class.java, CustomTracker.Factory())
    }
)
```

## Going further

As you see, the `MediaCompositionMediaItemSource` is created from an interface, so you can load custom MediaComposition easily into Pillarbox by
implementing your own `MediaCompositionDataSource`.

```kotlin
class MediaCompositionMapDataSource : MediaCompositionDataSource {
    private val mediaCompositionMap = HashMap<String, MediaComposition>()

    override suspend fun getMediaCompositionByUrn(urn: String): RemoteResult<MediaComposition> {
        return mediaCompositionMap[urn]?.let {
            RemoteResult.Success(it)
        } ?: RemoteResult.Error(IOException("$urn not found"), code = 404)
    }
}
```
