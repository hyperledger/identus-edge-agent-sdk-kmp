package io.iohk.atala.prism.protos.models

import kotlin.js.JsExport

@JsExport
public data class LedgerData(
    val transactionId: String,
    val ledger: LedgerEnum,
    val timestampInfo: TimestampInfo
)

public fun io.iohk.atala.prism.protos.LedgerData.toModel(): LedgerData {
    return LedgerData(
        transactionId,
        ledger.toModel(),
        timestampInfo!!.toModel()
    )
}

public fun LedgerData.toProto(): io.iohk.atala.prism.protos.LedgerData {
    return io.iohk.atala.prism.protos.LedgerData(
        transactionId,
        ledger.toProto(),
        timestampInfo.toProto()
    )
}
