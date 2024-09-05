@file:OptIn(pbandk.PublicForGeneratedCode::class)

package org.hyperledger.identus.protos

@pbandk.Export
public sealed class SortByDirection(override val value: Int, override val name: String? = null) : pbandk.Message.Enum {
    override fun equals(other: kotlin.Any?): Boolean = other is SortByDirection && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "SortByDirection.${name ?: "UNRECOGNIZED"}(value=$value)"

    public object UNKNOWN : SortByDirection(0, "SORT_BY_DIRECTION_UNKNOWN")
    public object ASCENDING : SortByDirection(1, "SORT_BY_DIRECTION_ASCENDING")
    public object DESCENDING : SortByDirection(2, "SORT_BY_DIRECTION_DESCENDING")
    public class UNRECOGNIZED(value: Int) : SortByDirection(value)

    public companion object : pbandk.Message.Enum.Companion<SortByDirection> {
        public val values: List<SortByDirection> by lazy { listOf(UNKNOWN, ASCENDING, DESCENDING) }
        override fun fromValue(value: Int): SortByDirection = values.firstOrNull { it.value == value } ?: UNRECOGNIZED(value)
        override fun fromName(name: String): SortByDirection = values.firstOrNull { it.name == name } ?: throw IllegalArgumentException("No SortByDirection with name: $name")
    }
}

@pbandk.Export
public sealed class Ledger(override val value: Int, override val name: String? = null) : pbandk.Message.Enum {
    override fun equals(other: kotlin.Any?): Boolean = other is Ledger && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "Ledger.${name ?: "UNRECOGNIZED"}(value=$value)"

    public object UNKNOWN_LEDGER : Ledger(0, "UNKNOWN_LEDGER")
    public object IN_MEMORY : Ledger(1, "IN_MEMORY")
    public object CARDANO_TESTNET : Ledger(4, "CARDANO_TESTNET")
    public object CARDANO_MAINNET : Ledger(5, "CARDANO_MAINNET")
    public class UNRECOGNIZED(value: Int) : Ledger(value)

    public companion object : pbandk.Message.Enum.Companion<Ledger> {
        public val values: List<Ledger> by lazy { listOf(UNKNOWN_LEDGER, IN_MEMORY, CARDANO_TESTNET, CARDANO_MAINNET) }
        override fun fromValue(value: Int): Ledger = values.firstOrNull { it.value == value } ?: UNRECOGNIZED(value)
        override fun fromName(name: String): Ledger = values.firstOrNull { it.name == name } ?: throw IllegalArgumentException("No Ledger with name: $name")
    }
}

@pbandk.Export
public sealed class OperationStatus(override val value: Int, override val name: String? = null) : pbandk.Message.Enum {
    override fun equals(other: kotlin.Any?): Boolean = other is OperationStatus && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "OperationStatus.${name ?: "UNRECOGNIZED"}(value=$value)"

    public object UNKNOWN_OPERATION : OperationStatus(0, "UNKNOWN_OPERATION")
    public object PENDING_SUBMISSION : OperationStatus(1, "PENDING_SUBMISSION")
    public object AWAIT_CONFIRMATION : OperationStatus(2, "AWAIT_CONFIRMATION")
    public object CONFIRMED_AND_APPLIED : OperationStatus(3, "CONFIRMED_AND_APPLIED")
    public object CONFIRMED_AND_REJECTED : OperationStatus(4, "CONFIRMED_AND_REJECTED")
    public class UNRECOGNIZED(value: Int) : OperationStatus(value)

    public companion object : pbandk.Message.Enum.Companion<OperationStatus> {
        public val values: List<OperationStatus> by lazy { listOf(UNKNOWN_OPERATION, PENDING_SUBMISSION, AWAIT_CONFIRMATION, CONFIRMED_AND_APPLIED, CONFIRMED_AND_REJECTED) }
        override fun fromValue(value: Int): OperationStatus = values.firstOrNull { it.value == value } ?: UNRECOGNIZED(value)
        override fun fromName(name: String): OperationStatus = values.firstOrNull { it.name == name } ?: throw IllegalArgumentException("No OperationStatus with name: $name")
    }
}

