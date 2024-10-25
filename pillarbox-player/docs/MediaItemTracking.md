# Media item tracking

To enable media item tracking, the corresponding [`Asset`][asset] must be filled with [`FactoryData`][factory-data] for each kind of [`MediaItemTracker`][media-item-tracker]. 
`PillarboxPlayer` 
takes care of starting and stopping [`MediaItemTracker`][media-item-tracker]s when needed.

## Create a `MediaItemTracker`

```kotlin
data class MyTrackingData(val data: String)

class DemoMediaItemTracker : MediaItemTracker<MyTrackingData> {
    override fun start(player: ExoPlayer, initialData: MyTrackingData) {
        // ....
    }
    
    override fun stop(player: ExoPlayer) {
        // ....
    }

    class Factory : MediaItemTracker.Factory<MyTrackingData>() {
        override fun create(): DemoMediaItemTracker {
            return DemoMediaItemTracker()
        }
    }
}
```

## Set up an `Asset` with a `MediaItemTracker`

`MediaItemTracker` can only be configured inside an [`AssetLoader`][asset-loader].

```kotlin
val trackerData = MutableMediaItemTrackerData()
trackerData[KEY] = FactoryData(DemoMediaItemTracker.Factory(), MyTrackingData("Data1"))

val asset = Asset(
    trackerData = trackerData,
    // ...
)
```

A full example with an implementation of a [`AssetLoader`][asset-loader]:

```kotlin
class DemoAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return true
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val trackerData = MutableMediaItemTrackerData()
        trackerData[key] = FactoryData(DemoMediaItemTracker.Factory(), DemoTrackerData("Data1"))

        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(mediaItem),
            trackersData = trackerData.toMediaItemTrackerData(),
        )
    }
}
```

Finally, add the [`AssetLoader`][asset-loader] to the [`PillarboxPlayer`][pillarbox-player]:

```kotlin
val player = PillarboxExoPlayer(context) {
    +DemoAssetLoader(context)
}
```

## Toggle tracking

You can enable or disable tracking during execution with `player.trackingEnabled`. It will start or stop all the provided `MediaItemTracker`. By 
default, tracking is enabled.

```kotlin
player.trackingEnabled = false
```

[media-item-tracker]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/tracker/MediaItemTracker.kt
[factory-data]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/tracker/MediaItemTrackerData.kt
[asset]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/asset/Asset.kt
[asset-loader]: https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/asset/AssetLoader.kt
[pillarbox-player]:https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/src/main/java/ch/srgssr/pillarbox/player/PillarboxPlayer.kt
