package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.iohk.atala.prism.walletsdk.domain.models.DID

fun DID.Companion.fromMethodAndMethodId(
    method: String?,
    methodId: String?
): DID {
    return DID(
        method = method ?: "test",
        methodId = methodId ?: "testableId"
    )
}

fun DID.Companion.fromIndex(index: Int): DID {
    return DID.fromMethodAndMethodId(
        method = "test$index",
        methodId = "testableId$index"
    )
}