@pbandk.Export
public data class HealthCheckRequest(
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.HealthCheckRequest = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.HealthCheckRequest> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.HealthCheckRequest> {
        public val defaultInstance: org.hyperledger.identus.protos.HealthCheckRequest by lazy { org.hyperledger.identus.protos.HealthCheckRequest() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.HealthCheckRequest = org.hyperledger.identus.protos.HealthCheckRequest.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.HealthCheckRequest> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.HealthCheckRequest, *>>(0)
            fieldsList.apply {
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.HealthCheckRequest",
                messageClass = org.hyperledger.identus.protos.HealthCheckRequest::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class HealthCheckResponse(
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.HealthCheckResponse = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.HealthCheckResponse> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.HealthCheckResponse> {
        public val defaultInstance: org.hyperledger.identus.protos.HealthCheckResponse by lazy { org.hyperledger.identus.protos.HealthCheckResponse() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.HealthCheckResponse = org.hyperledger.identus.protos.HealthCheckResponse.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.HealthCheckResponse> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.HealthCheckResponse, *>>(0)
            fieldsList.apply {
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.HealthCheckResponse",
                messageClass = org.hyperledger.identus.protos.HealthCheckResponse::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class Date(
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.Date = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.Date> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.Date> {
        public val defaultInstance: org.hyperledger.identus.protos.Date by lazy { org.hyperledger.identus.protos.Date() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.Date = org.hyperledger.identus.protos.Date.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.Date> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.Date, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "year",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "year",
                        value = org.hyperledger.identus.protos.Date::year
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "month",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "month",
                        value = org.hyperledger.identus.protos.Date::month
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "day",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "day",
                        value = org.hyperledger.identus.protos.Date::day
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.Date",
                messageClass = org.hyperledger.identus.protos.Date::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class TimeInterval(
    val startTimestamp: pbandk.wkt.Timestamp? = null,
    val endTimestamp: pbandk.wkt.Timestamp? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.TimeInterval = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.TimeInterval> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.TimeInterval> {
        public val defaultInstance: org.hyperledger.identus.protos.TimeInterval by lazy { org.hyperledger.identus.protos.TimeInterval() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.TimeInterval = org.hyperledger.identus.protos.TimeInterval.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.TimeInterval> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.TimeInterval, *>>(2)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "start_timestamp",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = pbandk.wkt.Timestamp.Companion),
                        jsonName = "startTimestamp",
                        value = org.hyperledger.identus.protos.TimeInterval::startTimestamp
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "end_timestamp",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = pbandk.wkt.Timestamp.Companion),
                        jsonName = "endTimestamp",
                        value = org.hyperledger.identus.protos.TimeInterval::endTimestamp
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.TimeInterval",
                messageClass = org.hyperledger.identus.protos.TimeInterval::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class BlockInfo(
    val number: Int = 0,
    val index: Int = 0,
    val timestamp: pbandk.wkt.Timestamp? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.BlockInfo = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.BlockInfo> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.BlockInfo> {
        public val defaultInstance: org.hyperledger.identus.protos.BlockInfo by lazy { org.hyperledger.identus.protos.BlockInfo() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.BlockInfo = org.hyperledger.identus.protos.BlockInfo.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.BlockInfo> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.BlockInfo, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "number",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "number",
                        value = org.hyperledger.identus.protos.BlockInfo::number
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "index",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "index",
                        value = org.hyperledger.identus.protos.BlockInfo::index
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "timestamp",
                        number = 4,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = pbandk.wkt.Timestamp.Companion),
                        jsonName = "timestamp",
                        value = org.hyperledger.identus.protos.BlockInfo::timestamp
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.BlockInfo",
                messageClass = org.hyperledger.identus.protos.BlockInfo::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class TransactionInfo(
    val transactionId: String = "",
    val ledger: org.hyperledger.identus.protos.Ledger = org.hyperledger.identus.protos.Ledger.fromValue(0),
    val block: org.hyperledger.identus.protos.BlockInfo? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.TransactionInfo = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.TransactionInfo> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.TransactionInfo> {
        public val defaultInstance: org.hyperledger.identus.protos.TransactionInfo by lazy { org.hyperledger.identus.protos.TransactionInfo() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.TransactionInfo = org.hyperledger.identus.protos.TransactionInfo.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.TransactionInfo> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.TransactionInfo, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "transaction_id",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "transactionId",
                        value = org.hyperledger.identus.protos.TransactionInfo::transactionId
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "ledger",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Enum(enumCompanion = org.hyperledger.identus.protos.Ledger.Companion),
                        jsonName = "ledger",
                        value = org.hyperledger.identus.protos.TransactionInfo::ledger
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "block",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.BlockInfo.Companion),
                        jsonName = "block",
                        value = org.hyperledger.identus.protos.TransactionInfo::block
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.TransactionInfo",
                messageClass = org.hyperledger.identus.protos.TransactionInfo::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
@pbandk.JsName("orDefaultForHealthCheckRequest")
public fun HealthCheckRequest?.orDefault(): org.hyperledger.identus.protos.HealthCheckRequest = this ?: HealthCheckRequest.defaultInstance

private fun HealthCheckRequest.protoMergeImpl(plus: pbandk.Message?): HealthCheckRequest = (plus as? HealthCheckRequest)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun HealthCheckRequest.Companion.decodeWithImpl(u: pbandk.MessageDecoder): HealthCheckRequest {

    val unknownFields = u.readMessage(this) { _, _ -> }

    return HealthCheckRequest(unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForHealthCheckResponse")
public fun HealthCheckResponse?.orDefault(): org.hyperledger.identus.protos.HealthCheckResponse = this ?: HealthCheckResponse.defaultInstance

private fun HealthCheckResponse.protoMergeImpl(plus: pbandk.Message?): HealthCheckResponse = (plus as? HealthCheckResponse)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun HealthCheckResponse.Companion.decodeWithImpl(u: pbandk.MessageDecoder): HealthCheckResponse {

    val unknownFields = u.readMessage(this) { _, _ -> }

    return HealthCheckResponse(unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForDate")
public fun Date?.orDefault(): org.hyperledger.identus.protos.Date = this ?: Date.defaultInstance

private fun Date.protoMergeImpl(plus: pbandk.Message?): Date = (plus as? Date)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun Date.Companion.decodeWithImpl(u: pbandk.MessageDecoder): Date {
    var year = 0
    var month = 0
    var day = 0

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> year = _fieldValue as Int
            2 -> month = _fieldValue as Int
            3 -> day = _fieldValue as Int
        }
    }

    return Date(year, month, day, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForTimeInterval")
public fun TimeInterval?.orDefault(): org.hyperledger.identus.protos.TimeInterval = this ?: TimeInterval.defaultInstance

private fun TimeInterval.protoMergeImpl(plus: pbandk.Message?): TimeInterval = (plus as? TimeInterval)?.let {
    it.copy(
        startTimestamp = startTimestamp?.plus(plus.startTimestamp) ?: plus.startTimestamp,
        endTimestamp = endTimestamp?.plus(plus.endTimestamp) ?: plus.endTimestamp,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun TimeInterval.Companion.decodeWithImpl(u: pbandk.MessageDecoder): TimeInterval {
    var startTimestamp: pbandk.wkt.Timestamp? = null
    var endTimestamp: pbandk.wkt.Timestamp? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> startTimestamp = _fieldValue as pbandk.wkt.Timestamp
            2 -> endTimestamp = _fieldValue as pbandk.wkt.Timestamp
        }
    }

    return TimeInterval(startTimestamp, endTimestamp, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForBlockInfo")
public fun BlockInfo?.orDefault(): org.hyperledger.identus.protos.BlockInfo = this ?: BlockInfo.defaultInstance

private fun BlockInfo.protoMergeImpl(plus: pbandk.Message?): BlockInfo = (plus as? BlockInfo)?.let {
    it.copy(
        timestamp = timestamp?.plus(plus.timestamp) ?: plus.timestamp,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun BlockInfo.Companion.decodeWithImpl(u: pbandk.MessageDecoder): BlockInfo {
    var number = 0
    var index = 0
    var timestamp: pbandk.wkt.Timestamp? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> number = _fieldValue as Int
            3 -> index = _fieldValue as Int
            4 -> timestamp = _fieldValue as pbandk.wkt.Timestamp
        }
    }

    return BlockInfo(number, index, timestamp, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForTransactionInfo")
public fun TransactionInfo?.orDefault(): org.hyperledger.identus.protos.TransactionInfo = this ?: TransactionInfo.defaultInstance

private fun TransactionInfo.protoMergeImpl(plus: pbandk.Message?): TransactionInfo = (plus as? TransactionInfo)?.let {
    it.copy(
        block = block?.plus(plus.block) ?: plus.block,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun TransactionInfo.Companion.decodeWithImpl(u: pbandk.MessageDecoder): TransactionInfo {
    var transactionId = ""
    var ledger: org.hyperledger.identus.protos.Ledger = org.hyperledger.identus.protos.Ledger.fromValue(0)
    var block: org.hyperledger.identus.protos.BlockInfo? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> transactionId = _fieldValue as String
            2 -> ledger = _fieldValue as org.hyperledger.identus.protos.Ledger
            3 -> block = _fieldValue as org.hyperledger.identus.protos.BlockInfo
        }
    }

    return TransactionInfo(transactionId, ledger, block, unknownFields)
}
