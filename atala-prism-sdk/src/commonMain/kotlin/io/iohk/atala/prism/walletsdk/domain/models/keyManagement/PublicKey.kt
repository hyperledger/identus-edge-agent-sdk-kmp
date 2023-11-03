package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.walletsdk.domain.models.Curve

abstract class PublicKey : Key() {

    fun getCurve(): String {
        return this.getProperty(CurveKey().property)
    }

    fun getCurveInstance(): Curve? {
        return try {
            Curve.valueOf(this.getProperty(CurveKey().property))
        } catch (e: Exception) {
            null
        }
    }

    fun getValue(): ByteArray {
        return this.raw
    }
}
