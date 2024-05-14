package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a Core Property in a DID Document.
 * This allows for extensibility of the properties.
 * As specified in [w3 standards](https://www.w3.org/TR/did-core/#data-model)
 **/
interface DIDDocumentCoreProperty

/**
 * Represents a DIDDocument with [DID] and [DIDDocumentCoreProperty]
 * As specified in [w3 standards](https://www.w3.org/TR/did-core/#data-model)
 * A DID Document consists of a DID, public keys, authentication protocols, service endpoints, and other metadata.
 * It is used to verify the authenticity and identity of the DID, and to discover and interact with the associated
 * subjects or objects.
 */
@Serializable
data class DIDDocument(
    val id: DID,
    val coreProperties: Array<DIDDocumentCoreProperty>
) {

    val services: Array<Service>
        get() = coreProperties.fold(arrayOf()) { acc, property ->
            if (property is DIDDocument.Services) acc.plus(property.values) else acc
        }

    /**
     * Overrides the equals method to compare two instances of DIDDocument.
     *
     * @param other The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DIDDocument

        if (id != other.id) return false
        if (!coreProperties.contentEquals(other.coreProperties)) return false

        return true
    }

    /**
     * Calculates the hash code for the DIDDocument object.
     *
     * The hash code is calculated based on the id field and the content hash code of the coreProperties array.
     *
     * @return The hash code of the DIDDocument object.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + coreProperties.contentHashCode()
        return result
    }

    /**
     * Represents a Verification Method, which is a public key or other evidence used to authenticate the identity of a
     * Decentralized Identifier (DID) or other subject or object.
     * A Verification Method consists of a type (indicating the type of key or evidence), a public key or other data, and
     * optional metadata such as a controller (the DID that controls the verification method) and purpose (the intended
     * use of the verification method). It is typically included in a DID Document or other authentication credential.
     **/
    data class VerificationMethod @JvmOverloads constructor(
        val id: DIDUrl,
        val controller: DID,
        val type: String,
        val publicKeyJwk: Map<String, String>? = null,
        val publicKeyMultibase: String? = null
    ) {
        companion object {
            /**
             * Returns the Curve value based on the given type.
             *
             * @param type The type of the curve.
             * @return The Curve object corresponding to the given type.
             * @throws CastorError.InvalidKeyError if the given type is not supported.
             */
            @JvmStatic
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

    /**
     * Represents a Service, which is a capability or endpoint offered by a Decentralized Identifier (DID) or other subject or object.
     * A Service consists of an ID, type, and service endpoint, as well as optional metadata such as a priority and a description.
     * It is typically included in a DID Document and can be used to discover and interact with the associated DID or subject or object.
     */
    @Serializable
    data class Service(
        val id: String,
        val type: Array<String>,
        val serviceEndpoint: ServiceEndpoint
    ) : DIDDocumentCoreProperty {
        /**
         * Determines whether the current object is equal to another object.
         *
         * @param other The object to compare with the current object.
         * @return true if the current object is equal to the other object, otherwise false.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Service

            if (id != other.id) return false
            if (!type.contentEquals(other.type)) return false
            if (serviceEndpoint != other.serviceEndpoint) return false

            return true
        }

        /**
         * Calculates the hash code for the Service object.
         * The hash code is calculated based on the id, type, and serviceEndpoint properties.
         *
         * @return The hash code of the Service object.
         */
        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + (type.contentHashCode())
            result = 31 * result + serviceEndpoint.hashCode()
            return result
        }
    }

    /**
     * Represents a service endpoint, which is a URI and other information that indicates how to access the service.
     */
    @Serializable
    data class ServiceEndpoint @JvmOverloads constructor(
        val uri: String,
        val accept: Array<String>? = arrayOf(),
        val routingKeys: Array<String>? = arrayOf()
    ) {
        /**
         * Determines whether the current object is equal to another object.
         *
         * @param other The object to compare with the current object.
         * @return true if the current object is equal to the other object, otherwise false.
         */
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

        /**
         * Calculates the hash code for the ServiceEndpoint object.
         * The hash code is calculated based on the uri, accept, and routingKeys properties.
         *
         * @return The hash code of the ServiceEndpoint object.
         */
        override fun hashCode(): Int {
            var result = uri.hashCode()
            result = 31 * result + (accept?.contentHashCode() ?: 0)
            result = 31 * result + (routingKeys?.contentHashCode() ?: 0)
            return result
        }
    }

    /**
     * Represents a "also known as" property, which is a list of alternative names or identifiers for a
     * Decentralized Identifier (DID) or other subject or object.
     * The "also known as" property is typically included in a DID Document and can be used to associate the DID or
     * subject or object with other names or identifiers.
     */
    data class AlsoKnownAs(
        val values: Array<String>
    ) : DIDDocumentCoreProperty {
        /**
         * Checks if the current object is equal to the given object.
         *
         * The current object is considered equal to the given object if and only if:
         *   - They refer to the same instance
         *   - The given object is not null and belongs to the same class as the current object
         *   - The values of the `values` property of both objects are equal
         *
         * @param other The object to compare with the current object
         *
         * @return `true` if the objects are equal, `false` otherwise
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AlsoKnownAs

            return values.contentEquals(other.values)
        }

        /**
         * Generates the hash code value for the current object.
         * The hash code is computed based on the values of the `values` property.
         *
         * @return The hash code value for the object
         */
        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    /**
     * Represents a "controller" property, which is a list of Decentralized Identifiers (DIDs) that control the associated
     * DID or subject or object.
     * The "controller" property is typically included in a DID Document and can be used to indicate who has the authority
     * to update or deactivate the DID or subject or object.
     */
    data class Controller(val values: Array<DID>) : DIDDocumentCoreProperty {
        /**
         * Compares this Controller object with the specified object for equality.
         *
         * @param other The object to compare for equality.
         * @return true if the given object is equal to this Controller object, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Controller

            return values.contentEquals(other.values)
        }

        /**
         * Calculates the hash code for the Controller object.
         *
         * @return The hash code value for the Controller object.
         */
        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    /**
     * Represents a "verification methods" property, which is a list of Verification Methods associated with a
     * Decentralized Identifier (DID) or other subject or object.
     * The "verification methods" property is typically included in a DID Document and can be used to authenticate the
     * identity of the DID or subject or object.
     */
    data class VerificationMethods(val values: Array<VerificationMethod>) : DIDDocumentCoreProperty {
        /**
         * Compares this `VerificationMethods` object with the specified object for equality.
         *
         * The comparison is based on the `values` property of the `VerificationMethods` object. If the specified object is
         * `null`, or is not an instance of `VerificationMethods`, or the `values` arrays are not equal, then the method
         * returns `false`. Otherwise, it returns `true`.
         *
         * @param other The object to compare for equality.
         * @return `true` if this `VerificationMethods` object is equal to the specified object, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as VerificationMethods

            return values.contentEquals(other.values)
        }

        /**
         * Calculates the hash code for the VerificationMethods object.
         *
         * This method calculates the hash code based on the content of the values array in the VerificationMethods object.
         * If two VerificationMethods objects have the same values array, their hash codes will be equal.
         *
         * @return The hash code for the VerificationMethods object.
         */
        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    /**
     * Represents a "services" property, which is a list of Services associated with a Decentralized Identifier (DID)
     * or other subject or object.
     * The "services" property is typically included in a DID Document and can be used to discover and interact with the
     * associated DID or subject or object.
     */
    data class Services(val values: Array<Service>) : DIDDocumentCoreProperty {
        /**
         * Determines whether the current object is equal to another object.
         *
         * @param other The object to compare with the current object.
         * @return true if the current object is equal to the other object, otherwise false.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Services

            return values.contentEquals(other.values)
        }

        /**
         * Calculates the hash code for the Services object.
         * The hash code is calculated based on the content of the values array property.
         *
         * @return The hash code of the Services object.
         */
        override fun hashCode(): Int {
            return values.contentHashCode()
        }
    }

    /**
     * Represents an "authentication" property, which is a list of URIs and Verification Methods that can be used to
     * authenticate the associated DID or subject or object.
     * The "authentication" property is typically included in a DID Document and can be used to verify the
     * identity of the DID or subject or object.
     */
    data class Authentication(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        /**
         * Checks if the current instance of [Authentication] is equal to the provided [other] object.
         *
         * @param other The object to compare with.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Authentication

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        /**
         *
         */
        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    /**
     * Represents an "assertion method" property, which is a list of URIs and Verification Methods that can be used to
     * assert the authenticity of a message or credential associated with a DID or other subject or object.
     * The "assertion method" property is typically included in a DID Document and can be used to verify the
     * authenticity of messages or credentials related to the DID or subject or object.
     */
    data class AssertionMethod(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        /**
         * Checks if the current AssertionMethod object is equal to the provided object.
         *
         * @param other The object to compare with the current AssertionMethod.
         * @return true if the objects are equal, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AssertionMethod

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        /**
         * Calculates the hash code for the AssertionMethod object.
         *
         * @return The hash code value for the AssertionMethod object.
         */
        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    /**
     * Represents a "key agreement" property, which is a list of URIs and Verification Methods that can be used to establish a
     * secure communication channel with a DID or other subject or object.
     * The "key agreement" property is typically included in a DID Document and can be used to establish a secure
     * communication channel with the DID or subject or object.
     */
    data class KeyAgreement(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        /**
         * Checks whether the current instance is equal to the given object.
         *
         * @param other The object to compare with the current instance.
         * @return Returns true if the current instance is equal to the given object, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as KeyAgreement

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        /**
         * Calculates the hash code for the `KeyAgreement` instance.
         *
         * @return The hash code value calculated based on the `urls` and `verificationMethods` properties.
         */
        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    /**
     * Represents a "capability invocation" property, which is a list of URIs and Verification Methods that can
     * be used to invoke a specific capability or service provided by a DID or other subject or object.
     * The "capability invocation" property is typically included in a DID Document and can be used to invoke a
     * specific capability or service provided by the DID or subject or object.
     */
    data class CapabilityInvocation(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        /**
         * Compares this object with the specified object for equality.
         * Returns true if the specified object is also a [CapabilityInvocation] and has the same values for the `urls` and `verificationMethods` properties.
         *
         * @param other The object to compare for equality.
         * @return True if the objects are equal, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as CapabilityInvocation

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        /**
         * Calculates the hash code for a CapabilityInvocation object.
         *
         * @return The calculated hash code.
         */
        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }

    /**
     * Represents a "capability delegation" property, which is a list of URIs and Verification Methods that can be used
     * to delegate a specific capability or service provided by a DID or other subject or object to another subject or object.
     * The "capability delegation" property is typically included in a DID Document and can be used to delegate a
     * specific capability or service provided by the DID or subject or object.
     */
    data class CapabilityDelegation(
        val urls: Array<String>,
        val verificationMethods: Array<VerificationMethod>
    ) : DIDDocumentCoreProperty {
        /**
         * Determines whether the current `CapabilityDelegation` object is equal to the provided object.
         *
         * @param other The object to compare with the current `CapabilityDelegation` object.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as CapabilityDelegation

            if (!urls.contentEquals(other.urls)) return false
            if (!verificationMethods.contentEquals(other.verificationMethods)) return false

            return true
        }

        /**
         * Calculates the hash code value for the `CapabilityDelegation` object.
         *
         * The hash code is calculated based on the `urls` and `verificationMethods` properties of the `CapabilityDelegation`.
         * Both properties are used to calculate the hash code by invoking the `contentHashCode()` method on the corresponding arrays.
         * The resulting hash codes are combined using the formula: `result = 31 * result + secondHashCode`.
         *
         * @return The hash code value for the `CapabilityDelegation` object.
         */
        override fun hashCode(): Int {
            var result = urls.contentHashCode()
            result = 31 * result + verificationMethods.contentHashCode()
            return result
        }
    }
}
