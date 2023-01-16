package io.iohk.atala.prism.domain.models

interface SecretResolver {

    fun resolve(secretIds: Array<String>)

//    fun resolve(secretId:Array<String>) TODO: Is this really necessary?
}
