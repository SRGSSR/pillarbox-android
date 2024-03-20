/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.asset.ChapterInterval
import ch.srgssr.pillarbox.player.getCurrentChapters

class ChapterTracker : MediaItemTracker {

    private lateinit var chapters: List<ChapterInterval>
    private val playerMessages = mutableListOf<PlayerMessage>()
    private var currentChapter: ChapterInterval? = null
    private val listener = ComponentListener()
    override fun start(player: ExoPlayer, initialData: Any?) {
        chapters = player.getCurrentChapters()
        player.addListener(listener)
        Log.d("Coucou", "Chapter trackers start with $chapters")
        /*
        * Message a send at the given position when the player position is automatically reached
        * The message is send again if player reach again the same position
        */
        val chapterEvent = PlayerMessage.Target { messageType, message ->
            val chapter = message as ChapterInterval
            when (messageType) {
                MESSAGE_CHAPTER_ENTER -> {
                    Log.d("Coucou", "Enter chapter ${chapter.id} / ${chapter.mediaMetadata.title}")
                    currentChapter = chapter
                }

                MESSAGE_CHAPTER_EXIT -> {
                    Log.d("Coucou", "Exit chapter ${chapter.id} / ${chapter.mediaMetadata.title}")
                    currentChapter = null
                }

                else -> {
                    Log.d("Coucou", "Unknow message type $messageType message=$message")
                }
            }
        }

        // Setup player messages start and end messages
        for (chapter in chapters) {
            val enterChapterMessage = player.createMessage(chapterEvent)
                .setLooper(player.applicationLooper)
                .setPayload(chapter)
                .setType(MESSAGE_CHAPTER_ENTER)
                .setDeleteAfterDelivery(false)
                .setPosition(chapter.start)
                .send()
            playerMessages.add(enterChapterMessage)
            val exitChapterMessage = player.createMessage(chapterEvent)
                .setLooper(player.applicationLooper)
                .setPayload(chapter)
                .setType(MESSAGE_CHAPTER_EXIT)
                .setDeleteAfterDelivery(false)
                .setPosition(chapter.end)
                .send()
            playerMessages.add(exitChapterMessage)
        }
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        player.removeListener(listener)
        clearPlayerMessages()
        currentChapter = null
    }

    private fun clearPlayerMessages() {
        for (message in playerMessages) {
            message.cancel()
        }
        playerMessages.clear()
    }

    private inner class ComponentListener : Player.Listener {

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            when (reason) {
                Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT, Player.DISCONTINUITY_REASON_SILENCE_SKIP -> {
                    val to = newPosition.positionMs
                    if (currentChapter != null && currentChapter!!.contain(to)) return
                    var selectedChapter = chapters.firstOrNull { it.contain(to) }
                    Log.d("Coucou", "chapter changes $currentChapter to ${selectedChapter?.mediaMetadata?.title}")
                    currentChapter = selectedChapter
                }
            }
        }
    }

    companion object {
        private const val MESSAGE_CHAPTER_ENTER = 1
        private const val MESSAGE_CHAPTER_EXIT = 2
    }
}
