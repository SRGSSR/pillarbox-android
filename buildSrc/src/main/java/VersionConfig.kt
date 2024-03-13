/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

/**
 * VersionConfig will build
 *  - VersionName for Pillarbox Demo
 *  - Version for Libraries
 */
object VersionConfig {
    /**
     * Environment variable automatically set by GitHub actions.
     * @see [GitHub](https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables)
     */
    val isCI: Boolean = System.getenv("CI")?.toBooleanStrictOrNull() ?: false

    /**
     * Environment variable set by workflow.
     */
    private val ENV_VERSION_NAME: String? = System.getenv("VERSION_NAME")
    private val versionOnlyRegex = "[0-9]+.[0-9].[0-9]".toRegex()

    /**
     * Version name
     *
     * @return "Local" if [ENV_VERSION_NAME] no set.
     */
    fun versionName(): String {
        return ENV_VERSION_NAME ?: "Local"
    }

    /**
     * Version code
     * It assumes that major.minor.patch each <= 99
     * 0.0.0, 0.0.99, 0.1.0, 0.99.99
     */
    fun versionCode(): Int {
        return ENV_VERSION_NAME
            ?.let { versionOnlyRegex.find(it)?.value }
            ?.let {
                val versions = it.split(".").map { value -> value.toInt() }
                versions[0] * 10000 + versions[1] * 100 + versions[2]
            } ?: 9999
    }
}
