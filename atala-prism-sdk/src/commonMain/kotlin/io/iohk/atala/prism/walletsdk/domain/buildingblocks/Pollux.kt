package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import kotlinx.serialization.json.JsonObject

interface Pollux {
    fun parseVerifiableCredential(data: String): Credential

    fun createRequestCredentialJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String

    fun createVerifiablePresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: Credential,
        requestPresentationJson: JsonObject
    ): String

    fun restoreCredential(restorationIdentifier: String, credentialData: ByteArray): Credential

    fun credentialToStorableCredential(credential: Credential): StorableCredential
}
