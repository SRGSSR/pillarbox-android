/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.service

/**
 * Remote result
 *
 * @param T type of the data
 */
sealed interface RemoteResult<T> {

    /**
     * Success
     *
     * @param T type of the data
     * @property data
     */
    data class Success<T>(val data: T) : RemoteResult<T>

    /**
     * Error
     *
     * @param T type of the data, not used here
     * @property throwable
     * @property code
     */
    data class Error<T>(val throwable: Throwable, val code: Int = 404) : RemoteResult<T>
}
