package io.iohk.atala.prism.walletsdk.castor.did

class InvalidDIDStringError(message: String? = null) : Exception(message) {
    val code: String = "InvalidDIDStringError"
}
