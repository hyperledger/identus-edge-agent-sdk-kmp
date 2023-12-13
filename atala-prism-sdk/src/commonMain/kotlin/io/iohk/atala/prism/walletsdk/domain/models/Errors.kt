package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes

/**
 * An interface that represents a base error in the Prism SDK.
 */
abstract interface Error {
    val code: Int?
    val underlyingErrors: Array<Error>?
    val errorDescription: String?
}

/**
 * A class representing an unknown error in a PRISM.
 *
 * @see Error
 * @see Throwable
 */
abstract class UnknownPrismError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : Error, Throwable(message, cause) {
    override val code: Int?
        get() = null

    override val underlyingErrors: Array<Error>?
        get() = emptyArray()

    override val errorDescription: String?
        get() = null
}

/**
 * A class representing a known error in a PRISM.
 *
 * @see Error
 * @see Throwable
 */
abstract class KnownPrismError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : Error, Throwable(message, cause) {
    override val code: Int?
        get() = null

    override val underlyingErrors: Array<Error>?
        get() = emptyArray()

    override val errorDescription: String?
        get() = null
}

/**
 * A class representing an unknown error if the error received does not conform to the [KnownPrismError], it will be
 * classified as an [UnknownPrismError].
 *
 * @see UnknownPrismError
 */
abstract class UnknownError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : UnknownPrismError(message, cause) {

    /**
     * A class representing an error that occurs when something goes wrong in the SDK.
     *
     * @param message The error message.
     * @param cause The underlying cause of the error.
     *
     * @see UnknownError
     */
    class SomethingWentWrongError
    @JvmOverloads
    constructor(
        message: String? = null,
        cause: Throwable? = null
    ) : UnknownError(message, cause) {
        override val code: Int
            get() = -1
    }
}

/**
 * A class representing a common error in a PRISM.
 *
 * @see KnownPrismError
 */
sealed class CommonError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : KnownPrismError(message, cause) {

    /**
     * A class representing an error that occurs when an invalid URL is encountered while trying to send a message.
     *
     * @param url The invalid URL that caused the error.
     *
     * @see CommonError
     */
    class InvalidURLError(url: String) : CommonError("Invalid url while trying to send message: $url") {
        override val code: Int
            get() = -2
    }

    /**
     * Represents an HTTP error encountered during an API request.
     *
     * @param code The HTTP status code of the error.
     * @param message The error message.
     *
     * @see CommonError
     */
    class HttpError
    @JvmOverloads
    constructor(
        override val code: Int,
        message: String? = null
    ) : CommonError("HTTP Request Error $code: $message") {
        override val errorDescription: String
            get() = "Code $code: $message"
    }
}

/**
 * A class representing a known error in an Apollo.
 *
 * @see KnownPrismError
 */
sealed class ApolloError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : KnownPrismError(message, cause) {

    /**
     * An implementation of the [ApolloError] class that represents an error caused by invalid mnemonic words.
     * This error occurs when one or more mnemonic words are invalid in a PRISM API response.
     *
     * @param invalidWords The array of invalid mnemonic words.
     * @property message The error message for this error, which is determined based on the presence of invalid words.
     *                   If `invalidWords` is not null, the message will be "The following mnemonic words are invalid: [invalidWords]".
     *                   If `invalidWords` is null, the message will be "The seed cannot be null".
     *
     * @see ApolloError
     */
    class InvalidMnemonicWord
    @JvmOverloads
    constructor(
        invalidWords: Array<String>? = null
    ) : ApolloError(
        if (invalidWords.isNullOrEmpty()) {
            "The following mnemonic words are invalid: $invalidWords"
        } else {
            "The seed cannot be null"
        }
    ) {
        override val code: Int
            get() = 11
    }

    /**
     * Class representing an error when the message string cannot be parsed to UTF8 data.
     *
     * @property code The error code for CouldNotParseMessageString, which is 12.
     *
     * @see ApolloError
     */
    class CouldNotParseMessageString : ApolloError("Could not get UTF8 Data from message string") {
        override val code: Int
            get() = 12
    }

    /**
     * A class representing an invalid JSON Web Key (JWK) error in the PRISM SDK.
     *
     * This error indicates that the JWK provided is not in a valid format.
     *
     * @property code The error
     *
     * @see ApolloError
     */
    class InvalidJWKError : ApolloError("JWK is not in a valid format") {
        override val code: Int
            get() = 13
    }

