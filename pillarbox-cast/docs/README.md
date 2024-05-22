[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Cast module

This module provides helpers to integrate cast with Pillarbox.

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-cast:$LATEST_RELEASE_VERSION")
```

More information can be found on the [top level README](../../docs/README.md)

## Documentation
- [Getting started](#getting-started)
- [Tracking](./MediaItemTracking.md)
- [Google Cast SDK](https://developers.google.com/cast/docs/android_sender)
## Known issues
- Nothing.

## Getting started

### Get the unique instance of CastContext.

```kotlin
val castContext = context.getCastContext()
```
## Display the MediaRouteButton

```kotlin
CastButton(modifier = Modifier)
```
