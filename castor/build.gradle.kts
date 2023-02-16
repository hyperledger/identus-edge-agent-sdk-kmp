import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target

val currentModuleName: String = "WalletCastor"
val os: OperatingSystem = OperatingSystem.current()
val apolloVersion = project.property("apollo_version")
val didpeerVersion = project.property("didpeer_version")

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
                implementation(project(":domain"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.iohk.atala.prism:didpeer:$didpeerVersion")
                implementation("io.iohk.atala.prism:apollo:$apolloVersion")
                api("io.iohk:pbandk-runtime:0.20.7") {
                    exclude("com.google.protobuf")
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":apollo"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation(kotlin("test"))
            }
        }
        val allButJSMain by creating {
            this.dependsOn(commonMain)
        }
        val allButJSTest by creating {
            this.dependsOn(commonTest)
        }
        val jvmMain by getting {
            this.dependsOn(allButJSMain)
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(kotlin("reflect"))
            }
        }
        val jvmTest by getting {
            this.dependsOn(allButJSTest)
        }
        val androidMain by getting {
            this.dependsOn(allButJSMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
            }
        }
        val androidTest by getting {
            this.dependsOn(allButJSTest)
        }
        val jsMain by getting {
            dependsOn(commonAntlr)
            dependencies {
                implementation("com.github.piacenti:antlr-kotlin-runtime-js:0.0.14")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.7.20")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.7.20")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
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
        // it.name == "compileCommonMainKotlinMetadata" ||
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
