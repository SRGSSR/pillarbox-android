/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import ch.srgssr.pillarbox.analytics.comscore.ComScorePageView
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlin.test.BeforeTest
import kotlin.test.Test

class SRGAnalyticsTest {
    private lateinit var comScore: ComScore
    private lateinit var commandersAct: CommandersAct
    private lateinit var analytics: SRGAnalytics.Analytics

    @BeforeTest
    fun setup() {
        comScore = mockk(relaxed = true)
        commandersAct = mockk(relaxed = true)
        analytics = SRGAnalytics.Analytics(
            comScore = comScore,
            commandersAct = commandersAct,
        )
    }

    @Test
    fun `sendPageView invokes comScore and commandersAct`() {
        val commandersActPageView = CommandersActPageView(
            name = "Title",
            type = "Type",
            levels = listOf("level1", "level2"),
        )
        val comScorePageView = ComScorePageView("Title")

        analytics.sendPageView(
            commandersAct = commandersActPageView,
            comScore = comScorePageView,
        )

        verifySequence {
            commandersAct.sendPageView(commandersActPageView)
            comScore.sendPageView(comScorePageView)
        }
    }

    @Test
    fun `sendEvent invokes only commandersAct`() {
        val commandersActEvent = CommandersActEvent(name = "name")

        analytics.sendEvent(commandersActEvent)

        verifySequence {
            commandersAct.sendEvent(commandersActEvent)
        }
        verify {
            comScore wasNot Called
        }
    }

    @Test
    fun `putPersistentLabels invokes comScore and commandersAct`() {
        val commandersActLabels = mapOf("key1" to "value1")
        val comScoreLabels = mapOf("key2" to "value2")

        analytics.putPersistentLabels(
            commandersActLabels = commandersActLabels,
            comScoreLabels = comScoreLabels,
        )

        verifySequence {
            comScore.putPersistentLabels(comScoreLabels)
            commandersAct.putPermanentData(commandersActLabels)
        }
    }

    @Test
    fun `removePersistentLabel invokes comScore and commandersAct`() {
        val label = "label"

        analytics.removePersistentLabel(label = label)

        verifySequence {
            comScore.removePersistentLabel(label)
            commandersAct.removePermanentData(label)
        }
    }

    @Test
    fun `getComScorePersistentLabel invokes comScore`() {
        val label = "label"

        analytics.getComScorePersistentLabel(label = label)

        verifySequence {
            comScore.getPersistentLabel(label)
        }
        verify {
            commandersAct wasNot Called
        }
    }

    @Test
    fun `getCommandersActPermanentData invokes commandersAct`() {
        val label = "label"

        analytics.getCommandersActPermanentData(label = label)

        verifySequence {
            commandersAct.getPermanentDataLabel(label)
        }
        verify {
            comScore wasNot Called
        }
    }

    @Test
    fun `setUserConsent invokes comScore and commandersAct`() {
        val userConsent = UserConsent()

        analytics.setUserConsent(userConsent)

        verifySequence {
            comScore.setUserConsent(userConsent.comScore)
            commandersAct.setConsentServices(userConsent.commandersActConsentServices)
        }
    }
}
