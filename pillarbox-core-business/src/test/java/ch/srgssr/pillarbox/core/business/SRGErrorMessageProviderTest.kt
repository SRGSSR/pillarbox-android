/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.media3.common.PlaybackException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.DataParsingException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class SRGErrorMessageProviderTest {
    private lateinit var context: Context
    private lateinit var errorMessageProvider: SRGErrorMessageProvider

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        errorMessageProvider = SRGErrorMessageProvider(context)
    }

    @Test
    fun `getErrorMessage BlockReasonException`() {
        val exception = BlockReasonException(BlockReason.AGERATING12)
        val (errorCode, errorMessage) = errorMessageProvider.getErrorMessage(playbackException(exception))

        assertEquals(0, errorCode)
        assertEquals(context.getString(R.string.blockReason_ageRating12), errorMessage)
    }

    @Test
    fun `getErrorMessage ResourceNotFoundException`() {
        val exception = ResourceNotFoundException()
        val (errorCode, errorMessage) = errorMessageProvider.getErrorMessage(playbackException(exception))

        assertEquals(0, errorCode)
        assertEquals(context.getString(R.string.noPlayableResourceFound), errorMessage)
    }

    @Test
    fun `getErrorMessage DataParsingException`() {
        val exception = DataParsingException()
        val (errorCode, errorMessage) = errorMessageProvider.getErrorMessage(playbackException(exception))

        assertEquals(0, errorCode)
        assertEquals(context.getString(R.string.invalidDataError), errorMessage)
    }

    @Test
    fun `getErrorMessage HttpResultException`() {
        val exception = HttpResultException("HTTP request failed")
        val (errorCode, errorMessage) = errorMessageProvider.getErrorMessage(playbackException(exception))

        assertEquals(0, errorCode)
        assertEquals(exception.message, errorMessage)
    }

    @Test
    fun `getErrorMessage IOException`() {
        val exception = IOException()
        val (errorCode, errorMessage) = errorMessageProvider.getErrorMessage(playbackException(exception))

        assertEquals(0, errorCode)
        assertEquals(context.getString(R.string.NoInternet), errorMessage)
    }

    @Test
    fun `getErrorMessage unknown cause cause`() {
        val exception = IllegalStateException()
        val (errorCode, errorMessage) = errorMessageProvider.getErrorMessage(playbackException(exception))

        assertEquals(PlaybackException.ERROR_CODE_UNSPECIFIED, errorCode)
        assertEquals(context.getString(R.string.unknownError), errorMessage)
    }

    @Test
    fun `getErrorMessage null cause`() {
        val (errorCode, errorMessage) = errorMessageProvider.getErrorMessage(playbackException(null))

        assertEquals(PlaybackException.ERROR_CODE_UNSPECIFIED, errorCode)
        assertEquals(context.getString(R.string.unknownError), errorMessage)
    }

    private fun playbackException(cause: Exception?): PlaybackException {
        return PlaybackException(null, cause, PlaybackException.ERROR_CODE_UNSPECIFIED)
    }
}
