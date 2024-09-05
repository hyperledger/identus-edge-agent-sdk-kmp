@file:OptIn(pbandk.PublicForGeneratedCode::class)

package org.hyperledger.identus.protos

@pbandk.Export
public sealed class KeyUsage(override val value: Int, override val name: String? = null) : pbandk.Message.Enum {
    override fun equals(other: kotlin.Any?): Boolean = other is KeyUsage && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "KeyUsage.${name ?: "UNRECOGNIZED"}(value=$value)"

    public object UNKNOWN_KEY : KeyUsage(0, "UNKNOWN_KEY")
    public object MASTER_KEY : KeyUsage(1, "MASTER_KEY")
    public object ISSUING_KEY : KeyUsage(2, "ISSUING_KEY")
    public object KEY_AGREEMENT_KEY : KeyUsage(3, "KEY_AGREEMENT_KEY")
    public object AUTHENTICATION_KEY : KeyUsage(4, "AUTHENTICATION_KEY")
    public object REVOCATION_KEY : KeyUsage(5, "REVOCATION_KEY")
    public object CAPABILITY_INVOCATION_KEY : KeyUsage(6, "CAPABILITY_INVOCATION_KEY")
    public object CAPABILITY_DELEGATION_KEY : KeyUsage(7, "CAPABILITY_DELEGATION_KEY")
    public class UNRECOGNIZED(value: Int) : KeyUsage(value)

    public companion object : pbandk.Message.Enum.Companion<KeyUsage> {
        public val values: List<KeyUsage> by lazy { listOf(UNKNOWN_KEY, MASTER_KEY, ISSUING_KEY, KEY_AGREEMENT_KEY, AUTHENTICATION_KEY, REVOCATION_KEY, CAPABILITY_INVOCATION_KEY, CAPABILITY_DELEGATION_KEY) }
        override fun fromValue(value: Int): KeyUsage = values.firstOrNull { it.value == value } ?: UNRECOGNIZED(value)
        override fun fromName(name: String): KeyUsage = values.firstOrNull { it.name == name } ?: throw IllegalArgumentException("No KeyUsage with name: $name")
    }
}

