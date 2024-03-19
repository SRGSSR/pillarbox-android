# Media item tracking

To enable media item tracking, you need 3 classes

- MediaItemTracker
- MediaItemTrackerData
- MediaItemTrackerProvider

## Getting started

`MediaItemTracker`s are stared and stopped when a `MediaItem` with `MediaItemTracker` changing is current state.
`PillarboxPlayer` takes care of starting and stopping `MediaItemTracker`s when needed.

### Create a MediaItemTracker

```kotlin
data class MyTrackingData(val data: String)

class DemoMediaItemTracker : MediaItemTracker {
    override fun start(player: ExoPlayer, initialData: Any?) {
        val data = initialData as MyTrackingData
        // ....
    }

    override fun stop(player: ExoPlayer, reason: StopReason, positionMs: Long) {
        // ....
    }

    class Factory : MediaItemTracker.Factory() {
        override fun create(): DemoMediaItemTracker {
            return DemoMediaItemTracker()
        }
    }
}
```

### Append MediaItemTrackerData to MediaItem at creation

Add `MediaItemTrackerData` to a `MediaItem` only when the uri is known. To add data for a `MediaItemTracker` you have to retrieve
a `MediaItemTrackerData` from a given `MediaItem` and put data in it. The data can be `null` if no data is required.

```kotlin
val trackerData = MediaItemTrackerData.Builder()
    .putData(DemoMediaItemTracker::class.java, MyTrackingData())
    .build()
val itemToPlay = MediaItem.Builder()
    .setUri("https://sample.com/sample.mp4")
    .setTrackerData(trackerData)
    .build()
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
    append(DemoMediaItemTracker::class.java, DemoMediaItemTracker.Factory())
    append(DemoMediaItemTracker2::class.java, DemoMediaItemTracker2.Factory())
}
```

### Inject into the `PillarboxPlayer`

```kotlin
val player = PillarboxPlayer(context = context, mediaItemTrackerProvider = mediaItemTrackerProvider)
// Make player ready to play content
player.prepare()
// Will start playback when a MediaItem is ready to play
player.play()
val itemToPlay = MediaItem.fromUri("https://sample.com/sample.mp4")
player.setMediaItem(itemToPlay)
```

### Toggle tracking

You can disable or enable tracking during execution with `player.trackingEnable`. It will start or stop all `MediaItemTracker` provided.

```kotlin
player.trackingEnabled = false
```

