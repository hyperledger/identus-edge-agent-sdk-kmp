import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target

val currentModuleName: String = "WalletApollo"
val os: OperatingSystem = OperatingSystem.current()
val apolloVersion = project.property("apollo_version")
plugins {
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

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.iohk.atala.prism:base-asymmetric-encryption:$apolloVersion")
                implementation("io.iohk.atala.prism:ecdsa:$apolloVersion")
                implementation("com.nimbusds:nimbus-jose-jwt:9.31")
                implementation(project(":domain"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.slf4j:slf4j-simple:1.7.30")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(kotlin("reflect"))
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
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.7.20")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.7.20")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation(npm("stream-browserify", "3.0.0"))
                implementation(npm("buffer", "6.0.3"))
            }
        }
        val jsTest by getting

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

ktlint {
    filter {
        exclude("build/generated-src/**")
        exclude("**/generated/**")
        exclude("**/generated-src/**")
        exclude {
            it.file.path.contains("generated-src")
        }
        exclude {
            it.file.path.contains("generated")
        }
    }
}

// Dokka implementation
tasks.withType<DokkaTask> {
    moduleName.set(project.name)
    moduleVersion.set(rootProject.version.toString())
    description = """
        This is a Kotlin Multiplatform Wallet-Core-SDK Library
    """.trimIndent()
    dokkaSourceSets {
        // TODO: Figure out how to include files to the documentations
        named("commonMain") {
            includes.from("Module.md", "docs/Module.md")
        }
    }
}

// afterEvaluate {
//    tasks.withType<AbstractTestTask> {
//        testLogging {
//            events("passed", "skipped", "failed", "standard_out", "standard_error")
//            showExceptions = true
//            showStackTraces = true
//        }
//    }
// }
