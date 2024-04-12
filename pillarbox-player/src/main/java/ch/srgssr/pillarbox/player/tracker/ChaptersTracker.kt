/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.PlayerMessage
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.Chapter
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.player.extension.pillarboxData
import kotlin.time.Duration.Companion.milliseconds

class ChaptersTracker(
    private val pillarboxExoPlayer: PillarboxExoPlayer
) : CurrentMediaItemTagTracker.Callback {

    private val listPlayerMessage = mutableListOf<PlayerMessage>()
    private var listChapter = emptyList<Chapter>()
    private val listener = Listener()

    private var lastChapter: Chapter? = pillarboxExoPlayer.getChapterAtPosition()
        set(value) {
            if (field != value) {
                field = value
                pillarboxExoPlayer.notifyCurrentChapterChanged(field)
            }
        }

    override fun onTagChanged(mediaItem: MediaItem?, tag: Any?) {
        clearPlayerMessage()
        lastChapter = pillarboxExoPlayer.getChapterAtPosition()
        pillarboxExoPlayer.removeListener(listener)
        if (tag == null || mediaItem == null) return
        listChapter = mediaItem.pillarboxData.chapters
        pillarboxExoPlayer.addListener(listener)
        createMessages()
    }

    private fun createMessages() {
        val messageHandler = PlayerMessage.Target { messageType, message ->
            val chapter = message as Chapter
            when (messageType) {
                TYPE_ENTER -> {
                    if (chapter != lastChapter) {
                        lastChapter = chapter
                        Log.i(TAG, "Enter chapter $chapter")
                    }
                }

                TYPE_EXIT -> {
                    Log.i(TAG, "Exit chapter $chapter")
                    lastChapter = null
                }
            }
        }

        listChapter.forEach {
            val messageEnter = pillarboxExoPlayer.createMessage(messageHandler).apply {
                deleteAfterDelivery = false
                looper = pillarboxExoPlayer.applicationLooper
                payload = it
                setPosition(it.start)
                type = TYPE_ENTER
            }
            messageEnter.send()
            listPlayerMessage.add(messageEnter)

            val messageExit = pillarboxExoPlayer.createMessage(messageHandler).apply {
                deleteAfterDelivery = false
                looper = pillarboxExoPlayer.applicationLooper
                payload = it
                setPosition(it.end)
                type = TYPE_EXIT
            }
            messageExit.send()
            listPlayerMessage.add(messageExit)
        }
    }

    private fun clearPlayerMessage() {
        listPlayerMessage.forEach {
            it.cancel()
        }
        listPlayerMessage.clear()
    }

    private inner class Listener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_POSITION_DISCONTINUITY
                )
            ) {
                val currentPosition = player.currentPosition
                val currentChapter = lastChapter?.let {
                    if (currentPosition in it) it else null
                } ?: player.getChapterAtPosition(currentPosition)

                if (currentChapter != lastChapter) {
                    lastChapter?.let {
                        Log.i(TAG, "Exit(${currentPosition.milliseconds}) chapter $it")
                    }
                    lastChapter = currentChapter
                    currentChapter?.let {
                        Log.i(TAG, "Enter(${currentPosition.milliseconds} chapter $it")
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "Chapters"
        private const val TYPE_ENTER = 1
        private const val TYPE_EXIT = 2
    }
}
