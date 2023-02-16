package io.iohk.atala.prism.walletsdk.castor.did

class InvalidDIDStringError(override val message: String) : Exception(message) {
    val code: String = "InvalidDIDStringError"
}
