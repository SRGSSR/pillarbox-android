/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

/**
 * VersionConfig will build
 *  - VersionName for Pillarbox Demo
 *  - Version for Libraries
 */
object VersionConfig {
    /**
     * Environement variable automatically set by Github actions.
     * @see [github](https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables)
     */
    val isCI: Boolean = System.getenv("CI")?.toBooleanStrictOrNull() ?: false

    /**
     * Environement variable set by workflow.
     */
    private val ENV_VERSION_NAME: String? = System.getenv("VERSION_NAME")
    private val versionRegex = "[0-9]+.[0-9].[0-9]-?\\S*".toRegex()
    private val versionOnlyRegex = "[0-9]+.[0-9].[0-9]".toRegex()
    private val versionSuffixRegex = "-\\S*".toRegex()

    /**
     * Maven artifact group
     */
    const val GROUP = "ch.srgssr.pillarbox"

    /**
     * Semantic version
     * @return Major.Minor.Patch string from [ENV_VERSION_NAME] or null if not set.
     */
    fun semanticVersion(): String? {
        System.out.println("version = $ENV_VERSION_NAME")
        return ENV_VERSION_NAME?.let { versionOnlyRegex.find(it)?.value }
    }

    /**
     * Version name
     *
     * @return Local if [ENV_VERSION_NAME] no set.
     */
    fun versionName(): String {
        return ENV_VERSION_NAME ?: "Local"
    }

    /**
     * @return -suffix from MARJOR.MINOR.PATCH-Suffix
     */
    fun versionNameSuffix(): String? {
        return ENV_VERSION_NAME?.let { versionSuffixRegex.find(it)?.value }
    }

    /**
     * Version code
     * It assumes that major.minor.patch each <= 99
     * 0.0.0, 0.0.99, 0.1.0, 0.99.99
     */
    fun versionCode(): Int {
        return semanticVersion()?.let {
            val versions = it.split(".").map { value -> value.toInt() }
            versions[0] * 10000 + versions[1] * 100 + versions[2]
        } ?: 9999
    }
}
