import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

val currentModuleName: String = "AtalaPrismSDK"
val os: OperatingSystem = OperatingSystem.current()
val apolloVersion = project.property("apollo_version")
val didpeerVersion = project.property("didpeer_version")

plugins {
    id("com.squareup.sqldelight")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
}

koverReport {
    defaults {
        // adds the contents of the reports of `release` Android build variant to default reports
        mergeWith("release")
        html {
            title = "$currentModuleName - Test Coverage"
            setReportDir(layout.buildDirectory.dir("kover/atala-prism-sdk/xml"))
        }
        xml {}
        filters {
            excludes {
                packages(
                    "io.iohk.atala.prism.protos",
                    "io.iohk.atala.prism.walletsdk.domain",
                    "io.iohk.atala.prism.walletsdk.castor.antlrgrammar",
                    "ioiohkatalaprismwalletsdkpluto.data"
                )
            }
        }
    }
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

                implementation("io.ktor:ktor-client-core:2.3.4")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
                implementation("io.ktor:ktor-client-logging:2.3.4")

                implementation("io.iohk.atala.prism.didcomm:didpeer:$didpeerVersion") {
                    exclude("io.iohk.atala.prism.apollo")
                }

                implementation("io.iohk.atala.prism.apollo:apollo:$apolloVersion")

                // implementation("com.nimbusds:nimbus-jose-jwt:9.31") // We are going to use the `nimbus-jose-jwt` that resides in `didcomm` lib

                implementation("pro.streem.pbandk:pbandk-runtime:0.14.2")

                implementation("org.didcommx:didcomm:0.3.2")

                implementation("com.google.protobuf:protoc:3.12.0") {
                    exclude("com.google.protobuf")
                }

                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.5")

                api("org.lighthousegames:logging:1.1.2")

                implementation("io.iohk.atala.prism.anoncredskmp:anoncreds-kmp:1.0.0")
                implementation("com.ionspin.kotlin:bignum:0.3.8")
                implementation("org.bouncycastle:bcprov-jdk15on:1.68")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                implementation("io.ktor:ktor-client-mock:2.3.4")
                implementation("junit:junit:4.13.2")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.4")
                implementation("com.squareup.sqldelight:sqlite-driver:1.5.5")
            }
        }
        val jvmTest by getting
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
                implementation("io.ktor:ktor-client-okhttp:2.3.4")
                implementation("com.squareup.sqldelight:android-driver:1.5.5")
            }
        }
        val androidInstrumentedTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation("androidx.test.espresso:espresso-core:3.5.1")
                implementation("androidx.test.ext:junit:1.1.5")
                implementation("junit:junit:4.13.2")
            }
        }
        /*
        Not going to support JS for the time being
        val jsMain by getting
        val jsTest by getting
         */

        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.ExperimentalStdlibApi")
            }
        }
    }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    packagingOptions {
        resources {
            merges += "**/**.proto"
        }
    }
}

sqldelight {
    database("PrismPlutoDb") {
        packageName = "io.iohk.atala.prism.walletsdk"
        sourceFolders = listOf("sqldelight")
    }
}

// Dokka implementation
tasks.withType<DokkaTask>().configureEach {
    moduleName.set(currentModuleName)
    moduleVersion.set(rootProject.version.toString())
    description = "This is a Kotlin Multiplatform implementation of AtalaPrismSDK"
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(11)
            languageVersion.set("1.7.20")
            apiVersion.set("2.0")
            includes.from(
                "docs/AtalaPrismSDK.md",
                "docs/Apollo.md",
                "docs/Castor.md",
                "docs/Mercury.md",
                "docs/Pluto.md",
                "docs/Pollux.md",
                "docs/PrismAgent.md"
            )
            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(URL("https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/tree/main/src"))
                remoteLineSuffix.set("#L")
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/latest/jvm/stdlib/"))
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx.serialization/"))
            }
            externalDocumentationLink {
                url.set(URL("https://api.ktor.io/"))
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx-datetime/"))
                packageListUrl.set(URL("https://kotlinlang.org/api/kotlinx-datetime/"))
            }
            externalDocumentationLink {
                url.set(URL("https://kotlinlang.org/api/kotlinx.coroutines/"))
            }
        }
    }
}

val antlrGenerationTask by tasks.register<com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask>("generateKotlinCommonGrammarSource") {
    // the classpath used to run antlr code generation
    antlrClasspath = configurations.detachedConfiguration(
        project.dependencies.create("com.github.piacenti:antlr-kotlin-runtime:0.0.14")
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

val buildProtoLibsGen by tasks.creating {
    group = "build"
    this.dependsOn(":protosLib:generateProto")
}

afterEvaluate {
    tasks.named("lintAnalyzeDebug") {
        this.enabled = false
    }
    tasks.named("lintAnalyzeRelease") {
        this.enabled = false
    }
    tasks.getByName("runKtlintCheckOverCommonMainSourceSet") {
        dependsOn(buildProtoLibsGen, antlrGenerationTask)
    }
    tasks.getByName("build") {
        dependsOn(buildProtoLibsGen, antlrGenerationTask)
    }

    tasks.withType<KotlinCompile> {
        dependsOn(buildProtoLibsGen, antlrGenerationTask)
    }

    tasks.withType<ProcessResources> {
        dependsOn(buildProtoLibsGen, antlrGenerationTask)
    }
}
