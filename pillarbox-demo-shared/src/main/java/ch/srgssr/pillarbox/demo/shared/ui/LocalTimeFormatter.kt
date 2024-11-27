/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui

import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * Local time formatter that format [LocalTime] to HH:mm:ss.
 */
val localTimeFormatter by lazy {
    LocalTime.Format {
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
        char(':')
        second(Padding.ZERO)
    }
}
