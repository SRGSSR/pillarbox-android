/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        val gitHubUsername = providers.gradleProperty("gpr.user")
            .orElse(providers.environmentVariable("USERNAME"))
            .get()
        val gitHubKey = providers.gradleProperty("gpr.key")
            .orElse(providers.environmentVariable("GITHUB_TOKEN"))
            .get()

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.github.com/SRGSSR/srgdataprovider-android") {
            credentials {
                username = gitHubUsername
                password = gitHubKey
            }

            content {
                includeGroup("ch.srg.data.provider")
            }
        }
        maven("https://maven.pkg.github.com/SRGSSR/MediaMaestro") {
            credentials {
                username = gitHubUsername
                password = gitHubKey
            }

            content {
                includeGroup("ch.srgssr.media.maestro")
            }
        }
    }
}

rootProject.name = "Pillarbox"

include(
    ":pillarbox-analytics",
    ":pillarbox-cast",
    ":pillarbox-core-business",
    ":pillarbox-core-business-cast",
    ":pillarbox-demo",
    ":pillarbox-demo-shared",
    ":pillarbox-demo-tv",
    ":pillarbox-player",
    ":pillarbox-player-testutils",
    ":pillarbox-ui",
)
