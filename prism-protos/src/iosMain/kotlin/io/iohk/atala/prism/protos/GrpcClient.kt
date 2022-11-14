package io.iohk.atala.prism.protos

import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.flow.Flow
import pbandk.Message

public actual class GrpcClient actual constructor(options: GrpcOptions) : Closeable {
    public actual suspend fun <Req : Message, Resp : Message> call(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Resp = TODO("iOS GRPC client is not supported yet")

    public actual suspend fun <Req : Message, Resp : Message> callAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Resp = TODO("iOS GRPC client is not supported yet")

    public actual fun <Req : Message, Resp : Message> stream(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Flow<Resp> = TODO("iOS GRPC client is not supported yet")

    public actual fun <Req : Message, Resp : Message> streamAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Flow<Resp> = TODO("iOS GRPC client is not supported yet")

    public override fun close() {
    }
}
