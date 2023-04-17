/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * User analytics interface to hold user connection information.
 */
interface UserAnalytics {
    /**
     * Current user id to pass to every analytics calls.
     */
    var userId: String?

    /**
     * Login status of the current user to pass to every analytics calls.
     */
    var isLogged: Boolean
}
