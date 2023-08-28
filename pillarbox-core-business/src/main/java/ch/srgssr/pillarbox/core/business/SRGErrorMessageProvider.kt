/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.os.RemoteException
import android.util.Pair
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import io.ktor.client.plugins.ClientRequestException

/**
 * Process error message from [PlaybackException]
 */
class SRGErrorMessageProvider : ErrorMessageProvider<PlaybackException> {

    override fun getErrorMessage(throwable: PlaybackException): Pair<Int, String> {
        return when (val cause = throwable.cause) {
            is BlockReasonException -> {
                Pair.create(0, cause.blockReason)
            }
            // When using MediaController, RemoteException is send instead of HttpException.
            is RemoteException ->
                Pair.create(throwable.errorCode, cause.message)

            is ClientRequestException -> {
                Pair.create(cause.response.status.value, cause.response.status.description)
            }

            is ResourceNotFoundException -> {
                Pair.create(0, "Can't find Resource to play")
            }

            else -> {
                Pair.create(throwable.errorCode, "${throwable.localizedMessage} (${throwable.errorCodeName})")
            }
        }
    }
}
