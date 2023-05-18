import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.dokka") version "1.7.10"
    // id("com.google.protobuf") version "0.9.2"
    // The following should be removed
    // id(Plugins.npmPublish) version PluginVersions.npmPublish apply false
    // id(Plugins.gitVersion) version PluginVersions.gitVersion
    // id(Plugins.compatibilityValidator) version PluginVersions.compatibilityValidator
    // id(Plugins.gitOps) version PluginVersions.gitOps
    // id(Plugins.koverage) version PluginVersions.koverage
}

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
        gradlePluginPortal()
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

allprojects {
    this.group = "io.iohk.atala.prism.walletsdk"
    this.version = "1.0.0-alpha"

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
        maven {
            this.url = uri("https://maven.pkg.github.com/input-output-hk/atala-prism-didcomm-kmm")
            credentials {
                this.username = System.getenv("ATALA_GITHUB_ACTOR")
                this.password = System.getenv("ATALA_GITHUB_TOKEN")
            }
        }
    }

    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.bouncycastle") {
                    when (requested.name) {
                        "bcprov-jdk15on", "bcprov-jdk15to18" -> {
                            useTarget("org.bouncycastle:bcprov-jdk15on:1.68")
                        }
                    }
                } else if (requested.group == "com.nimbusds") {
                    // Making sure we are using the latest version of `nimbus-jose-jwt` instead if 9.25.6
                    useTarget("com.nimbusds:nimbus-jose-jwt:9.31")
                }
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

    ktlint {
        verbose.set(true)
        outputToConsole.set(true)
        filter {
            exclude(
                "build/generated-src/**",
                "**/generated/**",
                "**/generated-src/**",
                "build/**",
                "build/generated/**",
                "**/antlrgrammar/**"
            )
            exclude {
                it.file.path.contains("generated-src") ||
                    it.file.toString().contains("generated") ||
                    it.file.path.contains("generated") ||
                    it.file.path.contains("antlrgrammar") ||
                    it.file.toString().contains("antlrgrammar")
            }
        }
    }
}

rootProject.plugins.withType(NodeJsRootPlugin::class.java) {
    rootProject.extensions.getByType(NodeJsRootExtension::class.java).nodeVersion = "16.17.0"
}

tasks.dokkaGfmMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
