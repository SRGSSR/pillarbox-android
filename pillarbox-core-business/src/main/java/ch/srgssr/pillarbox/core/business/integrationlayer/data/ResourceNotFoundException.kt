/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Resource not found exception is throw when :
 * - [Chapter] doesn't have a playable resource
 * - [Chapter.listResource] is empty or null
 */
object ResourceNotFoundException : RuntimeException("Unable to find suitable resources")
