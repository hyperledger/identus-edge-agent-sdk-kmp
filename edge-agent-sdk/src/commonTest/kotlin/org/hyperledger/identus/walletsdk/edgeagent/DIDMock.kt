package org.hyperledger.identus.walletsdk.edgeagent

import org.hyperledger.identus.walletsdk.domain.models.DID

fun DID.Companion.testable(
    schema: String = "did",
    method: String = "test1",
    methodId: String = "test1Id"
): DID {
    return DID(schema, method, methodId)
}
