pluginManagement {
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            library("filament-android", "com.google.android.filament:filament-android:1.52.0")
            library("gltfio-android", "com.google.android.filament:gltfio-android:1.52.0")
            library("filament-utils-android", "com.google.android.filament:filament-utils-android:1.52.0")
        }
    }
}


rootProject.name = "Z_ITI-271304_U3_E01"
include(":app")
