pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://www.jitpack.io")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("mihonx") {
            from(files("gradle/mihon.versions.toml"))
        }
    }

    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven(url = "https://www.jitpack.io")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// OS4 glass components are currently available on upstream main only. The submodule pins
// the complete Miuix module family to the same commit without GitHub Packages credentials.
val miuixSource = file("vendor/miuix")
check(miuixSource.resolve("settings.gradle.kts").isFile) {
    "Initialize Miuix sources with: git submodule update --init --recursive"
}
includeBuild(miuixSource) {
    name = "miuix-upstream"
    dependencySubstitution {
        listOf("core", "ui", "preference", "icons", "glass-icons", "material", "blur", "squircle", "glass")
            .forEach { suffix ->
                substitute(module("top.yukonga.miuix.kmp:miuix-$suffix"))
                    .using(project(":miuix-$suffix"))
                substitute(module("top.yukonga.miuix.kmp:miuix-$suffix-android"))
                    .using(project(":miuix-$suffix"))
            }
    }
}

rootProject.name = "Mihon"
include(":app")
include(":baseline-profile")
include(":core-metadata")
include(":core:archive")
include(":core:common")
include(":core:metro")
include(":data")
include(":domain")
include(":i18n")
include(":icons:custom-material-symbols")
include(":icons:material-symbols")
include(":icons:simple-icons")
include(":presentation-core")
include(":presentation-widget")
include(":source-api")
include(":source-local")
include(":telemetry")
