package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

interface DIDDocumentCoreProperty

@Serializable
@JsExport
data class DIDDocument(
    val id: DID,
    val coreProperties: Array<DIDDocumentCoreProperty>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DIDDocument

        if (id != other.id) return false
        if (!coreProperties.contentEquals(other.coreProperties)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + coreProperties.contentHashCode()
        return result
    }

    data class VerificationMethod(
        val id: DIDUrl,
        val controller: DID,
        val type: String,
        val publicKeyJwk: Map<String, String>? = null,
        val publicKeyMultibase: String? = null
    ) {
        companion object {
            fun getCurveByType(type: String): Curve {
                return when (type) {
                    Curve.X25519.value -> {
                        Curve.X25519
                    }

                    Curve.ED25519.value -> {
                        Curve.ED25519
                    }

                    Curve.SECP256K1.value -> {
                        Curve.SECP256K1
                    }

                    else -> {
                        throw CastorError.InvalidKeyError()
                    }
                }
            }
        }
    }

    @Serializable
    data class Service(
        val id: String,
        val type: Array<String>,
        val serviceEndpoint: ServiceEndpoint
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Service

            if (id != other.id) return false
            if (!type.contentEquals(other.type)) return false
            if (serviceEndpoint != other.serviceEndpoint) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + (type.contentHashCode())
            result = 31 * result + serviceEndpoint.hashCode()
            return result
        }
    }

    @Serializable
    data class ServiceEndpoint(
        val uri: String,
        val accept: Array<String>? = arrayOf(),
        val routingKeys: Array<String>? = arrayOf()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ServiceEndpoint

            if (uri != other.uri) return false
            if (accept != null) {
                if (other.accept == null) return false
                if (!accept.contentEquals(other.accept)) return false
            } else if (other.accept != null) return false
            if (routingKeys != null) {
                if (other.routingKeys == null) return false
                if (!routingKeys.contentEquals(other.routingKeys)) return false
            } else if (other.routingKeys != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = uri.hashCode()
            result = 31 * result + (accept?.contentHashCode() ?: 0)
            result = 31 * result + (routingKeys?.contentHashCode() ?: 0)
            return result
        }
    }

    data class AlsoKnownAs(
        val values: Array<String>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AlsoKnownAs

            if (!values.contentEquals(other.values)) return false

            return true
        }

        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    data class Controller(
        val values: Array<DID>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Controller

            if (!values.contentEquals(other.values)) return false

            return true
        }

        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    data class VerificationMethods(
        val values: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as VerificationMethods

            if (!values.contentEquals(other.values)) return false

            return true
        }

        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    data class Services(
        val values: Array<Service>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Services

            if (!values.contentEquals(other.values)) return false

            return true
        }

        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    data class Authentication(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Authentication

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    data class AssertionMethod(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AssertionMethod

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    data class KeyAgreement(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as KeyAgreement

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    data class CapabilityInvocation(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as CapabilityInvocation

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    data class CapabilityDelegation(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as CapabilityDelegation

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }
}
