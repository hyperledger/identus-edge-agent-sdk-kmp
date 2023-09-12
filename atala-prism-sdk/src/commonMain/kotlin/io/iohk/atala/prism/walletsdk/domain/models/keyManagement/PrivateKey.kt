package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

abstract class PrivateKey : Key() {

    fun getCurve(): String {
        return this.getProperty(CurveKey().property)
    }

    fun getIndex(): String {
        return this.getProperty(IndexKey().property)
    }

    fun getValue(): ByteArray {
        return this.raw
    }

    abstract fun publicKey(): PublicKey
}
