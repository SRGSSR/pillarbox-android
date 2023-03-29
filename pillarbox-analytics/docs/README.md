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

```kotlin
val analyticsConfig = AnalyticsConfig(
    distributor = AnalyticsConfig.BuDistributor.SRG,
    virtualSite = VIRTUAL_SITE,
    nonLocalizedApplicationName = "PillarboxDemo"
    )
val config = SRGAnalytics.Config(analyticsConfig = analyticsConfig, 
    commandersAct CommandersAct.Config.SRG_DEBUG)

val analytics = SRGAnalytics(appContext = appContext, config = config)
```

### Send page view

```kotlin
analytics.sendPageViewEvent(PageEvent(title = "main", levels = arrayOf("app", "pillarbox")))
```

### Send event

```kotlin
analytics.sendEvent(Event(name = "event"))
```

### Custom labels

Each types of events can have custom labels.

```kotlin
val labels = CustomLabels.Builder()
        .putCommandersActLabel("tc_key1","value1")
        .putComScoreLabel("srg_key1","value1")
        .putBothLabel("common_key","common")
        .build()
val pageEvent = PageEvent(title = "title", customLabels = labels)
```
