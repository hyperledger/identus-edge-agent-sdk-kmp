package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.models.DID

fun DID.Companion.testable(
    schema: String = "did",
    method: String = "test1",
    methodId: String = "test1Id",
): DID {
    return DID(schema, method, methodId)
}
