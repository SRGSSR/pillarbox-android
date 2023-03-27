package ch.srgssr.pillarbox.analytics.comscore

import android.text.TextUtils
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

/**
 * Copyright (c) SRG SSR. All rights reserved.
 *
 *
 * License information is available from the LICENSE file.
 */
object SRGPageId {
    private const val DEFAULT_TITLE = "untitled"
    private const val DEFAULT_LEVEL_1 = "app"
    private val NORMALIZE_AND_PATTERN = Pattern.compile("[&\\+]")
    private val NORMALIZE_DASH_PATTERN = Pattern.compile("[=/\\\\<>()]")
    private val NORMALIZE_DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    private val NORMALIZE_AZ_09_DASHSPACE_PATTERN = Pattern.compile("[^a-z0-9 -]")

    /**
     * Normalize a string for analytics usage.
     * Use it for every string that will end up in analytics.
     *
     * @param s String to normalize
     * @return the normalized String
     */
    fun normalizeTitle(s: String?): String {
        return if (s != null) {
            val tmp = NORMALIZE_AND_PATTERN.matcher(s.trim { it <= ' ' }).replaceAll("and")
            NORMALIZE_DASH_PATTERN.matcher(tmp).replaceAll("-")
        } else {
            ""
        }
    }

    /**
     * Normalize a string for analytics usage.
     * Use it for every string that will end up in analytics.
     *
     * @param s String to normalize
     * @return the normalized String
     */
    fun normalize(value: String?): String {
        var s = value
        return if (s != null) {
            s = s.lowercase(Locale.FRENCH).trim { it <= ' ' }
            s = Normalizer.normalize(s, Normalizer.Form.NFD)
            s = NORMALIZE_DIACRITICS_PATTERN.matcher(s).replaceAll("")
            s = NORMALIZE_AZ_09_DASHSPACE_PATTERN.matcher(s).replaceAll("")
            s = s.replace(" ", "-")
            s
        } else {
            ""
        }
    }

    fun getCategory(levels: Array<String>): StringBuilder {
        val category = StringBuilder()
        if (levels.isEmpty()) {
            category.append(DEFAULT_LEVEL_1)
        } else {
            var i = 0
            while (i < levels.size) {
                val lvl = normalize(levels[i])
                i++
                if (!TextUtils.isEmpty(lvl)) {
                    if (i > 1) {
                        category.append('.')
                    }
                    category.append(lvl)
                }
            }
        }
        return category
    }
}
