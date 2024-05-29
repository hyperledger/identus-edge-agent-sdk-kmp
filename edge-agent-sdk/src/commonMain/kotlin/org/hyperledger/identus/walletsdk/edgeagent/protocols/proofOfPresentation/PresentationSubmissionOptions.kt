package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

interface PresentationSubmissionOptions

data class PresentationSubmissionOptionsJWT(
    val presentationDefinitionRequest: PresentationDefinitionRequest
) : PresentationSubmissionOptions
