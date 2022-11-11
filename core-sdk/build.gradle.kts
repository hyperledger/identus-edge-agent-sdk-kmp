import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target

version = rootProject.version
val currentModuleName: String = "wallet-core-sdk"
val os: OperatingSystem = OperatingSystem.current()

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
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
    if (os.isMacOsX) {
        ios()
        tvos()
        watchos()
        macosX64()
        if (System.getProperty("os.arch") != "x86_64") { // M1Chip
            iosSimulatorArm64()
            tvosSimulatorArm64()
            watchosSimulatorArm64()
            macosArm64()
        }
    }
    js(IR) {
        this.moduleName = currentModuleName
        this.binaries.executable()
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
            this.commonWebpackConfig {
                this.cssSupport {
                    this.enabled = true
                }
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

    if (os.isMacOsX) {
        cocoapods {
            this.summary = "Wallet-Core-SDK"
            this.version = rootProject.version.toString()
            this.authors = "IOG"
            this.ios.deploymentTarget = "13.0"
            this.osx.deploymentTarget = "12.0"
            this.tvos.deploymentTarget = "13.0"
            this.watchos.deploymentTarget = "8.0"
            framework {
                this.baseName = currentModuleName
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:2.1.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.1.3")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.1.3")
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
                implementation("io.ktor:ktor-client-js:2.1.3")
            }
        }
        val jsTest by getting
        if (os.isMacOsX) {
            val iosMain by getting {
                dependencies {
                    implementation("io.ktor:ktor-client-darwin:2.1.3")
                }
            }
            val iosTest by getting
            val tvosMain by getting {
                dependencies {
                    implementation("io.ktor:ktor-client-darwin:2.1.3")
                }
            }
            val tvosTest by getting
            val watchosMain by getting {
                dependencies {
                    implementation("io.ktor:ktor-client-darwin:2.1.3")
                }
            }
            val watchosTest by getting
            val macosX64Main by getting {
                dependencies {
                    implementation("io.ktor:ktor-client-darwin:2.1.3")
                }
            }
            val macosX64Test by getting
            if (System.getProperty("os.arch") != "x86_64") { // M1Chip
                val iosSimulatorArm64Main by getting {
                    this.dependsOn(iosMain)
                }
                val iosSimulatorArm64Test by getting {
                    this.dependsOn(iosTest)
                }
                val tvosSimulatorArm64Main by getting {
                    this.dependsOn(tvosMain)
                }
                val tvosSimulatorArm64Test by getting {
                    this.dependsOn(tvosTest)
                }
                val watchosSimulatorArm64Main by getting {
                    this.dependsOn(watchosMain)
                }
                val watchosSimulatorArm64Test by getting {
                    this.dependsOn(watchosTest)
                }
                val macosArm64Main by getting {
                    this.dependsOn(macosX64Main)
                }
                val macosArm64Test by getting {
                    this.dependsOn(macosX64Test)
                }
            }
        }
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