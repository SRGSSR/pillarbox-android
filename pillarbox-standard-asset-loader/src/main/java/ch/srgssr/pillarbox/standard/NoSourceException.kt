/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import java.io.IOException

/**
 * [IOException] when [PlayerData.source] is null.
 */
class NoSourceException(message: String = "Source not valid") : IOException(message)
