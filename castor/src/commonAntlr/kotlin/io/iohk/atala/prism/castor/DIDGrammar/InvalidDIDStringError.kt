package io.iohk.atala.prism.castor.DIDGrammar

class InvalidDIDStringError(override val message: String) : Exception(message) {
    val code: String = "InvalidDIDStringError"
}