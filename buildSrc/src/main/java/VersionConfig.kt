import org.gradle.api.Project
import java.io.ByteArrayOutputStream

/*
 * Copyright (c) 2022.  SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
object VersionConfig {
    private const val MAJOR = 0
    private const val MINOR = 0
    private const val PATCH = 1

    private const val MAIN_BRANCH = "main"
    private const val SNAPSHOT_SUFFIX = "-SNAPSHOT"

    fun getLibVersionSuffix(gitBranch: String): String? {
        return if (isBranchMain(gitBranch)) {
            null
        } else {
            SNAPSHOT_SUFFIX
        }
    }

    fun getVersionNameFromProject(project: Project): String {
        val gitBranch = gitBranch(project)
        return if (isBranchMain(gitBranch)) {
            "$MAJOR.$MINOR.$PATCH"
        } else {
            "$MAJOR.$MINOR.$gitBranch"//.$SNAPSHOT_SUFFIX"
        }
    }

    /**
     * Utility function to retrieve the name of the current git branch.
     * Will not work if build tool detaches head after checkout, which some do!
     */
    private fun gitBranch(project: Project): String {
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