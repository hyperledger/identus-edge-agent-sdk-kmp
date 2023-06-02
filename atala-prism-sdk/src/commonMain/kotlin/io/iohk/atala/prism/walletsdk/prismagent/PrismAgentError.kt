package io.iohk.atala.prism.walletsdk.prismagent

sealed interface Error {
    val code: Int?
    val message: String?
    val underlyingErrors: Array<Error>?
    val errorDescription: String?
}

sealed class UnknownPrismError : Error, Throwable() {

    override val code: Int?
        get() = null
    override val message: String?
        get() = null

    override val underlyingErrors: Array<Error>?
        get() = emptyArray()
    override val errorDescription: String?
        get() = null
}

sealed class KnownPrismError : Error, Throwable() {
    override val code: Int?
        get() = null
    override val message: String?
        get() = null
    override val underlyingErrors: Array<Error>?
        get() = emptyArray()
    override val errorDescription: String?
        get() = null
}

sealed class UnknownError : UnknownPrismError() {

    class SomethingWentWrongError(
        private val customMessage: String? = null,
        private val customUnderlyingErrors: Array<io.iohk.atala.prism.walletsdk.domain.models.Error> = emptyArray()
    ) : UnknownError() {
        override val code: Int? = -1
        override val message: String?
            get() {
                val errorsMessages = customUnderlyingErrors.joinToString(separator = "\n") { it.errorDescription ?: "" }
                return "$customMessage $errorsMessages"
            }
    }
}

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

    class UnknownInvitationTypeError : PrismAgentError() {
        override val code: Int
            get() = 113

        override val message: String
            get() = "The type of the invitation is not supported."
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

    class MediationRequestFailedError(private val underlyingError: Array<Error>? = null) : PrismAgentError() {
        override val code: Int
            get() = 116

        override val message: String
            get() {
                val errorsMessages = underlyingError?.joinToString(separator = "\n") { it.errorDescription ?: "" }
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
}
