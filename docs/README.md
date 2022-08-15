[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases) 
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-apple)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE) 
## About

Pillarbox is the modern SRG SSR player ecosystem.

## Compatibility

The library is suitable for applications running on android SDK 21 and above. The project is meant to be compiled with the latest Android version.

## Integration

To use the library you need to access Github packages, you need to create a _Personal access tokens_ and use it as credential.

Add this maven repository to you repositories :

```
maven {
    url = uri("https://maven.pkg.github.com/SRGSSR/pillarbox-android")
    credentials {
         username = GITHUB_USER
         password = GITHUB_TOKEN // with read:packages access!
         }
    }
```

And then add one or more library to your module dependancies : 

```
implementation("ch.srgssr.pillarbox:pillarbox-MODULE_NAME:$LATEST_RELEASE_VERSION")
```

Do not set those credentials inside your repository!



## Contributing

If you want to contribute to the project have a look at our [contributing guide](CONTRIBUTING.md).

## License

See the [LICENSE](../LICENSE) file for more information.
