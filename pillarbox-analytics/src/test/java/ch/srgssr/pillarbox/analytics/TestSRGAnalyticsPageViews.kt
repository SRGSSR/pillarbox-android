/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestSRGAnalyticsPageViews {
    private lateinit var comScore: DummyComscore
    private lateinit var commandersAct: DummyCommandersAct
    private lateinit var analytics: SRGAnalytics.Analytics
    private lateinit var dispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        comScore = DummyComscore()
        commandersAct = DummyCommandersAct()
        analytics = SRGAnalytics.Analytics(comScore = comScore, commandersAct = commandersAct)
        dispatcher = UnconfinedTestDispatcher()
    }

    @Test
    fun testComScorePageView() = runTest {
        val pageView = PageView("Title", listOf("level1", "level2"))
        val actualTitle = ArrayList<String>()
        val job = launch(dispatcher) {
            comScore.pageViewFlow.take(1).toList(actualTitle)
        }
        analytics.sendPageView(pageView)
        Assert.assertEquals(pageView.title, actualTitle.first())
        job.cancel()
    }

    @Test
    fun testCommandersActPageView() = runTest {
        val pageView = PageView("Title", listOf("level1", "level2"))
        val actualPageView = ArrayList<PageView>()
        val job = launch(dispatcher) {
            commandersAct.pageViewFlow.take(1).toList(actualPageView)
        }
        analytics.sendPageView(pageView)
        Assert.assertEquals(pageView, actualPageView.first())
        job.cancel()
    }


    private class DummyComscore : ComScore {
        val pageViewFlow = MutableSharedFlow<String>(extraBufferCapacity = 1, replay = 1)

        override fun sendPageView(title: String) {
            Assert.assertTrue(pageViewFlow.tryEmit(title))
        }
    }

    private class DummyCommandersAct : CommandersAct {
        val pageViewFlow = MutableSharedFlow<PageView>(extraBufferCapacity = 1, replay = 1)

        override fun sendPageView(pageView: PageView) {
            Assert.assertTrue(pageViewFlow.tryEmit(pageView))
        }

        override fun sendEvent(event: Event) {

        }

        override fun sendTcMediaEvent(event: TCMediaEvent) {

        }

    }
}
