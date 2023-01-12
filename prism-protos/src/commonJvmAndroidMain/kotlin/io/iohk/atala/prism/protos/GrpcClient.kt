package io.iohk.atala.prism.protos

import io.grpc.*
import io.grpc.kotlin.ClientCalls
import io.grpc.stub.MetadataUtils
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import pbandk.Message
import pbandk.decodeFromStream
import pbandk.encodeToByteArray
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

public actual class GrpcClient actual constructor(options: GrpcOptions) : Closeable {
    private val channel: ManagedChannel =
        if (options.protocol == "http") {
            ManagedChannelBuilder.forAddress(options.host, options.port).usePlaintext().build()
        } else {
            ManagedChannelBuilder.forAddress(options.host, options.port).useTransportSecurity().build()
        }
    private val token: String? = options.token

    public class MessageMarshaller<T : Message>(private val companion: Message.Companion<T>) :
        MethodDescriptor.Marshaller<T> {
        override fun stream(value: T): InputStream =
            value.encodeToByteArray().inputStream()

        override fun parse(stream: InputStream?): T =
            companion.decodeFromStream(stream!!)
    }

    private fun <Req : Message, Resp : Message> methodDescriptorUnary(
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): MethodDescriptor<Req, Resp> {
        return MethodDescriptor
            .newBuilder<Req, Resp>()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
            .setRequestMarshaller(MessageMarshaller(reqCompanion))
            .setResponseMarshaller(MessageMarshaller(respCompanion))
            .build()
    }

    private fun <Req : Message, Resp : Message> methodDescriptorServerStreaming(
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): MethodDescriptor<Req, Resp> {
        return MethodDescriptor
            .newBuilder<Req, Resp>()
            .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
            .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
            .setRequestMarshaller(MessageMarshaller(reqCompanion))
            .setResponseMarshaller(MessageMarshaller(respCompanion))
            .build()
    }

    private fun toMetadata(prismMetadata: PrismMetadata): Metadata {
        val metadata = Metadata()
        if (token != null)
            metadata.put(PRISM_AUTH_TOKEN_HEADER, token)

        metadata.put(DID_HEADER, prismMetadata.did)
        metadata.put(DID_KEY_ID_HEADER, prismMetadata.didKeyId)
        metadata.put(
            DID_SIGNATURE_HEADER,
            Base64.getUrlEncoder().encode(prismMetadata.didSignature).decodeToString()
        )
        metadata.put(
            REQUEST_NONCE_HEADER,
            Base64.getUrlEncoder().encode(prismMetadata.requestNonce).decodeToString()
        )
        return metadata
    }

    private fun authChannel(prismMetadata: PrismMetadata): Channel {
        val metadata = toMetadata(prismMetadata)
        val interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)
        return ClientInterceptors.intercept(channel, interceptor)
    }

    private fun channelWithAuthTokenHeader(): Channel {
        val metadata = Metadata()
        if (token != null) {
            metadata.put(PRISM_AUTH_TOKEN_HEADER, token)
        }
        val interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)
        return ClientInterceptors.intercept(channel, interceptor)
    }

    public actual suspend fun <Req : Message, Resp : Message> call(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Resp {
        val methodDescriptor =
            methodDescriptorUnary(reqCompanion, respCompanion, serviceName, methodName)
        val tokenizedChannel = channelWithAuthTokenHeader()
        return ClientCalls.unaryRpc(tokenizedChannel, methodDescriptor, request)
    }

    public actual suspend fun <Req : Message, Resp : Message> callAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Resp {
        val authChannel = authChannel(prismMetadata)
        val methodDescriptor =
            methodDescriptorUnary(reqCompanion, respCompanion, serviceName, methodName)
        return ClientCalls.unaryRpc(authChannel, methodDescriptor, request)
    }

    public actual fun <Req : Message, Resp : Message> stream(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Flow<Resp> {
        val methodDescriptor =
            methodDescriptorServerStreaming(reqCompanion, respCompanion, serviceName, methodName)
        val tokenizedChannel = channelWithAuthTokenHeader()
        return ClientCalls.serverStreamingRpc(tokenizedChannel, methodDescriptor, request)
    }

    public actual fun <Req : Message, Resp : Message> streamAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Flow<Resp> {
        val authChannel = authChannel(prismMetadata)
        val methodDescriptor =
            methodDescriptorServerStreaming(reqCompanion, respCompanion, serviceName, methodName)
        return ClientCalls.serverStreamingRpc(authChannel, methodDescriptor, request)
    }

    public override fun close() {
        try {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
        } catch (err: Exception) {
            throw RuntimeException("Wasn't able to close the channel", err.cause)
        }
    }

    public companion object {
        public val DID_HEADER: Metadata.Key<String> =
            Metadata.Key.of(DID, Metadata.ASCII_STRING_MARSHALLER)
        public val DID_KEY_ID_HEADER: Metadata.Key<String> =
            Metadata.Key.of(DID_KEY_ID, Metadata.ASCII_STRING_MARSHALLER)
        public val DID_SIGNATURE_HEADER: Metadata.Key<String> =
            Metadata.Key.of(DID_SIGNATURE, Metadata.ASCII_STRING_MARSHALLER)
        public val REQUEST_NONCE_HEADER: Metadata.Key<String> =
            Metadata.Key.of(REQUEST_NONCE, Metadata.ASCII_STRING_MARSHALLER)
        public val PRISM_AUTH_TOKEN_HEADER: Metadata.Key<String> =
            Metadata.Key.of(PRISM_AUTH_TOKEN, Metadata.ASCII_STRING_MARSHALLER)
    }
}
