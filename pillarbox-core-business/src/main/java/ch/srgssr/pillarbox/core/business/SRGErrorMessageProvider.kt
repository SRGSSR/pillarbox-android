/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import android.os.RemoteException
import android.util.Pair
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import io.ktor.client.plugins.ClientRequestException
import kotlinx.serialization.SerializationException

/**
 * Process error message from [PlaybackException]
 */
class SRGErrorMessageProvider(private val context: Context) : ErrorMessageProvider<PlaybackException> {

    override fun getErrorMessage(throwable: PlaybackException): Pair<Int, String> {
        return when (val cause = throwable.cause) {
            is BlockReasonException -> {
                val message = context.resources.getStringArray(R.array.blockReasonArray)[cause.blockReason.ordinal]
                Pair.create(0, message)
            }
            // When using MediaController, RemoteException is send instead of HttpException.
            is RemoteException ->
                Pair.create(throwable.errorCode, cause.message)

            is ClientRequestException -> {
                Pair.create(cause.response.status.value, cause.response.status.description)
            }

            is ResourceNotFoundException -> {
                Pair.create(0, context.getString(R.string.noPlayableResourceFound))
            }

            is SerializationException -> {
                Pair.create(0, context.getString(R.string.invalidDataError))
            }

            else -> {
                Pair.create(throwable.errorCode, context.getString(R.string.unkownError))
            }
        }
    }
}
