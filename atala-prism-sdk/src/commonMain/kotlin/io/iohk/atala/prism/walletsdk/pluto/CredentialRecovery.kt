package io.iohk.atala.prism.walletsdk.pluto

/**
 * Class representing a credential recovery object.
 *
 * @property restorationId The restoration ID associated with the credential recovery.
 * @property credentialData The credential data as a byte array.
 */
class CredentialRecovery(val restorationId: String, val credentialData: ByteArray)
