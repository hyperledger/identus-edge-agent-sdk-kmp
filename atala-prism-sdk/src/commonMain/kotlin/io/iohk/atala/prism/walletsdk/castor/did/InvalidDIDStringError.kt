package io.iohk.atala.prism.walletsdk.castor.did

class InvalidDIDStringError @JvmOverloads constructor(message: String? = null) : Exception(message) {
    val code: String = "InvalidDIDStringError"
}
