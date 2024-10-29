/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.widget

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext

/**
 * A composable function that displays a Cast button. This button allows users to discover and connect to Cast-enabled devices.
 *
 * You must instantiate a [CastContext] before using this composable. This is typically done in your [Activity] or [Application] class.
 *
 * @param modifier The [Modifier] to be applied to the Cast button.
 *
 * @see [Context.getCastContext()][ch.srgssr.pillarbox.cast.getCastContext]
 */
@Composable
fun CastButton(
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MediaRouteButton(context).apply {
                CastButtonFactory.setUpMediaRouteButton(context, this)
            }
        },
    )
}
