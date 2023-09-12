package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

abstract class PublicKey : Key() {

    fun getCurve(): String {
        return this.getProperty(CurveKey().property)
    }

    fun getValue(): ByteArray {
        return this.raw
    }
}
