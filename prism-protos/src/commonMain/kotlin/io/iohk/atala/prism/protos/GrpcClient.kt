package io.iohk.atala.prism.protos

import io.iohk.atala.prism.common.PrismSdkInternal
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.flow.Flow
import pbandk.Message
import kotlin.js.JsExport
import kotlin.jvm.JvmOverloads

@JsExport
public data class GrpcOptions @JvmOverloads constructor(val protocol: String, val host: String, val port: Int, val token: String? = null)

@JsExport
public class PrismMetadata(
    public val did: String,
    public val didKeyId: String,
    public val didSignature: ByteArray,
    public val requestNonce: ByteArray
)

public const val DID: String = "did"
public const val DID_KEY_ID: String = "did-key-id"
public const val DID_SIGNATURE: String = "did-signature"
public const val REQUEST_NONCE: String = "request-nonce"
public const val PRISM_AUTH_TOKEN: String = "prism-auth-token"

@PrismSdkInternal
public expect class GrpcClient(options: GrpcOptions) : Closeable {
    public suspend fun <Req : Message, Resp : Message> call(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Resp

    public suspend fun <Req : Message, Resp : Message> callAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Resp

    public fun <Req : Message, Resp : Message> stream(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String
    ): Flow<Resp>

    public fun <Req : Message, Resp : Message> streamAuth(
        request: Req,
        reqCompanion: Message.Companion<Req>,
        respCompanion: Message.Companion<Resp>,
        serviceName: String,
        methodName: String,
        prismMetadata: PrismMetadata
    ): Flow<Resp>
}
