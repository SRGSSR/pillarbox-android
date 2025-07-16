/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Player
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.image.ImageOutput
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.asset.timeRange.firstOrNullAtPosition

/**
 * Pillarbox [Player] interface extension.
 */
@Suppress("ComplexInterface")
interface PillarboxPlayer : Player {

    /**
     * A listener for events specific to Pillarbox.
     */
    interface Listener : Player.Listener {

        /**
         * Called when the smooth seeking enabled state changes.
         *
         * @param smoothSeekingEnabled Whether smooth seeking is enabled.
         */
        fun onSmoothSeekingEnabledChanged(smoothSeekingEnabled: Boolean) {}

        /**
         * Called when the tracking state changes.
         *
         * @param trackingEnabled Whether tracking is enabled.
         */
        fun onTrackingEnabledChanged(trackingEnabled: Boolean) {}

        /**
         * Called when the current chapter changes. This can occur due to several reasons:
         *
         * - **Automatic playback:** the player's position progresses naturally during playback and enters a new chapter.
         * - **Seeking:** the user manually seeks to a new position within the content, landing within a different chapter.
         * - **Playlist change:** the current playlist is changed, potentially resulting in a different set of chapters and a new active chapter.
         *
         * @param chapter The currently active [Chapter]. This will be `null` if the current playback position is not within any defined chapter.
         */
        fun onChapterChanged(chapter: Chapter?) {}

        /**
         * Called when the player reaches a blocked time range.
         *
         * @param blockedTimeRange The [BlockedTimeRange] representing the time range that the player has reached.
         */
        fun onBlockedTimeRangeReached(blockedTimeRange: BlockedTimeRange) {}

        /**
         * Called when the current credit changes. This can occur due to several reasons:
         *
         * - **Automatic playback:** the player's position progresses naturally during playback and enters a new chapter.
         * - **Seeking:** the user manually seeks to a new position within the content, landing within a different chapter.
         * - **Playlist change:** the current playlist is changed, potentially resulting in a different set of chapters and a new active chapter.
         *
         * @param credit The currently active [Credit]. This will be `null` if the current playback position is not within any defined credit.
         */
        fun onCreditChanged(credit: Credit?) {}

        /**
         * Called when the Pillarbox metadata changes.
         */
        fun onPillarboxMetadataChanged(pillarboxMetadata: PillarboxMetadata) {}
    }

    /**
     * Controls whether smooth seeking behavior is enabled.
     *
     * When this property is `true`, subsequent seek events are sent only after the current seek operation is completed.
     *
     * For optimal results, it is important to:
     * 1. Pause the player during seek operations.
     * 2. Set the player's seek parameters to [SeekParameters.CLOSEST_SYNC] using [setSeekParameters].
     */
    var smoothSeekingEnabled: Boolean

    /**
     * Controls whether media item tracking is enabled.
     */
    var trackingEnabled: Boolean

    /**
     * Whether [setImageOutput] is supported.
     */
    val isImageOutputAvailable: Boolean

    /**
     * Whether the metrics are available.
     * Even if this is `true`, [getCurrentMetrics] may return `null`.
     */
    val isMetricsAvailable: Boolean

    /**
     * Whether [setSeekParameters] is supported.
     */
    val isSeekParametersAvailable: Boolean

    /**
     * The current [PillarboxMetadata] for the currently playing media item.
     */
    val currentPillarboxMetadata: PillarboxMetadata

    /**
     * Returns the chapters for the currently playing media item.
     *
     * @return A list of [Chapter] for the currently playing media item, or an empty list if there are no chapters or no current media item.
     */
    val currentChapters: List<Chapter>
        get() = currentPillarboxMetadata.chapters

    /**
     * Returns the credits for the currently playing media item.
     *
     * @return A list of [Credit] for the currently playing media item, or an empty list if there are no credits or no current media item.
     */
    val currentCredits: List<Credit>
        get() = currentPillarboxMetadata.credits

