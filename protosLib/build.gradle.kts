import com.google.protobuf.gradle.*
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    `java-library`
    id(Plugins.protobuf)
}

// Mock configuration which derives compile only.
// Needed to resolve jar files of the dependency
val jarPathConf by configurations.creating {
    extendsFrom(configurations.compileOnly.get())
}

dependencies {
    jarPathConf(Deps.pbandkPrismClientsGenerator)

    // This is needed for includes, ref: https://github.com/google/protobuf-gradle-plugin/issues/41#issuecomment-143884188
    compileOnly(Deps.protobufJava)
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
        artifact = if (Os.isFamily(Os.FAMILY_MAC)) {
            if (System.getProperty("os.arch") != "x86_64") {
                // In case of macOS and M1 chip then we need to use a different version of protobuf that support M1 chip arch
                "${Deps.protobufProtoc}:osx-x86_64" // "com.google.protobuf:protoc:3.12.0:osx-x86_64"
            } else {
                Deps.protobufProtoc
            }
        } else {
            Deps.protobufProtoc
        }
    }
    plugins {
        id("kotlin") {
            artifact = Deps.pbandkProtocGenJDK8
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
                    option("with_annotations=@io.iohk.atala.prism.common.PrismSdkInternal")
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
