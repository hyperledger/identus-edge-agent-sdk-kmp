package io.iohk.atala.prism.domain.models

sealed class CommonError(message: String? = null) : Throwable(message) {
    class SomethingWentWrongError(message: String? = null) : CommonError(message)
}

sealed class ApolloError(message: String? = null) : Throwable(message) {
    class InvalidMnemonicWord(message: String? = null) : ApolloError(message)
    class CouldNotParseMessageString(message: String? = null) : ApolloError(message)
}

sealed class CastorError(message: String? = null) : Throwable(message) {
    class InvalidLongFormDID(message: String? = null) : CastorError(message)
    class MethodIdIsDoesNotSatisfyRegex(message: String? = null) : CastorError(message)
    class InvalidPublicKeyEncoding(message: String? = null) : CastorError(message)
    class InvalidDIDString(message: String? = null) : CastorError(message)
    class InitialStateOfDIDChanged(message: String? = null) : CastorError(message)
    class NotPossibleToResolveDID(message: String? = null) : CastorError(message)
    class InvalidJWKKeysError(message: String? = null) : CastorError(message)
    class InvalidKeyError(message: String? = null) : CastorError(message)
    class InvalidPeerDIDError(message: String? = null) : CastorError(message)
}

sealed class MercuryError(message: String? = null) : Throwable(message) {
    class InvalidURLError(message: String? = null) : MercuryError(message)
    class NoDIDReceiverSetError(message: String? = null) : MercuryError(message)
    class NoValidServiceFoundError(message: String? = null) : MercuryError(message)
    class FromFieldNotSetError(message: String? = null) : MercuryError(message)
    class UnknownAttachmentDataError(message: String? = null) : MercuryError(message)
    class MessageAttachmentWithoutIDError(message: String? = null) : MercuryError(message)
    class MessageInvalidBodyDataError(message: String? = null) : MercuryError(message)
    class UnknownPackingMessageError(message: String? = null) : MercuryError(message)
    class CouldNotResolveDIDError(message: String? = null) : MercuryError(message)
    class DidCommError(message: String? = null) : MercuryError(message)
    class UrlSessionError(message: String? = null, statusCode: Int, error: Error?) : MercuryError(message)
}

sealed class PlutoError(message: String? = null) : Throwable(message) {
    class InvalidHolderDIDNotPersistedError(message: String? = null) : PlutoError(message)
    class MessageMissingFromOrToDIDError(message: String? = null) : PlutoError(message)
    class DidPairIsNotPersistedError(message: String? = null) : PlutoError(message)
    class HolderDIDAlreadyPairingError(message: String? = null) : PlutoError(message)
    class UnknownCredentialTypeError(message: String? = null) : PlutoError(message)
    class InvalidCredentialJsonError(message: String? = null) : PlutoError(message)
}

sealed class PolluxError(message: String? = null) : Throwable(message) {
    class InvalidCredentialError(message: String? = null) : PolluxError(message)
}
