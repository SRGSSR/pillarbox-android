/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.URL

/**
 * Load the Bitmap from a [SpriteSheet].
 *
 * This interface allows integrators to use their own implementation to load Bitmaps from an external library like Glide, Coil, ...
 */
fun interface SpriteSheetLoader {
    /**
     * Load sprite sheet
     *
     * @param spriteSheet The [SpriteSheet] to load the Bitmap from.
     * @param onComplete the callback to call when the Bitmap has been loaded.
     */
    fun loadSpriteSheet(spriteSheet: SpriteSheet, onComplete: (Bitmap?) -> Unit)

    /**
     * Default
     *
     * @param dispatcher The [CoroutineDispatcher] to use for loading the sprite sheet. Should not be on the main thread.
     */
    class Default(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : SpriteSheetLoader {
        override fun loadSpriteSheet(spriteSheet: SpriteSheet, onComplete: (Bitmap?) -> Unit) {
            MainScope().launch(dispatcher) {
                URL(spriteSheet.url).openStream().use {
                    onComplete(BitmapFactory.decodeStream(it))
                }
            }
        }
    }
}
