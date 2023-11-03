plugins {
    kotlin("jvm") version "1.8.21"
    idea
    java
    id("com.github.ben-manes.versions") version "0.47.0"
    id("net.serenity-bdd.serenity-gradle-plugin") version "4.0.1"
}

group = "io.iohk.atala.prism"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/input-output-hk/atala-automation/")
        credentials {
            username = System.getenv("ATALA_GITHUB_ACTOR")
            password = System.getenv("ATALA_GITHUB_TOKEN")
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/hyperledger-labs/open-enterprise-agent/")
        credentials {
            username = System.getenv("ATALA_GITHUB_ACTOR")
            password = System.getenv("ATALA_GITHUB_TOKEN")
        }
    }
}

dependencies {
    testImplementation("io.iohk.atala.prism.walletsdk:atala-prism-sdk:2.4.0.a")
    testImplementation("io.iohk.atala.prism:prism-kotlin-client:1.9.2")
    testImplementation("io.iohk.atala:atala-automation:0.3.0")
}

tasks.test {
    testLogging.showStandardStreams = true
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags"))
}

kotlin {
    jvmToolchain(11)
}
