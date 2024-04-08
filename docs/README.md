[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)

# Overview

[![Last release](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android?label=Release)](https://github.com/SRGSSR/pillarbox-android/releases)
[![Android min SDK](https://img.shields.io/badge/Android-21%2B-34A853)](https://github.com/SRGSSR/pillarbox-android)
[![Build status](https://img.shields.io/github/actions/workflow/status/SRGSSR/pillarbox-android/build.yml?label=Build)](https://github.com/SRGSSR/pillarbox-android/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/SRGSSR/pillarbox-android?label=License)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

Pillarbox is the modern SRG SSR multimedia player ecosystem, built on top of [AndroiX Media3](https://developer.android.com/media/media3). Pillarbox has been designed with robustness, flexibility, and  efficiency in mind, with full customization of:
- Metadata and asset URL retrieval.
- Asset resource loading, including support for Widevine and PlayReady.
- Analytics and QoS integration.
- User interface layout, in either [Compose](https://developer.android.com/develop/ui/compose/layouts) or [XML `View`s](https://developer.android.com/develop/ui/views/layout/declaring-layout).

Its robust player provides all essential playback features you might expect:
- Audio and video (including 360° videos) playback.
- Support for on-demand and live streams, with and without DVR.
- Integration with the system playback user experience.
- Playlist management, including bidirectional navigation.
- Support for alternative audio tracks, audio description, subtitles, ...
- Multiple instances support.
- Picture-in-picture support.
- Playback speed controls.

In addition, Pillarbox provides support for SRG SSR content by including the `pillarbox-core-business` module (see "Getting started" below).

## Demo

You can easily get your hands on Pillarbox, by running one of the demo applications available in this project: [pillarbox-demo](../pillarbox-demo) for phone/tablet, or [pillarbox-demo-tv](../pillarbox-demo-tv) for TV.

Each application allows you to:
- Try Pillarbox with various media content.
- See how Pillarbox answers various use cases (`pillarbox-demo` only).
- Access a wide range of SRG SSR content.
- Search for a specific SRG SSR content.

## Getting started

### Add the GitHub Packages repository

Pillarbox is deployed to [GitHub Packages](https://github.com/orgs/SRGSSR/packages?repo_name=pillarbox-android). So you need to add the following repository in your Gradle configuration:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/SRGSSR/pillarbox-android")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}
```

> [!TIP]
> You can check the [GitHub documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package) for more information.

### Add the Pillarbox dependencies

In your module's `build.gradle`/`build.gradle.kts` file, add the following dependencies, based on your needs:

```kotlin
// Basic player features
implementation("ch.srgssr.pillarbox:pillarbox-player:<pillarbox_version>")

// Library to handle SRG SSR content through media urns
implementation("ch.srgssr.pillarbox:pillarbox-core-business:<pillarbox_version>")
```

The latest stable version is [![Last release](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android?label=)](https://github.com/SRGSSR/pillarbox-android/releases/latest)

### Enable Java 17

If not already enabled, you also need to turn on Java 17 support in every `build.gradle`/`build.gradle.kts` files using Pillarbox. To do so, add/update the following to/in the `android` section:

```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlinOptions {
    jvmTarget = "17"
}
```

### Integrate Pillarbox

To start using Pillarbox in your project, you can check each module's documentation:
- [`pillarbox-player`](https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-player/docs/README.md)
- [`pillarbox-core-business`](https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-core-business/docs/README.md)
- [`pillarbox-analytics`](https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-analytics/docs/README.md)
- [`pillarbox-ui`](https://github.com/SRGSSR/pillarbox-android/blob/main/pillarbox-ui/docs/README.md)

## Contributing

If you want to contribute to the project have a look at our [contributing guide](CONTRIBUTING.md).

## License

See the [LICENSE](../LICENSE) file for more information.
