/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import java.net.URL

/**
 * Load a [Bitmap] from a [SpriteSheet].
 *
 * This interface allows integrators to use their own implementation to load [Bitmap]s using an external library like
 * [Glide](https://bumptech.github.io/glide/), [Coil](https://coil-kt.github.io/coil/), ...
 */
fun interface SpriteSheetLoader {
    /**
     * Load sprite sheet
     *
     * @param spriteSheet The [SpriteSheet] to load the [Bitmap] from.
     * @return The [Result] of the loading operation.
     */
    suspend fun loadSpriteSheet(spriteSheet: SpriteSheet): Result<Bitmap>

    /**
     * Default
     **/
    object Default : SpriteSheetLoader {
        override suspend fun loadSpriteSheet(spriteSheet: SpriteSheet): Result<Bitmap> {
            return runCatching {
                URL(spriteSheet.url).openStream().use {
                    BitmapFactory.decodeStream(it)
                }
            }
        }
    }
}
