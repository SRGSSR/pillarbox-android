[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

## Warning

The Pillarbox project is still under heavy development, any implementation, information or documentation found on this repository may change at 
anytime.

## About

Pillarbox is the modern SRG SSR player ecosystem. It is build on top of [Exoplayer](https://exoplayer.dev/). So if you know how to integrate _
Exoplayer_, then you know how to integrate _Pillarbox_.

It rely heavily on the version of [Media3](https://developer.android.com/jetpack/androidx/releases/media3) that integrate Exoplayer to the Android
Media framework.

## Compatibility

The library is suitable for applications running on android SDK 21 and above. The project is meant to be compiled with the latest Android version.

## Integration

To use the library inside your project, you need to access Github packages, you need to create a _Personal access tokens_ and use it as credential.

### 1 Add maven repository to you repositories

```gradle
maven {
    url = uri("https://maven.pkg.github.com/SRGSSR/pillarbox-android")
    credentials {
         username = GITHUB_USER
         password = GITHUB_TOKEN // with read:packages access!
         }
    }
```

Do not set those credentials inside your repository!

### 2 Add Pillarbox module dependencies

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-player:$LATEST_RELEASE_VERSION")
// Library to handle SRG content threw media urns
implementation("ch.srgssr.pillarbox:pillarbox-core-business:$LATEST_RELEASE_VERSION")
```

Get the [latest version](https://github.com/SRGSSR/pillarbox-android/releases/latest)

### 3 Turn on Java 8 support

If not enabled already, you also need to turn on Java 8 support in all
`build.gradle`, by adding the following to the
`android` section:

```gradle
compileOptions {
  targetCompatibility JavaVersion.VERSION_1_8
}
```

## Contributing

If you want to contribute to the project have a look at our [contributing guide](CONTRIBUTING.md).

## License

See the [LICENSE](../LICENSE) file for more information.
