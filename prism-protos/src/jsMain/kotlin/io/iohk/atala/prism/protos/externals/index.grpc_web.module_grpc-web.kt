@file:JsModule("grpc-web")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")
package io.iohk.atala.prism.protos.externals

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

internal external interface Metadata {
    @nativeGetter
    operator fun get(s: String): String?
    @nativeSetter
    operator fun set(s: String, value: String)
}

internal external open class AbstractClientBase {
    open fun <REQ, RESP> thenableCall(method: String, request: REQ, metadata: Metadata, methodDescriptor: MethodDescriptor<REQ, RESP>): Promise<RESP>
    open fun <REQ, RESP> rpcCall(method: String, request: REQ, metadata: Metadata, methodDescriptor: MethodDescriptor<REQ, RESP>, callback: (err: Error, response: RESP) -> Unit): ClientReadableStream<RESP>
    open fun <REQ, RESP> serverStreaming(method: String, request: REQ, metadata: Metadata, methodDescriptor: MethodDescriptor<REQ, RESP>): ClientReadableStream<RESP>
    open class MethodInfo<REQ, RESP>(responseType: Any, requestSerializeFn: (request: REQ) -> Any, responseDeserializeFn: (bytes: Uint8Array) -> RESP)
}

internal external open class ClientReadableStream<RESP> {
    open fun on(eventType: String /* "error" */, callback: (err: Error) -> Unit): ClientReadableStream<RESP>
    open fun on(eventType: String /* "status" */, callback: (status: Status) -> Unit): ClientReadableStream<RESP>
    open fun on(eventType: String /* "metadata" */, callback: (status: Metadata) -> Unit): ClientReadableStream<RESP>
    open fun on(eventType: String /* "data" */, callback: (response: RESP) -> Unit): ClientReadableStream<RESP>
    open fun on(eventType: String /* "end" */, callback: () -> Unit): ClientReadableStream<RESP>
    open fun removeListener(eventType: String /* "error" */, callback: (err: Error) -> Unit)
    open fun removeListener(eventType: String /* "status" */, callback: (status: Status) -> Unit)
    open fun removeListener(eventType: String /* "metadata" */, callback: (status: Metadata) -> Unit)
    open fun removeListener(eventType: String /* "data" */, callback: (response: RESP) -> Unit)
    open fun removeListener(eventType: String /* "end" */, callback: () -> Unit)
    open fun cancel()
}

internal external interface StreamInterceptor<REQ, RESP> {
    fun intercept(request: Request<REQ, RESP>, invoker: (request: Request<REQ, RESP>) -> ClientReadableStream<RESP>): ClientReadableStream<RESP>
}

internal external interface UnaryInterceptor<REQ, RESP> {
    fun intercept(request: Request<REQ, RESP>, invoker: (request: Request<REQ, RESP>) -> Promise<UnaryResponse<REQ, RESP>>): Promise<UnaryResponse<REQ, RESP>>
}

internal external open class CallOptions(options: Json)

internal external open class MethodDescriptor<REQ, RESP>(name: String, methodType: Any, requestType: Any, responseType: Any, requestSerializeFn: Any, responseDeserializeFn: Any) {
    open fun createRequest(requestMessage: REQ, metadata: Metadata, callOptions: CallOptions): UnaryResponse<REQ, RESP>
}

internal external open class Request<REQ, RESP> {
    open fun getRequestMessage(): REQ
    open fun getMethodDescriptor(): MethodDescriptor<REQ, RESP>
    open fun getMetadata(): Metadata
    open fun getCallOptions(): CallOptions
}

internal external open class UnaryResponse<REQ, RESP> {
    open fun getResponseMessage(): RESP
    open fun getMetadata(): Metadata
    open fun getMethodDescriptor(): MethodDescriptor<REQ, RESP>
    open fun getStatus(): Status
}

internal external interface GrpcWebClientBaseOptions {
    var format: String?
        get() = definedExternally
        set(value) = definedExternally
    var suppressCorsPreflight: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

internal external open class GrpcWebClientBase(options: GrpcWebClientBaseOptions) : AbstractClientBase

internal external interface Error {
    var code: Number
    var message: String
}

internal external interface Status {
    var code: Number
    var details: String
    var metadata: Metadata?
        get() = definedExternally
        set(value) = definedExternally
}