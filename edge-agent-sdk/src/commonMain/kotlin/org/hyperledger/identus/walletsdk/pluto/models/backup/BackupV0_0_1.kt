package org.hyperledger.identus.walletsdk.pluto.models.backup

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a backup object with version, credentials, DIDs, DID pairs, keys, messages, link secret, and mediators.
 *
 * @property version The version of the backup.
 * @property credentials The list of credentials.
 * @property dids The list of DIDs.
 * @property didPairs The list of DID pairs.
 * @property keys The list of keys.
 * @property messages The list of messages.
 * @property linkSecret The link secret.
 * @property mediators The list of mediators.
 */
@Suppress("ClassName")
@Serializable
class BackupV0_0_1
@OptIn(ExperimentalSerializationApi::class)
@JvmOverloads
constructor(
    @EncodeDefault
    val version: String = "0.0.1",
    val credentials: List<Credential>,
    val dids: List<DID>,
    @SerialName("did_pairs")
    val didPairs: List<DIDPair>,
    val keys: List<Key>,
    val messages: List<String>,
    @SerialName("link_secret")
    val linkSecret: String?,
    val mediators: List<Mediator>
) {

    /**
     * Returns true if the specified object is equal to this BackupV0_0_1 object.
     *
     * @param other the object to compare with this object for equality.
     *
     * @return true if the specified object is equal to this BackupV0_0_1 object, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is BackupV0_0_1) {
            return false
        }

        if (version != other.version) {
            return false
        }
        if (credentials != other.credentials) {
            return false
        }
        if (dids != other.dids) {
            return false
        }
        if (didPairs != other.didPairs) {
            return false
        }
        if (keys != other.keys) {
            return false
        }
        if (messages != other.messages) {
            return false
        }
        if (linkSecret != other.linkSecret) {
            return false
        }
        if (mediators != other.mediators) {
            return false
        }

        return true
    }

    /**
     * Returns the hash code value for this object.
     *
     * The hash code is computed by combining the hash codes of various properties of this object.
     * The properties used for computing the hash code are:
     *
     * - version: The hash code of the 'version' property
     * - credentials: The hash code of the 'credentials' property
     * - dids: The hash code of the 'dids' property
     * - didPairs: The hash code of the 'didPairs' property
     * - keys: The hash code of the 'keys' property
     * - messages: The hash code of the 'messages' property
     * - linkSecret: The hash code of the 'linkSecret' property, if not null. Otherwise, 0 is used.
     * - mediators: The hash code of the 'mediators' property
     *
     * @return the hash code value for this object
     */
    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + credentials.hashCode()
        result = 31 * result + dids.hashCode()
        result = 31 * result + didPairs.hashCode()
        result = 31 * result + keys.hashCode()
        result = 31 * result + messages.hashCode()
        result = 31 * result + (linkSecret.hashCode())
        result = 31 * result + mediators.hashCode()
        return result
    }

    // Nested data class for Credential
    /**
     * The Credential class represents a serialized credential object.
     *
     * @property recoveryId The recovery ID associated with the credential.
     * @property data The data contained in the credential.
     */
    @Serializable
    data class Credential(
        @SerialName("recovery_id")
        val recoveryId: String,
        val data: String
    ) {
        /**
         * Compares this Credential object with the specified object for equality.
         *
         * @param other the object to be compared for equality
         * @return `true` if the specified object is equal to this Credential object, `false` otherwise
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is Credential) {
                return false
            }

            if (recoveryId != other.recoveryId) {
                return false
            }
            if (data != other.data) {
                return false
            }

            return true
        }

        /**
         * Generates a hash code value for the object based on its properties.
         *
         * @return The hash code value for the object.
         */
        override fun hashCode(): Int {
            var result = recoveryId.hashCode()
            result = 31 * result + data.hashCode()
            return result
        }
    }

    // Nested data class for DID
    /**
     * Represents a Decentralized Identifier (DID) entity.
     *
     * @param did The DID string.
     * @param alias An optional alias for the DID.
     */
    @Serializable
    data class DID(
        val did: String,
        val alias: String? = null
    ) {
        /**
         * Checks if the current instance is equal to the given [other] object.
         *
         * @param other The object to compare with the current instance.
         *
         * @return `true` if the current instance is equal to the given [other] object, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is DID) {
                return false
            }

            if (did != other.did) {
                return false
            }
            if (alias != other.alias) {
                return false
            }

            return true
        }

        /**
         * Calculates the hash code for this object.
         *
         * @return The hash code value computed for this object.
         */
        override fun hashCode(): Int {
            var result = did.hashCode()
            result = 31 * result + (alias?.hashCode() ?: 0)
            return result
        }
    }

    // Nested data class for DIDPair
    /**
     * Represents a pair of DID (Decentralized Identifier) with additional metadata.
     *
     * @property holder The DID of the holder of the pair.
     * @property recipient The DID of the recipient of the pair.
     * @property alias An alias or label associated with the pair.
     */
    @Serializable
    data class DIDPair(
        val holder: String,
        val recipient: String,
        val alias: String
    ) {
        /**
         * Compares this [DIDPair] object with the specified [other] object for equality.
         *
         * @param other The object to compare for equality.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is DIDPair) {
                return false
            }

            if (holder != other.holder) {
                return false
            }
            if (recipient != other.recipient) {
                return false
            }
            if (alias != other.alias) {
                return false
            }

            return true
        }

        /**
         * Computes and returns the hash code for this object.
         *
         * The hash code is computed by combining the hash codes of the `holder`, `recipient` and `alias` fields using the
         * following formula: `31 * result + field.hashCode()`.
         *
         * @return The computed hash code for this object.
         */
        override fun hashCode(): Int {
            var result = holder.hashCode()
            result = 31 * result + recipient.hashCode()
            result = 31 * result + alias.hashCode()
            return result
        }
    }

    // Nested data class for Key
    /**
     * This class represents a Key object.
     * It contains the following properties:
     *
     * @property key The key value.
     * @property did The decentralized identifier associated with the key (optional).
     * @property index The index value associated with the key (optional).
     * @property recoveryId The recovery ID associated with the key (optional).
     */
    @Serializable
    data class Key(
        val key: String,
        val did: String? = null,
        val index: Int? = null,
        @SerialName("recovery_id")
        val recoveryId: String? = null
    ) {
        /**
         * Determines whether the current object is equal to another object.
         *
         * @param other The object to compare to.
         * @return true if the objects are equal, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is Key) {
                return false
            }

            if (key != other.key) {
                return false
            }
            if (did != other.did) {
                return false
            }
            if (index != other.index) {
                return false
            }
            if (recoveryId != other.recoveryId) {
                return false
            }

            return true
        }

        /**
         * Calculates the hash code value for the current object.
         *
         * @return The hash code value.
         */
        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + (did?.hashCode() ?: 0)
            result = 31 * result + (index ?: 0)
            result = 31 * result + (recoveryId?.hashCode() ?: 0)
            return result
        }
    }

    /**
     * The [Mediator] class represents a mediator entity used in a system.
     *
     * @property mediatorDid The unique identifier of the mediator.
     * @property holderDid The unique identifier of the holder.
     * @property routingDid The unique identifier of the routing.
     */
    @Serializable
    data class Mediator(
        @SerialName("mediator_did")
        val mediatorDid: String,
        @SerialName("holder_did")
        val holderDid: String,
        @SerialName("routing_did")
        val routingDid: String
    ) {
        /**
         * Checks if this Mediator object is equal to the specified object.
         *
         * @param other the object to compare with this Mediator
         * @return true if the specified object is equal to this Mediator, false otherwise
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is Mediator) {
                return false
            }

            if (mediatorDid != other.mediatorDid) {
                return false
            }
            if (holderDid != other.holderDid) {
                return false
            }
            if (routingDid != other.routingDid) {
                return false
            }

            return true
        }

        /**
         * Calculates the hash code for this object.
         *
         * The hash code is calculated by combining the hash codes of the
         * `mediatorDid`, `holderDid`, and `routingDid` properties. The algorithm
         * used for the calculation is as follows:
         *
         * 1. Get the hash code of the `mediatorDid` property.
         * 2. Multiply the hash code by 31 and add the hash code of the `holderDid` property.
         * 3. Multiply the result by 31 and add the hash code of the `routingDid` property.
         * 4. Return the final result.
         *
         * @return the hash code value for this object.
         */
        override fun hashCode(): Int {
            var result = mediatorDid.hashCode()
            result = 31 * result + holderDid.hashCode()
            result = 31 * result + routingDid.hashCode()
            return result
        }
    }
}
