package io.iohk.atala.prism.domain.models

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

data class Session(
    val uuid: Uuid = uuid4(),
    val seed: Seed
)
