package io.iohk.atala.prism.domain.models

open class CommonError : Throwable() {
    class SomethingWentWrongError : CommonError()
}

open class ApolloError : Throwable() {
    class InvalidMnemonicWord : ApolloError()
    class CouldNotParseMessageString : ApolloError()
}

open class CastorError : Throwable() {
    class InvalidLongFormDID : CastorError()
    class MethodIdIsDoesNotSatisfyRegex : CastorError()
    class InvalidPublicKeyEncoding : CastorError()
    class InvalidDIDString : CastorError()
    class InitialStateOfDIDChanged : CastorError()
    class NotPossibleToResolveDID : CastorError()
    class InvalidJWKKeysError : CastorError()
    class InvalidKeyError : CastorError()
    class InvalidPeerDIDError : CastorError()
}

open class MercuryError : Throwable() {
    class InvalidURLError : MercuryError()
    class NoDIDReceiverSetError : MercuryError()
    class NoValidServiceFoundError : MercuryError()
    class FromFieldNotSetError : MercuryError()
    class UnknownAttachmentDataError : MercuryError()
    class MessageAttachmentWithoutIDError : MercuryError()
    class MessageInvalidBodyDataError : MercuryError()
    class UnknownPackingMessageError : MercuryError()
    class CouldNotResolveDIDError : MercuryError()
    class DidCommError(msg: String) : MercuryError()
    class UrlSessionError(statusCode: Int, error: Error?, msg: String?) : MercuryError()
}

open class PlutoError : Throwable() {
    class InvalidHolderDIDNotPersistedError : PlutoError()
    class MessageMissingFromOrToDIDError : PlutoError()
    class DidPairIsNotPersistedError : PlutoError()
    class HolderDIDAlreadyPairingError : PlutoError()
    class UnknownCredentialTypeError : PlutoError()
    class InvalidCredentialJsonError : PlutoError()
}

open class PolluxError : Throwable() {
    class InvalidCredentialError : PolluxError()
}
