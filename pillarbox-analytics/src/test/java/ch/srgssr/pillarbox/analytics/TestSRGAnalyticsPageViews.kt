/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import ch.srgssr.pillarbox.analytics.comscore.ComScorePageView
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
        val commandersActPageView = CommandersActPageView(name = "Title", type = "Type", levels = listOf("level1", "level2"))
        val comscorePageView = ComScorePageView("Title")
        val actualTitle = ArrayList<ComScorePageView>()
        val job = launch(dispatcher) {
            comScore.pageViewFlow.take(1).toList(actualTitle)
        }
        analytics.sendPageView(commandersAct = commandersActPageView, comScore = comscorePageView)
        Assert.assertEquals(comscorePageView, actualTitle.first())
        job.cancel()
    }

    @Test
    fun testCommandersActPageView() = runTest {
        val commandersActPageView = CommandersActPageView(name = "Title", type = "Type", levels = listOf("level1", "level2"))
        val comscorePageView = ComScorePageView("Title")
        val actualPageView = ArrayList<CommandersActPageView>()
        val job = launch(dispatcher) {
            commandersAct.pageViewFlow.take(1).toList(actualPageView)
        }
        analytics.sendPageView(commandersAct = commandersActPageView, comScore = comscorePageView)
        Assert.assertEquals(commandersActPageView, actualPageView.first())
        job.cancel()
    }


    private class DummyComscore : ComScore {
        val pageViewFlow = MutableSharedFlow<ComScorePageView>(extraBufferCapacity = 1, replay = 1)

        override fun sendPageView(pageView: ComScorePageView) {
            Assert.assertTrue(pageViewFlow.tryEmit(pageView))
        }

        override fun putPersistentLabels(labels: Map<String, String>?) {
            // Nothing
        }

        override fun removePersistentLabel(label: String) {
            // Nothing
        }

        override fun getPersistentLabel(label: String): String? {
            // Nothing
            return null
        }
    }

    private class DummyCommandersAct : CommandersAct {
        val pageViewFlow = MutableSharedFlow<CommandersActPageView>(extraBufferCapacity = 1, replay = 1)

        override fun sendPageView(pageView: CommandersActPageView) {
            Assert.assertTrue(pageViewFlow.tryEmit(pageView))
        }

        override fun sendEvent(event: CommandersActEvent) {

        }

        override fun sendTcMediaEvent(event: TCMediaEvent) {

        }

    }
}
