package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import kotlinx.serialization.json.JsonObject

class PolluxMock : Pollux {

    override fun parseVerifiableCredential(jwtString: String): Credential {
        TODO("Not yet implemented")
    }

    override fun createRequestCredentialJWT(subjectDID: DID, privateKey: PrivateKey, offerJson: JsonObject): String {
        TODO("Not yet implemented")
    }

    override fun createVerifiablePresentationJWT(
        subjectDID: DID,
        privateKey: PrivateKey,
        credential: Credential,
        requestPresentationJson: JsonObject
    ): String {
        TODO("Not yet implemented")
    }

    override fun restoreCredential(restorationIdentifier: String, credentialData: ByteArray): Credential {
        TODO("Not yet implemented")
    }

    override fun credentialToStorableCredential(credential: Credential): StorableCredential {
        TODO("Not yet implemented")
    }
}
