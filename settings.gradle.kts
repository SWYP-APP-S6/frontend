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
        maven("https://repository.map.naver.com/archive/maven") {
            content {
                includeGroup("com.naver.maps")
            }
        }
        maven("https://devrepo.kakao.com/nexus/content/groups/public/") {
            content {
                includeGroup("com.kakao.sdk")
            }
        }
    }
}

rootProject.name = "mangro"
include(":app")
include(":core:network")
include(":core:designsystem")
