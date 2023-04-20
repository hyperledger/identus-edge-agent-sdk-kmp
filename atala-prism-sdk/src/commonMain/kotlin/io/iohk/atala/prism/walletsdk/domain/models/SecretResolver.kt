package io.iohk.atala.prism.walletsdk.domain.models

interface SecretResolver {
    fun resolve(secretIds: Array<String>)
}
