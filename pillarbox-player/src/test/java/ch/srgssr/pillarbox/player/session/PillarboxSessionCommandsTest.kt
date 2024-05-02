/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PillarboxSessionCommandsTest {
    @Test
    fun `empty smooth seeking enabled command`() {
        val command = PillarboxSessionCommands.COMMAND_SMOOTH_SEEKING_ENABLED

        assertEquals(PillarboxSessionCommands.SMOOTH_SEEKING_ENABLED, command.customAction)
        assertTrue(command.customExtras.isEmpty)
    }

    @Test
    fun `empty tracker enabled command`() {
        val command = PillarboxSessionCommands.COMMAND_TRACKER_ENABLED

        assertEquals(PillarboxSessionCommands.TRACKER_ENABLED, command.customAction)
        assertTrue(command.customExtras.isEmpty)
    }

    @Test
    fun `empty chapter changed command`() {
        val command = PillarboxSessionCommands.COMMAND_CHAPTER_CHANGED

        assertEquals(PillarboxSessionCommands.CHAPTER_CHANGED, command.customAction)
        assertTrue(command.customExtras.isEmpty)
    }

    @Test
    fun `empty blocked changed command`() {
        val command = PillarboxSessionCommands.COMMAND_BLOCKED_CHANGED

        assertEquals(PillarboxSessionCommands.BLOCKED_CHANGED, command.customAction)
        assertTrue(command.customExtras.isEmpty)
    }

    @Test
    fun `empty credit changed command`() {
        val command = PillarboxSessionCommands.COMMAND_CREDIT_CHANGED

        assertEquals(PillarboxSessionCommands.CREDIT_CHANGED, command.customAction)
        assertTrue(command.customExtras.isEmpty)
    }

    @Test
    fun `set smooth seeking enabled`() {
        val command = PillarboxSessionCommands.setSmoothSeekingEnabled(true)

        assertEquals(PillarboxSessionCommands.SMOOTH_SEEKING_ENABLED, command.customAction)
        assertEquals(1, command.customExtras.size())
        assertTrue(command.customExtras.getBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG))
    }

    @Test
    fun `set smooth seeking disabled`() {
        val command = PillarboxSessionCommands.setSmoothSeekingEnabled(false)

        assertEquals(PillarboxSessionCommands.SMOOTH_SEEKING_ENABLED, command.customAction)
        assertEquals(1, command.customExtras.size())
        assertFalse(command.customExtras.getBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG))
    }

    @Test
    fun `set tracker enabled`() {
        val command = PillarboxSessionCommands.setTrackerEnabled(true)

        assertEquals(PillarboxSessionCommands.TRACKER_ENABLED, command.customAction)
        assertEquals(1, command.customExtras.size())
        assertTrue(command.customExtras.getBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG))
    }

    @Test
    fun `set tracker disabled`() {
        val command = PillarboxSessionCommands.setTrackerEnabled(false)

        assertEquals(PillarboxSessionCommands.TRACKER_ENABLED, command.customAction)
        assertEquals(1, command.customExtras.size())
        assertFalse(command.customExtras.getBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG))
    }
}
