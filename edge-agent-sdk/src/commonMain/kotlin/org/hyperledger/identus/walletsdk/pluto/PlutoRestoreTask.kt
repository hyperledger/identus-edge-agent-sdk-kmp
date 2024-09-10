@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.pluto

import java.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.UnknownError
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.IndexKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.JWK
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask.BackUpMessage.JsonAsStringSerializer.descriptor
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask.BackUpMessage.JsonAsStringSerializer.deserialize
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask.BackUpMessage.JsonAsStringSerializer.serialize
import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.hyperledger.identus.walletsdk.pollux.models.SDJWTCredential
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Represents a task for restoring data in Pluto.
 *
 * @param pluto The Pluto instance to restore data to.
 * @param backup The Pluto backup object containing the data to be restored.
 */
open class PlutoRestoreTask(
    private val castor: Castor,
    private val pluto: Pluto,
    private val backup: BackupV0_0_1
) {

    /**
     * Restores various credentials, DIDs, key pairs, link secrets, and messages.
     * This method should be called to initialize or restore the necessary components.
     */
    fun run() {
        restoreDids()
        restoreDidPairs()
        restoreMediators()
        restoreLinkSecret()
        restoreCredentials()
        restoreKeys()
        restoreMessages()
    }

    /**
     * Restores credentials from a backup source.
     *
     * The method iterates over the credentials stored in the backup and restores them by mapping each credential
     * to its corresponding restoration ID. The restored credentials are then stored using the `pluto.storeCredential` method.
     *
     * @throws UnknownError.SomethingWentWrongError if an unknown recovery id is encountered while restoring a credential.
     */
    private fun restoreCredentials() {
        val credentials = this.backup.credentials.map {
            when (it.recoveryId) {
                BackUpRestorationId.JWT.value -> {
                    val jwtString = it.data.base64UrlDecoded
                    JWTCredential.fromJwtString(jwtString).toStorableCredential()
                }

                BackUpRestorationId.ANONCRED.value -> {
                    val json = it.data.base64UrlDecoded
                        .replace("\"null\"", "null")
                    Json.decodeFromString<AnonCredentialBackUp>(json)
                        .toAnonCredential().toStorableCredential()
                }

                BackUpRestorationId.SDJWT.value -> {
                    val sdjwtString = it.data.base64UrlDecoded
                        .replace("\"null\"", "null")
                    SDJWTCredential.fromSDJwtString(sdjwtString).toStorableCredential()
                }

                else -> {
                    throw UnknownError.SomethingWentWrongError("Unknown recovery id: ${it.recoveryId}")
                }
            }
        }
        credentials.forEach {
            pluto.storeCredential(it)
        }
    }

    /**
     * Restores the DIDs from the backup.
     *
     * The method iterates over the DIDs in the backup and converts each DID to a `PeerDID`
     * using the associated alias. Then, it stores each `PeerDID` in the `pluto` store.
     */
    private fun restoreDids() {
        this.backup.dids.map {
            it.did.toDID(it.alias)
        }.forEach {
            pluto.storePeerDID(it)
        }
    }

    /**
     * Restores the DID pairs from a backup.
     *
     * This method iterates over the `didPairs` list in the `backup` object and converts each item to a `Triple` object.
     * It then calls the `storeDIDPair` method on the `pluto` object to store the DID pair.
     */
    private fun restoreDidPairs() {
        this.backup.didPairs.map {
            val host = it.holder.toDID()
            val target = it.recipient.toDID()
            Triple(host, target, it.alias)
        }.forEach {
            pluto.storeDIDPair(it.first, it.second, it.third)
        }
    }

    /**
     * Restores the keys from the backup.
     * The method iterates through the keys in the backup, decodes them from base64 URL format,
     * converts them to JWK objects, and finally converts them to domain keys.
     * If the key has a recovery type, it is not stored in Pluto and is skipped.
     * If the key does not have a recovery type, it is stored in Pluto with the corresponding DID and index.
     *
     * @throws UnknownError.SomethingWentWrongError if the key is invalid, it does not have index or DID
     */
    private fun restoreKeys() {
        this.backup.keys.map {
            val jwkJson = it.key.base64UrlDecoded
            val jwk = Json.decodeFromString<JWK>(jwkJson)
            val key = jwkToPrivateKey(jwk)

            if (it.index != null) {
                key.keySpecification[IndexKey().property] = it.index.toString()
            }

            val restorationId: String = it.recoveryId ?: when (key) {
                is Secp256k1PrivateKey -> {
                    key.restorationIdentifier
                }

                is Ed25519PrivateKey -> {
                    key.restorationIdentifier
                }

                is X25519PrivateKey -> {
                    key.restorationIdentifier
                }

                else -> {
                    // Should never reach this step
                    throw UnknownError.SomethingWentWrongError("Unknown key type $key")
                }
            }

            Triple(key, restorationId, it.did?.toDID())
        }.forEach {
            if (it.third is DID) {
                if (it.third.toString().contains("peer")) {
                    val did = (it.third as DID)
                    val keyId = did.toString()
                    if (keyId.contains("#")) {
                        val simpleDid = keyId.split("#").first()
                        pluto.storePrivateKeys(
                            it.first as StorableKey,
                            DID(simpleDid),
                            (it.first.keySpecification[IndexKey().property])?.toInt(),
                            keyId
                        )
                    } else {
                        // This else is for jwe coming from TS/Swift which do not use didUrl for private key id
                        // and is a requirement for us.
                        runBlocking {
                            resolveDIDForPrivateKey(keyId, it.first)
                        }
                    }
                } else {
                    pluto.storePrismDIDAndPrivateKeys(
                        it.third as DID,
                        (it.first.keySpecification[IndexKey().property])?.toInt(),
                        null,
                        listOf(it.first as StorableKey)
                    )
                }
            } else {
                pluto.storePrivate(it.first as StorableKey, it.second)
            }
        }
    }

    private suspend fun resolveDIDForPrivateKey(did: String, privateKey: PrivateKey) {
        val document = castor.resolveDID(did)
        val listOfVerificationMethods: MutableList<DIDDocument.VerificationMethod> =
            mutableListOf()
        document.coreProperties.forEach {
            if (it is DIDDocument.Authentication) {
                listOfVerificationMethods.addAll(it.verificationMethods)
            }
            if (it is DIDDocument.KeyAgreement) {
                listOfVerificationMethods.addAll(it.verificationMethods)
            }
        }
        val verificationMethods =
            DIDDocument.VerificationMethods(listOfVerificationMethods.toTypedArray())

        verificationMethods.values.forEach {
            if (it.type.contains("X25519") && privateKey is X25519PrivateKey ||
                it.type.contains("Ed25519") && privateKey is Ed25519PrivateKey
            ) {
                pluto.storePrivateKeys(
                    privateKey as StorableKey,
                    DID(did),
                    (privateKey.keySpecification[IndexKey().property])?.toInt(),
                    it.id.toString()
                )
            }
        }
    }

    /**
     * Restores the link secret stored in the backup.
     * If the backup object has a link secret, it is decoded from base64 URL encoding and stored using the pluto.storeLinkSecret method.
     */
    private fun restoreLinkSecret() {
        if (this.backup.linkSecret != null) {
            pluto.storeLinkSecret(this.backup.linkSecret)
        }
    }

    /**
     * Restores messages from backup.
     *
     * This method iterates over the list of messages in the backup,
     * decodes each message from base64 URL format, and stores the
     * restored message using the Pluto storeMessage method.
     */
    private fun restoreMessages() {
        this.backup.messages.map {
            val json = it.base64UrlDecoded
            Json.decodeFromString<BackUpMessage>(json)
        }.forEach {
            pluto.storeMessage(it.toMessage())
        }
    }

    /**
     * Restores the mediators from the backup.
     *
     * This method retrieves the mediators from the backup and stores them using the `storeMediator` method.
     */
    private fun restoreMediators() {
        this.backup.mediators.map {
            Triple(it.mediatorDid.toDID(), it.holderDid.toDID(), it.routingDid.toDID())
        }.forEach {
            pluto.storeMediator(it.first, it.second, it.third)
        }
    }

    /**
     * This function converts a `JWK` object into a specific `PrivateKey` object based on the curve value.
     * The `JWK` object should be of type `OKP` or `EC` and it should not be null.
     *
     * @param jwk The `JWK` object to be converted.
     * @return A `PrivateKey` object that represents the converted `JWK`.
     *
     * @throws Exception If the curve value of `JWK` is not supported.
     * @throws Exception If the KTY of `JWK` is not `OKP` or `EC`.
     */
    private fun jwkToPrivateKey(jwk: JWK): PrivateKey {
        if ((jwk.kty == "OKP" || jwk.kty == "EC") && jwk.d != null) {
            return when (jwk.crv) {
                Curve.SECP256K1.value -> {
                    Secp256k1PrivateKey(jwk.d.base64UrlDecodedBytes)
                }

                Curve.ED25519.value -> {
                    Ed25519PrivateKey(jwk.d.base64UrlDecodedBytes)
                }

                Curve.X25519.value -> {
                    X25519PrivateKey(jwk.d.base64UrlDecodedBytes)
                }

                else -> throw UnknownError.SomethingWentWrongError("JWK Curve ${jwk.crv} is not supported.")
            }
        } else {
            throw UnknownError.SomethingWentWrongError("JWK KTY ${jwk.kty} is not supported.")
        }
    }

    /**
     * This function converts a string into a `DID` object.
     * The string should be in the format `x:y:z`.
     * If an optional alias is provided, it's applied to the resulting `DID` object.
     *
     * @param alias An optional string that can be used as an alias.
     * @return A `DID` object that represents the parsed information from the string.
     *
     * @throws IndexOutOfBoundsException If the input string is not in the required format `x:y:z`
     */
    @Throws(IndexOutOfBoundsException::class)
    private fun String.toDID(alias: String? = null): DID =
        DID(
            DID.getSchemaFromString(this),
            DID.getMethodFromString(this),
            DID.getMethodIdFromString(this),
            alias
        )

    /**
     * Represents the various types of backup restoration IDs that can be used.
     *
     * @property value The value associated with the backup restoration ID.
     */
    enum class BackUpRestorationId(val value: String) {
        JWT("jwt"),
        ANONCRED("anoncred"),
        W3C("w3c"),
        SDJWT("sd-jwt+credential");

        /**
         * Converts a BackUpRestorationId object to a RestorationID object from the RestorationID class.
         *
         * @return The corresponding RestorationID object based on the current BackUpRestorationId.
         */
        fun toRestorationId(): RestorationID =
            when (this) {
                JWT -> RestorationID.JWT
                ANONCRED -> RestorationID.ANONCRED
                W3C -> RestorationID.W3C
                SDJWT -> RestorationID.SDJWT
            }
    }

    /**
     * Represents the backup of anonymous credentials.
     *
     * @property schemaID The schema ID of the credential.
     * @property credentialDefinitionID The credential definition ID.
     * @property values The attribute values of the credential.
     * @property signature The signature of the credential.
     * @property signatureCorrectnessProof The signature correctness proof.
     * @property revocationRegistryId The revocation registry ID.
     * @property revocationRegistry The revocation registry.
     * @property witnessJson The witness JSON string.
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    class AnonCredentialBackUp @JvmOverloads constructor(
        @SerialName("schema_id")
        @JsonNames("schema_id", "schemaID")
        val schemaID: String,
        @SerialName("cred_def_id")
        @JsonNames("cred_def_id", "credentialDefinitionID")
        val credentialDefinitionID: String,
        val values: Map<String, AnonCredential.Attribute>,
        @SerialName("signature")
        @JsonNames("signature", "signatureJson")
        val signature: Signature,
        @SerialName("signature_correctness_proof")
        @JsonNames("signature_correctness_proof", "signatureCorrectnessProofJson")
        val signatureCorrectnessProof: SignatureCorrectnessProof,
        @SerialName("rev_reg_id")
        @JsonNames("revocation_registry_id", "revocationRegistryId")
        val revocationRegistryId: String? = null,
        @SerialName("rev_reg")
        @JsonNames("revocation_registry", "revocationRegistryJson", "rev_reg")
        val revocationRegistry: RevocationRegistry? = null,
        @SerialName("witness")
        @JsonNames("witness", "witnessJson")
        val witnessJson: Witness? = null,
        val revoked: Boolean? = null
    ) {
        /**
         * Converts the object to an instance of AnonCredential.
         *
         * @return An instance of AnonCredential.
         */
        fun toAnonCredential(): AnonCredential {
            val credential = AnonCredential(
                schemaID = schemaID,
                credentialDefinitionID = credentialDefinitionID,
                values = values,
                signatureJson = Json.encodeToString(signature),
                signatureCorrectnessProofJson = Json.encodeToString(signatureCorrectnessProof),
                revocationRegistryId = revocationRegistryId,
                revocationRegistryJson = Json.encodeToString(revocationRegistry),
                witnessJson = Json.encodeToString(witnessJson),
                Json.encodeToString(this)
            )
            credential.revoked = this.revoked
            return credential
        }

        /**
         * Represents a signature object.
         *
         * @property primaryCredential The P Credential object.
         * @property revocationCredential The R Credential string.
         */
        @Serializable
        class Signature @JvmOverloads constructor(
            @SerialName("p_credential")
            val primaryCredential: PrimaryCredential,
            @SerialName("r_credential")
            val revocationCredential: RevocationCredential? = null
        ) {
            /**
             * Represents a serializable class for storing user credentials.
             *
             * @param m2 The m2 property of the credential.
             * @param a The a property of the credential.
             * @param e The e property of the credential.
             * @param v The v property of the credential.
             */
            @Serializable
            class PrimaryCredential(
                @SerialName("m_2")
                val m2: String,
                val a: String,
                val e: String,
                val v: String
            )

            @Serializable
            class RevocationCredential(
                val sigma: String,
                val c: String,
                @SerialName("vr_prime_prime")
                val vrPrimePrime: String,
                @SerialName("witness_signature")
                val witnessSignature: WitnessSignature,
                @SerialName("g_i")
                val gI: String,
                val i: Int,
                val m2: String,
            ) {
                @Serializable
                class WitnessSignature(
                    @SerialName("sigma_i")
                    val sigmaI: String,
                    @SerialName("u_i")
                    val uI: String,
                    @SerialName("g_i")
                    val gI: String
                )
            }
        }

        /**
         * Represents a signature correctness proof for verifying the correctness of a signature.
         *
         * @property se The scalar value that represents the proof.
         * @property c The response value that represents the proof.
         */
        @Serializable
        class SignatureCorrectnessProof(
            val se: String,
            val c: String
        )

        @Serializable
        class RevocationRegistry(
            val accum: String? = null
        )

        @Serializable
        class Witness(
            val omega: String? = null
        )

        companion object {
            /**
             * Converts a byte array of storable credential data to an [AnonCredentialBackUp] object.
             *
             * @param data The byte array containing the storable credential data.
             * @return The converted [AnonCredentialBackUp] object.
             */
            @JvmStatic
            fun fromStorableData(data: ByteArray): AnonCredentialBackUp {
                val dataString = data.decodeToString()
                val cred = Json.decodeFromString<AnonCredentialBackUp>(dataString)
                return cred
            }
        }
    }

    /**
     * The `BackUpMessage` class represents a backup message that contains various properties such as ID, PIURI, from, to,
     * fromPrior, body, extraHeaders, createdTime, expiresTime, attachments, thid, pthid, ack, and direction.
     *
     * This class provides methods to convert the `BackUpMessage` object to a `Message` object.
     *
     * @property id The ID of the backup message. If not provided, a random UUID will be generated.
     * @property piuri The PIURI (Public Identifier Universal Resource Identifier) of the backup message.
     * @property from The sender of the backup message represented by a DID (Decentralized Identifier).
     * @property to The recipient of the backup message represented by a DID (Decentralized Identifier).
     * @property fromPrior The previous DID of the sender, if any.
     * @property body The body of the backup message.
     * @property extraHeaders Additional headers of the backup message as a key-value map.
     * @property createdTime The timestamp of when the backup message was created, represented as a string of epoch seconds.
     * @property expiresTime The timestamp of when the backup message expires, represented as a string of epoch seconds.
     * @property attachments The attachments of the backup message as an array of `AttachmentDescriptor` objects.
     * @property thid The thread ID of the backup message, if any.
     * @property pthid The parent thread ID of the backup message, if any.
     * @property ack The acknowledgements of the backup message as an array of string values.
     * @property direction The direction of the backup message, represented by the `Message.Direction` enum class.
     */
    @Serializable
    class BackUpMessage
    @OptIn(ExperimentalSerializationApi::class)
    @JvmOverloads
    constructor(
        @EncodeDefault
        val id: String = UUID.randomUUID().toString(),
        val piuri: String,
        @Serializable(with = DIDMessageSerializer::class)
        @EncodeDefault
        val from: DID? = null,
        @Serializable(with = DIDMessageSerializer::class)
        @EncodeDefault
        val to: DID? = null,
        @EncodeDefault
        val fromPrior: String? = null,
        @Serializable(with = JsonAsStringSerializer::class)
        val body: String,
        @SerialName("extra_headers")
        @JsonNames("extra_headers", "extraHeaders")
        val extraHeaders: Map<String, String> = emptyMap(),
        @Serializable(with = EpochSecondsSerializer::class)
        @SerialName("created_time")
        @JsonNames("created_time", "createdTime")
        val createdTime: Long? = null,
        @Serializable(with = EpochSecondsSerializer::class)
        @SerialName("expires_time_plus")
        @JsonNames("expires_time_plus", "expiresTime", "expiresTimePlus")
        val expiresTime: Long? = null,
        val attachments: Array<AttachmentDescriptor> = arrayOf(),
        val thid: String? = null,
        val pthid: String? = null,
        val ack: Array<String>? = emptyArray(),
        @Serializable(with = DirectionSerializer::class)
        val direction: Message.Direction = Message.Direction.RECEIVED
    ) {

        /**
         * Converts the current object to a Message object.
         *
         * @return The converted Message object.
         */
        fun toMessage(): Message =
            Message(
                id,
                piuri,
                from,
                to,
                fromPrior,
                body,
                extraHeaders,
                createdTime.toString(),
                expiresTime.toString(),
                attachments,
                thid,
                pthid,
                ack,
                direction
            )

        /**
         * Serializer for the [Message.Direction] enum class.
         */
        private object DirectionSerializer : KSerializer<Message.Direction> {
            /**
             * The descriptor for the 'Direction' variable.
             *
             * This descriptor represents a primitive serial descriptor for the 'Direction' variable.
             * It has a type of 'INT' in the 'PrimitiveKind' enumeration.
             *
             * @see PrimitiveSerialDescriptor
             * @see PrimitiveKind
             */
            override val descriptor =
                PrimitiveSerialDescriptor("Direction", PrimitiveKind.INT)

            /**
             * Serializes the value of type [Message.Direction] using the provided [encoder].
             *
             * @param encoder The encoder to use for serializing the value.
             * @param value The value to be serialized.
             */
            override fun serialize(encoder: Encoder, value: Message.Direction) {
                encoder.encodeInt(value.value)
            }

            /**
             * Deserializes the value from the given decoder and returns the corresponding Message.Direction.
             *
             * @param decoder The decoder used for deserialization.
             * @return The deserialized Message.Direction value.
             */
            override fun deserialize(decoder: Decoder): Message.Direction =
                Message.Direction.fromValue(decoder.decodeInt())
        }

        /**
         * A serializer for converting JSON strings to Kotlin strings and vice versa.
         *
         * This serializer is used to encode a JSON string into a Kotlin string using [serialize] method,
         * and decode a JSON string into a Kotlin string using [deserialize] method.
         *
         * @property descriptor The serial descriptor associated with this serializer.
         */
        private object JsonAsStringSerializer : KSerializer<String> {
            /**
             * Provides the descriptor for a JSON value represented as a string.
             *
             * @return The serial descriptor.
             */
            override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("JsonAsString", PrimitiveKind.STRING)

            /**
             * Serializes a string value into an encoded format using the provided encoder.
             *
             * @param encoder the encoder to use for encoding the value
             * @param value the string value to be serialized
             */
            override fun serialize(encoder: Encoder, value: String) {
                val jsonObject = Json.parseToJsonElement(value).jsonObject
                encoder.encodeString(Json.encodeToString(jsonObject))
            }

            /**
             * Deserializes a JSON string into a String object.
             *
             * @param decoder The decoder used to decode the JSON string.
             * @return The deserialized String object.
             * @throws UnknownError.SomethingWentWrongError if the JSON string is invalid.
             */
            override fun deserialize(decoder: Decoder): String {
                val jsonDecoder = decoder as JsonDecoder
                val jsonElement = jsonDecoder.decodeJsonElement()
                if (jsonElement is JsonPrimitive && jsonElement.isString) {
                    return jsonElement.content
                } else if (jsonElement is JsonObject) {
                    val jsonObject = jsonElement.jsonObject
                    return Json.encodeToString(jsonObject)
                } else {
                    throw UnknownError.SomethingWentWrongError("Invalid JSON string: $jsonElement")
                }
            }
        }

        /**
         * A custom serializer that converts a string representation of epoch seconds to a Long value and vice versa.
         *
         * This serializer implements the `KSerializer` interface and is specifically designed to work with strings representing epoch seconds.
         * It can be used with Kotlin serialization library for serializing and deserializing objects with epoch seconds properties.
         *
         * This serializer converts a string representation of epoch seconds to a Long value during serialization, and
         * converts a Long value back to a string representation of epoch seconds during deserialization.
         *
         * This serializer is a singleton object implemented as an `object` in Kotlin.
         *
         * @see KSerializer
         */
        private object EpochSecondsSerializer : KSerializer<Long> {
            /**
             * Variable descriptor for storing a serialized representation of epoch seconds as a string.
             * The descriptor is used for serializing and deserializing objects.
             */
            override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("EpochSecondsString", PrimitiveKind.LONG)

            /**
             * Checks if the given epoch time is likely to be in seconds.
             *
             * @param epochTime The epoch time to be checked.
             * @return true if the epoch time is likely in seconds, false otherwise.
             */
            private fun isEpochTimeLikelySeconds(epochTime: String): Boolean {
                return epochTime.length <= 10 // Epoch in seconds has up to 10 digits
            }

            /**
             * Serializes a string value.
             *
             * @param encoder the encoder to use for serialization
             * @param value the string value to be serialized
             */
            override fun serialize(encoder: Encoder, value: Long) {
                if (isEpochTimeLikelySeconds(value.toString())) {
                    encoder.encodeLong(value)
                } else {
                    encoder.encodeLong(value.toDuration(DurationUnit.MILLISECONDS).inWholeSeconds)
                }
            }

            /**
             * Deserializes a value from a Decoder.
             *
             * @param decoder The decoder used to deserialize the value.
             * @return The deserialized value as a String.
             */
            override fun deserialize(decoder: Decoder): Long {
                val jsonDecoder = decoder as JsonDecoder
                val jsonElement = jsonDecoder.decodeJsonElement()
                if (jsonElement is JsonPrimitive) {
                    try {
                        return jsonElement.content.toLong()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        throw ex
                    }
                } else {
                    throw UnknownError.SomethingWentWrongError("Invalid JSON string: $jsonElement")
                }
            }
        }

        /**
         * The DIDMessageSerializer class implements the KSerializer interface for serializing and deserializing DID objects.
         */
        private object DIDMessageSerializer : KSerializer<DID> {
            /**
             * Returns the serial descriptor for the variable.
             *
             * The descriptor represents the type of the variable and its serialization format.
             *
             * @return the serial descriptor
             */
            override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("DIDString", PrimitiveKind.STRING)

            /**
             * Serializes the given [value] to the specified [encoder].
             *
             * @param encoder The encoder used to serialize the [value].
             * @param value The [DID] to be serialized.
             */
            override fun serialize(encoder: Encoder, value: DID) {
                encoder.encodeString(value.toString())
            }

            /**
             * Deserialize method deserializes the given Decoder object and returns a new instance of DID.
             *
             * @param decoder The Decoder object used for deserialization.
             * @return A new instance of DID.
             */
            override fun deserialize(decoder: Decoder): DID =
                DID(decoder.decodeString())
        }
    }
}
