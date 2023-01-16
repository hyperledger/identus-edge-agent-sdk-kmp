package io.iohk.atala.prism.domain.models

enum class CommonError {
    somethingWentWrongError
}

enum class ApolloError {
    invalidMnemonicWord,
    couldNotParseMessageString
}

enum class CastorError {
    invalidLongFormDID,
    methodIdIsDoesNotSatisfyRegex,
    invalidPublicKeyEncoding,
    invalidDIDString,
    initialStateOfDIDChanged,
    notPossibleToResolveDID,
    invalidJWKKeysError,
    invalidKeyError,
    invalidPeerDIDError
}

enum class MercuryError {
    invalidURLError,
    noDIDReceiverSetError,
    noValidServiceFoundError,
    fromFieldNotSetError,
    unknownAttachmentDataError,
    messageAttachmentWithoutIDError,
    messageInvalidBodyDataError,
    unknowPackingMessageError,
    couldNotResolveDIDError,
//    didcommError(msg: String),
//    urlSessionError(statusCode: Int, error: Error?, msg: String?)
}

enum class PlutoError {
    invalidHolderDIDNotPersistedError,
    messageMissingFromOrToDIDError,
    didPairIsNotPersistedError,
    holderDIDAlreadyPairingError,
    unknownCredentialTypeError,
    invalidCredentialJsonError
}

enum class PolluxError {
    invalidCredentialError
}