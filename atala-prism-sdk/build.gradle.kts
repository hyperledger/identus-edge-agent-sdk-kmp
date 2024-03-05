import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

val currentModuleName: String = "AtalaPrismSDK"
val os: OperatingSystem = OperatingSystem.current()
val apolloVersion = project.property("apollo_version")
val didpeerVersion = project.property("didpeer_version")

plugins {
    id("app.cash.sqldelight") version "2.0.1"
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
                    "ioiohkatalaprismwalletsdkpluto.data"
                )
            }
        }
    }
}

/**
 * The `javadocJar` variable is used to register a `Jar` task to generate a Javadoc JAR file.
 * The Javadoc JAR file is created with the classifier "javadoc" and it includes the HTML documentation generated
 * by the `dokkaHtml` task.
 */
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

kotlin {
    androidTarget {
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
        publishing {
            publications {
                withType<MavenPublication> {
                    artifact(javadocJar)
                }
            }
        }
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
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

                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")

                api("org.lighthousegames:logging:1.1.2")

                implementation("io.iohk.atala.prism.anoncredskmp:anoncreds-kmp:0.4.2")
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
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
        }
        val jvmTest by getting
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
                implementation("io.ktor:ktor-client-okhttp:2.3.4")
                implementation("app.cash.sqldelight:android-driver:2.0.1")
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
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "io.iohk.atala.prism.walletsdk"
    defaultConfig {
        minSdk = 21
        targetSdk = 33

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
    databases {
        create("PrismPlutoDb") {
            packageName.set("io.iohk.atala.prism.walletsdk")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
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
            languageVersion.set("1.9.22")
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

val buildProtoLibsGen: Task by tasks.creating {
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
        dependsOn(buildProtoLibsGen)
    }
    tasks.getByName("build") {
        dependsOn(buildProtoLibsGen)
    }

    tasks.withType<KotlinCompile> {
        dependsOn(buildProtoLibsGen)
    }

    tasks.withType<ProcessResources> {
        dependsOn(buildProtoLibsGen)
    }
}
