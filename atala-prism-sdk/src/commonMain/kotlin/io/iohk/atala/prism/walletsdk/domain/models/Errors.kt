package io.iohk.atala.prism.walletsdk.domain.models

/**
 * An interface that represents a base error in the Prism SDK.
 */
abstract interface Error {
    val code: Int?
    val underlyingErrors: Array<Error>?
    val errorDescription: String?
}

/**
 * A class representing an unknown error in a Prism API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code, an error message, and possibly an array of underlying errors. If the error
 * received does not conform to the [Error] interface, it will be classified as an [UnknownPrismError].
 */
abstract class UnknownPrismError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : Error, Throwable(message, cause) {

    override val code: Int?
        get() = null
    override val message: String?
        get() = null

    override val underlyingErrors: Array<Error>?
        get() = emptyArray()
    override val errorDescription: String?
        get() = null
}

/**
 * A class representing a known error in a Prism API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code and an error message. If the error received conforms to the [KnownPrismError],
 * it will be classified as a known error.
 */
abstract class KnownPrismError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : Error, Throwable(message, cause) {
    override val code: Int?
        get() = null
    override val message: String?
        get() = null
    override val underlyingErrors: Array<Error>?
        get() = emptyArray()
    override val errorDescription: String?
        get() = null
}

/**
 * A class representing an unknown error in a Prism API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code, an error message, and possibly an array of underlying errors.
 * If the error received does not conform to the [KnownPrismError], it will be classified as an [UnknownPrismError].
 */
abstract class UnknownError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : UnknownPrismError(message, cause) {

    class SomethingWentWrongError(
        message: String? = null,
        cause: Throwable? = null,
        private val customMessage: String? = null,
        private val customUnderlyingErrors: Array<Error> = emptyArray()
    ) : UnknownError(message, cause) {
        override val code: Int? = -1
        override val message: String?
            get() {
                val errorsMessages = customUnderlyingErrors.joinToString(separator = "\n") { it.errorDescription ?: "" }
                return "$customMessage $errorsMessages"
            }
    }
}

/**
 * A class representing a known error in a Prism API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code and an error message. If the error received conforms to the [KnownPrismError],
 * it will be classified as a known error.
 */
sealed class CommonError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : KnownPrismError(message, cause) {
    class InvalidURLError(val url: String) : CommonError() {
        override val code: Int
            get() = -2
        override val message: String
            get() = "Invalid url while trying to send message: $url"
    }

    class HttpError(private val customCode: Int, private val customMessage: String? = null) : CommonError() {
        override val code: Int
            get() = customCode
        override val message: String
            get() = "HTTP Request Error $customCode: $customMessage"
        override val errorDescription: String
            get() = "Code $code: $message"
    }
}

/**
 * A class representing a known error in an Apollo API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code and an error message. If the error received conforms to the [KnownPrismError],
 * it will be classified as a known error.
 */
sealed class ApolloError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : KnownPrismError(message, cause) {
    class InvalidMnemonicWord(private val invalidWords: Array<String>? = null) : ApolloError() {
        override val code: Int
            get() = 11
        override val message: String
            get() {
                return if (invalidWords != null) {
                    "The following mnemonic words are invalid: $invalidWords"
                } else {
                    "The seed cannot be null"
                }
            }
    }

    class CouldNotParseMessageString : ApolloError() {
        override val code: Int
            get() = 12

        override val message: String
            get() = "Could not get UTF8 Data from message string"
    }

    class InvalidJWKError : ApolloError() {

        override val code: Int
            get() = 13
        override val message: String
            get() = "JWK is not in a valid format"
    }

    class InvalidKeyCurve(val invalidCurve: String, val validCurves: Array<String>) : ApolloError() {

        override val code: Int
            get() = 14
        override val message: String
            get() = "Invalid key curve $invalidCurve. Valid options are: ${validCurves.joinToString(", ")}"
    }