    /**
     * Represents an error that occurs when an invalid key curve is provided.
     *
     * @param invalidCurve The invalid key curve that was provided.
     *
     * @see ApolloError
     */
    class InvalidKeyCurve(
        invalidCurve: String
    ) : ApolloError(
        "Invalid key curve $invalidCurve. Valid options are: ${
        Curve.values().map { it.value }.toTypedArray().joinToString(", ")
        }"
    ) {
        override val code: Int
            get() = 14
    }

    /**
     * Represents an error indicating that a specific key curve is invalid.
     *
     * @param invalidCurve The invalid key curve.
     * @param validCurves An array of valid key curves.
     *
     * @see ApolloError
     */
    class InvalidSpecificKeyCurve(
        invalidCurve: String,
        validCurves: Array<String>
    ) : ApolloError(
        "Invalid key curve $invalidCurve. Valid options are: ${validCurves.joinToString(", ")}"
    ) {
        override val code: Int
            get() = 14
    }

    /**
     * Represents an error that occurs when an invalid key type is used.
     *
     * @param invalidType The invalid key type.
     *
     * @see ApolloError
     */
    class InvalidKeyType(
        invalidType: String
    ) : ApolloError(
        "Invalid key type $invalidType. Valid options are: ${
        KeyTypes.values().map { it.type }.toTypedArray().joinToString(", ")
        }"
    ) {
        override val code: Int
            get() = 15
    }

    /**
     * Represents an error that occurs when an invalid index is provided.
     *
     * @param message The error message associated with the exception.
     *
     * @see ApolloError
     */
    class InvalidIndex(message: String) : ApolloError(message) {
        override val code: Int
            get() = 16
    }

    /**
     * A class representing an invalid derivation path error.
     *
     * @property message The error message.
     *
     * @see ApolloError
     */
    class InvalidDerivationPath(message: String) : ApolloError(message) {
        override val code: Int
            get() = 17
    }

    /**
     * A class representing an error that occurs when an invalid seed is used.
     *
     * @param message The error message.
     *
     * @see ApolloError
     */
    class InvalidSeed(message: String) : ApolloError(message) {
        override val code: Int
            get() = 18
    }

    /**
     * Represents an error that occurs due to invalid raw data.
     *
     * @param message A detailed error message.
     *
     * @see ApolloError
     */
    class InvalidRawData(message: String) : ApolloError(message) {
        override val code: Int
            get() = 19
    }

    /**
     * Represents an error that occurs when restoration fails due to no identifier or invalid identifier.
     *
     * @see ApolloError
     */
    class RestorationFailedNoIdentifierOrInvalid : ApolloError("Restoration failed: no identifier or invalid") {
        override val code: Int
            get() = 20
    }
}

/**
 * A class representing a known error in a Castor.
 *
 * @see KnownPrismError
 */
sealed class CastorError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : KnownPrismError(message, cause) {

    /**
     * An error that indicates that the given key curve is not supported for a specific functionality.
     *
     * @param curve The unsupported key curve.
     *
     * @see CastorError
     */
    class KeyCurveNotSupported(curve: String) : CastorError(
        "Key curve $curve is not supported for this functionality"
    ) {
        override val code: Int
            get() = 21
    }

    /**
     * Represents an error that occurs when the long form PRISM DID is invalid or changed.
     *
     * @property code The error code associated with the InvalidLongFormDID error.
     *
     * @see CastorError
     */
    class InvalidLongFormDID : CastorError("Long form prism DID is invalid or changed") {
        override val code: Int
            get() = 22
    }

    /**
     * Represents an error that occurs when the provided method ID does not satisfy a given regex pattern.
     *
     * @param regex The regex pattern that the method ID should satisfy.
     *
     * @see CastorError
     */
    class MethodIdIsDoesNotSatisfyRegex(regex: String) : CastorError(
        "The Prism DID provided is not passing the regex validation: $regex"
    ) {
        override val code: Int
            get() = 23
    }

    /**
     * Represents an error that occurs when there is an invalid encoding or decoding
     * of a public key.
     *
     * @param didMethod The DID method being used when the error occurred.
     * @param curve The curve of the key when the error occurred.
     *
     * @see CastorError
     */
    class InvalidPublicKeyEncoding(
        didMethod: String,
        curve: String
    ) : CastorError("Invalid encoding/decoding of key $curve while trying to compute $didMethod") {
        override val code: Int
            get() = 24
    }

    /**
     * Represents an error that occurs when trying to parse an invalid DID string.
     *
     * @param did The invalid DID string.
     *
     * @see CastorError
     */
    class InvalidDIDString(did: String) : CastorError("Trying to parse invalid DID String: $did") {
        override val code: Int
            get() = 25
    }

    /**
     * Represents an error that occurs when the initial state of a Prism DID changes,
     * making it invalid.
     *
     * @see CastorError
     */
    class InitialStateOfDIDChanged : CastorError(
        "While trying to resolve Prism DID state changed making it invalid"
    ) {
        override val code: Int
            get() = 26
    }

