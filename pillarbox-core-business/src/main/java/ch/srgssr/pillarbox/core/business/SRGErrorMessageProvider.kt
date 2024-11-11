/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import android.util.Pair
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DataSourceException
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.DataParsingException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import java.io.IOException

/**
 * Provides user-friendly error messages for [PlaybackException] instances.
 *
 * This class analyzes the cause of a [PlaybackException] and maps it to a localized error message suitable for display to the user.
 *
 * @param context The Android context used to access string resources.
 */
class SRGErrorMessageProvider(private val context: Context) : ErrorMessageProvider<PlaybackException> {

    override fun getErrorMessage(throwable: PlaybackException): Pair<Int, String> {
        return when (val cause = throwable.cause) {
            is BlockReasonException -> {
                Pair.create(0, context.getString(cause.messageResId))
            }

            is ResourceNotFoundException -> {
                Pair.create(0, context.getString(R.string.noPlayableResourceFound))
            }

            is DataParsingException -> {
                Pair.create(0, context.getString(R.string.invalidDataError))
            }

            is HttpResultException -> {
                Pair.create(0, cause.message)
            }

            is DataSourceException -> {
                Pair.create(cause.reason, cause.message)
            }

            is IOException -> {
                Pair.create(0, context.getString(R.string.NoInternet))
            }

            else -> {
                Pair.create(throwable.errorCode, context.getString(R.string.unknownError))
            }
        }
    }
}
