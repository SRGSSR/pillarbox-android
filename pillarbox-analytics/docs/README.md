# Module pillarbox-analytics

Provides SRG SSR implementation for [Commanders Act](https://www.commandersact.com/) and [ComScore](https://comscore.com/) to send page view events
and custom events.

**Note:** custom events are only supported with Commanders Act.

## Integration

To use this module, add the following dependency to your module's `build.gradle`/`build.gradle.kts` file:

```kotlin
implementation("ch.srgssr.pillarbox:pillarbox-analytics:<pillarbox_version>")
```

## Getting started

### Configure analytics

Before using any functionality, [SRGAnalytics][ch.srgssr.pillarbox.analytics.SRGAnalytics] must be initialized in your
[Application][android.app.Application]'s [onCreate()][android.app.Application.onCreate] method using either the
[initSRGAnalytics()][ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics] or the
[SRGAnalytics.init()][ch.srgssr.pillarbox.analytics.SRGAnalytics.init] method and providing an
[AnalyticsConfig][ch.srgssr.pillarbox.analytics.AnalyticsConfig] instance.

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = AnalyticsConfig(
            vendor = AnalyticsConfig.Vendor.SRG,
            appSiteName = "Your AppSiteName here",
            sourceKey = SourceKey.DEVELOPMENT,
            nonLocalizedApplicationName = "Your non-localized AppSiteName here",
        )

        initSRGAnalytics(config)
        // or
        SRGAnalytics.init(this, config)
    }
}
```

### Handle user consent

User consent can be configured when initializing analytics in your [Application][android.app.Application]'s
[onCreate()][android.app.Application.onCreate] method:

```kotlin
val userConsent = UserConsent(
    comScore = ComScoreUserConsent.UNKNOWN,
    commandersActConsentServices = emptyList(),
)

val config = AnalyticsConfig(
    vendor = AnalyticsConfig.Vendor.SRG,
    appSiteName = "Your AppSiteName here",
    sourceKey = SourceKey.DEVELOPMENT,
    nonLocalizedApplicationName = "Your non-localized AppSiteName here",
    userConsent = userConsent,
)

initSRGAnalytics(config)
```

Or it can be updated at any time using the following code snippet:

```kotlin
val userConsent = UserConsent(
    comScore = ComScoreUserConsent.DECLINED,
    commandersActConsentServices = listOf("service1_id", "service2_id"),
)

SRGAnalytics.setUserConsent(userConsent)
```

The updated values will be sent with the next analytics event.

### Send page view

To send a page view, use [SRGAnalytics.sendPageView()][ch.srgssr.pillarbox.analytics.SRGAnalytics.sendPageView]. It will send the event only to
Commanders Act.

```kotlin
val commandersActPageView = CommandersActPageView(
    name = "page_name",
    type = "page_type",
    levels = listOf("level1", "level2"),
)

SRGAnalytics.sendPageView(
    commandersAct = commandersActPageView
)
```

In the case of a multi-pane view, each pane view can send a page view. It is useful when reusing views from a single pane view inside the multi-pane
view.
For Android Auto applications, it is not recommended to send page view.

### Send event

Events are application events that the analytics team wants to track. It could be a click event, a user choice, etc...

```kotlin
val commandersActEvent = CommandersActEvent(name = "event")

SRGAnalytics.sendEvent(commandersActEvent)
```

# Package ch.srgssr.pillarbox.analytics

Top-level entry point for managing analytics in Pillarbox for SRG SSR applications.

# Package ch.srgssr.pillarbox.analytics.commandersact

Commanders Act specific classes.

# Package ch.srgssr.pillarbox.analytics.comscore

ComScore specific classes.

[android.app.Application]: https://developer.android.com/reference/kotlin/android/app/Application.html
[android.app.Application.onCreate]: https://developer.android.com/reference/kotlin/android/app/Application.html#oncreate
[ch.srgssr.pillarbox.analytics.AnalyticsConfig]: https://android.pillarbox.ch/api/pillarbox-analytics/ch.srgssr.pillarbox.analytics/-analytics-config/index.html
[ch.srgssr.pillarbox.analytics.SRGAnalytics]: https://android.pillarbox.ch/api/pillarbox-analytics/ch.srgssr.pillarbox.analytics/-s-r-g-analytics/index.html
[ch.srgssr.pillarbox.analytics.SRGAnalytics.init]: https://android.pillarbox.ch/api/pillarbox-analytics/ch.srgssr.pillarbox.analytics/-s-r-g-analytics/init.html
[ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics]: https://android.pillarbox.ch/api/pillarbox-analytics/ch.srgssr.pillarbox.analytics/-s-r-g-analytics/init-s-r-g-analytics.html
[ch.srgssr.pillarbox.analytics.SRGAnalytics.sendPageView]: https://android.pillarbox.ch/api/pillarbox-analytics/ch.srgssr.pillarbox.analytics/-s-r-g-analytics/send-page-view.html
