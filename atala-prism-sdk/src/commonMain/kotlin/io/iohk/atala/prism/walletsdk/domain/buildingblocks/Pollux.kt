package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import kotlinx.serialization.json.JsonObject

interface Pollux {
    fun parseVerifiableCredential(jwtString: String): VerifiableCredential

    fun createRequestCredentialJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        offerJson: JsonObject
    ): String

    fun createVerifiablePresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: VerifiableCredential,
        requestPresentationJson: JsonObject
    ): String
}