    /**
     * Represents an error that occurs when it is not possible to resolve a Decentralized Identifier (DID) due to a specific reason.
     *
     * @param did The DID that could not be resolved.
     * @param reason The reason why the DID could not be resolved.
     *
     * @see CastorError
     */
    class NotPossibleToResolveDID(did: String, reason: String, cause: Throwable? = null) :
        CastorError("Not possible to resolve DID $did due to $reason", cause) {
        override val code: Int
            get() = 27
    }

    /**
     * Represents an error that occurs when the JWK (JSON Web Key) keys are not in a valid format.
     *
     * @property code The error code associated with the `InvalidJWKKeysError`.
     *
     * @see CastorError
     */
    class InvalidJWKKeysError : CastorError("JWK is not in a valid format") {
        override val code: Int
            get() = 28
    }

    /**
     * Represents an error that occurs when an invalid key is encountered.
     *
     * @see CastorError
     */
    class InvalidKeyError @JvmOverloads constructor(message: String? = null) : CastorError(message)

    /**
     * An error that occurs when the provided PeerDID is invalid.
     *
     * @see CastorError
     */
    class InvalidPeerDIDError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) :
        CastorError(message, cause)

    /**
     * Class representing an error thrown when no resolvers are available to resolve a given DID method.
     *
     * @param method The method that couldn't be resolved.
     *
     * @see CastorError
     */
    class NoResolversAvailableForDIDMethod(method: String) : CastorError(
        "No resolvers in castor are able to resolve the method $method, please provide a resolver"
    ) {
        override val code: Int
            get() = 29
    }
}

/**
 * A class representing a known error in a Mercury.
 *
 * @see KnownPrismError
 */
sealed class MercuryError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : KnownPrismError(message, cause) {

    /**
     * Error class representing a message that has no recipient set.
     * To send a message, please set the "to" field.
     *
     * @see MercuryError
     */
    class NoDIDReceiverSetError :
        MercuryError("Message has no recipient set, to send a message please set the \"to\"") {
        override val code: Int
            get() = 31
    }

    /**
     * An error class that indicates that no valid service is found for a given DID.
     *
     * @constructor Creates an instance of NoValidServiceFoundError.
     * @param did The DID that has no valid services
     *
     * @see MercuryError
     */
    class NoValidServiceFoundError
    @JvmOverloads
    constructor(
        did: String? = null
    ) : MercuryError(did?.let { "The did ($did) has no valid services" } ?: "No valid services") {
        override val code: Int
            get() = 32
    }

    /**
     * Represents an error that occurs when a message does not have a sender set.
     *
     * @see MercuryError
     */
    class NoDIDSenderSetError : MercuryError(
        "Message has no sender set, to send a message please set the \"from\""
    ) {
        override val code: Int
            get() = 33
    }

    /**
     * Represents an error that occurs when an unknown AttachmentData type is found while decoding a message.
     *
     * @see MercuryError
     */
    class UnknownAttachmentDataError : MercuryError(
        "Unknown AttachmentData type was found while decoding message"
    ) {
        override val code: Int
            get() = 34
    }

    /**
     * Represents an error that occurs when decoding a message and a message attachment is found without an "id".
     *
     * @see MercuryError
     */
    class MessageAttachmentWithoutIDError : MercuryError(
        "While decoding a message, a message attachment was found without \"id\" this is invalid"
    ) {
        override val code: Int
            get() = 35
    }

    /**
     * A class representing an error that occurs when decoding a message with an invalid body.
     *
     * @see MercuryError
     */
    class MessageInvalidBodyDataError : MercuryError(
        "While decoding a message, a body was found to be invalid while decoding"
    ) {
        override val code: Int
            get() = 36
    }

    /**
     * A class representing a DIDComm error in the Prism SDK.
     *
     * @param customMessage The custom message associated with the error. Defaults to null.
     * @param customUnderlyingErrors The array of underlying errors associated with the error.
     *
     * @see MercuryError
     */
    class DidCommError
    @JvmOverloads
    constructor(
        customMessage: String? = null,
        customUnderlyingErrors: Array<Error>
    ) : MercuryError(
        "DIDComm error has occurred with message: $customMessage\nErrors: ${
        customUnderlyingErrors.joinToString(
            separator = "\n"
        ) { it.errorDescription ?: "" }
        }"
    ) {
        override val code: Int
            get() = 37
    }
}

/**
 * A class representing a known error in a Pluto.
 *
 * @see KnownPrismError
 */
