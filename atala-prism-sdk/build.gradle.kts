import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask

val currentModuleName: String = "AtalaPrismSDK"
val os: OperatingSystem = OperatingSystem.current()
val apolloVersion = project.property("apollo_version")
val didpeerVersion = project.property("didpeer_version")

plugins {
    id("com.squareup.sqldelight")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.7.20"
    id("com.android.library")
    id("org.jetbrains.dokka")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    /*
    Not going to support JS for the time being
    js(IR) {
        this.moduleName = currentModuleName
        this.binaries.library()
        this.useCommonJs()
        this.compilations["main"].packageJson {
            this.version = rootProject.version.toString()
        }
        this.compilations["test"].packageJson {
            this.version = rootProject.version.toString()
        }
        browser {
            this.webpackTask {
                this.output.library = currentModuleName
                this.output.libraryTarget = Target.VAR
            }
            this.testTask {
                this.useKarma {
                    this.useChromeHeadless()
                }
            }
        }
        nodejs {
            this.testTask {
                this.useKarma {
                    this.useChromeHeadless()
                }
            }
        }
    }
     */

    sourceSets {
        val commonAntlr by creating {
            kotlin.srcDir("build/generated-src/commonAntlr/kotlin")
            dependencies {
                api(kotlin("stdlib-common"))
                api("com.github.piacenti:antlr-kotlin-runtime:0.0.14")
            }
        }
        val commonMain by getting {
            this.dependsOn(commonAntlr)
            kotlin.srcDir("${project(":protosLib").buildDir}/generated/source/proto/main/kotlin")
            resources.srcDir("${project(":protosLib").projectDir}/src/main")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

                implementation("io.ktor:ktor-client-core:2.1.3")
                implementation("io.ktor:ktor-client-content-negotiation:2.1.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.3")
                implementation("io.ktor:ktor-client-logging:2.1.3")

                implementation("io.iohk.atala.prism.didcomm:didpeer:$didpeerVersion")

                implementation("io.iohk.atala.prism.apollo:base64:$apolloVersion")
                implementation("io.iohk.atala.prism.apollo:base-asymmetric-encryption:$apolloVersion")
                implementation("io.iohk.atala.prism.apollo:ecdsa:$apolloVersion")
                implementation("io.iohk.atala.prism.apollo:hashing:$apolloVersion")
                implementation("io.iohk.atala.prism.apollo:uuid:$apolloVersion")

                // implementation("com.nimbusds:nimbus-jose-jwt:9.31") // We are going to use the `nimbus-jose-jwt` that resides in `didcomm` lib

                implementation("pro.streem.pbandk:pbandk-runtime:0.14.2")

                implementation("org.didcommx:didcomm:0.3.0")

                implementation("com.google.protobuf:protoc:3.12.0") {
                    exclude("com.google.protobuf")
                }

                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation("io.ktor:ktor-client-mock:2.1.3")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.1.3")
                implementation("org.bouncycastle:bcprov-jdk15on:1.68")
                implementation("com.squareup.sqldelight:sqlite-driver:1.5.4")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
                implementation("io.ktor:ktor-client-okhttp:2.1.3")
                implementation("org.bouncycastle:bcprov-jdk15on:1.68")
                implementation("com.squareup.sqldelight:android-driver:1.5.4")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        /*
        Not going to support JS for the time being
        val jsMain by getting
        val jsTest by getting
         */

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    /**
     * Because Software Components will not be created automatically for Maven publishing from
     * Android Gradle Plugin 8.0. To opt-in to the future behavior, set the Gradle property android.
     * disableAutomaticComponentCreation=true in the `gradle.properties` file or use the new
     * publishing DSL.
     */
    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
        }
    }
}

sqldelight {
    database("PrismPlutoDb") {
        packageName = "io.iohk.atala.prism.walletsdk.pluto"
        sourceFolders = listOf("sqldelight")
    }
}

// Dokka implementation
tasks.withType<DokkaTask> {
    moduleName.set(project.name)
    moduleVersion.set(rootProject.version.toString())
    description = """
        This is a Kotlin Multiplatform AtalaPrismSDK
    """.trimIndent()
    dokkaSourceSets {
        // TODO: Figure out how to include files to the documentations
        named("commonMain") {
            includes.from("Module.md", "docs/Module.md")
        }
    }
}

val antlrGenerationTask by tasks.register<com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask>("generateKotlinCommonGrammarSource") {
    // the classpath used to run antlr code generation
    antlrClasspath = configurations.detachedConfiguration(
        project.dependencies.create("com.github.piacenti:antlr-kotlin-runtime:0.0.14"),
    )
    maxHeapSize = "64m"
    packageName = "io.iohk.atala.prism.walletsdk.castor.antlrgrammar"
    arguments = listOf("-long-messages", "-Dlanguage=JavaScript")
    source = project.objects
        .sourceDirectorySet("antlr", "antlr")
        .srcDir("src/commonAntlr/antlr").apply {
            include("*.g4")
        }
    // outputDirectory is required, put it into the build directory
    // if you do not want to add the generated sources to version control
    outputDirectory = File("build/generated-src/commonAntlr/kotlin")
    // use this setting if you want to add the generated sources to version control
    // outputDirectory = File("src/commonAntlr/kotlin")
}

tasks.matching {
    it.name == "compileCommonAntlrKotlinMetadata" ||
        it.name == "runKtlintCheckOverCommonMainSourceSet" ||
        it.name == "jvmSourcesJar" ||
        it.name == "sourcesJar" ||
        it.name == "compileCommonMainKotlinMetadata" ||
        it.name == "compileReleaseKotlinAndroid" ||
        it.name == "compileDebugKotlinAndroid" ||
        it.name == "compileKotlinJs" ||
        it.name == "compileKotlinJvm"
}.all {
    this.dependsOn(":protosLib:generateProto")
    this.dependsOn(antlrGenerationTask)
}

val buildProtoLibsGen by tasks.creating {
    group = "build"
    this.dependsOn(":protosLib:generateProto")
}

tasks.getByName("build") {
    this.doFirst {
        ":protosLib:generateProto"
    }
}
