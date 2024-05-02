/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
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

        google()
        mavenCentral()
        maven("https://maven.pkg.github.com/SRGSSR/pillarbox-android") {
            credentials {
                username = gitHubUsername
                password = gitHubKey
            }
        }
        maven("https://maven.pkg.github.com/SRGSSR/srgdataprovider-android") {
            credentials {
                username = gitHubUsername
                password = gitHubKey
            }
        }
    }
}

rootProject.name = "Pillarbox"

include(
    ":pillarbox-analytics",
    ":pillarbox-core-business",
    ":pillarbox-demo",
    ":pillarbox-demo-shared",
    ":pillarbox-demo-tv",
    ":pillarbox-player",
    ":pillarbox-player-testutils",
    ":pillarbox-ui",
)
include(":pillarbox-cast")
include(":pillarbox-demo-cast")
