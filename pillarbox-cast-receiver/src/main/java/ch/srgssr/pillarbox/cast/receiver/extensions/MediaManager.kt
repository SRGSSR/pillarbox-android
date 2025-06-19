/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver.extensions

import android.support.v4.media.session.MediaSessionCompat
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import com.google.android.gms.cast.tv.media.MediaManager

/**
 * Set the session token from the provided [PillarboxMediaSession].
 * @param pillarboxMediaSession The [PillarboxMediaSession] to use that is connected to a [ch.srgssr.pillarbox.cast.receiver.PillarboxCastReceiverPlayer]
 * @see MediaManager.setSessionCompatToken
 */
fun MediaManager.setSessionTokenFromPillarboxMediaSession(pillarboxMediaSession: PillarboxMediaSession) {
    setSessionCompatToken(pillarboxMediaSession.mediaSession.getAppCompatToken())
}

private fun MediaSession.getAppCompatToken(): MediaSessionCompat.Token {
    return MediaSessionCompat.Token.fromToken(platformToken)
}
