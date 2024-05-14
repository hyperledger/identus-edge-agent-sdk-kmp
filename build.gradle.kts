import org.gradle.internal.os.OperatingSystem
import java.util.Base64

val publishedMavenId = "org.hyperledger.identus"
val os: OperatingSystem = OperatingSystem.current()

plugins {
    id("com.android.library") version "8.1.4" apply false
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.8.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.dokka") version "1.9.0"
    id("org.jetbrains.kotlin.kapt") version "1.9.10"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-1"
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.1")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.5")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.23.1")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

allprojects {
    this.group = publishedMavenId

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
        // Needed for Kotlin coroutines that support new memory management mode
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
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
                } else if (requested.group == "com.google.protobuf") {
                    // Because of Duplicate Classes issue happening on the sampleapp module
                    if (requested.name == "protobuf-javalite" || requested.name == "protobuf-java") {
                        useTarget("com.google.protobuf:protobuf-java:3.14.0")
                    }
                }
            }
        }
    }
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
                "build/generated/**"
            )
            exclude {
                it.file.path.contains("generated-src") ||
                    it.file.toString().contains("generated") ||
                    it.file.path.contains("generated")
            }
        }
    }

    if (this.name == "edge-agent-sdk") {
        apply(plugin = "org.gradle.maven-publish")
        apply(plugin = "org.gradle.signing")

        publishing {
            publications {
                withType<MavenPublication> {
                    groupId = publishedMavenId
                    artifactId = project.name
                    version = project.version.toString()
                    pom {
                        name.set("Identus Edge Agent SDK")
                        description.set("Identus Edge Agent - Kotlin Multiplatform (Android/JVM)")
                        url.set("https://docs.atalaprism.io/")
                        organization {
                            name.set("IOG")
                            url.set("https://iog.io/")
                        }
                        issueManagement {
                            system.set("Github")
                            url.set("https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm")
                        }
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("hamada147")
                                name.set("Ahmed Moussa")
                                email.set("ahmed.moussa@iohk.io")
                                organization.set("IOG")
                                roles.add("developer")
                                url.set("https://github.com/hamada147")
                            }
                            developer {
                                id.set("elribonazo")
                                name.set("Javier Ribó")
                                email.set("javier.ribo@iohk.io")
                                organization.set("IOG")
                                roles.add("developer")
                            }
                            developer {
                                id.set("cristianIOHK")
                                name.set("Cristian Gonzalez")
                                email.set("cristian.castro@iohk.io")
                                organization.set("IOG")
                                roles.add("developer")
                                url.set("https://github.com/cristianIOHK")
                            }
                            developer {
                                id.set("amagyar-iohk")
                                name.set("Allain Magyar")
                                email.set("allain.magyar@iohk.io")
                                organization.set("IOG")
                                roles.add("qc")
                            }
                            developer {
                                id.set("antonbaliasnikov")
                                name.set("Anton Baliasnikov")
                                email.set("anton.baliasnikov@iohk.io")
                                organization.set("IOG")
                                roles.add("qc")
                            }
                            developer {
                                id.set("goncalo-frade-iohk")
                                name.set("Gonçalo Frade")
                                email.set("goncalo.frade@iohk.io")
                                organization.set("IOG")
                                roles.add("developer")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://input-output-hk/atala-prism-wallet-sdk-kmm.git")
                            developerConnection.set("scm:git:ssh://input-output-hk/atala-prism-wallet-sdk-kmm.git")
                            url.set("https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm")
                        }
                    }
                    if (System.getenv("BASE64_ARMORED_GPG_SIGNING_KEY_MAVEN") != null) {
                        if (System.getenv("BASE64_ARMORED_GPG_SIGNING_KEY_MAVEN").isNotBlank()) {
                            signing {
                                val base64EncodedAsciiArmoredSigningKey: String =
                                    System.getenv("BASE64_ARMORED_GPG_SIGNING_KEY_MAVEN") ?: ""
                                val signingKeyPassword: String =
                                    System.getenv("SIGNING_KEY_PASSWORD") ?: ""
                                useInMemoryPgpKeys(
                                    String(
                                        Base64.getDecoder().decode(
                                            base64EncodedAsciiArmoredSigningKey.toByteArray()
                                        )
                                    ),
                                    signingKeyPassword
                                )
                                sign(this@withType)
                            }
                        }
                    }
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}
