package io.iohk.atala.prism.walletsdk.domain.models

import kotlin.jvm.JvmOverloads

sealed class CommonError @JvmOverloads constructor(message: String? = null) : Throwable(message) {
    class SomethingWentWrongError @JvmOverloads constructor(message: String? = null) : CommonError(message)
}

sealed class ApolloError @JvmOverloads constructor(message: String? = null) : Throwable(message) {
    class InvalidMnemonicWord @JvmOverloads constructor(message: String? = null) : ApolloError(message)
    class CouldNotParseMessageString @JvmOverloads constructor(message: String? = null) : ApolloError(message)
    class InvalidKeyCurve @JvmOverloads constructor(message: String? = null) : ApolloError(message)
}

sealed class CastorError @JvmOverloads constructor(message: String? = null) : Throwable(message) {
    class InvalidLongFormDID @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class MethodIdIsDoesNotSatisfyRegex @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class InvalidPublicKeyEncoding @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class InvalidDIDString @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class InitialStateOfDIDChanged @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class NotPossibleToResolveDID @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class InvalidJWKKeysError @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class InvalidKeyError @JvmOverloads constructor(message: String? = null) : CastorError(message)
    class InvalidPeerDIDError @JvmOverloads constructor(message: String? = null) : CastorError(message)
}

sealed class MercuryError @JvmOverloads constructor(message: String? = null) : Throwable(message) {
    class InvalidURLError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class NoDIDReceiverSetError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class NoDIDSenderSetError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class NoValidServiceFoundError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class FromFieldNotSetError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class UnknownAttachmentDataError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class MessageAttachmentWithoutIDError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class MessageInvalidBodyDataError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class UnknownPackingMessageError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class CouldNotResolveDIDError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class DidCommError @JvmOverloads constructor(message: String? = null) : MercuryError(message)
    class UrlSessionError @JvmOverloads constructor(message: String? = null, val statusCode: Int, val error: Error?) : MercuryError(message)
}

sealed class PlutoError @JvmOverloads constructor(message: String? = null) : Throwable(message) {
    class InvalidHolderDIDNotPersistedError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class MessageMissingFromOrToDIDError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class DidPairIsNotPersistedError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class HolderDIDAlreadyPairingError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class UnknownCredentialTypeError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class InvalidCredentialJsonError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class DatabaseConnectionError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class DatabaseContextError @JvmOverloads constructor(message: String? = null) : PlutoError(message)
    class DatabaseServiceAlreadyRunning @JvmOverloads constructor(message: String? = null) : PlutoError(message)
}

sealed class PolluxError @JvmOverloads constructor(message: String? = null) : Throwable(message) {
    class InvalidPrismDID @JvmOverloads constructor(message: String? = null) : PolluxError(message)
    class InvalidCredentialError @JvmOverloads constructor(message: String? = null) : PolluxError(message)
    class InvalidJWTString @JvmOverloads constructor(message: String? = null) : PolluxError(message)
    class NoDomainOrChallengeFound @JvmOverloads constructor(message: String? = null) : PolluxError(message)
}

sealed class PrismAgentError @JvmOverloads constructor(message: String? = null) : Throwable(message) {
    class InvalidURLError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class CannotFindDIDKeyPairIndex @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class CannotFindDIDPrivateKey @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvitationHasNoFromDIDError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class NoValidServiceEndpointError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvitationIsInvalidError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class NoConnectionOpenError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class NoHandshakeResponseError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class UnknownInvitationTypeError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class UnknownPrismOnboardingTypeError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class FailedToOnboardError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidPickupDeliveryMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidOfferCredentialMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidProposedCredentialMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidIssueCredentialMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidRequestCredentialMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidPresentationMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidRequestPresentationMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidProposePresentationMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidMediationGrantMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidMessageError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class NoMediatorAvailableError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class MediationRequestFailedError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
    class InvalidStepError @JvmOverloads constructor(message: String? = null) : PrismAgentError(message)
}