    /**
     * Returns the blocked time ranges for the currently playing media item.
     *
     * @return A list of [BlockedTimeRange] for the currently playing media item,
     * or an empty list if there are no blocked time ranges or no current media item.
     */
    val currentBlockedTimeRanges: List<BlockedTimeRange>
        get() = currentPillarboxMetadata.blockedTimeRanges

    /**
     * Retrieves the [Chapter] that encompasses the given position in the media playback.
     *
     * @param positionMs The position in the media playback, in milliseconds.
     * @return The [Chapter] at the given position, or `null` if no chapter is found at that position.
     */
    fun getChapterAtPosition(positionMs: Long = currentPosition): Chapter? {
        return currentChapters.firstOrNullAtPosition(positionMs)
    }

    /**
     * Retrieves the [Credit] that encompasses the given position in the media playback.
     *
     * @param positionMs The position in the media playback, in milliseconds.
     * @return The [Credit] at the given position, or `null` if no credit is found at that position.
     */
    fun getCreditAtPosition(positionMs: Long = currentPosition): Credit? {
        return currentCredits.firstOrNullAtPosition(positionMs)
    }

    /**
     * Retrieves the [BlockedTimeRange] that encompasses the given position in the media playback.
     *
     * @param positionMs The position in the media playback, in milliseconds.
     * @return The [BlockedTimeRange] at the given position, or `null` if no blocked time range is found at that position.
     */
    fun getBlockedTimeRangeAtPosition(positionMs: Long = currentPosition): BlockedTimeRange? {
        return currentBlockedTimeRanges.firstOrNullAtPosition(positionMs)
    }

    /**
     * Get current metrics
     * @return `null` if there is no current metrics.
     */
    fun getCurrentMetrics(): PlaybackMetrics? = null

    /**
     * @return The current playback session id if any.
     */
    fun getCurrentPlaybackSessionId(): String? = getCurrentMetrics()?.sessionId

    /**
     * Sets the parameters that control how seek operations are performed.
     *
     * This method must only be called if [isSeekParametersAvailable] returns `true`.
     *
     * @param seekParameters The seek parameters, or `null` to use the defaults.
     */
    fun setSeekParameters(seekParameters: SeekParameters?)

    /**
     * @return the currently active [SeekParameters] of the player when [isSeekParametersAvailable] is `true`.
     * */
    fun getSeekParameters(): SeekParameters

    /**
     * Sets the [ImageOutput] where rendered images will be forwarded.
     * This method does nothing if the player doesn't render anything.
     * @param imageOutput The [ImageOutput] to forward image to.
     * @see androidx.media3.exoplayer.ExoPlayer.setImageOutput
     */
    fun setImageOutput(imageOutput: ImageOutput?)

    /**
     * Adds a Pillarbox [Listener], which will be notified of events specific to Pillarbox in addition to the standard player events.
     */
    fun addListener(listener: Listener)

    /**
     * Removes a [Listener] previously added with [addListener].
     */
    fun removeListener(listener: Listener)



    companion object {

        /**
         * Event indicating that a blocked time range has been reached.
         */
        const val EVENT_BLOCKED_TIME_RANGE_REACHED = 100

        /**
         * Event indicating that the current [Chapter] has changed.
         */
        const val EVENT_CHAPTER_CHANGED = 101

        /**
         * Event indicating that the current [Credit] has changed.
         */
        const val EVENT_CREDIT_CHANGED = 102

        /**
         * Event indicating that the media item [tracking state][trackingEnabled] has changed.
         */
        const val EVENT_TRACKING_ENABLED_CHANGED = 103

        /**
         * Event indicating that the [smooth seeking state][smoothSeekingEnabled] has changed.
         */
        const val EVENT_SMOOTH_SEEKING_ENABLED_CHANGED = 104

        /**
         * Event indicating that the [Pillarbox metadata][currentPillarboxMetadata] has changed.
         */
        const val EVENT_PILLARBOX_METADATA_CHANGED = 105
    }
}
