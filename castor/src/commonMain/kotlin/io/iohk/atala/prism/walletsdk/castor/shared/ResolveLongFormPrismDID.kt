package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.apollo.base64.base64Encoded
import io.iohk.atala.prism.apollo.base64.base64UrlDecodedBytes
import io.iohk.atala.prism.apollo.hashing.SHA256
import io.iohk.atala.prism.apollo.hashing.internal.toHexString
import io.iohk.atala.prism.protos.AtalaOperation
import io.iohk.atala.prism.walletsdk.castor.did.DIDParser
import io.iohk.atala.prism.walletsdk.castor.did.prismdid.PrismDIDPublicKey
import io.iohk.atala.prism.walletsdk.castor.io.iohk.atala.prism.walletsdk.castor.did.prismdid.LongFormPrismDID
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocumentCoreProperty
import io.iohk.atala.prism.walletsdk.domain.models.DIDUrl
import pbandk.decodeFromByteArray

suspend fun ResolveLongFormPrismDID(apollo: Apollo, didString: String): DIDDocument {
    val did = DIDParser.parse(didString)
    val prismDID = LongFormPrismDID(did)

    val (verificationMethods, services) = try {
        decodeState(
            apollo = apollo,
            did = did,
            stateHash = prismDID.stateHash,
            encodedData = prismDID.encodedState.base64UrlDecodedBytes
        )
    } catch (e: Throwable) {
        // TODO: Add logger here
        throw CastorError.InitialStateOfDIDChanged(e.message)
    }

    val servicesProperty = DIDDocument.Services(services.toTypedArray())
    val verificationMethodsProperty = DIDDocument.VerificationMethods(verificationMethods.values.toTypedArray())
    val coreProperties = mutableListOf<DIDDocumentCoreProperty>()

    val authenticate = verificationMethods.entries.map {
        DIDDocument.Authentication(
            urls = arrayOf(it.key),
            verificationMethods = verificationMethods.values.toTypedArray()
        )
    }

    authenticate.forEach {
        coreProperties.add(it)
    }

    coreProperties.add(servicesProperty)
    coreProperties.add(verificationMethodsProperty)

    return DIDDocument(
        id = did,
        coreProperties = coreProperties.toTypedArray()
    )
}

internal fun decodeState(
    apollo: Apollo,
    did: DID,
    stateHash: String,
    encodedData: ByteArray,
): Pair<Map<String, DIDDocument.VerificationMethod>, List<DIDDocument.Service>> {

    val sha256 = SHA256()
    val verifyEncodedState = sha256.digest(encodedData)
    val verifyEncodedStateHex = verifyEncodedState.toHexString()

    require(stateHash == verifyEncodedStateHex) {
        throw CastorError.InitialStateOfDIDChanged()
    }

    val operation = AtalaOperation.decodeFromByteArray(encodedData)

    val publicKeys = operation.createDid?.didData?.publicKeys?.map {
        try {
            PrismDIDPublicKey(apollo, it)
        } catch (e: Exception) {
            throw e
        }
    } ?: listOf()

    val services = operation.createDid?.didData?.services?.map {
        DIDDocument.Service(
            it.id,
            arrayOf(it.type),
            DIDDocument.ServiceEndpoint(
                uri = it.serviceEndpoint.first()
            )
        )
    } ?: listOf()

    var verificationMethods =
        publicKeys.fold(emptyMap<String, DIDDocument.VerificationMethod>()) { partialResult, publicKey ->
            val didUrl = DIDUrl(
                did = did,
                fragment = publicKey.id
            )
            val method = DIDDocument.VerificationMethod(
                id = didUrl,
                controller = did,
                type = publicKey.keyData.curve.curve.value,
                publicKeyMultibase = publicKey.keyData.value.base64Encoded
            )
            partialResult + (didUrl.string() to method)
        }

    return Pair(verificationMethods, services)
}
