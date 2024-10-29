# Module pillarbox-core-business

Provides a [`MediaSource`](https://developer.android.com/reference/androidx/media3/exoplayer/source/MediaSource) for handling SRG SSR media URNs to
Pillarbox. It basically converts an integration layer [`MediaComposition`](ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition)
to a playable `MediaSource`.

The supported contents are:

- On demand video and audio.
- Live streams, with and without DRM.
- Token-protected content.
- DRM protected content.
- 360° content (see [`SphericalSurfaceShowcase`](https://github.com/SRGSSR/pillarbox-android/tree/main/pillarbox-demo/src/main/java/ch/srgssr/pillarbox/demo/ui/showcases/misc/SphericalSurfaceShowcase.kt)).

To use this module, add the following dependency to your project's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-core-business:<pillarbox_version>")
```
