package org.hyperledger.identus.walletsdk.castor.resolvers

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.didcommx.didcomm.common.Typ
import org.hyperledger.identus.walletsdk.castor.PRISM
import org.hyperledger.identus.walletsdk.castor.did.DIDUrlParser
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.DIDResolver
import org.hyperledger.identus.walletsdk.domain.models.KeyValue
import org.hyperledger.identus.walletsdk.domain.models.httpClient

class PrismDIDApiResolver(
    private val apollo: Apollo,
    private val cloudAgentUrl: String,
    private val api: Api? = ApiImpl(
        httpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }
        }
    )
) : DIDResolver {
    override val method: String = PRISM

    override suspend fun resolve(didString: String): DIDDocument {
        val response = api!!.request(
            HttpMethod.Get.value,
            "${this.cloudAgentUrl}/dids/$didString",
            emptyArray(),
            arrayOf(
                KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ),
                KeyValue(HttpHeaders.Accept, "*/*"),
            ),
            null
        )

        if (response.status != 200) {
            throw CastorError.NotPossibleToResolveDID(did = didString, reason = response.jsonString)
        }

        val body = Json.parseToJsonElement(response.jsonString)
        val didDocJson = body.jsonObject["didDocument"].toString()

        val didDocument = didDocumentFromJson(didDocJson)

        return didDocument
    }
}

private fun didDocumentFromJson(jsonString: String): DIDDocument {
    val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
    val id = if (jsonObject.containsKey("id") && jsonObject["id"] != null && jsonObject["id"] is JsonPrimitive) {
        DID(jsonObject["id"]!!.jsonPrimitive.content)
    } else {
        throw CastorError.CouldNotParseJsonIntoDIDDocument("id")
    }

    val coreProperties = mutableListOf<DIDDocumentCoreProperty>()
    val verificationMethods: Array<DIDDocument.VerificationMethod> = getVerificationMethods(jsonObject)

    // Authentications
    val authenticationDidUrls = getDIDUrlsByName(jsonObject, "authentication")
    val authenticationVerificationMethods =
        getVerificationMethodsFromDIDUrls(authenticationDidUrls, verificationMethods)

    if (authenticationDidUrls.isNotEmpty() && authenticationVerificationMethods.isNotEmpty()) {
        val authentication = DIDDocument.Authentication(
            urls = authenticationDidUrls,
            verificationMethods = authenticationVerificationMethods
        )
        coreProperties.add(authentication)
    }

    // Assertion methods
    val assertionMethodDidUrls = getDIDUrlsByName(jsonObject, "assertionMethod")
    val assertionMethodVerificationMethods =
        getVerificationMethodsFromDIDUrls(assertionMethodDidUrls, verificationMethods)

    if (assertionMethodDidUrls.isNotEmpty() && assertionMethodVerificationMethods.isNotEmpty()) {
        val assertionMethod = DIDDocument.AssertionMethod(
            urls = assertionMethodDidUrls,
            verificationMethods = assertionMethodVerificationMethods
        )
        coreProperties.add(assertionMethod)
    }

    // Key agreement
    val keyAgreementDidUrls = getDIDUrlsByName(jsonObject, "keyAgreement")
    val keyAgreementVerificationMethods = getVerificationMethodsFromDIDUrls(keyAgreementDidUrls, verificationMethods)

    if (keyAgreementDidUrls.isNotEmpty() && keyAgreementVerificationMethods.isNotEmpty()) {
        val keyAgreement = DIDDocument.KeyAgreement(
            urls = keyAgreementDidUrls,
            verificationMethods = keyAgreementVerificationMethods
        )
        coreProperties.add(keyAgreement)
    }

    // Capability invocation
    val capabilityInvocationDidUrls = getDIDUrlsByName(jsonObject, "capabilityInvocation")
    val capabilityVerificationMethods =
        getVerificationMethodsFromDIDUrls(capabilityInvocationDidUrls, verificationMethods)

    if (capabilityInvocationDidUrls.isNotEmpty() && capabilityVerificationMethods.isNotEmpty()) {
        val capabilityInvocation = DIDDocument.CapabilityInvocation(
            urls = capabilityInvocationDidUrls,
            verificationMethods = capabilityVerificationMethods
        )
        coreProperties.add(capabilityInvocation)
    }

    // Capability delegation
    val capabilityDelegationDidUrls = getDIDUrlsByName(jsonObject, "capabilityDelegation")
    val capabilityDelegationVerificationMethods =
        getVerificationMethodsFromDIDUrls(capabilityDelegationDidUrls, verificationMethods)

    if (capabilityDelegationDidUrls.isNotEmpty() && capabilityDelegationVerificationMethods.isNotEmpty()) {
        val capabilityDelegation = DIDDocument.CapabilityDelegation(
            urls = capabilityDelegationDidUrls,
            verificationMethods = capabilityDelegationVerificationMethods
        )
        coreProperties.add(capabilityDelegation)
    }

    // Service
    val serviceDidUrls = getDIDUrlsByName(jsonObject, "service")
    val serviceVerificationMethods = getVerificationMethodsFromDIDUrls(serviceDidUrls, verificationMethods)
    if (serviceDidUrls.isNotEmpty() && serviceVerificationMethods.isNotEmpty()) {
        val serviceDelegation = DIDDocument.CapabilityDelegation(
            urls = serviceDidUrls,
            verificationMethods = serviceVerificationMethods
        )
        coreProperties.add(serviceDelegation)
    }

    return DIDDocument(id, coreProperties = coreProperties.toTypedArray())
}

