import org.gradle.internal.os.OperatingSystem
import com.google.protobuf.gradle.*

val os: OperatingSystem = OperatingSystem.current()

plugins {
    `java-library`
    id("com.google.protobuf")
}

// Mock configuration which derives compile only.
// Needed to resolve jar files of the dependency
val jarPathConf by configurations.creating {
    extendsFrom(configurations.compileOnly.get())
}

dependencies {
    jarPathConf("io.iohk.atala:pbandk-prism-clients-generator:0.20.7")

    // This is needed for includes, ref: https://github.com/google/protobuf-gradle-plugin/issues/41#issuecomment-143884188
    // compileOnly("com.google.protobuf:protobuf-java:3.21.9")
}

sourceSets {
    main {
        proto {
            setSrcDirs(listOf("src/main/proto"))
            setIncludes(
                listOf(
                    "common_*.proto",
                    "node_*.proto",
                    "connector_*.proto",
                    "console_*.proto",
                    "status.proto",
                    "credential_*.proto"
                )
            )
        }
    }
}

protobuf {
    protoc {
        artifact = if (os.isMacOsX) {
            if (System.getProperty("os.arch") != "x86_64") {
                // In case of macOS and M1 chip then we need to use a different version of protobuf that support M1 chip arch
                "com.google.protobuf:protoc:3.21.9:osx-x86_64" // "com.google.protobuf:protoc:3.12.0:osx-x86_64"
            } else {
                "com.google.protobuf:protoc:3.21.9"
            }
        } else {
            "com.google.protobuf:protoc:3.21.9"
        }
    }
    plugins {
        id("kotlin") {
            artifact = "io.iohk:protoc-gen-pbandk-jvm:0.20.7:jvm8@jar"
        }
    }

    val pbandkClientsGeneratorJar = configurations["jarPathConf"].files(configurations["jarPathConf"].dependencies.first()).first()

    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            task.builtins {
                remove("java")
            }
            task.plugins {
                id("kotlin") {
                    option("kotlin_package=io.iohk.atala.prism.protos")
                    option("kotlin_service_gen=$pbandkClientsGeneratorJar|io.iohk.atala.prism.generator.Generator")
                    option("visibility=public")
                    option("js_export=true")
                }
            }
        }
    }
}

tasks {
    compileJava {
        enabled = false
    }
}
