/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        val gitHubUsername = providers.gradleProperty("gpr.user").getOrElse(System.getenv("USERNAME"))
        val gitHubKey = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_TOKEN"))

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

include(":pillarbox-demo")
include(":pillarbox-player")
include(":pillarbox-analytics")
include(":pillarbox-core-business")
include(":pillarbox-ui")
include(":pillarbox-player-testutils")
include(":pillarbox-demo-tv")
include(":pillarbox-demo-shared")
