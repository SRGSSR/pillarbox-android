// Handle version name
/**
 * Task configured is call before the build start during configuration
 * Inspired by : https://octopus.com/blog/teamcity-version-numbers-based-on-branches
 *
 * Set variables
 *  project.ext.set("versionCode", 19)
 *
 * Use variables
 *  versionCode project.versionCode
 */
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.util.*

val MAIN_BRANCH = "main"
val SNAPSHOT_SUFFIX = "-SNAPSHOT"
val MAJOR = 0
val MINOR = 0
val PATCH = 1

/*
task configured {
    def versionName = VERSION_MAJOR + "." + VERSION_MINOR
    def versionNameSuffix = ""
    def versionCode
            println "Configuring version name project for version " + versionName
    // TeamCity's auto-incrementing build counter; ensures each build is unique
    def buildCounter = System.getenv("BUILD_COUNTER")
    def teamcityBuild = buildCounter != null
    println "Build from teamcity : " + teamcityBuild + " BUILD_COUNTER: " + buildCounter
    println System.getenv("PATH") //println "$System.env.PATH"
    if (teamcityBuild) {
        def branch = System.getenv("GIT_BRANCH")
        // Sometimes the branch will be a full path, e.g., 'refs/heads/master'.
        // If so we'll base our logic just on the last part.
        if (branch.contains("/")) {
            branch = branch.substring(branch.lastIndexOf("/")).trim()
            if (branch.startsWith("/")) {
                branch = branch.substring(1)
            }
        }
        println "branch : " + branch
        versionCode = Integer.parseInt(buildCounter)
        def buildNumber
                if (branch.equalsIgnoreCase("master")) {
                    versionName = versionName + "." + VERSION_FIX
                    buildNumber = versionName
                } else if (branch.equalsIgnoreCase("develop")) {
                    versionNameSuffix = "-SNAPSHOT"
                    buildNumber = versionName + versionNameSuffix + "-" + buildCounter
                } else if (branch.matches("release-.*")) {
                    println "Release branches are not really supported here"
                    //$specificRelease = ($branch - replace 'release-(.*)', '$1' )
                    //$buildNumber = "${specificRelease}.${buildCounter}"
                    versionName = versionName + "." + buildCounter
                    versionNameSuffix = "-RELEASE"
                    buildNumber = versionName + versionNameSuffix + "-" + buildCounter
                } else {
                    //#If the branch starts with "feature-", just use the feature name
                    branch = branch.replace("feature-", "")
                    versionName = versionName + "." + branch
                    versionNameSuffix = "-SNAPSHOT"
                    buildNumber = versionName + versionNameSuffix + "-" + buildCounter
                }
        // Write teamcity buildNumber
        println "Try to set teamcity buildNumber to" + buildNumber
        println "##teamcity[buildNumber '$buildNumber']"
    } else {
        buildCounter = 9999
        versionCode = buildCounter;
        versionNameSuffix = "-debug"
        def buildNumber = versionName + versionNameSuffix + "-" + buildCounter
        println "Local[buildNumber '$buildNumber']"
    }

    project.ext.set("versionName", versionName)
    project.ext.set("versionCode", versionCode)
    project.ext.set("versionNameSuffix", versionNameSuffix)
    project.ext.set("pomVersion", versionName + versionNameSuffix)
    project.ext.set("buildDate", getBuildDate())
}

static def getBuildDate() {
    def date = new Date()
    return (date.format("yyyy-MM-dd HH:mm:ss"))
}
*/

extra["version_name"] = "0.0"

tasks.register("version") {
    extra["version_name"] = computeVersionNameFromBranchName()
    println("VersionName = ${extra["version_name"]} build upon branch = ${gitBranch()} at ${getBuildDate()}") //During sync
}

tasks.getByPath(":pillarbox-demo:preBuild").dependsOn(":version")

fun computeVersionNameFromBranchName(): String {
    val gitBranch = gitBranch()
    return if (gitBranch.contains(MAIN_BRANCH)) {
        "$MAJOR.$MINOR.$PATCH"
    } else {
        "$MAJOR.$MINOR.$gitBranch.$SNAPSHOT_SUFFIX"
    }
}

/**
 * Utility function to retrieve the name of the current git branch.
 * Will not work if build tool detaches head after checkout, which some do!
 */
fun gitBranch(): String {
    return try {
        val byteOut = ByteArrayOutputStream()
        project.exec {
            commandLine = "git rev-parse --abbrev-ref HEAD".split(" ")
            standardOutput = byteOut
        }
        String(byteOut.toByteArray()).trim().also {
            if (it == "HEAD")
                logger.warn("Unable to determine current branch: Project is checked out with detached head!")
        }
    } catch (e: Exception) {
        logger.warn("Unable to determine current branch: ${e.message}")
        "Unknown Branch"
    }
}

fun getBuildDate(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(Date())
}