pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
        maven("https://jitpack.io")
    }
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        google()
        maven("https://plugins.gradle.org/m2/")
        // Needed for Kotlin coroutines that support new memory management mode
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
        }
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("io.arrow-kt:arrow-ank-gradle:0.11.0")
    }
}

rootProject.name = "wallet-sdk"
include(":protosLib")
include(":wallet-sdk")
include(":domain")
include(":apollo")
include(":castor")
include(":mercury")
include(":pluto")
include(":pollux")
include(":prism-agent")
