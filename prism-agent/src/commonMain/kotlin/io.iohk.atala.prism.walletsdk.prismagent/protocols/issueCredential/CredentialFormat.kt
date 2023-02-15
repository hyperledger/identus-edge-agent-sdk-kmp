package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class CredentialFormat(val attachId: String, val format: String)