@pbandk.Export
public data class TimestampInfo(
    val blockSequenceNumber: Int = 0,
    val operationSequenceNumber: Int = 0,
    val blockTimestamp: pbandk.wkt.Timestamp? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.TimestampInfo = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.TimestampInfo> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.TimestampInfo> {
        public val defaultInstance: org.hyperledger.identus.protos.TimestampInfo by lazy { org.hyperledger.identus.protos.TimestampInfo() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.TimestampInfo = org.hyperledger.identus.protos.TimestampInfo.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.TimestampInfo> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.TimestampInfo, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "block_sequence_number",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.UInt32(),
                        jsonName = "blockSequenceNumber",
                        value = org.hyperledger.identus.protos.TimestampInfo::blockSequenceNumber
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "operation_sequence_number",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Primitive.UInt32(),
                        jsonName = "operationSequenceNumber",
                        value = org.hyperledger.identus.protos.TimestampInfo::operationSequenceNumber
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "block_timestamp",
                        number = 4,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = pbandk.wkt.Timestamp.Companion),
                        jsonName = "blockTimestamp",
                        value = org.hyperledger.identus.protos.TimestampInfo::blockTimestamp
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.TimestampInfo",
                messageClass = org.hyperledger.identus.protos.TimestampInfo::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class ECKeyData(
    val curve: String = "",
    val x: pbandk.ByteArr = pbandk.ByteArr.empty,
    val y: pbandk.ByteArr = pbandk.ByteArr.empty,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.ECKeyData = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ECKeyData> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.ECKeyData> {
        public val defaultInstance: org.hyperledger.identus.protos.ECKeyData by lazy { org.hyperledger.identus.protos.ECKeyData() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.ECKeyData = org.hyperledger.identus.protos.ECKeyData.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ECKeyData> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.ECKeyData, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "curve",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "curve",
                        value = org.hyperledger.identus.protos.ECKeyData::curve
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "x",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "x",
                        value = org.hyperledger.identus.protos.ECKeyData::x
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "y",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "y",
                        value = org.hyperledger.identus.protos.ECKeyData::y
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.ECKeyData",
                messageClass = org.hyperledger.identus.protos.ECKeyData::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class CompressedECKeyData(
    val curve: String = "",
    val data: pbandk.ByteArr = pbandk.ByteArr.empty,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.CompressedECKeyData = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CompressedECKeyData> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.CompressedECKeyData> {
        public val defaultInstance: org.hyperledger.identus.protos.CompressedECKeyData by lazy { org.hyperledger.identus.protos.CompressedECKeyData() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.CompressedECKeyData = org.hyperledger.identus.protos.CompressedECKeyData.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CompressedECKeyData> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.CompressedECKeyData, *>>(2)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "curve",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "curve",
                        value = org.hyperledger.identus.protos.CompressedECKeyData::curve
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "data",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "data",
                        value = org.hyperledger.identus.protos.CompressedECKeyData::data
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.CompressedECKeyData",
                messageClass = org.hyperledger.identus.protos.CompressedECKeyData::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class PublicKey(
    val id: String = "",
    val usage: org.hyperledger.identus.protos.KeyUsage = org.hyperledger.identus.protos.KeyUsage.fromValue(0),
    val addedOn: org.hyperledger.identus.protos.LedgerData? = null,
    val revokedOn: org.hyperledger.identus.protos.LedgerData? = null,
    val keyData: KeyData<*>? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    public sealed class KeyData<V>(value: V) : pbandk.Message.OneOf<V>(value) {
        public class EcKeyData(ecKeyData: org.hyperledger.identus.protos.ECKeyData) : KeyData<org.hyperledger.identus.protos.ECKeyData>(ecKeyData)
        public class CompressedEcKeyData(compressedEcKeyData: org.hyperledger.identus.protos.CompressedECKeyData) : KeyData<org.hyperledger.identus.protos.CompressedECKeyData>(compressedEcKeyData)
    }

    val ecKeyData: org.hyperledger.identus.protos.ECKeyData?
        get() = (keyData as? KeyData.EcKeyData)?.value
    val compressedEcKeyData: org.hyperledger.identus.protos.CompressedECKeyData?
        get() = (keyData as? KeyData.CompressedEcKeyData)?.value

    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.PublicKey = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.PublicKey> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.PublicKey> {
        public val defaultInstance: org.hyperledger.identus.protos.PublicKey by lazy { org.hyperledger.identus.protos.PublicKey() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.PublicKey = org.hyperledger.identus.protos.PublicKey.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.PublicKey> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.PublicKey, *>>(6)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "id",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "id",
                        value = org.hyperledger.identus.protos.PublicKey::id
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "usage",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Enum(enumCompanion = org.hyperledger.identus.protos.KeyUsage.Companion),
                        jsonName = "usage",
                        value = org.hyperledger.identus.protos.PublicKey::usage
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "added_on",
                        number = 5,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.LedgerData.Companion),
                        jsonName = "addedOn",
                        value = org.hyperledger.identus.protos.PublicKey::addedOn
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "revoked_on",
                        number = 6,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.LedgerData.Companion),
                        jsonName = "revokedOn",
                        value = org.hyperledger.identus.protos.PublicKey::revokedOn
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "ec_key_data",
                        number = 8,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.ECKeyData.Companion),
                        oneofMember = true,
                        jsonName = "ecKeyData",
                        value = org.hyperledger.identus.protos.PublicKey::ecKeyData
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "compressed_ec_key_data",
                        number = 9,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.CompressedECKeyData.Companion),
                        oneofMember = true,
                        jsonName = "compressedEcKeyData",
                        value = org.hyperledger.identus.protos.PublicKey::compressedEcKeyData
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.PublicKey",
                messageClass = org.hyperledger.identus.protos.PublicKey::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class DIDData(
    val id: String = "",
    val publicKeys: List<org.hyperledger.identus.protos.PublicKey> = emptyList(),
    val services: List<org.hyperledger.identus.protos.Service> = emptyList(),
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.DIDData = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.DIDData> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.DIDData> {
        public val defaultInstance: org.hyperledger.identus.protos.DIDData by lazy { org.hyperledger.identus.protos.DIDData() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.DIDData = org.hyperledger.identus.protos.DIDData.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.DIDData> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.DIDData, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "id",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "id",
                        value = org.hyperledger.identus.protos.DIDData::id
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "public_keys",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Repeated<org.hyperledger.identus.protos.PublicKey>(valueType = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.PublicKey.Companion)),
                        jsonName = "publicKeys",
                        value = org.hyperledger.identus.protos.DIDData::publicKeys
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "services",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Repeated<org.hyperledger.identus.protos.Service>(valueType = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.Service.Companion)),
                        jsonName = "services",
                        value = org.hyperledger.identus.protos.DIDData::services
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.DIDData",
                messageClass = org.hyperledger.identus.protos.DIDData::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class CreateDIDOperation(
    val didData: org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.CreateDIDOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CreateDIDOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.CreateDIDOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.CreateDIDOperation by lazy { org.hyperledger.identus.protos.CreateDIDOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.CreateDIDOperation = org.hyperledger.identus.protos.CreateDIDOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CreateDIDOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.CreateDIDOperation, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "did_data",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData.Companion),
                        jsonName = "didData",
                        value = org.hyperledger.identus.protos.CreateDIDOperation::didData
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.CreateDIDOperation",
                messageClass = org.hyperledger.identus.protos.CreateDIDOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }

    public data class DIDCreationData(
        val publicKeys: List<org.hyperledger.identus.protos.PublicKey> = emptyList(),
        val services: List<org.hyperledger.identus.protos.Service> = emptyList(),
        override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
    ) : pbandk.Message {
        override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData = protoMergeImpl(other)
        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData> get() = Companion.descriptor
        override val protoSize: Int by lazy { super.protoSize }
        public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData> {
            public val defaultInstance: org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData by lazy { org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData() }
            override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData = org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData.decodeWithImpl(u)

            override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData> by lazy {
                val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData, *>>(2)
                fieldsList.apply {
                    add(
                        pbandk.FieldDescriptor(
                            messageDescriptor = this@Companion::descriptor,
                            name = "public_keys",
                            number = 2,
                            type = pbandk.FieldDescriptor.Type.Repeated<org.hyperledger.identus.protos.PublicKey>(valueType = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.PublicKey.Companion)),
                            jsonName = "publicKeys",
                            value = org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData::publicKeys
                        )
                    )
                    add(
                        pbandk.FieldDescriptor(
                            messageDescriptor = this@Companion::descriptor,
                            name = "services",
                            number = 3,
                            type = pbandk.FieldDescriptor.Type.Repeated<org.hyperledger.identus.protos.Service>(valueType = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.Service.Companion)),
                            jsonName = "services",
                            value = org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData::services
                        )
                    )
                }
                pbandk.MessageDescriptor(
                    fullName = "io.iohk.atala.prism.protos.CreateDIDOperation.DIDCreationData",
                    messageClass = org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData::class,
                    messageCompanion = this,
                    fields = fieldsList
                )
            }
        }
    }
}

@pbandk.Export
public data class AddKeyAction(
    val key: org.hyperledger.identus.protos.PublicKey? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.AddKeyAction = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.AddKeyAction> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.AddKeyAction> {
        public val defaultInstance: org.hyperledger.identus.protos.AddKeyAction by lazy { org.hyperledger.identus.protos.AddKeyAction() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.AddKeyAction = org.hyperledger.identus.protos.AddKeyAction.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.AddKeyAction> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.AddKeyAction, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "key",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.PublicKey.Companion),
                        jsonName = "key",
                        value = org.hyperledger.identus.protos.AddKeyAction::key
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.AddKeyAction",
                messageClass = org.hyperledger.identus.protos.AddKeyAction::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class RemoveKeyAction(
    val keyId: String = "",
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.RemoveKeyAction = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RemoveKeyAction> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.RemoveKeyAction> {
        public val defaultInstance: org.hyperledger.identus.protos.RemoveKeyAction by lazy { org.hyperledger.identus.protos.RemoveKeyAction() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.RemoveKeyAction = org.hyperledger.identus.protos.RemoveKeyAction.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RemoveKeyAction> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.RemoveKeyAction, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "keyId",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "keyId",
                        value = org.hyperledger.identus.protos.RemoveKeyAction::keyId
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.RemoveKeyAction",
                messageClass = org.hyperledger.identus.protos.RemoveKeyAction::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class AddServiceAction(
    val service: org.hyperledger.identus.protos.Service? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.AddServiceAction = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.AddServiceAction> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.AddServiceAction> {
        public val defaultInstance: org.hyperledger.identus.protos.AddServiceAction by lazy { org.hyperledger.identus.protos.AddServiceAction() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.AddServiceAction = org.hyperledger.identus.protos.AddServiceAction.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.AddServiceAction> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.AddServiceAction, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "service",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.Service.Companion),
                        jsonName = "service",
                        value = org.hyperledger.identus.protos.AddServiceAction::service
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.AddServiceAction",
                messageClass = org.hyperledger.identus.protos.AddServiceAction::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class RemoveServiceAction(
    val serviceId: String = "",
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.RemoveServiceAction = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RemoveServiceAction> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.RemoveServiceAction> {
        public val defaultInstance: org.hyperledger.identus.protos.RemoveServiceAction by lazy { org.hyperledger.identus.protos.RemoveServiceAction() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.RemoveServiceAction = org.hyperledger.identus.protos.RemoveServiceAction.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RemoveServiceAction> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.RemoveServiceAction, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "serviceId",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "serviceId",
                        value = org.hyperledger.identus.protos.RemoveServiceAction::serviceId
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.RemoveServiceAction",
                messageClass = org.hyperledger.identus.protos.RemoveServiceAction::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class UpdateServiceAction(
    val serviceId: String = "",
    val type: String = "",
    val serviceEndpoints: List<String> = emptyList(),
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.UpdateServiceAction = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateServiceAction> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.UpdateServiceAction> {
        public val defaultInstance: org.hyperledger.identus.protos.UpdateServiceAction by lazy { org.hyperledger.identus.protos.UpdateServiceAction() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.UpdateServiceAction = org.hyperledger.identus.protos.UpdateServiceAction.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateServiceAction> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.UpdateServiceAction, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "serviceId",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "serviceId",
                        value = org.hyperledger.identus.protos.UpdateServiceAction::serviceId
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "type",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "type",
                        value = org.hyperledger.identus.protos.UpdateServiceAction::type
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "service_endpoints",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Repeated<String>(valueType = pbandk.FieldDescriptor.Type.Primitive.String()),
                        jsonName = "serviceEndpoints",
                        value = org.hyperledger.identus.protos.UpdateServiceAction::serviceEndpoints
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.UpdateServiceAction",
                messageClass = org.hyperledger.identus.protos.UpdateServiceAction::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class UpdateDIDAction(
    val action: Action<*>? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    public sealed class Action<V>(value: V) : pbandk.Message.OneOf<V>(value) {
        public class AddKey(addKey: org.hyperledger.identus.protos.AddKeyAction) : Action<org.hyperledger.identus.protos.AddKeyAction>(addKey)
        public class RemoveKey(removeKey: org.hyperledger.identus.protos.RemoveKeyAction) : Action<org.hyperledger.identus.protos.RemoveKeyAction>(removeKey)
        public class AddService(addService: org.hyperledger.identus.protos.AddServiceAction) : Action<org.hyperledger.identus.protos.AddServiceAction>(addService)
        public class RemoveService(removeService: org.hyperledger.identus.protos.RemoveServiceAction) : Action<org.hyperledger.identus.protos.RemoveServiceAction>(removeService)
        public class UpdateService(updateService: org.hyperledger.identus.protos.UpdateServiceAction) : Action<org.hyperledger.identus.protos.UpdateServiceAction>(updateService)
    }

    val addKey: org.hyperledger.identus.protos.AddKeyAction?
        get() = (action as? Action.AddKey)?.value
    val removeKey: org.hyperledger.identus.protos.RemoveKeyAction?
        get() = (action as? Action.RemoveKey)?.value
    val addService: org.hyperledger.identus.protos.AddServiceAction?
        get() = (action as? Action.AddService)?.value
    val removeService: org.hyperledger.identus.protos.RemoveServiceAction?
        get() = (action as? Action.RemoveService)?.value
    val updateService: org.hyperledger.identus.protos.UpdateServiceAction?
        get() = (action as? Action.UpdateService)?.value

    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.UpdateDIDAction = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateDIDAction> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.UpdateDIDAction> {
        public val defaultInstance: org.hyperledger.identus.protos.UpdateDIDAction by lazy { org.hyperledger.identus.protos.UpdateDIDAction() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.UpdateDIDAction = org.hyperledger.identus.protos.UpdateDIDAction.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateDIDAction> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.UpdateDIDAction, *>>(5)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "add_key",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.AddKeyAction.Companion),
                        oneofMember = true,
                        jsonName = "addKey",
                        value = org.hyperledger.identus.protos.UpdateDIDAction::addKey
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "remove_key",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.RemoveKeyAction.Companion),
                        oneofMember = true,
                        jsonName = "removeKey",
                        value = org.hyperledger.identus.protos.UpdateDIDAction::removeKey
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "add_service",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.AddServiceAction.Companion),
                        oneofMember = true,
                        jsonName = "addService",
                        value = org.hyperledger.identus.protos.UpdateDIDAction::addService
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "remove_service",
                        number = 4,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.RemoveServiceAction.Companion),
                        oneofMember = true,
                        jsonName = "removeService",
                        value = org.hyperledger.identus.protos.UpdateDIDAction::removeService
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "update_service",
                        number = 5,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.UpdateServiceAction.Companion),
                        oneofMember = true,
                        jsonName = "updateService",
                        value = org.hyperledger.identus.protos.UpdateDIDAction::updateService
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.UpdateDIDAction",
                messageClass = org.hyperledger.identus.protos.UpdateDIDAction::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class UpdateDIDOperation(
    val previousOperationHash: pbandk.ByteArr = pbandk.ByteArr.empty,
    val id: String = "",
    val actions: List<org.hyperledger.identus.protos.UpdateDIDAction> = emptyList(),
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.UpdateDIDOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateDIDOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.UpdateDIDOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.UpdateDIDOperation by lazy { org.hyperledger.identus.protos.UpdateDIDOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.UpdateDIDOperation = org.hyperledger.identus.protos.UpdateDIDOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateDIDOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.UpdateDIDOperation, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "previous_operation_hash",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "previousOperationHash",
                        value = org.hyperledger.identus.protos.UpdateDIDOperation::previousOperationHash
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "id",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "id",
                        value = org.hyperledger.identus.protos.UpdateDIDOperation::id
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "actions",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Repeated<org.hyperledger.identus.protos.UpdateDIDAction>(valueType = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.UpdateDIDAction.Companion)),
                        jsonName = "actions",
                        value = org.hyperledger.identus.protos.UpdateDIDOperation::actions
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.UpdateDIDOperation",
                messageClass = org.hyperledger.identus.protos.UpdateDIDOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class CredentialBatchData(
    val issuerDid: String = "",
    val merkleRoot: pbandk.ByteArr = pbandk.ByteArr.empty,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.CredentialBatchData = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CredentialBatchData> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.CredentialBatchData> {
        public val defaultInstance: org.hyperledger.identus.protos.CredentialBatchData by lazy { org.hyperledger.identus.protos.CredentialBatchData() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.CredentialBatchData = org.hyperledger.identus.protos.CredentialBatchData.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CredentialBatchData> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.CredentialBatchData, *>>(2)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "issuer_did",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "issuerDid",
                        value = org.hyperledger.identus.protos.CredentialBatchData::issuerDid
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "merkle_root",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "merkleRoot",
                        value = org.hyperledger.identus.protos.CredentialBatchData::merkleRoot
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.CredentialBatchData",
                messageClass = org.hyperledger.identus.protos.CredentialBatchData::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class IssueCredentialBatchOperation(
    val credentialBatchData: org.hyperledger.identus.protos.CredentialBatchData? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.IssueCredentialBatchOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.IssueCredentialBatchOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.IssueCredentialBatchOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.IssueCredentialBatchOperation by lazy { org.hyperledger.identus.protos.IssueCredentialBatchOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.IssueCredentialBatchOperation = org.hyperledger.identus.protos.IssueCredentialBatchOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.IssueCredentialBatchOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.IssueCredentialBatchOperation, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "credential_batch_data",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.CredentialBatchData.Companion),
                        jsonName = "credentialBatchData",
                        value = org.hyperledger.identus.protos.IssueCredentialBatchOperation::credentialBatchData
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.IssueCredentialBatchOperation",
                messageClass = org.hyperledger.identus.protos.IssueCredentialBatchOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class RevokeCredentialsOperation(
    val previousOperationHash: pbandk.ByteArr = pbandk.ByteArr.empty,
    val credentialBatchId: String = "",
    val credentialsToRevoke: List<pbandk.ByteArr> = emptyList(),
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.RevokeCredentialsOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RevokeCredentialsOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.RevokeCredentialsOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.RevokeCredentialsOperation by lazy { org.hyperledger.identus.protos.RevokeCredentialsOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.RevokeCredentialsOperation = org.hyperledger.identus.protos.RevokeCredentialsOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RevokeCredentialsOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.RevokeCredentialsOperation, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "previous_operation_hash",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "previousOperationHash",
                        value = org.hyperledger.identus.protos.RevokeCredentialsOperation::previousOperationHash
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "credential_batch_id",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "credentialBatchId",
                        value = org.hyperledger.identus.protos.RevokeCredentialsOperation::credentialBatchId
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "credentials_to_revoke",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Repeated<pbandk.ByteArr>(valueType = pbandk.FieldDescriptor.Type.Primitive.Bytes()),
                        jsonName = "credentialsToRevoke",
                        value = org.hyperledger.identus.protos.RevokeCredentialsOperation::credentialsToRevoke
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.RevokeCredentialsOperation",
                messageClass = org.hyperledger.identus.protos.RevokeCredentialsOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class ProtocolVersionUpdateOperation(
    val proposerDid: String = "",
    val version: org.hyperledger.identus.protos.ProtocolVersionInfo? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.ProtocolVersionUpdateOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersionUpdateOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.ProtocolVersionUpdateOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.ProtocolVersionUpdateOperation by lazy { org.hyperledger.identus.protos.ProtocolVersionUpdateOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.ProtocolVersionUpdateOperation = org.hyperledger.identus.protos.ProtocolVersionUpdateOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersionUpdateOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.ProtocolVersionUpdateOperation, *>>(2)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "proposer_did",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "proposerDid",
                        value = org.hyperledger.identus.protos.ProtocolVersionUpdateOperation::proposerDid
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "version",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.ProtocolVersionInfo.Companion),
                        jsonName = "version",
                        value = org.hyperledger.identus.protos.ProtocolVersionUpdateOperation::version
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.ProtocolVersionUpdateOperation",
                messageClass = org.hyperledger.identus.protos.ProtocolVersionUpdateOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class ProtocolVersion(
    val majorVersion: Int = 0,
    val minorVersion: Int = 0,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.ProtocolVersion = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersion> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.ProtocolVersion> {
        public val defaultInstance: org.hyperledger.identus.protos.ProtocolVersion by lazy { org.hyperledger.identus.protos.ProtocolVersion() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.ProtocolVersion = org.hyperledger.identus.protos.ProtocolVersion.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersion> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.ProtocolVersion, *>>(2)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "major_version",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "majorVersion",
                        value = org.hyperledger.identus.protos.ProtocolVersion::majorVersion
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "minor_version",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "minorVersion",
                        value = org.hyperledger.identus.protos.ProtocolVersion::minorVersion
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.ProtocolVersion",
                messageClass = org.hyperledger.identus.protos.ProtocolVersion::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class ProtocolVersionInfo(
    val versionName: String = "",
    val effectiveSince: Int = 0,
    val protocolVersion: org.hyperledger.identus.protos.ProtocolVersion? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.ProtocolVersionInfo = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersionInfo> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.ProtocolVersionInfo> {
        public val defaultInstance: org.hyperledger.identus.protos.ProtocolVersionInfo by lazy { org.hyperledger.identus.protos.ProtocolVersionInfo() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.ProtocolVersionInfo = org.hyperledger.identus.protos.ProtocolVersionInfo.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersionInfo> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.ProtocolVersionInfo, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "version_name",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "versionName",
                        value = org.hyperledger.identus.protos.ProtocolVersionInfo::versionName
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "effective_since",
                        number = 4,
                        type = pbandk.FieldDescriptor.Type.Primitive.Int32(),
                        jsonName = "effectiveSince",
                        value = org.hyperledger.identus.protos.ProtocolVersionInfo::effectiveSince
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "protocol_version",
                        number = 5,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.ProtocolVersion.Companion),
                        jsonName = "protocolVersion",
                        value = org.hyperledger.identus.protos.ProtocolVersionInfo::protocolVersion
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.ProtocolVersionInfo",
                messageClass = org.hyperledger.identus.protos.ProtocolVersionInfo::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class DeactivateDIDOperation(
    val previousOperationHash: pbandk.ByteArr = pbandk.ByteArr.empty,
    val id: String = "",
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.DeactivateDIDOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.DeactivateDIDOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.DeactivateDIDOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.DeactivateDIDOperation by lazy { org.hyperledger.identus.protos.DeactivateDIDOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.DeactivateDIDOperation = org.hyperledger.identus.protos.DeactivateDIDOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.DeactivateDIDOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.DeactivateDIDOperation, *>>(2)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "previous_operation_hash",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "previousOperationHash",
                        value = org.hyperledger.identus.protos.DeactivateDIDOperation::previousOperationHash
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "id",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "id",
                        value = org.hyperledger.identus.protos.DeactivateDIDOperation::id
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.DeactivateDIDOperation",
                messageClass = org.hyperledger.identus.protos.DeactivateDIDOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class AtalaOperation(
    val operation: Operation<*>? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    public sealed class Operation<V>(value: V) : pbandk.Message.OneOf<V>(value) {
        public class CreateDid(createDid: org.hyperledger.identus.protos.CreateDIDOperation) : Operation<org.hyperledger.identus.protos.CreateDIDOperation>(createDid)
        public class UpdateDid(updateDid: org.hyperledger.identus.protos.UpdateDIDOperation) : Operation<org.hyperledger.identus.protos.UpdateDIDOperation>(updateDid)
        public class IssueCredentialBatch(issueCredentialBatch: org.hyperledger.identus.protos.IssueCredentialBatchOperation) : Operation<org.hyperledger.identus.protos.IssueCredentialBatchOperation>(issueCredentialBatch)
        public class RevokeCredentials(revokeCredentials: org.hyperledger.identus.protos.RevokeCredentialsOperation) : Operation<org.hyperledger.identus.protos.RevokeCredentialsOperation>(revokeCredentials)
        public class ProtocolVersionUpdate(protocolVersionUpdate: org.hyperledger.identus.protos.ProtocolVersionUpdateOperation) : Operation<org.hyperledger.identus.protos.ProtocolVersionUpdateOperation>(protocolVersionUpdate)
        public class DeactivateDid(deactivateDid: org.hyperledger.identus.protos.DeactivateDIDOperation) : Operation<org.hyperledger.identus.protos.DeactivateDIDOperation>(deactivateDid)
    }

    val createDid: org.hyperledger.identus.protos.CreateDIDOperation?
        get() = (operation as? Operation.CreateDid)?.value
    val updateDid: org.hyperledger.identus.protos.UpdateDIDOperation?
        get() = (operation as? Operation.UpdateDid)?.value
    val issueCredentialBatch: org.hyperledger.identus.protos.IssueCredentialBatchOperation?
        get() = (operation as? Operation.IssueCredentialBatch)?.value
    val revokeCredentials: org.hyperledger.identus.protos.RevokeCredentialsOperation?
        get() = (operation as? Operation.RevokeCredentials)?.value
    val protocolVersionUpdate: org.hyperledger.identus.protos.ProtocolVersionUpdateOperation?
        get() = (operation as? Operation.ProtocolVersionUpdate)?.value
    val deactivateDid: org.hyperledger.identus.protos.DeactivateDIDOperation?
        get() = (operation as? Operation.DeactivateDid)?.value

    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.AtalaOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.AtalaOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.AtalaOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.AtalaOperation by lazy { org.hyperledger.identus.protos.AtalaOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.AtalaOperation = org.hyperledger.identus.protos.AtalaOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.AtalaOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.AtalaOperation, *>>(6)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "create_did",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.CreateDIDOperation.Companion),
                        oneofMember = true,
                        jsonName = "createDid",
                        value = org.hyperledger.identus.protos.AtalaOperation::createDid
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "update_did",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.UpdateDIDOperation.Companion),
                        oneofMember = true,
                        jsonName = "updateDid",
                        value = org.hyperledger.identus.protos.AtalaOperation::updateDid
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "issue_credential_batch",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.IssueCredentialBatchOperation.Companion),
                        oneofMember = true,
                        jsonName = "issueCredentialBatch",
                        value = org.hyperledger.identus.protos.AtalaOperation::issueCredentialBatch
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "revoke_credentials",
                        number = 4,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.RevokeCredentialsOperation.Companion),
                        oneofMember = true,
                        jsonName = "revokeCredentials",
                        value = org.hyperledger.identus.protos.AtalaOperation::revokeCredentials
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "protocol_version_update",
                        number = 5,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.ProtocolVersionUpdateOperation.Companion),
                        oneofMember = true,
                        jsonName = "protocolVersionUpdate",
                        value = org.hyperledger.identus.protos.AtalaOperation::protocolVersionUpdate
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "deactivate_did",
                        number = 6,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.DeactivateDIDOperation.Companion),
                        oneofMember = true,
                        jsonName = "deactivateDid",
                        value = org.hyperledger.identus.protos.AtalaOperation::deactivateDid
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.AtalaOperation",
                messageClass = org.hyperledger.identus.protos.AtalaOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class SignedAtalaOperation(
    val signedWith: String = "",
    val signature: pbandk.ByteArr = pbandk.ByteArr.empty,
    val operation: org.hyperledger.identus.protos.AtalaOperation? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.SignedAtalaOperation = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.SignedAtalaOperation> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.SignedAtalaOperation> {
        public val defaultInstance: org.hyperledger.identus.protos.SignedAtalaOperation by lazy { org.hyperledger.identus.protos.SignedAtalaOperation() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.SignedAtalaOperation = org.hyperledger.identus.protos.SignedAtalaOperation.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.SignedAtalaOperation> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.SignedAtalaOperation, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "signed_with",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "signedWith",
                        value = org.hyperledger.identus.protos.SignedAtalaOperation::signedWith
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "signature",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(),
                        jsonName = "signature",
                        value = org.hyperledger.identus.protos.SignedAtalaOperation::signature
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "operation",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.AtalaOperation.Companion),
                        jsonName = "operation",
                        value = org.hyperledger.identus.protos.SignedAtalaOperation::operation
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.SignedAtalaOperation",
                messageClass = org.hyperledger.identus.protos.SignedAtalaOperation::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class LedgerData(
    val transactionId: String = "",
    val ledger: org.hyperledger.identus.protos.Ledger = org.hyperledger.identus.protos.Ledger.fromValue(0),
    val timestampInfo: org.hyperledger.identus.protos.TimestampInfo? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.LedgerData = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.LedgerData> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.LedgerData> {
        public val defaultInstance: org.hyperledger.identus.protos.LedgerData by lazy { org.hyperledger.identus.protos.LedgerData() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.LedgerData = org.hyperledger.identus.protos.LedgerData.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.LedgerData> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.LedgerData, *>>(3)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "transaction_id",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "transactionId",
                        value = org.hyperledger.identus.protos.LedgerData::transactionId
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "ledger",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Enum(enumCompanion = org.hyperledger.identus.protos.Ledger.Companion),
                        jsonName = "ledger",
                        value = org.hyperledger.identus.protos.LedgerData::ledger
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "timestamp_info",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.TimestampInfo.Companion),
                        jsonName = "timestampInfo",
                        value = org.hyperledger.identus.protos.LedgerData::timestampInfo
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.LedgerData",
                messageClass = org.hyperledger.identus.protos.LedgerData::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class OperationOutput(
    val result: Result<*>? = null,
    val operationMaybe: OperationMaybe<*>? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    public sealed class Result<V>(value: V) : pbandk.Message.OneOf<V>(value) {
        public class BatchOutput(batchOutput: org.hyperledger.identus.protos.IssueCredentialBatchOutput) : Result<org.hyperledger.identus.protos.IssueCredentialBatchOutput>(batchOutput)
        public class CreateDidOutput(createDidOutput: org.hyperledger.identus.protos.CreateDIDOutput) : Result<org.hyperledger.identus.protos.CreateDIDOutput>(createDidOutput)
        public class UpdateDidOutput(updateDidOutput: org.hyperledger.identus.protos.UpdateDIDOutput) : Result<org.hyperledger.identus.protos.UpdateDIDOutput>(updateDidOutput)
        public class RevokeCredentialsOutput(revokeCredentialsOutput: org.hyperledger.identus.protos.RevokeCredentialsOutput) : Result<org.hyperledger.identus.protos.RevokeCredentialsOutput>(revokeCredentialsOutput)
        public class ProtocolVersionUpdateOutput(protocolVersionUpdateOutput: org.hyperledger.identus.protos.ProtocolVersionUpdateOutput) : Result<org.hyperledger.identus.protos.ProtocolVersionUpdateOutput>(protocolVersionUpdateOutput)
        public class DeactivateDidOutput(deactivateDidOutput: org.hyperledger.identus.protos.DeactivateDIDOutput) : Result<org.hyperledger.identus.protos.DeactivateDIDOutput>(deactivateDidOutput)
    }

    val batchOutput: org.hyperledger.identus.protos.IssueCredentialBatchOutput?
        get() = (result as? Result.BatchOutput)?.value
    val createDidOutput: org.hyperledger.identus.protos.CreateDIDOutput?
        get() = (result as? Result.CreateDidOutput)?.value
    val updateDidOutput: org.hyperledger.identus.protos.UpdateDIDOutput?
        get() = (result as? Result.UpdateDidOutput)?.value
    val revokeCredentialsOutput: org.hyperledger.identus.protos.RevokeCredentialsOutput?
        get() = (result as? Result.RevokeCredentialsOutput)?.value
    val protocolVersionUpdateOutput: org.hyperledger.identus.protos.ProtocolVersionUpdateOutput?
        get() = (result as? Result.ProtocolVersionUpdateOutput)?.value
    val deactivateDidOutput: org.hyperledger.identus.protos.DeactivateDIDOutput?
        get() = (result as? Result.DeactivateDidOutput)?.value

    public sealed class OperationMaybe<V>(value: V) : pbandk.Message.OneOf<V>(value) {
        public class OperationId(operationId: pbandk.ByteArr = pbandk.ByteArr.empty) : OperationMaybe<pbandk.ByteArr>(operationId)
        public class Error(error: String = "") : OperationMaybe<String>(error)
    }

    val operationId: pbandk.ByteArr?
        get() = (operationMaybe as? OperationMaybe.OperationId)?.value
    val error: String?
        get() = (operationMaybe as? OperationMaybe.Error)?.value

    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.OperationOutput = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.OperationOutput> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.OperationOutput> {
        public val defaultInstance: org.hyperledger.identus.protos.OperationOutput by lazy { org.hyperledger.identus.protos.OperationOutput() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.OperationOutput = org.hyperledger.identus.protos.OperationOutput.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.OperationOutput> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.OperationOutput, *>>(8)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "batch_output",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.IssueCredentialBatchOutput.Companion),
                        oneofMember = true,
                        jsonName = "batchOutput",
                        value = org.hyperledger.identus.protos.OperationOutput::batchOutput
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "create_did_output",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.CreateDIDOutput.Companion),
                        oneofMember = true,
                        jsonName = "createDidOutput",
                        value = org.hyperledger.identus.protos.OperationOutput::createDidOutput
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "update_did_output",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.UpdateDIDOutput.Companion),
                        oneofMember = true,
                        jsonName = "updateDidOutput",
                        value = org.hyperledger.identus.protos.OperationOutput::updateDidOutput
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "revoke_credentials_output",
                        number = 4,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.RevokeCredentialsOutput.Companion),
                        oneofMember = true,
                        jsonName = "revokeCredentialsOutput",
                        value = org.hyperledger.identus.protos.OperationOutput::revokeCredentialsOutput
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "operation_id",
                        number = 5,
                        type = pbandk.FieldDescriptor.Type.Primitive.Bytes(hasPresence = true),
                        oneofMember = true,
                        jsonName = "operationId",
                        value = org.hyperledger.identus.protos.OperationOutput::operationId
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "error",
                        number = 6,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(hasPresence = true),
                        oneofMember = true,
                        jsonName = "error",
                        value = org.hyperledger.identus.protos.OperationOutput::error
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "protocol_version_update_output",
                        number = 7,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.ProtocolVersionUpdateOutput.Companion),
                        oneofMember = true,
                        jsonName = "protocolVersionUpdateOutput",
                        value = org.hyperledger.identus.protos.OperationOutput::protocolVersionUpdateOutput
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "deactivate_did_output",
                        number = 8,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.DeactivateDIDOutput.Companion),
                        oneofMember = true,
                        jsonName = "deactivateDidOutput",
                        value = org.hyperledger.identus.protos.OperationOutput::deactivateDidOutput
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.OperationOutput",
                messageClass = org.hyperledger.identus.protos.OperationOutput::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class IssueCredentialBatchOutput(
    val batchId: String = "",
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.IssueCredentialBatchOutput = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.IssueCredentialBatchOutput> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.IssueCredentialBatchOutput> {
        public val defaultInstance: org.hyperledger.identus.protos.IssueCredentialBatchOutput by lazy { org.hyperledger.identus.protos.IssueCredentialBatchOutput() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.IssueCredentialBatchOutput = org.hyperledger.identus.protos.IssueCredentialBatchOutput.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.IssueCredentialBatchOutput> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.IssueCredentialBatchOutput, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "batch_id",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "batchId",
                        value = org.hyperledger.identus.protos.IssueCredentialBatchOutput::batchId
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.IssueCredentialBatchOutput",
                messageClass = org.hyperledger.identus.protos.IssueCredentialBatchOutput::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class CreateDIDOutput(
    val didSuffix: String = "",
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.CreateDIDOutput = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CreateDIDOutput> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.CreateDIDOutput> {
        public val defaultInstance: org.hyperledger.identus.protos.CreateDIDOutput by lazy { org.hyperledger.identus.protos.CreateDIDOutput() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.CreateDIDOutput = org.hyperledger.identus.protos.CreateDIDOutput.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.CreateDIDOutput> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.CreateDIDOutput, *>>(1)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "did_suffix",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "didSuffix",
                        value = org.hyperledger.identus.protos.CreateDIDOutput::didSuffix
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.CreateDIDOutput",
                messageClass = org.hyperledger.identus.protos.CreateDIDOutput::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class UpdateDIDOutput(
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.UpdateDIDOutput = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateDIDOutput> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.UpdateDIDOutput> {
        public val defaultInstance: org.hyperledger.identus.protos.UpdateDIDOutput by lazy { org.hyperledger.identus.protos.UpdateDIDOutput() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.UpdateDIDOutput = org.hyperledger.identus.protos.UpdateDIDOutput.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.UpdateDIDOutput> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.UpdateDIDOutput, *>>(0)
            fieldsList.apply {
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.UpdateDIDOutput",
                messageClass = org.hyperledger.identus.protos.UpdateDIDOutput::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class RevokeCredentialsOutput(
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.RevokeCredentialsOutput = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RevokeCredentialsOutput> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.RevokeCredentialsOutput> {
        public val defaultInstance: org.hyperledger.identus.protos.RevokeCredentialsOutput by lazy { org.hyperledger.identus.protos.RevokeCredentialsOutput() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.RevokeCredentialsOutput = org.hyperledger.identus.protos.RevokeCredentialsOutput.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.RevokeCredentialsOutput> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.RevokeCredentialsOutput, *>>(0)
            fieldsList.apply {
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.RevokeCredentialsOutput",
                messageClass = org.hyperledger.identus.protos.RevokeCredentialsOutput::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class ProtocolVersionUpdateOutput(
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.ProtocolVersionUpdateOutput = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersionUpdateOutput> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.ProtocolVersionUpdateOutput> {
        public val defaultInstance: org.hyperledger.identus.protos.ProtocolVersionUpdateOutput by lazy { org.hyperledger.identus.protos.ProtocolVersionUpdateOutput() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.ProtocolVersionUpdateOutput = org.hyperledger.identus.protos.ProtocolVersionUpdateOutput.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.ProtocolVersionUpdateOutput> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.ProtocolVersionUpdateOutput, *>>(0)
            fieldsList.apply {
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.ProtocolVersionUpdateOutput",
                messageClass = org.hyperledger.identus.protos.ProtocolVersionUpdateOutput::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class DeactivateDIDOutput(
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.DeactivateDIDOutput = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.DeactivateDIDOutput> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.DeactivateDIDOutput> {
        public val defaultInstance: org.hyperledger.identus.protos.DeactivateDIDOutput by lazy { org.hyperledger.identus.protos.DeactivateDIDOutput() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.DeactivateDIDOutput = org.hyperledger.identus.protos.DeactivateDIDOutput.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.DeactivateDIDOutput> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.DeactivateDIDOutput, *>>(0)
            fieldsList.apply {
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.DeactivateDIDOutput",
                messageClass = org.hyperledger.identus.protos.DeactivateDIDOutput::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
public data class Service(
    val id: String = "",
    val type: String = "",
    val serviceEndpoint: List<String> = emptyList(),
    val addedOn: org.hyperledger.identus.protos.LedgerData? = null,
    val deletedOn: org.hyperledger.identus.protos.LedgerData? = null,
    override val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message {
    override operator fun plus(other: pbandk.Message?): org.hyperledger.identus.protos.Service = protoMergeImpl(other)
    override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.Service> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }
    public companion object : pbandk.Message.Companion<org.hyperledger.identus.protos.Service> {
        public val defaultInstance: org.hyperledger.identus.protos.Service by lazy { org.hyperledger.identus.protos.Service() }
        override fun decodeWith(u: pbandk.MessageDecoder): org.hyperledger.identus.protos.Service = org.hyperledger.identus.protos.Service.decodeWithImpl(u)

        override val descriptor: pbandk.MessageDescriptor<org.hyperledger.identus.protos.Service> by lazy {
            val fieldsList = ArrayList<pbandk.FieldDescriptor<org.hyperledger.identus.protos.Service, *>>(5)
            fieldsList.apply {
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "id",
                        number = 1,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "id",
                        value = org.hyperledger.identus.protos.Service::id
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "type",
                        number = 2,
                        type = pbandk.FieldDescriptor.Type.Primitive.String(),
                        jsonName = "type",
                        value = org.hyperledger.identus.protos.Service::type
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "service_endpoint",
                        number = 3,
                        type = pbandk.FieldDescriptor.Type.Repeated<String>(valueType = pbandk.FieldDescriptor.Type.Primitive.String()),
                        jsonName = "serviceEndpoint",
                        value = org.hyperledger.identus.protos.Service::serviceEndpoint
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "added_on",
                        number = 4,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.LedgerData.Companion),
                        jsonName = "addedOn",
                        value = org.hyperledger.identus.protos.Service::addedOn
                    )
                )
                add(
                    pbandk.FieldDescriptor(
                        messageDescriptor = this@Companion::descriptor,
                        name = "deleted_on",
                        number = 5,
                        type = pbandk.FieldDescriptor.Type.Message(messageCompanion = org.hyperledger.identus.protos.LedgerData.Companion),
                        jsonName = "deletedOn",
                        value = org.hyperledger.identus.protos.Service::deletedOn
                    )
                )
            }
            pbandk.MessageDescriptor(
                fullName = "io.iohk.atala.prism.protos.Service",
                messageClass = org.hyperledger.identus.protos.Service::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

@pbandk.Export
@pbandk.JsName("orDefaultForTimestampInfo")
public fun TimestampInfo?.orDefault(): org.hyperledger.identus.protos.TimestampInfo = this ?: TimestampInfo.defaultInstance

private fun TimestampInfo.protoMergeImpl(plus: pbandk.Message?): TimestampInfo = (plus as? TimestampInfo)?.let {
    it.copy(
        blockTimestamp = blockTimestamp?.plus(plus.blockTimestamp) ?: plus.blockTimestamp,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun TimestampInfo.Companion.decodeWithImpl(u: pbandk.MessageDecoder): TimestampInfo {
    var blockSequenceNumber = 0
    var operationSequenceNumber = 0
    var blockTimestamp: pbandk.wkt.Timestamp? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            2 -> blockSequenceNumber = _fieldValue as Int
            3 -> operationSequenceNumber = _fieldValue as Int
            4 -> blockTimestamp = _fieldValue as pbandk.wkt.Timestamp
        }
    }

    return TimestampInfo(blockSequenceNumber, operationSequenceNumber, blockTimestamp, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForECKeyData")
public fun ECKeyData?.orDefault(): org.hyperledger.identus.protos.ECKeyData = this ?: ECKeyData.defaultInstance

private fun ECKeyData.protoMergeImpl(plus: pbandk.Message?): ECKeyData = (plus as? ECKeyData)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun ECKeyData.Companion.decodeWithImpl(u: pbandk.MessageDecoder): ECKeyData {
    var curve = ""
    var x: pbandk.ByteArr = pbandk.ByteArr.empty
    var y: pbandk.ByteArr = pbandk.ByteArr.empty

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> curve = _fieldValue as String
            2 -> x = _fieldValue as pbandk.ByteArr
            3 -> y = _fieldValue as pbandk.ByteArr
        }
    }

    return ECKeyData(curve, x, y, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForCompressedECKeyData")
public fun CompressedECKeyData?.orDefault(): org.hyperledger.identus.protos.CompressedECKeyData = this ?: CompressedECKeyData.defaultInstance

private fun CompressedECKeyData.protoMergeImpl(plus: pbandk.Message?): CompressedECKeyData = (plus as? CompressedECKeyData)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun CompressedECKeyData.Companion.decodeWithImpl(u: pbandk.MessageDecoder): CompressedECKeyData {
    var curve = ""
    var data: pbandk.ByteArr = pbandk.ByteArr.empty

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> curve = _fieldValue as String
            2 -> data = _fieldValue as pbandk.ByteArr
        }
    }

    return CompressedECKeyData(curve, data, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForPublicKey")
public fun PublicKey?.orDefault(): org.hyperledger.identus.protos.PublicKey = this ?: PublicKey.defaultInstance

private fun PublicKey.protoMergeImpl(plus: pbandk.Message?): PublicKey = (plus as? PublicKey)?.let {
    it.copy(
        addedOn = addedOn?.plus(plus.addedOn) ?: plus.addedOn,
        revokedOn = revokedOn?.plus(plus.revokedOn) ?: plus.revokedOn,
        keyData = when {
            keyData is PublicKey.KeyData.EcKeyData && plus.keyData is PublicKey.KeyData.EcKeyData ->
                PublicKey.KeyData.EcKeyData(keyData.value + plus.keyData.value)
            keyData is PublicKey.KeyData.CompressedEcKeyData && plus.keyData is PublicKey.KeyData.CompressedEcKeyData ->
                PublicKey.KeyData.CompressedEcKeyData(keyData.value + plus.keyData.value)
            else ->
                plus.keyData ?: keyData
        },
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun PublicKey.Companion.decodeWithImpl(u: pbandk.MessageDecoder): PublicKey {
    var id = ""
    var usage: org.hyperledger.identus.protos.KeyUsage = org.hyperledger.identus.protos.KeyUsage.fromValue(0)
    var addedOn: org.hyperledger.identus.protos.LedgerData? = null
    var revokedOn: org.hyperledger.identus.protos.LedgerData? = null
    var keyData: PublicKey.KeyData<*>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> id = _fieldValue as String
            2 -> usage = _fieldValue as org.hyperledger.identus.protos.KeyUsage
            5 -> addedOn = _fieldValue as org.hyperledger.identus.protos.LedgerData
            6 -> revokedOn = _fieldValue as org.hyperledger.identus.protos.LedgerData
            8 -> keyData = PublicKey.KeyData.EcKeyData(_fieldValue as org.hyperledger.identus.protos.ECKeyData)
            9 -> keyData = PublicKey.KeyData.CompressedEcKeyData(_fieldValue as org.hyperledger.identus.protos.CompressedECKeyData)
        }
    }

    return PublicKey(id, usage, addedOn, revokedOn,
        keyData, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForDIDData")
public fun DIDData?.orDefault(): org.hyperledger.identus.protos.DIDData = this ?: DIDData.defaultInstance

private fun DIDData.protoMergeImpl(plus: pbandk.Message?): DIDData = (plus as? DIDData)?.let {
    it.copy(
        publicKeys = publicKeys + plus.publicKeys,
        services = services + plus.services,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun DIDData.Companion.decodeWithImpl(u: pbandk.MessageDecoder): DIDData {
    var id = ""
    var publicKeys: pbandk.ListWithSize.Builder<org.hyperledger.identus.protos.PublicKey>? = null
    var services: pbandk.ListWithSize.Builder<org.hyperledger.identus.protos.Service>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> id = _fieldValue as String
            2 -> publicKeys = (publicKeys ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<org.hyperledger.identus.protos.PublicKey> }
            3 -> services = (services ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<org.hyperledger.identus.protos.Service> }
        }
    }

    return DIDData(id, pbandk.ListWithSize.Builder.fixed(publicKeys), pbandk.ListWithSize.Builder.fixed(services), unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForCreateDIDOperation")
public fun CreateDIDOperation?.orDefault(): org.hyperledger.identus.protos.CreateDIDOperation = this ?: CreateDIDOperation.defaultInstance

private fun CreateDIDOperation.protoMergeImpl(plus: pbandk.Message?): CreateDIDOperation = (plus as? CreateDIDOperation)?.let {
    it.copy(
        didData = didData?.plus(plus.didData) ?: plus.didData,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun CreateDIDOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): CreateDIDOperation {
    var didData: org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> didData = _fieldValue as org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData
        }
    }

    return CreateDIDOperation(didData, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForCreateDIDOperationDIDCreationData")
public fun CreateDIDOperation.DIDCreationData?.orDefault(): org.hyperledger.identus.protos.CreateDIDOperation.DIDCreationData = this ?: CreateDIDOperation.DIDCreationData.defaultInstance

private fun CreateDIDOperation.DIDCreationData.protoMergeImpl(plus: pbandk.Message?): CreateDIDOperation.DIDCreationData = (plus as? CreateDIDOperation.DIDCreationData)?.let {
    it.copy(
        publicKeys = publicKeys + plus.publicKeys,
        services = services + plus.services,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun CreateDIDOperation.DIDCreationData.Companion.decodeWithImpl(u: pbandk.MessageDecoder): CreateDIDOperation.DIDCreationData {
    var publicKeys: pbandk.ListWithSize.Builder<org.hyperledger.identus.protos.PublicKey>? = null
    var services: pbandk.ListWithSize.Builder<org.hyperledger.identus.protos.Service>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            2 -> publicKeys = (publicKeys ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<org.hyperledger.identus.protos.PublicKey> }
            3 -> services = (services ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<org.hyperledger.identus.protos.Service> }
        }
    }

    return CreateDIDOperation.DIDCreationData(pbandk.ListWithSize.Builder.fixed(publicKeys), pbandk.ListWithSize.Builder.fixed(services), unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForAddKeyAction")
public fun AddKeyAction?.orDefault(): org.hyperledger.identus.protos.AddKeyAction = this ?: AddKeyAction.defaultInstance

private fun AddKeyAction.protoMergeImpl(plus: pbandk.Message?): AddKeyAction = (plus as? AddKeyAction)?.let {
    it.copy(
        key = key?.plus(plus.key) ?: plus.key,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun AddKeyAction.Companion.decodeWithImpl(u: pbandk.MessageDecoder): AddKeyAction {
    var key: org.hyperledger.identus.protos.PublicKey? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> key = _fieldValue as org.hyperledger.identus.protos.PublicKey
        }
    }

    return AddKeyAction(key, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForRemoveKeyAction")
public fun RemoveKeyAction?.orDefault(): org.hyperledger.identus.protos.RemoveKeyAction = this ?: RemoveKeyAction.defaultInstance

private fun RemoveKeyAction.protoMergeImpl(plus: pbandk.Message?): RemoveKeyAction = (plus as? RemoveKeyAction)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun RemoveKeyAction.Companion.decodeWithImpl(u: pbandk.MessageDecoder): RemoveKeyAction {
    var keyId = ""

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> keyId = _fieldValue as String
        }
    }

    return RemoveKeyAction(keyId, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForAddServiceAction")
public fun AddServiceAction?.orDefault(): org.hyperledger.identus.protos.AddServiceAction = this ?: AddServiceAction.defaultInstance

private fun AddServiceAction.protoMergeImpl(plus: pbandk.Message?): AddServiceAction = (plus as? AddServiceAction)?.let {
    it.copy(
        service = service?.plus(plus.service) ?: plus.service,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun AddServiceAction.Companion.decodeWithImpl(u: pbandk.MessageDecoder): AddServiceAction {
    var service: org.hyperledger.identus.protos.Service? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> service = _fieldValue as org.hyperledger.identus.protos.Service
        }
    }

    return AddServiceAction(service, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForRemoveServiceAction")
public fun RemoveServiceAction?.orDefault(): org.hyperledger.identus.protos.RemoveServiceAction = this ?: RemoveServiceAction.defaultInstance

private fun RemoveServiceAction.protoMergeImpl(plus: pbandk.Message?): RemoveServiceAction = (plus as? RemoveServiceAction)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun RemoveServiceAction.Companion.decodeWithImpl(u: pbandk.MessageDecoder): RemoveServiceAction {
    var serviceId = ""

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> serviceId = _fieldValue as String
        }
    }

    return RemoveServiceAction(serviceId, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForUpdateServiceAction")
public fun UpdateServiceAction?.orDefault(): org.hyperledger.identus.protos.UpdateServiceAction = this ?: UpdateServiceAction.defaultInstance

private fun UpdateServiceAction.protoMergeImpl(plus: pbandk.Message?): UpdateServiceAction = (plus as? UpdateServiceAction)?.let {
    it.copy(
        serviceEndpoints = serviceEndpoints + plus.serviceEndpoints,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun UpdateServiceAction.Companion.decodeWithImpl(u: pbandk.MessageDecoder): UpdateServiceAction {
    var serviceId = ""
    var type = ""
    var serviceEndpoints: pbandk.ListWithSize.Builder<String>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> serviceId = _fieldValue as String
            2 -> type = _fieldValue as String
            3 -> serviceEndpoints = (serviceEndpoints ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<String> }
        }
    }

    return UpdateServiceAction(serviceId, type, pbandk.ListWithSize.Builder.fixed(serviceEndpoints), unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForUpdateDIDAction")
public fun UpdateDIDAction?.orDefault(): org.hyperledger.identus.protos.UpdateDIDAction = this ?: UpdateDIDAction.defaultInstance

private fun UpdateDIDAction.protoMergeImpl(plus: pbandk.Message?): UpdateDIDAction = (plus as? UpdateDIDAction)?.let {
    it.copy(
        action = when {
            action is UpdateDIDAction.Action.AddKey && plus.action is UpdateDIDAction.Action.AddKey ->
                UpdateDIDAction.Action.AddKey(action.value + plus.action.value)
            action is UpdateDIDAction.Action.RemoveKey && plus.action is UpdateDIDAction.Action.RemoveKey ->
                UpdateDIDAction.Action.RemoveKey(action.value + plus.action.value)
            action is UpdateDIDAction.Action.AddService && plus.action is UpdateDIDAction.Action.AddService ->
                UpdateDIDAction.Action.AddService(action.value + plus.action.value)
            action is UpdateDIDAction.Action.RemoveService && plus.action is UpdateDIDAction.Action.RemoveService ->
                UpdateDIDAction.Action.RemoveService(action.value + plus.action.value)
            action is UpdateDIDAction.Action.UpdateService && plus.action is UpdateDIDAction.Action.UpdateService ->
                UpdateDIDAction.Action.UpdateService(action.value + plus.action.value)
            else ->
                plus.action ?: action
        },
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun UpdateDIDAction.Companion.decodeWithImpl(u: pbandk.MessageDecoder): UpdateDIDAction {
    var action: UpdateDIDAction.Action<*>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> action = UpdateDIDAction.Action.AddKey(_fieldValue as org.hyperledger.identus.protos.AddKeyAction)
            2 -> action = UpdateDIDAction.Action.RemoveKey(_fieldValue as org.hyperledger.identus.protos.RemoveKeyAction)
            3 -> action = UpdateDIDAction.Action.AddService(_fieldValue as org.hyperledger.identus.protos.AddServiceAction)
            4 -> action = UpdateDIDAction.Action.RemoveService(_fieldValue as org.hyperledger.identus.protos.RemoveServiceAction)
            5 -> action = UpdateDIDAction.Action.UpdateService(_fieldValue as org.hyperledger.identus.protos.UpdateServiceAction)
        }
    }

    return UpdateDIDAction(action, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForUpdateDIDOperation")
public fun UpdateDIDOperation?.orDefault(): org.hyperledger.identus.protos.UpdateDIDOperation = this ?: UpdateDIDOperation.defaultInstance

private fun UpdateDIDOperation.protoMergeImpl(plus: pbandk.Message?): UpdateDIDOperation = (plus as? UpdateDIDOperation)?.let {
    it.copy(
        actions = actions + plus.actions,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun UpdateDIDOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): UpdateDIDOperation {
    var previousOperationHash: pbandk.ByteArr = pbandk.ByteArr.empty
    var id = ""
    var actions: pbandk.ListWithSize.Builder<org.hyperledger.identus.protos.UpdateDIDAction>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> previousOperationHash = _fieldValue as pbandk.ByteArr
            2 -> id = _fieldValue as String
            3 -> actions = (actions ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<org.hyperledger.identus.protos.UpdateDIDAction> }
        }
    }

    return UpdateDIDOperation(previousOperationHash, id, pbandk.ListWithSize.Builder.fixed(actions), unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForCredentialBatchData")
public fun CredentialBatchData?.orDefault(): org.hyperledger.identus.protos.CredentialBatchData = this ?: CredentialBatchData.defaultInstance

private fun CredentialBatchData.protoMergeImpl(plus: pbandk.Message?): CredentialBatchData = (plus as? CredentialBatchData)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun CredentialBatchData.Companion.decodeWithImpl(u: pbandk.MessageDecoder): CredentialBatchData {
    var issuerDid = ""
    var merkleRoot: pbandk.ByteArr = pbandk.ByteArr.empty

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> issuerDid = _fieldValue as String
            2 -> merkleRoot = _fieldValue as pbandk.ByteArr
        }
    }

    return CredentialBatchData(issuerDid, merkleRoot, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForIssueCredentialBatchOperation")
public fun IssueCredentialBatchOperation?.orDefault(): org.hyperledger.identus.protos.IssueCredentialBatchOperation = this ?: IssueCredentialBatchOperation.defaultInstance

private fun IssueCredentialBatchOperation.protoMergeImpl(plus: pbandk.Message?): IssueCredentialBatchOperation = (plus as? IssueCredentialBatchOperation)?.let {
    it.copy(
        credentialBatchData = credentialBatchData?.plus(plus.credentialBatchData) ?: plus.credentialBatchData,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun IssueCredentialBatchOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): IssueCredentialBatchOperation {
    var credentialBatchData: org.hyperledger.identus.protos.CredentialBatchData? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> credentialBatchData = _fieldValue as org.hyperledger.identus.protos.CredentialBatchData
        }
    }

    return IssueCredentialBatchOperation(credentialBatchData, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForRevokeCredentialsOperation")
public fun RevokeCredentialsOperation?.orDefault(): org.hyperledger.identus.protos.RevokeCredentialsOperation = this ?: RevokeCredentialsOperation.defaultInstance

private fun RevokeCredentialsOperation.protoMergeImpl(plus: pbandk.Message?): RevokeCredentialsOperation = (plus as? RevokeCredentialsOperation)?.let {
    it.copy(
        credentialsToRevoke = credentialsToRevoke + plus.credentialsToRevoke,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun RevokeCredentialsOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): RevokeCredentialsOperation {
    var previousOperationHash: pbandk.ByteArr = pbandk.ByteArr.empty
    var credentialBatchId = ""
    var credentialsToRevoke: pbandk.ListWithSize.Builder<pbandk.ByteArr>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> previousOperationHash = _fieldValue as pbandk.ByteArr
            2 -> credentialBatchId = _fieldValue as String
            3 -> credentialsToRevoke = (credentialsToRevoke ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<pbandk.ByteArr> }
        }
    }

    return RevokeCredentialsOperation(previousOperationHash, credentialBatchId, pbandk.ListWithSize.Builder.fixed(credentialsToRevoke), unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForProtocolVersionUpdateOperation")
public fun ProtocolVersionUpdateOperation?.orDefault(): org.hyperledger.identus.protos.ProtocolVersionUpdateOperation = this ?: ProtocolVersionUpdateOperation.defaultInstance

private fun ProtocolVersionUpdateOperation.protoMergeImpl(plus: pbandk.Message?): ProtocolVersionUpdateOperation = (plus as? ProtocolVersionUpdateOperation)?.let {
    it.copy(
        version = version?.plus(plus.version) ?: plus.version,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun ProtocolVersionUpdateOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): ProtocolVersionUpdateOperation {
    var proposerDid = ""
    var version: org.hyperledger.identus.protos.ProtocolVersionInfo? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> proposerDid = _fieldValue as String
            2 -> version = _fieldValue as org.hyperledger.identus.protos.ProtocolVersionInfo
        }
    }

    return ProtocolVersionUpdateOperation(proposerDid, version, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForProtocolVersion")
public fun ProtocolVersion?.orDefault(): org.hyperledger.identus.protos.ProtocolVersion = this ?: ProtocolVersion.defaultInstance

private fun ProtocolVersion.protoMergeImpl(plus: pbandk.Message?): ProtocolVersion = (plus as? ProtocolVersion)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun ProtocolVersion.Companion.decodeWithImpl(u: pbandk.MessageDecoder): ProtocolVersion {
    var majorVersion = 0
    var minorVersion = 0

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> majorVersion = _fieldValue as Int
            2 -> minorVersion = _fieldValue as Int
        }
    }

    return ProtocolVersion(majorVersion, minorVersion, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForProtocolVersionInfo")
public fun ProtocolVersionInfo?.orDefault(): org.hyperledger.identus.protos.ProtocolVersionInfo = this ?: ProtocolVersionInfo.defaultInstance

private fun ProtocolVersionInfo.protoMergeImpl(plus: pbandk.Message?): ProtocolVersionInfo = (plus as? ProtocolVersionInfo)?.let {
    it.copy(
        protocolVersion = protocolVersion?.plus(plus.protocolVersion) ?: plus.protocolVersion,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun ProtocolVersionInfo.Companion.decodeWithImpl(u: pbandk.MessageDecoder): ProtocolVersionInfo {
    var versionName = ""
    var effectiveSince = 0
    var protocolVersion: org.hyperledger.identus.protos.ProtocolVersion? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> versionName = _fieldValue as String
            4 -> effectiveSince = _fieldValue as Int
            5 -> protocolVersion = _fieldValue as org.hyperledger.identus.protos.ProtocolVersion
        }
    }

    return ProtocolVersionInfo(versionName, effectiveSince, protocolVersion, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForDeactivateDIDOperation")
public fun DeactivateDIDOperation?.orDefault(): org.hyperledger.identus.protos.DeactivateDIDOperation = this ?: DeactivateDIDOperation.defaultInstance

private fun DeactivateDIDOperation.protoMergeImpl(plus: pbandk.Message?): DeactivateDIDOperation = (plus as? DeactivateDIDOperation)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun DeactivateDIDOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): DeactivateDIDOperation {
    var previousOperationHash: pbandk.ByteArr = pbandk.ByteArr.empty
    var id = ""

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> previousOperationHash = _fieldValue as pbandk.ByteArr
            2 -> id = _fieldValue as String
        }
    }

    return DeactivateDIDOperation(previousOperationHash, id, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForAtalaOperation")
public fun AtalaOperation?.orDefault(): org.hyperledger.identus.protos.AtalaOperation = this ?: AtalaOperation.defaultInstance

private fun AtalaOperation.protoMergeImpl(plus: pbandk.Message?): AtalaOperation = (plus as? AtalaOperation)?.let {
    it.copy(
        operation = when {
            operation is AtalaOperation.Operation.CreateDid && plus.operation is AtalaOperation.Operation.CreateDid ->
                AtalaOperation.Operation.CreateDid(operation.value + plus.operation.value)
            operation is AtalaOperation.Operation.UpdateDid && plus.operation is AtalaOperation.Operation.UpdateDid ->
                AtalaOperation.Operation.UpdateDid(operation.value + plus.operation.value)
            operation is AtalaOperation.Operation.IssueCredentialBatch && plus.operation is AtalaOperation.Operation.IssueCredentialBatch ->
                AtalaOperation.Operation.IssueCredentialBatch(operation.value + plus.operation.value)
            operation is AtalaOperation.Operation.RevokeCredentials && plus.operation is AtalaOperation.Operation.RevokeCredentials ->
                AtalaOperation.Operation.RevokeCredentials(operation.value + plus.operation.value)
            operation is AtalaOperation.Operation.ProtocolVersionUpdate && plus.operation is AtalaOperation.Operation.ProtocolVersionUpdate ->
                AtalaOperation.Operation.ProtocolVersionUpdate(operation.value + plus.operation.value)
            operation is AtalaOperation.Operation.DeactivateDid && plus.operation is AtalaOperation.Operation.DeactivateDid ->
                AtalaOperation.Operation.DeactivateDid(operation.value + plus.operation.value)
            else ->
                plus.operation ?: operation
        },
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun AtalaOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): AtalaOperation {
    var operation: AtalaOperation.Operation<*>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> operation = AtalaOperation.Operation.CreateDid(_fieldValue as org.hyperledger.identus.protos.CreateDIDOperation)
            2 -> operation = AtalaOperation.Operation.UpdateDid(_fieldValue as org.hyperledger.identus.protos.UpdateDIDOperation)
            3 -> operation = AtalaOperation.Operation.IssueCredentialBatch(_fieldValue as org.hyperledger.identus.protos.IssueCredentialBatchOperation)
            4 -> operation = AtalaOperation.Operation.RevokeCredentials(_fieldValue as org.hyperledger.identus.protos.RevokeCredentialsOperation)
            5 -> operation = AtalaOperation.Operation.ProtocolVersionUpdate(_fieldValue as org.hyperledger.identus.protos.ProtocolVersionUpdateOperation)
            6 -> operation = AtalaOperation.Operation.DeactivateDid(_fieldValue as org.hyperledger.identus.protos.DeactivateDIDOperation)
        }
    }

    return AtalaOperation(operation, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForSignedAtalaOperation")
public fun SignedAtalaOperation?.orDefault(): org.hyperledger.identus.protos.SignedAtalaOperation = this ?: SignedAtalaOperation.defaultInstance

private fun SignedAtalaOperation.protoMergeImpl(plus: pbandk.Message?): SignedAtalaOperation = (plus as? SignedAtalaOperation)?.let {
    it.copy(
        operation = operation?.plus(plus.operation) ?: plus.operation,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun SignedAtalaOperation.Companion.decodeWithImpl(u: pbandk.MessageDecoder): SignedAtalaOperation {
    var signedWith = ""
    var signature: pbandk.ByteArr = pbandk.ByteArr.empty
    var operation: org.hyperledger.identus.protos.AtalaOperation? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> signedWith = _fieldValue as String
            2 -> signature = _fieldValue as pbandk.ByteArr
            3 -> operation = _fieldValue as org.hyperledger.identus.protos.AtalaOperation
        }
    }

    return SignedAtalaOperation(signedWith, signature, operation, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForLedgerData")
public fun LedgerData?.orDefault(): org.hyperledger.identus.protos.LedgerData = this ?: LedgerData.defaultInstance

private fun LedgerData.protoMergeImpl(plus: pbandk.Message?): LedgerData = (plus as? LedgerData)?.let {
    it.copy(
        timestampInfo = timestampInfo?.plus(plus.timestampInfo) ?: plus.timestampInfo,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun LedgerData.Companion.decodeWithImpl(u: pbandk.MessageDecoder): LedgerData {
    var transactionId = ""
    var ledger: org.hyperledger.identus.protos.Ledger = org.hyperledger.identus.protos.Ledger.fromValue(0)
    var timestampInfo: org.hyperledger.identus.protos.TimestampInfo? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> transactionId = _fieldValue as String
            2 -> ledger = _fieldValue as org.hyperledger.identus.protos.Ledger
            3 -> timestampInfo = _fieldValue as org.hyperledger.identus.protos.TimestampInfo
        }
    }

    return LedgerData(transactionId, ledger, timestampInfo, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForOperationOutput")
public fun OperationOutput?.orDefault(): org.hyperledger.identus.protos.OperationOutput = this ?: OperationOutput.defaultInstance

private fun OperationOutput.protoMergeImpl(plus: pbandk.Message?): OperationOutput = (plus as? OperationOutput)?.let {
    it.copy(
        result = when {
            result is OperationOutput.Result.BatchOutput && plus.result is OperationOutput.Result.BatchOutput ->
                OperationOutput.Result.BatchOutput(result.value + plus.result.value)
            result is OperationOutput.Result.CreateDidOutput && plus.result is OperationOutput.Result.CreateDidOutput ->
                OperationOutput.Result.CreateDidOutput(result.value + plus.result.value)
            result is OperationOutput.Result.UpdateDidOutput && plus.result is OperationOutput.Result.UpdateDidOutput ->
                OperationOutput.Result.UpdateDidOutput(result.value + plus.result.value)
            result is OperationOutput.Result.RevokeCredentialsOutput && plus.result is OperationOutput.Result.RevokeCredentialsOutput ->
                OperationOutput.Result.RevokeCredentialsOutput(result.value + plus.result.value)
            result is OperationOutput.Result.ProtocolVersionUpdateOutput && plus.result is OperationOutput.Result.ProtocolVersionUpdateOutput ->
                OperationOutput.Result.ProtocolVersionUpdateOutput(result.value + plus.result.value)
            result is OperationOutput.Result.DeactivateDidOutput && plus.result is OperationOutput.Result.DeactivateDidOutput ->
                OperationOutput.Result.DeactivateDidOutput(result.value + plus.result.value)
            else ->
                plus.result ?: result
        },
        operationMaybe = plus.operationMaybe ?: operationMaybe,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun OperationOutput.Companion.decodeWithImpl(u: pbandk.MessageDecoder): OperationOutput {
    var result: OperationOutput.Result<*>? = null
    var operationMaybe: OperationOutput.OperationMaybe<*>? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> result = OperationOutput.Result.BatchOutput(_fieldValue as org.hyperledger.identus.protos.IssueCredentialBatchOutput)
            2 -> result = OperationOutput.Result.CreateDidOutput(_fieldValue as org.hyperledger.identus.protos.CreateDIDOutput)
            3 -> result = OperationOutput.Result.UpdateDidOutput(_fieldValue as org.hyperledger.identus.protos.UpdateDIDOutput)
            4 -> result = OperationOutput.Result.RevokeCredentialsOutput(_fieldValue as org.hyperledger.identus.protos.RevokeCredentialsOutput)
            5 -> operationMaybe = OperationOutput.OperationMaybe.OperationId(_fieldValue as pbandk.ByteArr)
            6 -> operationMaybe = OperationOutput.OperationMaybe.Error(_fieldValue as String)
            7 -> result = OperationOutput.Result.ProtocolVersionUpdateOutput(_fieldValue as org.hyperledger.identus.protos.ProtocolVersionUpdateOutput)
            8 -> result = OperationOutput.Result.DeactivateDidOutput(_fieldValue as org.hyperledger.identus.protos.DeactivateDIDOutput)
        }
    }

    return OperationOutput(result, operationMaybe, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForIssueCredentialBatchOutput")
public fun IssueCredentialBatchOutput?.orDefault(): org.hyperledger.identus.protos.IssueCredentialBatchOutput = this ?: IssueCredentialBatchOutput.defaultInstance

private fun IssueCredentialBatchOutput.protoMergeImpl(plus: pbandk.Message?): IssueCredentialBatchOutput = (plus as? IssueCredentialBatchOutput)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun IssueCredentialBatchOutput.Companion.decodeWithImpl(u: pbandk.MessageDecoder): IssueCredentialBatchOutput {
    var batchId = ""

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> batchId = _fieldValue as String
        }
    }

    return IssueCredentialBatchOutput(batchId, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForCreateDIDOutput")
public fun CreateDIDOutput?.orDefault(): org.hyperledger.identus.protos.CreateDIDOutput = this ?: CreateDIDOutput.defaultInstance

private fun CreateDIDOutput.protoMergeImpl(plus: pbandk.Message?): CreateDIDOutput = (plus as? CreateDIDOutput)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun CreateDIDOutput.Companion.decodeWithImpl(u: pbandk.MessageDecoder): CreateDIDOutput {
    var didSuffix = ""

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> didSuffix = _fieldValue as String
        }
    }

    return CreateDIDOutput(didSuffix, unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForUpdateDIDOutput")
public fun UpdateDIDOutput?.orDefault(): org.hyperledger.identus.protos.UpdateDIDOutput = this ?: UpdateDIDOutput.defaultInstance

private fun UpdateDIDOutput.protoMergeImpl(plus: pbandk.Message?): UpdateDIDOutput = (plus as? UpdateDIDOutput)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun UpdateDIDOutput.Companion.decodeWithImpl(u: pbandk.MessageDecoder): UpdateDIDOutput {

    val unknownFields = u.readMessage(this) { _, _ -> }

    return UpdateDIDOutput(unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForRevokeCredentialsOutput")
public fun RevokeCredentialsOutput?.orDefault(): org.hyperledger.identus.protos.RevokeCredentialsOutput = this ?: RevokeCredentialsOutput.defaultInstance

private fun RevokeCredentialsOutput.protoMergeImpl(plus: pbandk.Message?): RevokeCredentialsOutput = (plus as? RevokeCredentialsOutput)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun RevokeCredentialsOutput.Companion.decodeWithImpl(u: pbandk.MessageDecoder): RevokeCredentialsOutput {

    val unknownFields = u.readMessage(this) { _, _ -> }

    return RevokeCredentialsOutput(unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForProtocolVersionUpdateOutput")
public fun ProtocolVersionUpdateOutput?.orDefault(): org.hyperledger.identus.protos.ProtocolVersionUpdateOutput = this ?: ProtocolVersionUpdateOutput.defaultInstance

private fun ProtocolVersionUpdateOutput.protoMergeImpl(plus: pbandk.Message?): ProtocolVersionUpdateOutput = (plus as? ProtocolVersionUpdateOutput)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun ProtocolVersionUpdateOutput.Companion.decodeWithImpl(u: pbandk.MessageDecoder): ProtocolVersionUpdateOutput {

    val unknownFields = u.readMessage(this) { _, _ -> }

    return ProtocolVersionUpdateOutput(unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForDeactivateDIDOutput")
public fun DeactivateDIDOutput?.orDefault(): org.hyperledger.identus.protos.DeactivateDIDOutput = this ?: DeactivateDIDOutput.defaultInstance

private fun DeactivateDIDOutput.protoMergeImpl(plus: pbandk.Message?): DeactivateDIDOutput = (plus as? DeactivateDIDOutput)?.let {
    it.copy(
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun DeactivateDIDOutput.Companion.decodeWithImpl(u: pbandk.MessageDecoder): DeactivateDIDOutput {

    val unknownFields = u.readMessage(this) { _, _ -> }

    return DeactivateDIDOutput(unknownFields)
}

@pbandk.Export
@pbandk.JsName("orDefaultForService")
public fun Service?.orDefault(): org.hyperledger.identus.protos.Service = this ?: Service.defaultInstance

private fun Service.protoMergeImpl(plus: pbandk.Message?): Service = (plus as? Service)?.let {
    it.copy(
        serviceEndpoint = serviceEndpoint + plus.serviceEndpoint,
        addedOn = addedOn?.plus(plus.addedOn) ?: plus.addedOn,
        deletedOn = deletedOn?.plus(plus.deletedOn) ?: plus.deletedOn,
        unknownFields = unknownFields + plus.unknownFields
    )
} ?: this

@Suppress("UNCHECKED_CAST")
private fun Service.Companion.decodeWithImpl(u: pbandk.MessageDecoder): Service {
    var id = ""
    var type = ""
    var serviceEndpoint: pbandk.ListWithSize.Builder<String>? = null
    var addedOn: org.hyperledger.identus.protos.LedgerData? = null
    var deletedOn: org.hyperledger.identus.protos.LedgerData? = null

    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> id = _fieldValue as String
            2 -> type = _fieldValue as String
            3 -> serviceEndpoint = (serviceEndpoint ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<String> }
            4 -> addedOn = _fieldValue as org.hyperledger.identus.protos.LedgerData
            5 -> deletedOn = _fieldValue as org.hyperledger.identus.protos.LedgerData
        }
    }

    return Service(id, type, pbandk.ListWithSize.Builder.fixed(serviceEndpoint), addedOn,
        deletedOn, unknownFields)
}
