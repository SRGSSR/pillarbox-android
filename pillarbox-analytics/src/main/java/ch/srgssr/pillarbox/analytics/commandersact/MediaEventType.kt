/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

/**
 * CommandersAct [TCMediaEvent] Media event type
 */
enum class MediaEventType {
    Play,
    Pause,
    Eof,
    Stop,
    Seek,
    Pos,
    Uptime;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}
