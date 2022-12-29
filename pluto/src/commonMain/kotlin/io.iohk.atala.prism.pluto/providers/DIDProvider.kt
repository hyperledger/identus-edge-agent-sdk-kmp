package io.iohk.atala.prism.pluto

import io.iohk.atala.prism.pluto.models.DID

interface DIDProvider {

    fun getAll()

    fun getDIDInfo(alias: String)

    fun getDIDInfo(did: DID)

    fun getDIDInfo(keyPairIndex: Int)

    fun getLastKeyPairIndex()
}