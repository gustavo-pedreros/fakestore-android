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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Fakestore"
include(":app")
include(":catalog:data")
include(":catalog:domain")
include(":catalog:ui")
include(":core:common")
include(":core:connectivity")
include(":core:database")
include(":core:designsystem")
include(":core:network")
include(":favorites:data")
include(":favorites:domain")
include(":favorites:ui")
include(":shared:kernel")
