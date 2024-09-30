import com.android.build.gradle.tasks.PackageAndroidArtifact
import com.android.build.gradle.tasks.SourceJarTask
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

val currentModuleName: String = "EdgeAgentSDK"
val os: OperatingSystem = OperatingSystem.current()
val apolloVersion = project.property("apollo_version")
val didpeerVersion = project.property("didpeer_version")

plugins {
    id("app.cash.sqldelight") version "2.0.1"
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_TOKEN")
            }
        }
    }
    publications {
        withType<MavenPublication> {
            artifactId = project.name
            version = project.version.toString()
            pom {
                name.set("Edge Agent SDK")
                description.set(" Edge Agent SDK - Kotlin Multiplatform (Android/JVM)")
                url.set("https://docs.atalaprism.io/")
                organization {
                    name.set("Hyperledger")
                    url.set("https://hyperledger.org/")
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/hyperledger/identus-edge-agent-sdk-kmp")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("cristianIOHK")
                        name.set("Cristian Gonzalez")
                        email.set("cristian.castro@iohk.io")
                        organization.set("IOG")
                        roles.add("developer")
                        url.set("https://github.com/cristianIOHK")
                    }
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
                    connection.set("scm:git:git://hyperledger/identus-edge-agent-sdk-kmp.git")
                    developerConnection.set("scm:git:ssh://hyperledger/identus-edge-agent-sdk-kmp.git")
                    url.set("https://github.com/hyperledger/identus-edge-agent-sdk-kmp")
                }
            }
        }
    }
}

if (System.getenv().containsKey("OSSRH_GPG_SECRET_KEY")) {
    signing {
        useInMemoryPgpKeys(
            System.getenv("OSSRH_GPG_SECRET_KEY"),
            System.getenv("OSSRH_GPG_SECRET_KEY_PASSWORD")
        )
        sign(publishing.publications)
    }
}

kover {
    useJacoco("0.8.11")
    excludeJavaCode()
    excludeInstrumentation {
        packages("androidx.test.espresso", "androidx.test.ext")
    }
}

koverReport {
    filters {
        excludes {
            packages(
                "org.hyperledger.identus.protos",
                "org.hyperledger.identus.walletsdk.domain",
                "org.hyperledger.identus.walletsdk.pluto.data"
            )
        }
    }

    defaults {
        xml {
            setReportFile(layout.buildDirectory.file("reports/jvm/result.xml"))
        }
        html {
            title = "Wallet SDK - JVM"
            setReportDir(layout.buildDirectory.dir("reports/jvm/html"))
        }
    }

    androidReports("release") {
        xml {
            setReportFile(layout.buildDirectory.file("reports/android/result.xml"))
        }
        html {
            title = "Wallet SDK - Android"
            setReportDir(layout.buildDirectory.dir("reports/android/html"))
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
                jvmTarget = "17"
            }
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
            kotlin.srcDir("${project(":protosLib").layout.buildDirectory.asFile.get()}/generated/source/proto/main/kotlin")
            resources.srcDir("${project(":protosLib").projectDir}/src/main")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

                implementation("io.ktor:ktor-client-core:2.3.11")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
                implementation("io.ktor:ktor-client-logging:2.3.11")
                implementation("io.ktor:ktor-websockets:2.3.11")

                implementation("io.iohk.atala.prism.didcomm:didpeer:$didpeerVersion")

                implementation("org.hyperledger.identus.apollo:apollo:$apolloVersion")

                implementation("org.kotlincrypto.hash:sha2:0.4.0")

                implementation("pro.streem.pbandk:pbandk-runtime:0.14.2")

                implementation("org.didcommx:didcomm:0.3.2") {
                    exclude("com.google.protobuf")
                }

                implementation("com.google.protobuf:protoc:3.12.0") {
                    exclude("com.google.protobuf")
                }

                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")

                api("org.lighthousegames:logging:1.1.2")

                implementation("org.hyperledger:anoncreds_uniffi:0.2.0-wrapper.1")
                implementation("com.ionspin.kotlin:bignum:0.3.9")
                implementation("org.bouncycastle:bcprov-jdk15on:1.68")
                implementation("eu.europa.ec.eudi:eudi-lib-jvm-sdjwt-kt:0.4.0") {
                    exclude(group = "com.nimbusds", module = "nimbus-jose-jwt")
                }
                implementation(kotlin("reflect"))
                implementation("org.bouncycastle:bcprov-jdk15on:1.68")

                implementation("com.apicatalog:titanium-json-ld-jre8:1.4.0")
                implementation("org.glassfish:jakarta.json:2.0.1")
                implementation("io.setl:rdf-urdna:1.3")

                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
                implementation("io.ktor:ktor-client-mock:2.3.11")
                implementation("org.mockito:mockito-core:4.4.0")
                implementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.11")
                implementation("io.ktor:ktor-client-java:2.3.11")
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
        }
        val jvmTest by getting
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
                implementation("io.ktor:ktor-client-okhttp:2.3.11")
                implementation("io.ktor:ktor-client-android:2.3.11")
                implementation("app.cash.sqldelight:android-driver:2.0.1")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                dependsOn(commonTest)
                implementation("androidx.test.espresso:espresso-core:3.5.1")
                implementation("androidx.test.ext:junit:1.1.5")
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
    compileSdk = 34
    namespace = "org.hyperledger.identus"
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

    packaging {
        resources {
            merges += "**/**.proto"
        }
    }
}

sqldelight {
    databases {
        create("SdkPlutoDb") {
            packageName.set("org.hyperledger.identus.walletsdk")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}

// Dokka implementation
tasks.withType<DokkaTask>().configureEach {
    moduleName.set(currentModuleName)
    moduleVersion.set(rootProject.version.toString())
    description = "This is a Kotlin Multiplatform implementation of Edge Agent SDK KMP"
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(17)
            languageVersion.set("1.9.22")
            apiVersion.set("2.0")
            includes.from(
                "docs/EdgeAgentSDK.md",
                "docs/Apollo.md",
                "docs/Castor.md",
                "docs/Mercury.md",
                "docs/Pluto.md",
                "docs/Pollux.md",
                "docs/EdgeAgent.md",
                "docs/BackUp.md"
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
    tasks.withType<PublishToMavenRepository> {
        dependsOn(tasks.withType<Sign>())
    }
    tasks.withType<PublishToMavenLocal> {
        dependsOn(tasks.withType<Sign>())
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
    tasks.withType<SourceJarTask> {
        dependsOn(buildProtoLibsGen)
    }
    tasks.withType<org.gradle.jvm.tasks.Jar> {
        dependsOn(buildProtoLibsGen)
    }
    tasks.withType<PackageAndroidArtifact> {
        dependsOn(buildProtoLibsGen)
    }
    tasks.named("packageDebugResources") {
        dependsOn(buildProtoLibsGen)
    }
    tasks.named("packageReleaseResources") {
        dependsOn(buildProtoLibsGen)
    }
    tasks.named("androidReleaseSourcesJar") {
        dependsOn(buildProtoLibsGen)
    }
    tasks.named("androidDebugSourcesJar") {
        dependsOn(buildProtoLibsGen)
    }
    tasks.named("jvmSourcesJar") {
        dependsOn(buildProtoLibsGen)
    }
    tasks.named("sourcesJar") {
        dependsOn(buildProtoLibsGen)
    }
}
