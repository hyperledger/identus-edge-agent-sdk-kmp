import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    explicitApi()
    explicitApi = ExplicitApiMode.Strict

    android {
        publishAllLibraryVariants()
    }
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    js(IR) {
        moduleName = "protos"
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.library()
        useCommonJs()

        val prismVersion: String by rootProject.extra
        compilations["main"].packageJson {
            version = prismVersion
        }

        compilations["test"].packageJson {
            version = prismVersion
        }
    }
    ios("ios") {
        binaries.all {}
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("${project(":protosLib").buildDir}/generated/source/proto/main/kotlin")
            resources.srcDir("${project(":protosLib").projectDir}/src/main")
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-new-mm-dev2")
                api("io.iohk:pbandk-runtime:0.20.7") {
                    exclude("com.google.protobuf")
                }
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
                implementation("io.ktor:ktor-io:1.6.5")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            kotlin.srcDir("src/commonJvmAndroidMain/kotlin")
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
        val jvmMain by getting {
            kotlin.srcDir("src/commonJvmAndroidMain/kotlin")
            kotlin.srcDir("${project(":protosLib").buildDir}/generated/source/proto/commonJvmAndroidMain/kotlin")
            dependencies {
                implementation("io.grpc:grpc-kotlin-stub:1.0.0")
                implementation("io.grpc:grpc-okhttp:1.36.0")
                implementation("io.grpc:grpc-protobuf-lite:1.36.0")
                implementationimplementation("io.iohk:protoc-gen-pbandk-jvm:0.20.7:jvm8@jar")
            }
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
            languageSettings.optIn("io.iohk.atala.prism.common.PrismSdkInternal")
        }
    }
}

android {
    compileSdkVersion(Versions.androidTargetSdk)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(29)
    }
}

tasks {
    "jvmTest"(Test::class) {
        useJUnitPlatform()
    }

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
