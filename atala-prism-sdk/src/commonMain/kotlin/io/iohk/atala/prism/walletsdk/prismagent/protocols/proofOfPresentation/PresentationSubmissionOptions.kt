package io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation

interface PresentationSubmissionOptions

data class PresentationSubmissionOptionsJWT(
    val presentationDefinitionRequest: PresentationDefinitionRequest
) : PresentationSubmissionOptions
