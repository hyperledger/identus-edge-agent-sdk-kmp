package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.Error
import io.iohk.atala.prism.walletsdk.domain.models.KnownPrismError

sealed class PrismAgentError : KnownPrismError() {

    class CannotFindDIDKeyPairIndex : PrismAgentError() {
        override val code: Int
            get() = 111

        override val message: String
            get() = "To sign with a DID a key pair needs to be registered, please register the key pair first"
    }

    class InvitationIsInvalidError : PrismAgentError() {
        override val code: Int
            get() = 112

        override val message: String
            get() = "The system could not parse the invitation, the message/json are invalid"
    }

    class UnknownInvitationTypeError(private val type: String) : PrismAgentError() {
        override val code: Int
            get() = 113

        override val message: String
            get() = "The type of the invitation is not supported: $type"
    }

    class InvalidMessageType(private val type: String, private val shouldBe: String) : PrismAgentError() {
        override val code: Int
            get() = 114

        override val message: String
            get() = "The following message $type, does not represent the protocol $shouldBe.\nAlso the message should have \"from\" and \"to\" fields\n"
    }

    class NoMediatorAvailableError : PrismAgentError() {
        override val code: Int
            get() = 115

        override val message: String
            get() = "There is no mediator.\nYou need to provide a mediation handler and start the prism agent before doing some operations."
    }

    class MediationRequestFailedError
    @JvmOverloads constructor(private val underlyingError: Array<Error>? = null) : PrismAgentError() {
        override val code: Int
            get() = 116

        override val message: String
            get() {
                val errorsMessages = underlyingError?.joinToString(separator = "\n") { errorDescription ?: "" }
                val message = errorsMessages?.map { "Errors: $it" }
                return "Something failed while trying to achieve mediation. $message"
            }
    }

    class OfferDoesNotProvideEnoughInformation : PrismAgentError() {
        override val code: Int
            get() = 117

        override val message: String
            get() = "Offer provided doesnt have challenge and domain in the attachments"
    }

    class CannotFindDIDPrivateKey(private val did: String) : PrismAgentError() {

        override val code: Int
            get() = 118
        override val message: String
            get() = "Could not find private key for DID: $did"
    }

    class FailedToOnboardError(private val statusCode: Int, private val response: String) : PrismAgentError() {
        override val code: Int
            get() = 119

        override val message: String
            get() = "Failed to onboard.\nStatus code: $statusCode\nResponse: $response"
    }

    class InvalidCredentialError constructor(
        private val credential: Credential? = null,
        private val type: CredentialType? = null
    ) :
        PrismAgentError() {
        override val code: Int
            get() = 120

        override val message: String
            get() = when {
                credential != null -> "Invalid credential, ${credential::class.simpleName} is not supported."
                type != null -> "Invalid credential type, ${type.name} is not supported."
                else -> "Invalid credential. No further information is available."
            }
    }

    class InvalidCredentialFormatError constructor(private val expectedFormat: CredentialType) :
        PrismAgentError() {
        override val code: Int
            get() = 121

        override val message: String
            get() = "Invalid credential format, it must be ${expectedFormat.type}"
    }

    class AttachmentTypeNotSupported @JvmOverloads constructor() :
        PrismAgentError() {
        override val code: Int
            get() = 122

        override val message: String
            get() = "Attachment type not supported, expecting base 64 attachment."
    }

    class PresentationSubmissionDoesNotContainChallenge @JvmOverloads constructor() :
        PrismAgentError() {
        override val code: Int
            get() = 123

        override val message: String
            get() = "Presentation submission must contain a challenge."
    }

    class PrismAgentStateAcceptOnlyOneObserver @JvmOverloads constructor() :
        PrismAgentError() {
        override val code: Int
            get() = 124

        override val message: String
            get() = "Agent state only accepts one subscription."
    }

    class InvalidCredentialMetadata @JvmOverloads constructor() :
        PrismAgentError() {
        override val code: Int
            get() = 125

        override val message: String
            get() = "Invalid or null credential metadata"
    }

    class MissingOrNullFieldError constructor(private val field: String, private val parent: String) :
        PrismAgentError() {
        override val code: Int
            get() = 126

        override val message: String = ""
            get() = "$field from $parent is missing or null, and is mandatory"
    }
}
