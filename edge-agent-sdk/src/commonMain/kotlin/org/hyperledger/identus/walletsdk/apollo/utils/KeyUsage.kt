package org.hyperledger.identus.walletsdk.apollo.utils

/**
 * Enumeration class representing different key usages.
 *
 * Each key usage is assigned a unique integer value.
 */
enum class KeyUsage(val value: Int) {
    UNKNOWN_KEY(0),
    MASTER_KEY(1),
    ISSUING_KEY(2),
    KEY_AGREEMENT_KEY(3),
    AUTHENTICATION_KEY(4),
    REVOCATION_KEY(5),
    CAPABILITY_INVOCATION_KEY(6),
    CAPABILITY_DELEGATION_KEY(7)
}