    class InvalidKeyType(val invalidType: String, val validTypes: Array<String>) : ApolloError() {

        override val code: Int
            get() = 15
        override val message: String
            get() = "Invalid key type $invalidType. Valid options are: ${validTypes.joinToString(", ")}"
    }

    class InvalidIndex(val invalidMessage: String) : ApolloError() {

        override val code: Int
            get() = 16
        override val message: String
            get() = invalidMessage
    }

    class InvalidDerivationPath(val invalidMessage: String) : ApolloError() {

        override val code: Int
            get() = 17
        override val message: String
            get() = invalidMessage
    }

    class InvalidSeed(val invalidMessage: String) : ApolloError() {

        override val code: Int
            get() = 18
        override val message: String
            get() = invalidMessage
    }

    class InvalidRawData(val invalidMessage: String) : ApolloError() {

        override val code: Int
            get() = 19
        override val message: String
            get() = invalidMessage
    }
}

/**
 * A class representing a known error in a Castor API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code and an error message. If the error received conforms to the [KnownPrismError],
 * it will be classified as a known error.
 */
sealed class CastorError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : KnownPrismError(message, cause) {
    class KeyCurveNotSupported(val curve: String) : CastorError() {
        override val code: Int
            get() = 21

        override val message: String
            get() = "Key curve $curve is not supported for this functionality"
    }

    class InvalidLongFormDID : CastorError() {
        override val code: Int
            get() = 22

        override val message: String
            get() = "Long form prism DID is invalid or changed"
    }

    class MethodIdIsDoesNotSatisfyRegex(private val regex: String) : CastorError() {
        override val code: Int
            get() = 23

        override val message: String
            get() = "The Prism DID provided is not passing the regex validation: $regex"
    }

    class InvalidPublicKeyEncoding(private val didMethod: String, val curve: String) :
        CastorError() {
        override val code: Int
            get() = 24

        override val message: String
            get() = "Invalid encoding/decoding of key $curve while trying to compute $didMethod"
    }

    class InvalidDIDString(val did: String) : CastorError() {
        override val code: Int
            get() = 25

        override val message: String
            get() = "Trying to parse invalid DID String: $did"
    }

    class InitialStateOfDIDChanged : CastorError() {
        override val code: Int
            get() = 26

        override val message: String
            get() = "While trying to resolve Prism DID state changed making it invalid"
    }

    class NotPossibleToResolveDID(val did: String, private val reason: String) :
        CastorError() {
        override val code: Int
            get() = 27

        override val message: String
            get() = "Not possible to resolve DID $did due to $reason"
    }

    class InvalidJWKKeysError : CastorError() {
        override val code: Int
            get() = 28

        override val message: String
            get() = "JWK is not in a valid format"
    }

    class InvalidKeyError : CastorError()

    class InvalidPeerDIDError : CastorError()

    class NoResolversAvailableForDIDMethod(val method: String) : CastorError() {
        override val code: Int
            get() = 29

        override val message: String
            get() = "No resolvers in castor are able to resolve the method $method, please provide a resolver"
    }
}

/**
 * A class representing a known error in a Mercury API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code and an error message. If the error received conforms to the [KnownPrismError],
 * it will be classified as a known error.
 */
sealed class MercuryError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : KnownPrismError(message, cause) {

    class NoDIDReceiverSetError : MercuryError() {
        override val code: Int
            get() = 31

        override val message: String
            get() = "Message has no recipient set, to send a message please set the \"to\""
    }

    class NoValidServiceFoundError(val did: String? = null) : MercuryError() {
        override val code: Int
            get() = 32

        override val message: String
            get() = did?.let { "The did ($did) has no valid services" } ?: "No valid services"
    }

    class NoDIDSenderSetError : MercuryError() {
        override val code: Int
            get() = 33

        override val message: String
            get() = "Message has no sender set, to send a message please set the \"from\""
    }

    class UnknownAttachmentDataError : MercuryError() {
        override val code: Int
            get() = 34