private fun getVerificationMethods(jsonObject: JsonObject): Array<DIDDocument.VerificationMethod> {
    return if (jsonObject.containsKey("verificationMethod")) {
        val verificationMethodsArray = jsonObject["verificationMethod"]?.jsonArray
        val verificationMethods: MutableList<DIDDocument.VerificationMethod> = mutableListOf()
        verificationMethodsArray?.forEach {
            val verificationMethod = it.jsonObject
            val publicKeyJwk = verificationMethod["publicKeyJwk"]?.jsonObject?.let { jwk ->
                val jwkMap = mutableMapOf<String, String>()
                jwk["crv"]?.jsonPrimitive?.content?.let { crv ->
                    jwkMap["crv"] = crv
                }
                jwk["x"]?.jsonPrimitive?.content?.let { x ->
                    jwkMap["x"] = x
                }
                jwk["y"]?.jsonPrimitive?.content?.let { y ->
                    jwkMap["y"] = y
                }
                jwk["kty"]?.jsonPrimitive?.content?.let { kty ->
                    jwkMap["kty"] = kty
                }
                jwkMap
            }
            val didId =
                verificationMethod["id"]?.jsonPrimitive?.content ?: throw CastorError.NullOrMissingRequiredField(
                    "id",
                    "verificationMethod"
                )
            val controller = verificationMethod["controller"]?.jsonPrimitive?.content
                ?: throw CastorError.NullOrMissingRequiredField("controller", "verificationMethod")
            val type = verificationMethod["type"]?.jsonPrimitive?.content
                ?: throw CastorError.NullOrMissingRequiredField("type", "verificationMethod")
            val method = DIDDocument.VerificationMethod(
                id = DIDUrlParser.parse(didId),
                controller = DID(controller),
                type = type,
                publicKeyJwk = publicKeyJwk,
                publicKeyMultibase = null
            )
            verificationMethods.add(method)
        }
        verificationMethods.toTypedArray()
    } else {
        emptyArray<DIDDocument.VerificationMethod>()
    }
}

private fun getDIDUrlsByName(jsonObject: JsonObject, name: String): Array<String> {
    return if (jsonObject.containsKey(name) &&
        jsonObject[name]?.jsonArray?.isNotEmpty() == true
    ) {
        jsonObject[name]!!.jsonArray.map {
            it.jsonPrimitive.content
        }.toTypedArray()
    } else {
        emptyArray<String>()
    }
}

private fun getVerificationMethodsFromDIDUrls(
    didUrls: Array<String>,
    verificationMethods: Array<DIDDocument.VerificationMethod>
): Array<DIDDocument.VerificationMethod> {
    val vm: MutableList<DIDDocument.VerificationMethod> = mutableListOf()
    didUrls.forEach { didUrl ->
        verificationMethods.forEach {
            if (it.id.toString() == didUrl) {
                vm.add(it)
            }
        }
    }
    return vm.toTypedArray()
}
