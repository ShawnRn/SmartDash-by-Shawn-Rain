import java.io.File

plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
}

// Keep generated Gradle output out of iCloud Drive for local development.
// CI retains Gradle's conventional project-relative output paths because the
// release workflow collects and verifies artifacts from app/build/outputs.
val isCiBuild = providers.environmentVariable("CI").orNull?.toBoolean() == true
if (!isCiBuild) {
    val smartDashBuildRoot = providers.environmentVariable("SMARTDASH_GRADLE_BUILD_ROOT")
        .orElse(
            File(
                System.getProperty("user.home"),
                "Library/Caches/SmartDash/gradle-build"
            ).absolutePath
        )

    layout.buildDirectory.set(file("${smartDashBuildRoot.get()}/root"))
    subprojects {
        layout.buildDirectory.set(file("${smartDashBuildRoot.get()}/$name"))
    }
}
