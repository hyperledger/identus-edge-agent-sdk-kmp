package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.walletsdk.domain.models.Secret
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface SecretsResolver {
    fun findSecrets(secretIds: Array<String>): Array<String>
    fun getSecret(secretId: String): Secret?
}
