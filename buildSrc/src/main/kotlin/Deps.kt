object Deps {
    const val kotlinSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerializationJson}"
    const val kotlinBignum = "com.ionspin.kotlin:bignum:${Versions.kotlinBignum}"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val kotlinCoroutinesJDK8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesJDK8}"
    const val kotlinNodejs = "org.jetbrains.kotlinx:kotlinx-nodejs:${Versions.kotlinNodejs}"
    const val kotlinDatetime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinDatetime}"
    const val grpcKotlinStub = "io.grpc:grpc-kotlin-stub:${Versions.grpcKotlinStub}"
    const val grpcKotlinOkhttp = "io.grpc:grpc-okhttp:${Versions.grpcKotlinOkhttp}"

    const val bitcoinj = "org.bitcoinj:bitcoinj-core:${Versions.bitcoinj}"
    const val bitcoinKmp = "fr.acinq.bitcoin:bitcoin-kmp:${Versions.bitcoinKmp}"

    const val pbandkRuntime = "io.iohk:pbandk-runtime:${Versions.pbandk}"
    const val pbandkProtocGen = "io.iohk:protoc-gen-pbandk-lib-jvm:${Versions.pbandk}"
    // NOTE: this adds Kotlin interop with some of the jvm8 features (e.g. CompletableFuture).
    const val pbandkProtocGenJDK8 = "io.iohk:protoc-gen-pbandk-jvm:${Versions.pbandk}:jvm8@jar"
    const val pbandkPrismClientsGenerator = "io.iohk.atala:pbandk-prism-clients-generator:${Versions.pbandk}"

    const val protobufJava = "com.google.protobuf:protobuf-java:${Versions.protobuf}"
    const val protobufProtoc = "com.google.protobuf:protoc:${Versions.protobuf}"
    const val protobufLite = "io.grpc:grpc-protobuf-lite:${Versions.protobufLite}"

    const val betterParse = "com.github.h0tk3y.betterParse:better-parse:${Versions.betterParse}"
    const val krypto = "com.soywiz.korlibs.krypto:krypto:${Versions.krypto}"
    const val guava = "com.google.guava:guava:${Versions.guava}"
    const val spongyCastle = "com.madgag.spongycastle:prov:${Versions.spongyCastle}"
    const val bouncyCastle = "org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}"
    const val uuid = "com.benasher44:uuid:${Versions.uuid}"
}
