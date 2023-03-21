# Media item tracking

To enable media item tracking, you need 3 classes

- MediaItemTracker
- MediaItemTrackerData
- MediaItemTrackerProvider

## Getting started

### Create a MediaItemTracker

```kotlin
class DemoMediaItemTracker : MediaItemTracker {
    override fun start(player: ExoPlayer) {
        player.addAnalyticsListener(eventLogger)
    }

    override fun stop(player: ExoPlayer) {
        player.removeAnalyticsListener(eventLogger)
    }

    override fun update(data: Any) {
        // Do something with data
    }

    class Factory : MediaItemTracker.Factory() {
        override fun create(): DemoMediaItemTracker {
            return DemoMediaItemTracker()
        }
    }
}
```

### Append MediaItemTrackerData to MediaItem at creation

Add `MediaItemTrackerData` to a `MediaItem` only when the Uri is known. To add data for a `MediaItemTracker` you have to retrieve
a `MediaItemTrackerData`
from given `MediaItem` and put data in it. The data can be null if no data is required.

```kotlin
class DefaultMediaItemSource : MediaItemSource {
    override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
        val trackerData = mediaItem.getMediaItemTrackerData()
        trackerData.putData(DemoMediaItemTracker::class.java, MyTrackingData())
        return mediaItem.buildUpon().setTrackerData(trackerData).build()
    }
}
```

### Create MediaItemTrackerProvider or append to MediaItemTrackerRepository

```kotlin
class DemoTrackerProvider : MediaItemTrackerProvider {
    override fun getMediaItemTrackerFactory(trackerClass: Class<*>): MediaItemTracker.Factory {
        return DemoMediaItemTracker.Factory()
    }
}
```

If you have multiple `MediaItemTracker` you may choose `MediaItemTrackerRepository`.

```kotlin
val mediaItemTrackerProvider = MediaItemTrackerRepository().apply {
    append(DefaultMediaItemSource::class.java, DefaultMediaItemSource.Factory())
}
```

### Inject into the Player

```kotlin
val player = PillarboxPlayer(context = context, mediaItemSource = DefaultMediaItemSource(), mediaItemTrackerProvider = DemoTrackerProvider())
// Make player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play()
val itemToPlay = MediaItem.fromUri("https://sample.com/sample.mp4")
player.setMediaItem(itemToPlay)
```


