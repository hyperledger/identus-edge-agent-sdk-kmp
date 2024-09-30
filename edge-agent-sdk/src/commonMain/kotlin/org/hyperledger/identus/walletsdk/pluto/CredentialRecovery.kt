package org.hyperledger.identus.walletsdk.pluto

import kotlinx.serialization.Serializable

/**
 * Class representing a credential recovery object.
 *
 * @property restorationId The restoration ID associated with the credential recovery.
 * @property credentialData The credential data as a byte array.
 */
@Serializable
class CredentialRecovery(val restorationId: String, val credentialData: ByteArray, val revoked: Boolean)

/**
 * The RestorationID enum class represents different types of restoration IDs.
 * Each restoration ID has a corresponding value.
 *
 * @property value The value associated with the restoration ID.
 */
enum class RestorationID(val value: String) {
    JWT("jwt+credential"),
    ANONCRED("anon+credential"),
    W3C("w3c+credential"),
    SDJWT("sd-jwt+credential");

    /**
     * Converts a RestorationID object to a BackUpRestorationId object from the PlutoRestoreTask class.
     *
     * @return The corresponding BackUpRestorationId object based on the current RestorationID.
     */
    fun toBackUpRestorationId(): PlutoRestoreTask.BackUpRestorationId {
        return when (this) {
            JWT -> PlutoRestoreTask.BackUpRestorationId.JWT
            ANONCRED -> PlutoRestoreTask.BackUpRestorationId.ANONCRED
            W3C -> PlutoRestoreTask.BackUpRestorationId.W3C
            SDJWT -> PlutoRestoreTask.BackUpRestorationId.SDJWT
        }
    }
}
