pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("ktorLibs").from("io.ktor:ktor-version-catalog:3.5.2")
    }
}

rootProject.name = "Anudeep-Ananth"

// projects.anudeepananth.com (Compose web app + Ktor API). Skipped when the folder is absent,
// e.g. inside the portfolio's Docker build, which excludes it via .dockerignore.
if (file("projects").exists()) {
    include(":projects:shared")
    include(":projects:server")
    include(":projects:composeApp")
}
