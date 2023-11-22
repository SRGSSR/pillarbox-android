/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // known bug for libs : https://developer.android.com/studio/preview/features#gradle-version-catalogs-known-issues
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt)
}

apply(plugin = "android-reporting")

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    // Official site : https://detekt.dev/docs/gettingstarted/gradle
    // Tutorial : https://medium.com/@nagendran.p/integrating-detekt-in-the-android-studio-442128e971f8
    detekt {
        config.setFrom(files("../config/detekt/detekt.yml"))
        source.setFrom(files("src/main/java", "src/main/kotlin"))
        // preconfigure defaults
        buildUponDefaultConfig = false
        ignoredBuildTypes = listOf("release")
        autoCorrect = true
        parallel = true
    }

    dependencies {
        detekt(libs.detekt.cli)
        detektPlugins(libs.detekt.formatting)
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        reports {
            xml.required = false
            html.required = true
            txt.required = false
            sarif.required = false
            md.required = false
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

/*
 * https://detekt.dev/docs/gettingstarted/git-pre-commit-hook
 * https://medium.com/@alistair.cerio/android-ktlint-and-pre-commit-git-hook-5dd606e230a9
 */
tasks.register<Copy>("installGitHook") {
    description = "Install the Git pre-commit hook locally"
    from(file("${rootProject.rootDir}/git_hooks/pre-commit"))
    into { file("${rootProject.rootDir}/.git/hooks") }
    filePermissions {
        unix("rwxr-xr-x")
    }
}

tasks.getByPath(":pillarbox-demo:preBuild").dependsOn(":installGitHook")