sealed class PlutoError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : KnownPrismError(message, cause) {

    /**
     * Represents an error that occurs when data is not persisted for a specific type while adding or making changes to another object.
     *
     * @param type The type of data that is not persisted.
     * @param affecting The object to which the data is being added or modified.
     *
     * @see PlutoError
     */
    class MissingDataPersistence(type: String, affecting: String) :
        PlutoError("$type is not persisted while trying to add or make changes to $affecting") {
        override val code: Int
            get() = 41
    }

    /**
     * Represents a specific error that occurs when required fields are missing.
     *
     * @param type The type that requires the fields.
     * @param fields The array of missing fields.
     *
     * @see PlutoError
     */
    class MissingRequiredFields(type: String, fields: Array<String>) :
        PlutoError("$type requires the following fields: ${fields.joinToString(", ")}") {

        override val code: Int
            get() = 42
    }

    /**
     * Represents a duplication error when trying to save an object with an existing ID.
     *
     * @param type The type of object that is being duplicated.
     *
     * @see PlutoError
     */
    class Duplication(type: String) : PlutoError("Trying to save $type with an ID that already exists") {
        override val code: Int
            get() = 43
    }

    /**
     * A class representing an unknown credential type error in the Pluto SDK.
     *
     * @see PlutoError
     */
    class UnknownCredentialTypeError : PlutoError("The credential type needs to be JWT or W3C") {
        override val code: Int
            get() = 44
    }

    /**
     * Represents an error that occurs when the credential JSON cannot be decoded.
     *
     * @see PlutoError
     */
    class InvalidCredentialJsonError : PlutoError("Could not decode the credential JSON") {
        override val code: Int
            get() = 45
    }

    /**
     * An error class representing a database connection error.
     *
     * @see PlutoError
     */
    class DatabaseConnectionError : PlutoError("Database connection error") {
        override val code: Int
            get() = 46
    }

    /**
     * Custom error class that represents an error when a context is required to be initialized in Pluto.
     *
     * @see PlutoError
     */
    class DatabaseContextError : PlutoError("Pluto requires a context to be initialized") {
        override val code: Int
            get() = 47
    }

    /**
     * Class representing an error that occurs when the database service is already running.
     *
     * @see PlutoError
     */
    class DatabaseServiceAlreadyRunning : PlutoError("Database service already running.") {
        override val code: Int
            get() = 48
    }

    /**
     * Represents an error that occurs when an invalid restoration identifier is encountered.
     *
     * @see PlutoError
     */
    class InvalidRestorationIdentifier : PlutoError("Invalid restoration identifier") {
        override val code: Int
            get() = 49
    }
}

/**
 * A class representing a known error in a Pollux.
 *
 * @see KnownPrismError
 */
sealed class PolluxError
@JvmOverloads
constructor(
    message: String? = null,
    cause: Throwable? = null
) : KnownPrismError(message, cause) {

    /**
     * Represents an error that occurs when attempting to create a JWT presentation without providing a PRISM DID.
     *
     * @see PolluxError
     */
    class InvalidPrismDID(message: String? = "To create a JWT presentation a Prism DID is required") :
        PolluxError(message) {
        override val code: Int
            get() = 53
    }

    /**
     * Represents an error encountered when invalid credentials are provided.
     *
     * @see PolluxError
     */
    class InvalidCredentialError
    @JvmOverloads
    constructor(
        cause: Throwable? = null
    ) : PolluxError("Invalid credential, could not decode", cause) {
        override val code: Int
            get() = 51
    }

    /**
     * Represents an error that occurs when an invalid JWT string is encountered while decoding a credential.
     *
     * @see PolluxError
     */
    class InvalidJWTString : PolluxError("Invalid JWT while decoding credential") {
        override val code: Int
            get() = 52
    }

    /**
     * A class representing an error when creating a JSON Web Token (JWT) presentation with an invalid JWTCredential.
     *
     * This error occurs when the provided JWTCredential is not valid or is missing required information.
     *
     * @see PolluxError
     */
    class InvalidJWTCredential : PolluxError("To create a JWT presentation please provide a valid JWTCredential") {
        override val code: Int
            get() = 54
    }

    /**
     * A class representing an error when no domain or challenge is found as part of the offer JSON.
     *
     * @see PolluxError
     */
    class NoDomainOrChallengeFound : PolluxError("No domain or challenge found as part of the offer json") {
        override val code: Int
            get() = 55
    }

    /**
     * Represents an error that occurs when there is an invalid credential definition.
     *
     * @see PolluxError
     */
    class InvalidCredentialDefinitionError : PolluxError("Invalid credential definition") {
        override val code: Int
            get() = 56
    }
}
