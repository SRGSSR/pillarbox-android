/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.2.1" apply false
    id("com.android.library") version "7.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
    // https://github.com/detekt/detekt
    id("io.gitlab.arturbosch.detekt").version("1.21.0")
    id("androidx.navigation.safeargs.kotlin").version(Dependencies.navigationVersion).apply(false)
}

apply(plugin = "android-reporting")

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    // Official site : https://detekt.dev/docs/gettingstarted/gradle
    // Tutorial : https://medium.com/@nagendran.p/integrating-detekt-in-the-android-studio-442128e971f8
    detekt {
        config = files("../config/detekt/detekt.yml")
        source = files("src/main/java", "src/main/kotlin")
        // preconfigure defaults
        buildUponDefaultConfig = false
        ignoredBuildTypes = listOf("release")
        autoCorrect = true
    }

    dependencies {
        detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.21.0")
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "1.8"
        reports {
            xml.required.set(false)
            html.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

/*
 * https://detekt.dev/docs/gettingstarted/git-pre-commit-hook
 * https://medium.com/@alistair.cerio/android-ktlint-and-pre-commit-git-hook-5dd606e230a9
 */
tasks.register("installGitHook", Copy::class) {
    description = "Adding git hook script to local working copy"
    from(file("rootProject.rootDir/git_hooks/pre-commit"))
    into { file("rootProject.rootDir/.git/hooks") }
    fileMode = 0x777
}

/**
 * Setup the Teamcity buildNumber with a pretty name.
 * The buildNumber will be equal to the library version.
 */
tasks.register("pushVersionToTeamcity") {
    doLast {
        println("Try to set teamcity buildNumber to")
        println("##teamcity[buildNumber '${VersionConfig.getLibraryVersionNameFromProject(project)}']")
    }
}

/**
 * Setup the Teamcity buildNumber with a pretty name.
 * The buildNumber will be equal to the application version without suffix.
 */
tasks.register("pushDemoVersionToTeamcity") {
    doLast {
        println("Try to set teamcity buildNumber to")
        println("##teamcity[buildNumber '${VersionConfig.getVersionNameFromProject(project)}']")
    }
}

tasks.getByPath(":pillarbox-demo:preBuild").dependsOn(":installGitHook")
