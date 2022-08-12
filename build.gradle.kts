/*
 * Copyright (c) 2022.  SRG SSR. All rights reserved.
 */
import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application").version("7.2.1").apply(false)
    id("com.android.library").version("7.2.1").apply(false)
    id("org.jetbrains.kotlin.android").version("1.7.10").apply(false)
    // https://github.com/detekt/detekt
    id("io.gitlab.arturbosch.detekt").version("1.21.0").apply(true)
}

apply(plugin = "android-reporting")

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    // Official site : https://detekt.dev/docs/gettingstarted/gradle
    // Tutorial : https://medium.com/@nagendran.p/integrating-detekt-in-the-android-studio-442128e971f8
    detekt {
        // preconfigure defaults
        buildUponDefaultConfig = true
        ignoredBuildTypes = listOf("release")
        allRules = true
        autoCorrect = true
        config = files("../config/detekt/detekt.yml")
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

tasks.register("pushVersionToTeamcity") {
    doLast {
        println("Try to set teamcity buildNumber to")
        println("##teamcity[buildNumber '${VersionConfig.getLibraryVersionNameFromProject(project)}']")
    }
}

tasks.getByPath(":pillarbox-demo:preBuild").dependsOn(":installGitHook")

// From https://github.com/detekt/detekt/blob/main/build.gradle.kts
val analysisDir = file(projectDir)
// val baselineFile = file("$rootDir/config/detekt/baseline.xml")
val configFile = file("$rootDir/config/detekt/detekt.yml")
// val statisticsConfigFile = file("$rootDir/config/detekt/statistics.yml")

val kotlinFiles = "**/*.kt"
val kotlinScriptFiles = "**/*.kts"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"
val buildSrcFiles = "**/buildSrc/**"

val detektFormat by tasks.registering(Detekt::class) {
    description = "Formats whole project."
    parallel = true
    disableDefaultRuleSets = true
    buildUponDefaultConfig = true
    autoCorrect = true
    setSource(analysisDir)
    // config.setFrom(listOf(statisticsConfigFile, configFile))
    config.setFrom(listOf(configFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    exclude(buildSrcFiles)
    // baseline.set(baselineFile)
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
        md.required.set(false)
    }
}

val detektAll by tasks.registering(Detekt::class) {
    description = "Runs detekt to the whole project at once."
    parallel = true
    buildUponDefaultConfig = true
    setSource(analysisDir)
    // config.setFrom(listOf(statisticsConfigFile, configFile))
    config.setFrom(listOf(configFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    exclude(buildSrcFiles)
    // baseline.set(baselineFile)
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(false)
        txt.required.set(false)
        md.required.set(false)
    }
}

tasks.register("build") {
    dependsOn(gradle.includedBuild("detekt-gradle-plugin").task(":build"))
}
