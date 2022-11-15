import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

version = rootProject.version
val currentModuleName: String = "protos"
val os: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
    mavenLocal()
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
            kotlin.srcDir("${project(":protosLib").buildDir}/generated/source/proto/main/kotlin")
            resources.srcDir("${project(":protosLib").projectDir}/src/main")
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                api("io.iohk:pbandk-runtime:0.20.7") {
                    exclude("com.google.protobuf")
                }
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("io.ktor:ktor-io:2.1.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val commonJvmAndroidMain by creating {
            dependsOn(commonMain)
        }
        val commonJvmAndroidTest by creating {
            dependsOn(commonTest)
        }
        val androidMain by getting {
            dependsOn(commonJvmAndroidMain) // kotlin.srcDir("src/commonJvmAndroidMain/kotlin")
            kotlin.srcDir("${project(":protosLib").buildDir}/generated/source/proto/commonJvmAndroidMain/kotlin")
            dependencies {
                implementation("io.grpc:grpc-kotlin-stub:1.0.0") {
                    exclude("io.grpc", "grpc-protobuf")
                    exclude("com.google.protobuf")
                }
                implementation("io.grpc:grpc-okhttp:1.36.0")
                implementation("io.grpc:grpc-protobuf-lite:1.36.0")
                implementation("io.iohk:protoc-gen-pbandk-jvm:0.20.7:jvm8@jar")
            }
        }
        val androidTest by getting {
            dependsOn(commonJvmAndroidTest)
        }
        val jvmMain by getting {
            dependsOn(commonJvmAndroidMain) // kotlin.srcDir("src/commonJvmAndroidMain/kotlin")
            kotlin.srcDir("${project(":protosLib").buildDir}/generated/source/proto/commonJvmAndroidMain/kotlin")
            dependencies {
                implementation("io.grpc:grpc-kotlin-stub:1.0.0")
                implementation("io.grpc:grpc-okhttp:1.36.0")
                implementation("io.grpc:grpc-protobuf-lite:1.36.0")
                implementation("io.iohk:protoc-gen-pbandk-jvm:0.20.7:jvm8@jar")
            }
        }
        val jvmTest by getting {
            dependsOn(commonJvmAndroidTest)
        }
        val jsMain by getting {
            kotlin.srcDir("${project(":protosLib").buildDir}/generated/source/proto/jsMain/kotlin")
            dependencies {
                implementation(npm("grpc-web", "1.2.1"))
                // Polyfill dependencies
                implementation(npm("stream-browserify", "3.0.0"))
                implementation(npm("buffer", "6.0.3"))
            }
        }
        val jsTest by getting
        val iosMain by getting
        val iosTest by getting

        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
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

tasks {
    project(":protosLib").tasks
        .matching { it.name == "generateProto" }
        .all {
            val compileTasks = listOf<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>(
                named<KotlinCompile>("compileReleaseKotlinAndroid").get(),
                named<KotlinCompile>("compileDebugKotlinAndroid").get(),
                named<KotlinCompile>("compileKotlinJvm").get(),
                named<KotlinJsCompile>("compileKotlinJs").get(),
                named<KotlinNativeCompile>("compileKotlinIosX64").get(),
                named<KotlinNativeCompile>("compileKotlinIosArm64").get(),
                named<KotlinCompileCommon>("compileKotlinMetadata").get()
            )

            compileTasks.forEach {
                it.dependsOn(this)
                it.kotlinOptions {
                    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlinx.coroutines.DelicateCoroutinesApi,kotlin.RequiresOptIn"
                    suppressWarnings = true
                }
            }
        }
}
