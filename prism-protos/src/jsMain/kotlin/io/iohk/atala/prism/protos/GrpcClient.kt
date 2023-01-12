package io.iohk.atala.prism.protos

import io.iohk.atala.prism.protos.externals.*
import io.iohk.atala.prism.protos.util.Base64Utils
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import pbandk.Message
import pbandk.decodeFromByteArray
import pbandk.encodeToByteArray
import kotlin.js.json

public actual class GrpcClient actual constructor(
    private val options: GrpcOptions
) : Closeable {
    private fun channel(serviceName: String, methodName: String) =
        "${options.protocol}://${options.host}:${options.port}/$serviceName/$methodName"

    private fun <Req : Message, Resp : Message> methodDescriptor(
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): MethodDescriptor<Req, Resp> {
        return MethodDescriptor(
            "/$serviceName/$methodName",
            "unary",
            { reqCompanion.asDynamic().defaultInstance },
            { respCompanion.asDynamic().defaultInstance },
            { req: Req -> req.encodeToByteArray() },
            { b: ByteArray -> respCompanion.decodeFromByteArray(b) }
        )
    }

    public actual suspend fun <Req : Message, Resp : Message> call(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Resp =
        call(
            request,
            reqCompanion,
            respCompanion,
            serviceName,
            methodName,
            metadataFromOptions()
        )

    public actual suspend fun <Req : Message, Resp : Message> callAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Resp =
        call(
            request,
            reqCompanion,
            respCompanion,
            serviceName,
            methodName,
            metadata(prismMetadata)
        )

    public actual fun <Req : Message, Resp : Message> stream(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Flow<Resp> = callbackFlow {
        stream(
            request,
            reqCompanion,
            respCompanion,
            serviceName,
            methodName,
            metadataFromOptions()
        )
    }

    public actual fun <Req : Message, Resp : Message> streamAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Flow<Resp> =
        callbackFlow {
            stream(
                request,
                reqCompanion,
                respCompanion,
                serviceName,
                methodName,
                metadata(prismMetadata)
            )
        }

    private suspend fun <Req : Message, Resp : Message> stream(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        metadata: Metadata
    ): Flow<Resp> = callbackFlow {
        val client = GrpcWebClientBase(object : GrpcWebClientBaseOptions {})
        val methodDescriptor =
            methodDescriptor(reqCompanion, respCompanion, serviceName, methodName)

        val stream: ClientReadableStream<Resp> = client.serverStreaming(
            channel(serviceName, methodName),
            request,
            metadata,
            methodDescriptor
        )

        stream.on("data") { response: Resp -> trySend(response) }
        stream.on("error") { error: Error -> close(RuntimeException(error.message)) }
        stream.on("end") { -> close() }

        awaitClose { cancel() }
    }

    private suspend fun <Req : Message, Resp : Message> call(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        metadata: Metadata
    ): Resp {
        val client = GrpcWebClientBase(object : GrpcWebClientBaseOptions {})
        val methodDescriptor =
            methodDescriptor(reqCompanion, respCompanion, serviceName, methodName)

        return client.thenableCall(
            channel(serviceName, methodName),
            request,
            metadata,
            methodDescriptor
        ).await()
    }

    private fun metadataFromOptions() = json(
        PRISM_AUTH_TOKEN to options.token
    ).unsafeCast<Metadata>()

    private fun metadata(prismMetadata: PrismMetadata) = json(
        DID to prismMetadata.did,
        DID_KEY_ID to prismMetadata.didKeyId,
        DID_SIGNATURE to Base64Utils.encode(prismMetadata.didSignature),
        REQUEST_NONCE to Base64Utils.encode(prismMetadata.requestNonce),
        PRISM_AUTH_TOKEN to options.token // See also metadataFromOptions
    ).unsafeCast<Metadata>()

    public override fun close() {
    }
}
