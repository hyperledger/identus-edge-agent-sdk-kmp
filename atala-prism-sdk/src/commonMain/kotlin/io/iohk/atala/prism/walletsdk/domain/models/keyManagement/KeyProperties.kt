package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

abstract class KeyProperties {
    abstract val property: String
}

class AlgorithmKey(override val property: String = "algorithm") : KeyProperties()
class CurveKey(override val property: String = "curve") : KeyProperties()
class SeedKey(override val property: String = "seed") : KeyProperties()
class RawKey(override val property: String = "raw") : KeyProperties()
class DerivationPathKey(override val property: String = "derivationPath") : KeyProperties()
class IndexKey(override val property: String = "index") : KeyProperties()
class TypeKey(override val property: String = "type") : KeyProperties()
class CurvePointXKey(override val property: String = "curvePoint.x") : KeyProperties()
class CurvePointYKey(override val property: String = "curvePoint.y") : KeyProperties()
class CustomKey(override val property: String) : KeyProperties()

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
