package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

interface PresentationSubmissionOptions

data class PresentationSubmissionOptionsJWT(
    val presentationDefinitionRequest: String
) : PresentationSubmissionOptions

data class PresentationSubmissionOptionsSDJWT(
    val presentationDefinitionRequest: String
) : PresentationSubmissionOptions

data class PresentationSubmissionOptionsAnoncreds(
    val presentationDefinitionRequest: String
) : PresentationSubmissionOptions
