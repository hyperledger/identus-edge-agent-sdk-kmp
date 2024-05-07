package org.hyperledger.identus.walletsdk.domain.models.keyManagement

/**
 * The `KeyProperties` class is an abstract class that provides a framework for defining key properties.
 * Each subclass of `KeyProperties` must implement the `property` property.
 */
abstract class KeyProperties {
    abstract val property: String
}

/**
 * Represents a key that holds an algorithm property.
 *
 * @property property The value of the algorithm property.
 */
class AlgorithmKey(override val property: String = "algorithm") : KeyProperties()

/**
 * Represents a key property for the curve.
 *
 * @property property The name of the key property.
 */
class CurveKey(override val property: String = "curve") : KeyProperties()

/**
 * Represents a seed key used for deriving private keys.
 *
 * @param property The property name for the seed key. Default value is "seed".
 */
class SeedKey(override val property: String = "seed") : KeyProperties()

/**
 * Represents a raw key with the property "raw".
 * @property property The property of the raw key.
 * @constructor Creates an instance of [RawKey] with the specified property, which defaults to "raw".
 */
class RawKey(override val property: String = "raw") : KeyProperties()

/**
 * Represents a derivation path key.
 * Inherits from the KeyProperties class.
 *
 * @property property The property of the derivation path key.
 */
class DerivationPathKey(override val property: String = "derivationPath") : KeyProperties()

/**
 * Class representing an index key.
 * @property property The property of the index key.
 */
class IndexKey(override val property: String = "index") : KeyProperties()

/**
 * Represents a specific type of key with a "type" property.
 * Inherits from the abstract class KeyProperties.
 *
 * @param property The value of the "type" property.
 */
class TypeKey(override val property: String = "type") : KeyProperties()

/**
 * Class representing the CurvePointXKey.
 *
 * @property property The property associated with CurvePointXKey.
 * @constructor Creates a CurvePointXKey object.
 */
class CurvePointXKey(override val property: String = "curvePoint.x") : KeyProperties()

/**
 * Represents a key property for the y-coordinate of a curve point.
 *
 * @property property The name of the property.
 */
class CurvePointYKey(override val property: String = "curvePoint.y") : KeyProperties()

/**
 * Represents a custom key with a specific property.
 *
 * @param property The property associated with the key.
 */
class CustomKey(override val property: String) : KeyProperties()

/**
 * Returns a KeyProperties object based on the given name.
 *
 * @param name The name of the key property.
 * @return A KeyProperties object corresponding to the given name.
 */
fun getKeyPropertyByName(name: String): KeyProperties {
    when (name) {
        AlgorithmKey().property -> {
            return AlgorithmKey()
        }

        CurveKey().property -> {
            return CurveKey()
        }

        SeedKey().property -> {
            return SeedKey()
        }

        RawKey().property -> {
            return RawKey()
        }

        DerivationPathKey().property -> {
            return DerivationPathKey()
        }

        IndexKey().property -> {
            return IndexKey()
        }

        TypeKey().property -> {
            return TypeKey()
        }

        CurvePointXKey().property -> {
            return CurvePointXKey()
        }

        CurvePointYKey().property -> {
            return CurvePointYKey()
        }

        else -> {
            return CustomKey(name)
        }
    }
}
