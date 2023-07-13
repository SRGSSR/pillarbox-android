/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.analytics.comscore.ComScoreLabel
import ch.srgssr.pillarbox.analytics.comscore.ComScoreSrg
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
class TestComScoreSrg {

    private lateinit var comScore: ComScoreSrg
    private lateinit var dispatcher: CoroutineDispatcher

    @Before
    fun setup() {
        dispatcher = UnconfinedTestDispatcher()
        val appContext = getInstrumentation().targetContext.applicationContext
        comScore = ComScoreSrg.init(config = TestUtils.analyticsConfig, context = appContext)
        comScore.start(appContext) // Simulate Activity start
    }

    @Test
    fun testSendPageView() = runTest {
        val tracker = PageViewTracking()
        comScore.debugListener = tracker
        val pageTitle = "Title"
        val actualLabels = ArrayList<Map<String, String>>()
        val expectedLabels = listOf(mapOf(Pair(ComScoreLabel.C8.label, pageTitle)))
        val job = launch(dispatcher) {
            tracker.pageViewFlow.take(1).toList(actualLabels)
        }
        comScore.sendPageView(pageTitle)
        Assert.assertEquals(expectedLabels, actualLabels)
        job.cancel()
    }

    private class PageViewTracking : ComScoreSrg.DebugListener {
        val pageViewFlow = MutableSharedFlow<Map<String, String>>(extraBufferCapacity = 1, replay = 1)

        override fun onPageViewSend(labels: Map<String, String>) {
            Assert.assertTrue(pageViewFlow.tryEmit(labels))
        }

    }
}
