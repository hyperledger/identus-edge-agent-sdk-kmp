package io.iohk.atala.prism.domain.models

interface SecretResolver {
    fun resolve(secretIds: Array<String>)
}
