package org.hyperledger.identus.walletsdk.domain.models.keyManagement

import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.models.ApolloError
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve

/**
 * Abstraction defining the base of what a Key is.
 */
abstract class Key {
    abstract val type: KeyTypes
    abstract val keySpecification: MutableMap<String, String>
    abstract val size: Int
    abstract val raw: ByteArray

    /**
     * Checks if the current Key object is equal to the provided object.
     * Two Key objects are considered equal if the following conditions are met:
     * - The objects are references to the same memory location (this === other).
     * - The other object is not null and is of the same class as Key.
     * - The raw byte arrays of the keys are equal (raw.contentEquals(other.raw)).
     * - The keySpecification map contains the CurveKey property and its value is equal to the value of the other key's CurveKey property.
     *
     * @param other The object to compare with the current Key.
     * @return True if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Key

        if (!raw.contentEquals(other.raw)) {
            return false
        }
        if (!keySpecification.containsKey(CurveKey().property) ||
            keySpecification[CurveKey().property] != other.getProperty(CurveKey().property)
        ) {
            return false
        }
        return true
    }

    /**
     * Computes the hash code of the Key object.
     * The hash code is calculated based on the properties of the keySpecification map and the raw byte array.
     * Two Key objects with the same keySpecification map and the raw byte array are guaranteed to have the same hash code.
     *
     * @return The hash code of the Key object.
     */
    override fun hashCode(): Int {
        var result = keySpecification[CurveKey().property].hashCode()
        result = 31 * result + raw.contentHashCode()
        return result
    }

    /**
     * Returns the encoded raw value into base 64 url
     */
    fun getEncoded(): ByteArray {
        return raw.base64UrlEncoded.encodeToByteArray()
    }

    /**
     * Evaluates if this key implements ExportableKey
     */
    fun isExportable(): Boolean {
        return this is ExportableKey
    }

    /**
     * Evaluates if this key implements ImportableKey
     */
    fun isImportable(): Boolean {
        return this is ImportableKey
    }

    /**
     * Evaluates if this key implements SignableKey
     */
    fun isSignable(): Boolean {
        return this is SignableKey
    }

    /**
     * Evaluates if this key implements DerivableKey
     */
    fun isDerivable(): Boolean {
        return this is DerivableKey
    }

    /**
     * Evaluates if this key implements VerifiableKey
     */
    fun canVerify(): Boolean {
        return this is VerifiableKey
    }

    /**
     * Searches the value based on the input key, if it exists
     */
    fun getProperty(name: String): String {
        if (!keySpecification.containsKey(name)) {
            throw Exception("KeySpecification do not contain $name")
        }
        return this.keySpecification[name].toString()
    }

    /**
     *  Evaluates if the input curve matches the actual curve this key has
     */
    fun isCurve(curve: String): Boolean {
        val keyCurve = keySpecification[CurveKey().property]
        return keyCurve == curve
    }
}

/**
 *  Method to get a KeyCurve instance based on a key String name.
 */
fun getKeyCurveByNameAndIndex(name: String, index: Int?): KeyCurve {
    return when (name) {
        Curve.X25519.value -> {
            KeyCurve(Curve.X25519)
        }

        Curve.ED25519.value -> {
            KeyCurve(Curve.ED25519)
        }

        Curve.SECP256K1.value -> {
            KeyCurve(Curve.SECP256K1, index)
        }

        else -> {
            throw ApolloError.InvalidKeyCurve(name)
        }
    }
}
