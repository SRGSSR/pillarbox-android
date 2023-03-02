/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

/**
 * Player disposable with a dispose method to clear events listening.
 */
interface PlayerDisposable {
    /**
     * Dispose
     * Should be called in ViewModel.onCleared or in a DisposableEffect if using compose.
     */
    fun dispose()
}
