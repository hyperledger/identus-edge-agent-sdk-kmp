import com.google.protobuf.gradle.* // ktlint-disable no-wildcard-imports
import org.gradle.internal.os.OperatingSystem

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
    // This is needed for includes, ref: https://github.com/google/protobuf-gradle-plugin/issues/41#issuecomment-143884188
    compileOnly("com.google.protobuf:protobuf-java:3.12.0")
}

sourceSets {
    main {
        proto {
            setSrcDirs(listOf("src/main/proto"))
            setIncludes(
                listOf(
                    "common_*.proto",
                    "node_*.proto"
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
                "com.google.protobuf:protoc:3.12.0:osx-x86_64" // "com.google.protobuf:protoc:3.12.0:osx-x86_64"
            } else {
                "com.google.protobuf:protoc:3.12.0"
            }
        } else {
            "com.google.protobuf:protoc:3.12.0"
        }
    }
    plugins {
        id("kotlin") {
            artifact = "pro.streem.pbandk:protoc-gen-pbandk-jvm:0.14.2:jvm8@jar"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            task.builtins {
                remove("java")
            }
            task.plugins {
                id("kotlin") {
                    option("kotlin_package=io.iohk.atala.prism.protos")
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
