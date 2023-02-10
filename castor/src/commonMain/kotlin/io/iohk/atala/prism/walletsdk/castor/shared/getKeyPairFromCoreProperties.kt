package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocumentCoreProperty
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey

fun getKeyPairFromCoreProperties(coreProperties: Array<DIDDocumentCoreProperty>): List<PublicKey> {
    return coreProperties
        .filterIsInstance<DIDDocument.Authentication>()
        .flatMap { it.verificationMethods.toList() }
        .mapNotNull {
            it.publicKeyMultibase?.let { publicKey ->
                PublicKey(
                    curve = KeyCurve(DIDDocument.VerificationMethod.getCurveByType(it.type)),
                    value = publicKey.encodeToByteArray()
                )
            }
        }
}
