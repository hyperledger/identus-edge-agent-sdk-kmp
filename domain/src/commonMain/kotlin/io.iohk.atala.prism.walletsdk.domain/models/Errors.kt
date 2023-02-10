package io.iohk.atala.prism.walletsdk.domain.models

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

sealed class PrismAgentError(message: String? = null) : Throwable(message) {
    class invalidURLError(message: String? = null) : PrismAgentError(message)
    class cannotFindDIDKeyPairIndex(message: String? = null) : PrismAgentError(message)
    class cannotFindDIDPrivateKey(message: String? = null) : PrismAgentError(message)
    class invitationHasNoFromDIDError(message: String? = null) : PrismAgentError(message)
    class noValidServiceEndpointError(message: String? = null) : PrismAgentError(message)
    class invitationIsInvalidError(message: String? = null) : PrismAgentError(message)
    class noConnectionOpenError(message: String? = null) : PrismAgentError(message)
    class noHandshakeResponseError(message: String? = null) : PrismAgentError(message)
    class unknownInvitationTypeError(message: String? = null) : PrismAgentError(message)
    class unknownPrismOnboardingTypeError(message: String? = null) : PrismAgentError(message)
    class failedToOnboardError(message: String? = null) : PrismAgentError(message)
    class invalidPickupDeliveryMessageError(message: String? = null) : PrismAgentError(message)
    class invalidOfferCredentialMessageError(message: String? = null) : PrismAgentError(message)
    class invalidProposedCredentialMessageError(message: String? = null) : PrismAgentError(message)
    class invalidIssueCredentialMessageError(message: String? = null) : PrismAgentError(message)
    class invalidRequestCredentialMessageError(message: String? = null) : PrismAgentError(message)
    class invalidPresentationMessageError(message: String? = null) : PrismAgentError(message)
    class invalidRequestPresentationMessageError(message: String? = null) : PrismAgentError(message)
    class invalidProposePresentationMessageError(message: String? = null) : PrismAgentError(message)
    class invalidMediationGrantMessageError(message: String? = null) : PrismAgentError(message)
    class noMediatorAvailableError(message: String? = null) : PrismAgentError(message)
    class mediationRequestFailedError(message: String? = null) : PrismAgentError(message)
}
