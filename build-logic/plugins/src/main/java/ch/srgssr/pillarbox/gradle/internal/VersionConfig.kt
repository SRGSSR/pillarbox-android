/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

/**
 * VersionConfig will build
 *  - VersionName for Pillarbox Demo
 *  - Version for Libraries
 *
 * @param envVersionName Environment variable set by workflow.
 */
internal class VersionConfig(
    private val envVersionName: String? = System.getenv("VERSION_NAME"),
) {
    private val versionOnlyRegex = "^[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{1,2}$".toRegex()

    /**
     * Version name
     *
     * @param default The default value to use if no version name is available.
     *
     * @return "Local" if [envVersionName] no set.
     */
    internal fun versionName(default: String = "Local"): String {
        return envVersionName ?: default
    }

    /**
     * Version code
     * It assumes that major.minor.patch each <= 99
     * 0.0.0, 0.0.99, 0.1.0, 0.99.99
     */
    internal fun versionCode(): Int {
        return envVersionName
            ?.let { versionOnlyRegex.find(it)?.value }
            ?.let {
                val versions = it.split(".").map { value -> value.toInt() }
                versions[0] * 10000 + versions[1] * 100 + versions[2]
            } ?: 9999
    }
}
