package io.iohk.atala.prism.protos.models

import kotlin.js.JsExport

public typealias LedgerEnum = Int

// TODO: This smell-code is because Kotlin/JS doesn't support interfaces and enum classes now.
// We should switch to a enum class once Kotlin/JS fixes the issue: https://youtrack.jetbrains.com/issue/KT-37916.
@JsExport
public object Ledger {
    public val UNKNOWN_LEDGER: Int = 0
    public val IN_MEMORY: Int = 1
    public val CARDANO_TESTNET: Int = 4
    public val CARDANO_MAINNET: Int = 5

    public fun asString(ledger: LedgerEnum): String =
        when (ledger) {
            0 -> "UNKNOWN_LEDGER"
            1 -> "IN_MEMORY"
            4 -> "CARDANO_TESTNET"
            5 -> "CARDANO_MAINNET"
            else -> throw IllegalArgumentException("Unrecognized ledger type")
        }

    public fun fromString(string: String): LedgerEnum =
        when (string) {
            "UNKNOWN_LEDGER" -> UNKNOWN_LEDGER
            "IN_MEMORY" -> IN_MEMORY
            "CARDANO_TESTNET" -> CARDANO_TESTNET
            "CARDANO_MAINNET" -> CARDANO_MAINNET
            else -> throw IllegalArgumentException("Unrecognized ledger type")
        }
}

public fun io.iohk.atala.prism.protos.Ledger.toModel(): LedgerEnum {
    return Ledger.fromString(name!!)
}

public fun LedgerEnum.toProto(): io.iohk.atala.prism.protos.Ledger {
    return io.iohk.atala.prism.protos.Ledger.fromName(Ledger.asString(this))
}
