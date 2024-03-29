[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![Build](https://github.com/SRGSSR/pillarbox-android/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/SRGSSR/pillarbox-android/actions/workflows/build.yml)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

## About

Pillarbox is the modern SRG SSR player ecosystem. It is build on top of [Media3 Exoplayer](https://developer.android.com/media/media3/exoplayer). So if you know how to work with
_Exoplayer_, then you know how to work with _Pillarbox_.

## Compatibility

The library is suitable for applications running on android SDK 21 and above. The project is meant to be compiled with the latest Android version.

## Integration

To use the library inside your project, you need to access Github packages, you need to create a _Personal access tokens_ and use it as credential.

### Add maven repository to you repositories

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

### Add Pillarbox module dependencies

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-player:$LATEST_RELEASE_VERSION")
// Library to handle SRG content through media urns
implementation("ch.srgssr.pillarbox:pillarbox-core-business:$LATEST_RELEASE_VERSION")
```

Get the [latest version](https://github.com/SRGSSR/pillarbox-android/releases/latest)

### Turn on Java 17 support

If not enabled already, you also need to turn on Java 17 support in every
`build.gradle`/`build.gradle.kts` files, by adding/updating the following to the
`android` section:

```gradle
compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlinOptions {
  jvmTarget = "17"
}
```

## Contributing

If you want to contribute to the project have a look at our [contributing guide](CONTRIBUTING.md).

## License

See the [LICENSE](../LICENSE) file for more information.
