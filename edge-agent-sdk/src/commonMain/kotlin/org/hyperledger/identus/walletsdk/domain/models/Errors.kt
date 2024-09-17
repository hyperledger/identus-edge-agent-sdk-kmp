package org.hyperledger.identus.walletsdk.domain.models

import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes

/**
 * An interface that represents a base error in the Prism SDK.
 */
interface Error {
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
        message = "Invalid key curve $invalidCurve. Valid options are: ${
            Curve.entries.map { it.value }.toTypedArray().joinToString(", ")
        }"
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
        message = "Invalid key type $invalidType. Valid options are: ${
            KeyTypes.entries.map { it.type }.toTypedArray().joinToString(", ")
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
            get() = 110
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
     * Represents an error that occurs when the initial state of a PRISM DID changes,
     * making it invalid.
     *
     * @see CastorError
     */
    class InitialStateOfDIDChanged(message: String? = null) : CastorError(
        "While trying to resolve Prism DID state changed making it invalid. $message"
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
    class InvalidKeyError @JvmOverloads constructor(message: String? = null) : CastorError(message) {
        override val code: Int
            get() = 29
    }

    /**
     * An error that occurs when the provided PeerDID is invalid.
     *
     * @see CastorError
     */
    class InvalidPeerDIDError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) :
        CastorError(message, cause) {
        override val code: Int
            get() = 210
    }

    /**
     * Class representing an error thrown a json representation fo a DIDDocument cannot be parsed into the actual model.
     *
     * @param field The missing field
     *
     * @see CastorError
     */
    class CouldNotParseJsonIntoDIDDocument(field: String) : CastorError(
        "Provided json cannot be parsed into DIDDocument. Missing field $field"
    ) {
        override val code: Int
            get() = 211
    }

    /**
     * Class representing an error thrown when a field is missing or null when it should not.
     *
     * @param field The missing field
     *
     * @see CastorError
     */
    class NullOrMissingRequiredField(field: String, parent: String) : CastorError(
        "$field is missing or null from $parent when it must be provided and not null."
    ) {
        override val code: Int
            get() = 212
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
     * An error class representing a database connection error.
     *
     * @see PlutoError
     */
    class DatabaseConnectionError(message: String? = "Database connection error") : PlutoError(message) {
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
    class InvalidJWTString(msg: String? = null) : PolluxError(msg ?: "Invalid JWT while decoding credential") {
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
    class InvalidJWTCredential(msg: String? = null) :
        PolluxError(msg ?: "To create a JWT presentation please provide a valid JWTCredential") {
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

    /**
     * Represents an error that occurs when there is an invalid credential definition.
     *
     * @see PolluxError
     */
    class InvalidJWTPresentationDefinitionError(msg: String? = null) :
        PolluxError(msg ?: "Invalid JWT presentation definition") {
        override val code: Int
            get() = 57
    }

    class CredentialTypeNotSupportedError(msg: String) :
        PolluxError(msg) {
        override val code: Int
            get() = 58
    }

    class PrivateKeyTypeNotSupportedError(msg: String? = null) :
        PolluxError(msg ?: "Provided private key should be Secp256k1") {
        override val code: Int
            get() = 59
    }

    /**
     * A class representing an error when a verification is unsuccessful.
     *
     * @see PolluxError
     */
    class VerificationUnsuccessful(reason: String) : PolluxError(reason) {
        override val code: Int
            get() = 510
    }

    /**
     * A class representing an error when a provided key is the wrong type.
     *
     * @see PolluxError
     */
    class WrongKeyProvided(expected: String?, actual: String?) :
        PolluxError("Provided key is: $actual but should be $expected") {
        override val code: Int
            get() = 511
    }

    /*
     * Represents an error that occurs when the status list index is out of bounds compared to the decoded and decompressed value of encodedList.
     */
    class StatusListOutOfBoundIndex : PolluxError("Status list index is out of bound") {
        override val code: Int
            get() = 513
    }

    /**
     * Represents an error that occurs when a revocation registry json is missing a field.
     */
    class RevocationRegistryJsonMissingFieldError(val field: String) : PolluxError("Revocation registry json missing: $field") {
        override val code: Int
            get() = 514
    }

    /**
     * Represents an error that occurs when a revocation registry json is missing a field.
     */
    class UnsupportedTypeError(val type: String) : PolluxError("Unsupported type: $type") {
        override val code: Int
            get() = 515
    }

    /**
     * Represents an error that occurs when a field is null but should not be.
     */
    class NonNullableError(val field: String) : PolluxError("Field $field is non nullable.") {
        override val code: Int
            get() = 516
    }

    /**
     * Represents an error that occurs when a proof cannot be verified.
     */
    class VerifyProofError() : PolluxError("The verification failed.") {
        override val code: Int
            get() = 517
    }

    /**
     * Represents an error that occurs when a proof cannot be verified.
     */
    class PresentationDefinitionRequestError(msg: String) : PolluxError(msg) {
        override val code: Int
            get() = 69
    }
}
