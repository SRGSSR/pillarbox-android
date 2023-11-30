[![Pillarbox logo](https://github.com/SRGSSR/pillarbox-apple/blob/main/docs/README-images/logo.jpg)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub releases](https://img.shields.io/github/v/release/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/releases)
[![android](https://img.shields.io/badge/android-21+-green)](https://github.com/SRGSSR/pillarbox-android)
[![GitHub license](https://img.shields.io/github/license/SRGSSR/pillarbox-android)](https://github.com/SRGSSR/pillarbox-android/blob/main/LICENSE)

# Pillarbox Analytics module

Provides SRG SSR implementation for CommandersAct and ComScore to send page view events and custom events.

Custom events are supported only with CommandersAct!

## Integration

```gradle
implementation("ch.srgssr.pillarbox:pillarbox-analytics:$LATEST_RELEASE_VERSION")
```

## Getting started

### Configuration and create

Before using `SRGAnalytics` make sure to call `SRGAnalytics.init` first, otherwise it can lead to an undefined behavior.
It is strongly recommended to call the initializer inside your `Application.onCreate` method.

```kotlin
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        val config = AnalyticsConfig(
            vendor = AnalyticsConfig.Vendor.SRG,
            nonLocalizedApplicationName = "PillarboxDemo",
            appSiteName = "pillarbox-demo-android",
            sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
        )
        
        initSRGAnalytics(config = config)
    }
}
```

### Handle user consent
User consent can be configured at initialization:

```kotlin
val initialUserConsent = UserConsent(
    comScore = ComScoreUserConsent.UNKNOWN,
    commandersActConsentServices = emptyList()
)

val config = AnalyticsConfig(
    vendor = AnalyticsConfig.Vendor.SRG,
    nonLocalizedApplicationName = "PillarboxDemo",
    appSiteName = "pillarbox-demo-android",
    sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG,
    userConsent = initialUserConsent
)

initSRGAnalytics(config = config)
```
Update user consent at runtime:
```kotlin
val updatedUserConsent = UserConsent(
    comScore = ComScoreUserConsent.DECLINED, //or ComScoreUserConsent.ACCEPTED
    commandersActConsentServices = listOf("service1_id", "service2_id")
)
SRGAnalytics.setUserConsent(updatedUserConsent)
```
User consent values will be updated with the next analytics event.

### Send page view

To send a page view use `SRGAnalytics.sendPageView`. It will trigger a CommandersActs and a Comscore page view event directly.

```kotlin
val commandersActEvent = CommandersActPageView(name = "main", type = "tbd", levels = listOf("app", "pillarbox"))
val comScoreEvent = ComScorePageView(name = "main")
SRGAnalytics.sendPageView(commandersAct = commandersActEvent, comScore = comScoreEvent)
```

In the case of a multi pane view each pane view can send a page view. It is useful then reusing view from single pane view inside the multi pane view.

For Android Auto application it is not recommended to send page view.

### Send event

Events are application event the analytics team of the application want to track. It could be click event, user choice etc..

```kotlin
SRGAnalytics.sendEvent(CommandersActEvent(name = "event"))
```
