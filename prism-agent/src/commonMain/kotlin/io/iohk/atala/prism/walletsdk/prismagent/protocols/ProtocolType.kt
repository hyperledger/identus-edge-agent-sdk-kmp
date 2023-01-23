package io.iohk.atala.prism.walletsdk.prismagent.protocols

enum class ProtocolType(val value: String) {
    didcommMediationRequest("https://didcomm.org/coordinate-mediation/2.0/mediate-request"),
    didcommMediationGrant("https://didcomm.org/coordinate-mediation/2.0/mediate-grant"),
    didcommMediationDeny("https://didcomm.org/coordinate-mediation/2.0/mediate-deny"),
    didcommMediationKeysUpdate("https://didcomm.org/coordinate-mediation/2.0/keylist-update"),
    didcommPresentation("https://didcomm.atalaprism.io/present-proof/3.0/presentation"),
    didcommRequestPresentation("https://didcomm.atalaprism.io/present-proof/3.0/request-presentation"),
    didcommProposePresentation("https://didcomm.atalaprism.io/present-proof/3.0/propose-presentation"),
    didcommCredentialPreview("https://didcomm.org/issue-credential/2.0/credential-preview"),
    didcommIssueCredential("https://didcomm.org/issue-credential/2.0/issue-credential"),
    didcommOfferCredential("https://didcomm.org/issue-credential/2.0/offer-credential"),
    didcommProposeCredential("https://didcomm.org/issue-credential/2.0/propose-credential"),
    didcommRequestCredential("https://didcomm.org/issue-credential/2.0/request-credential"),
    didcommconnectionRequest("https://atalaprism.io/mercury/connections/1.0/request"),
    didcommconnectionResponse("https://atalaprism.io/mercury/connections/1.0/response"),
    didcomminvitation("https://didcomm.org/out-of-band/2.0/invitation"),
    prismOnboarding("https://atalaprism.io/did-request"),
    pickupRequest("https://didcomm.org/messagepickup/3.0/delivery-request"),
    pickupDelivery("https://didcomm.org/messagepickup/3.0/delivery"),
    pickupStatus("https://didcomm.org/messagepickup/3.0/status"),
    pickupReceived("https://didcomm.org/messagepickup/3.0/messages-received")
}
