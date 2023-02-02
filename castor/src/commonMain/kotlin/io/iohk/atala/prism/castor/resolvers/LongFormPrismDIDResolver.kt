package io.iohk.atala.prism.castor.io.iohk.atala.prism.castor.resolvers

import io.iohk.atala.prism.apollo.base64.base64Encoded
import io.iohk.atala.prism.castor.did.DIDParser
import io.iohk.atala.prism.castor.io.iohk.atala.prism.castor.did.prismdid.LongFormPrismDID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.DIDResolver
import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.apollo.hashing.SHA256
import io.iohk.atala.prism.castor.did.prismdid.PrismDIDPublicKey
import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.models.CastorError
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocumentCoreProperty
import io.iohk.atala.prism.domain.models.DIDUrl
import io.iohk.atala.prism.protos.AtalaOperation
import pbandk.decodeFromByteArray

class LongFormPrismDIDResolver(
    private val apollo: Apollo,
    ) : DIDResolver {
    override val method: String = "prism"

    override suspend fun resolve(didString: String): DIDDocument {
        val did = DIDParser.parse(didString)
        val prismDID = LongFormPrismDID(did)

        val data = try {
            prismDID.encodedState.base64UrlDecoded
        } catch (e: Throwable) {
            //TODO: Add logger here
            throw CastorError.InitialStateOfDIDChanged(e.message)
        }

        val (verificationMethods, services) = decodeState(
            did = did,
            stateHash = prismDID.stateHash,
            encodedData = data.encodeToByteArray()
        )


        val authenticate = verificationMethods.entries.map {
            DIDDocument.Authentication(
                urls = arrayOf(it.key),
                verificationMethods = arrayOf()
            )
        }


        val servicesProperty = DIDDocument.Services(services.toTypedArray())
        val verificationMethodsProperty = DIDDocument.VerificationMethods(verificationMethods.values.toTypedArray())

        val coreProperties = mutableListOf<DIDDocumentCoreProperty>()

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

    private fun decodeState(did: DID, stateHash: String, encodedData: ByteArray): Pair<Map<String, DIDDocument.VerificationMethod>, List<DIDDocument.Service>> {
        val sha256 = SHA256()

        sha256.update(encodedData)

        val verifyEncodedState = sha256.digest().toString()

        require (stateHash == verifyEncodedState) {
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

        var verificationMethods = publicKeys.fold(emptyMap<String, DIDDocument.VerificationMethod>()) { partialResult, publicKey ->
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
            partialResult + (didUrl.toString() to method)
        }

        return Pair(verificationMethods, services)
    }
}
