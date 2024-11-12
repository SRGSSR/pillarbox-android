/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

/**
 * Represents the type of media event for Commanders Act's [TCMediaEvent]. This enum defines the various events that can occur during media playback,
 * such as play, pause, end of file, etc.
 *
 * These values are used to track and report user interactions with media content to Commanders Act for analytics purposes.
 */
@Suppress("UndocumentedPublicProperty")
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