        override val message: String
            get() = "Unknown AttachamentData type was found while decoding message"
    }

    class MessageAttachmentWithoutIDError : MercuryError() {
        override val code: Int
            get() = 35

        override val message: String
            get() = "While decoding a message, a message attachment was found without \"id\" this is invalid"
    }

    class MessageInvalidBodyDataError : MercuryError() {
        override val code: Int
            get() = 36

        override val message: String
            get() = "While decoding a message, a body was found to be invalid while decoding"
    }

    class DidCommError @JvmOverloads constructor(
        private val customMessage: String? = null,
        private val customUnderlyingErrors: Array<Error>
    ) :
        MercuryError() {
        override val code: Int
            get() = 37

        override val message: String
            get() {
                val errorsMessages = customUnderlyingErrors.joinToString(separator = "\n") { it.errorDescription ?: "" }
                return "DIDComm error has occurred with message: $customMessage\nErrors: $errorsMessages"
            }
    }
}

/**
 * A class representing a known error in a Pluto API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code and an error message. If the error received conforms to the [KnownPrismError],
 * it will be classified as a known error.
 */
sealed class PlutoError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : KnownPrismError(message, cause) {
    class MissingDataPersistence(val type: String, private val affecting: String) :
        PlutoError() {
        override val code: Int
            get() = 41

        override val message: String
            get() = "$type is not persisted while trying to add or make changes to $affecting"
    }

    class MissingRequiredFields(val type: String, private val fields: Array<String>) :
        PlutoError() {

        override val code: Int
            get() = 42

        override val message: String
            get() = "$type requires the following fields: ${fields.joinToString(", ")}"
    }

    class Duplication(val type: String) : PlutoError() {
        override val code: Int
            get() = 43

        override val message: String
            get() = "Trying to save $type with an ID that already exists"
    }

    class UnknownCredentialTypeError : PlutoError() {
        override val code: Int
            get() = 44

        override val message: String
            get() = "The credential type needs to be JWT or W3C"
    }

    class InvalidCredentialJsonError : PlutoError() {
        override val code: Int
            get() = 45

        override val message: String
            get() = "Could not decode the credential JSON"
    }

    class DatabaseConnectionError : PlutoError() {
        override val code: Int
            get() = 46

        override val message: String
            get() = "Database connection error"
    }

    class DatabaseContextError : PlutoError() {
        override val code: Int
            get() = 47

        override val message: String
            get() = "Pluto requires a context to be initialized"
    }

    class DatabaseServiceAlreadyRunning : PlutoError() {
        override val code: Int
            get() = 48

        override val message: String
            get() = "Database service already running."
    }
}

/**
 * A class representing a known error in a Pollux API response.
 *
 * Note: When an error occurs during an API request/response cycle, the server may return an error object in the response.
 * This object may include an error code and an error message. If the error received conforms to the [KnownPrismError],
 * it will be classified as a known error.
 */
sealed class PolluxError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : KnownPrismError(message, cause) {
    class InvalidPrismDID : PolluxError() {
        override val code: Int
            get() = 53

        override val message: String
            get() = "To create a JWT presentation a Prism DID is required"
    }

    class InvalidCredentialError @JvmOverloads constructor(message: String? = null, cause: Throwable? = null) : PolluxError(message, cause) {
        override val code: Int
            get() = 51

        override val message: String
            get() = "Invalid credential, could not decode"
    }

    class InvalidJWTString : PolluxError() {
        override val code: Int
            get() = 52

        override val message: String
            get() = "Invalid JWT while decoding credential"
    }

    class InvalidJWTCredential : PolluxError() {
        override val code: Int
            get() = 54

        override val message: String
            get() = "To create a JWT presentation please provide a valid JWTCredential"
    }

    class NoDomainOrChallengeFound : PolluxError() {
        override val code: Int
            get() = 55

        override val message: String
            get() = "No domain or challenge found as part of the offer json"
    }
}
