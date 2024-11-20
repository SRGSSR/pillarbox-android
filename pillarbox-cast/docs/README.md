# Module pillarbox-cast

Provides helpers to integrate Cast with Pillarbox.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-cast:<pillarbox_version>")
```

## Getting started

### Get the `CastContext` instance

```kotlin
val castContext = context.getCastContext()
```

### Display a Cast button

```kotlin
CastButton(modifier = Modifier)
```

## Additional resources

- [Google Cast SDK](https://developers.google.com/cast/docs/android_sender)
