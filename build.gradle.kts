import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    java
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.dokka") version "1.7.10"
}

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
        // Needed for Kotlin coroutines that support new memory management mode
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.1")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.4")
        classpath("com.github.piacenti:antlr-kotlin-gradle-plugin:0.0.14")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

version = "1.0.0-alpha"
group = "io.iohk.atala.prism"

allprojects {
    this.group = group
    this.version = version

    repositories {
        mavenCentral()
        mavenLocal()
        google()
        maven("https://plugins.gradle.org/m2/")
        // Needed for Kotlin coroutines that support new memory management mode
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
        }
        maven {
            this.url = uri("https://maven.pkg.github.com/input-output-hk/atala-prism-apollo")
            credentials {
                this.username = System.getenv("ATALA_GITHUB_ACTOR")
                this.password = System.getenv("ATALA_GITHUB_TOKEN")
            }
        }
    }

//    apply(plugin = "org.gradle.maven-publish")

//    publishing {
//        repositories {
//            maven {
//                this.name = "GitHubPackages"
//                this.url = uri("https://maven.pkg.github.com/input-output-hk/atala-prism-wallet-sdk-kmm/")
//                credentials {
//                    this.username = System.getenv("ATALA_GITHUB_ACTOR")
//                    this.password = System.getenv("ATALA_GITHUB_TOKEN")
//                }
//            }
//        }
//    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        verbose.set(true)
        outputToConsole.set(true)
    }
}

rootProject.plugins.withType(NodeJsRootPlugin::class.java) {
    rootProject.extensions.getByType(NodeJsRootExtension::class.java).nodeVersion = "16.17.0"
}

tasks.dokkaGfmMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}

ktlint {
    filter {
        exclude("**/generated/**", "/pluto/build/generated/**")
        exclude { entry ->
            entry.file.toString().contains("generated")
        }
    }
}
