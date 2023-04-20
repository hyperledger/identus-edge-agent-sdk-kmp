package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.apollo.uuid.UUID
import kotlin.jvm.JvmOverloads

data class Session @JvmOverloads constructor(
    val uuid: UUID = UUID.randomUUID4(),
    val seed: Seed
)
