# Module pillarbox-cast

Provides a [MediaItemConverter][androidx.media3.cast.MediaItemConverter] implementation that works with SRG SSR receivers.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-core-business-cast:<pillarbox_version>")
```

The main goal of this module is to be able to connect to a SRG SSR receiver with a [PillarboxCastPlayer][ch.srgssr.pillarbox.cast.PillarboxCastPlayer].

## Getting started

```kotlin
val castContext = context.getCastContext()
val player = PillarboxCastPlayer(castContext = castContext, mediaItemConverter = SRGMediaItemConverter())
```

[ch.srgssr.pillarbox.cast.PillarboxCastPlayer]: https://android.pillarbox.ch/api/pillarbox-cast/ch.srgssr.pillarbox.cast/-pillarbox-cast-player/index.html
[androidx.media3.cast.MediaItemConverter]: https://developer.android.com/reference/androidx/media3/cast/MediaItemConverter
