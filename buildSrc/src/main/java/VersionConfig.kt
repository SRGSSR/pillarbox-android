/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

/**
 * VersionConfig will build
 *  - VersionName for Pillarbox Demo
 *  - Version for Libraries
 */
object VersionConfig {
    private const val MAJOR = 0 // 0..99
    private const val MINOR = 1 // 0..99
    private const val PATCH = 0 // 0..99

    private const val MAIN_BRANCH = "main"
    private const val SNAPSHOT_SUFFIX = "SNAPSHOT"
    const val GROUP = "ch.srgssr.pillarbox"

    /**
     * @return Version code build from MAJOR, MINOR and PATCH
     * <pre>
     *  Samples :
     *  1.0.0 => 010000
     *  1.0.1 => 010001
     *  1.2.0 => 010200
     *  1.80.40 => 018040
     *  1.80.2 => 018002
     *  32.12.67 => 321267
     *  </pre>
     */
    fun versionCode(): Int {
        return MAJOR * 10000 + MINOR * 100 + MINOR
    }

    /**
     * Release version name for Demo app and libraries
     *
     * @return [MAJOR].[MINOR].[PATCH]
     */
    private fun getVersionName(): String {
        return "$MAJOR.$MINOR.$PATCH"
    }

    /**
     * Get version name with git branch
     *
     * @param suffix added to the end of the version name
     * @return [MAJOR].[MINOR].[PATCH].[suffix]
     */
    private fun getVersionNameWithSuffix(suffix: String): String {
        return "${getVersionName()}.$suffix"
    }


    /**
     * if on main branch return $MAJOR.$MINOR.$PATCH
     * else $MAJOR.$MINOR.{current git branch name}
     * @return the a version name to set to the project
     */
    fun getVersionNameFromProject(project: Project): String {
        val gitBranch = gitBranch(project)
        return if (isBranchMain(gitBranch)) {
            getVersionName()
        } else {
            getVersionNameWithSuffix(gitBranch)
        }
    }

    /**
     * if on main branch return $MAJOR.$MINOR.$PATCH
     * else $MAJOR.$MINOR.{current git branch name}-SNAPSHOT
     * @return the a version name to set to the project
     */
    fun getLibraryVersionNameFromProject(project: Project): String {
        val gitBranch = gitBranch(project)
        return if (isBranchMain(gitBranch)) {
            getVersionName()
        } else {
            val versionName = getVersionNameWithSuffix(gitBranch)
            if (isSnapshot()) {
                "$versionName-$SNAPSHOT_SUFFIX"
            } else {
                versionName
            }

        }
    }

    /**
     * Is snapshot
     *
     * @return true is environment variable "isSnapshot" is set to true
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun isSnapshot(): Boolean {
        return System.getenv("IS_SNAPSHOT")?.toBoolean() ?: false
    }

    /**
     * Utility function to retrieve the name of the current git branch.
     * If CI send a branch name we use it instead.
     * Will not work if build tool detaches head after checkout, which some do!
     * Useful for build trigger from github PR's
     */
    private fun gitBranch(project: Project): String {
        val ciBranch: String? = System.getenv("GITHUB_BRANCH_NAME")
        if (ciBranch != null) {
            return ciBranch
        }
        return try {
            val byteOut = ByteArrayOutputStream()
            project.exec {
                commandLine = "git rev-parse --abbrev-ref HEAD".split(" ")
                standardOutput = byteOut
            }
            String(byteOut.toByteArray()).trim().also {
                if (it == "HEAD")
                    project.logger.warn("Unable to determine current branch: Project is checked out with detached head!")
            }
        } catch (e: Exception) {
            project.logger.warn("Unable to determine current branch: ${e.message}")
            "Unknown Branch"
        }
    }

    private fun isBranchMain(gitBranch: String): Boolean {
        return gitBranch.contains(MAIN_BRANCH)
    }
}
