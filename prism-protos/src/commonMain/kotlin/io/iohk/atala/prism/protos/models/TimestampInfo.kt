package io.iohk.atala.prism.protos.models

import kotlinx.datetime.Instant
import pbandk.wkt.Timestamp
import kotlin.js.JsExport

@JsExport
public data class TimestampInfo(
    val atalaBlockTimestamp: Long, // timestamp provided from the underlying blockchain
    val atalaBlockSequenceNumber: Int, // transaction index inside the underlying blockchain block
    val operationSequenceNumber: Int // operation index inside the AtalaBlock
) {
    public fun occurredBefore(later: TimestampInfo): Boolean {
        return (atalaBlockTimestamp < later.atalaBlockTimestamp) ||
            (
                atalaBlockTimestamp == later.atalaBlockTimestamp &&
                    atalaBlockSequenceNumber < later.atalaBlockSequenceNumber
                ) ||
            (
                atalaBlockTimestamp == later.atalaBlockTimestamp &&
                    atalaBlockSequenceNumber == later.atalaBlockSequenceNumber &&
                    operationSequenceNumber < later.operationSequenceNumber
                )
    }
}

public fun io.iohk.atala.prism.protos.TimestampInfo.toModel(): TimestampInfo {
    val instant = Instant.fromEpochSeconds(blockTimestamp?.seconds!!, blockTimestamp.nanos)
    return TimestampInfo(
        atalaBlockTimestamp = instant.toEpochMilliseconds(),
        atalaBlockSequenceNumber = blockSequenceNumber,
        operationSequenceNumber = operationSequenceNumber
    )
}

public fun TimestampInfo.toProto(): io.iohk.atala.prism.protos.TimestampInfo {
    val instant = Instant.fromEpochMilliseconds(atalaBlockTimestamp)
    return io.iohk.atala.prism.protos.TimestampInfo(
        blockTimestamp = Timestamp(seconds = instant.epochSeconds, nanos = instant.nanosecondsOfSecond),
        blockSequenceNumber = atalaBlockSequenceNumber,
        operationSequenceNumber = operationSequenceNumber
    )
}
