package io.iohk.atala.prism.walletsdk.prismagent.protocols

enum class ProtocolType(val value: String) {
    DidcommMediationRequest("https://didcomm.org/coordinate-mediation/2.0/mediate-request"),
    DidcommMediationGrant("https://didcomm.org/coordinate-mediation/2.0/mediate-grant"),
    DidcommMediationDeny("https://didcomm.org/coordinate-mediation/2.0/mediate-deny"),
    DidcommMediationKeysUpdate("https://didcomm.org/coordinate-mediation/2.0/keylist-update"),
    DidcommPresentation("https://didcomm.atalaprism.io/present-proof/3.0/presentation"),
    DidcommRequestPresentation("https://didcomm.atalaprism.io/present-proof/3.0/request-presentation"),
    DidcommProposePresentation("https://didcomm.atalaprism.io/present-proof/3.0/propose-presentation"),
    DidcommCredentialPreview("https://didcomm.org/issue-credential/2.0/credential-preview"),
    DidcommIssueCredential("https://didcomm.org/issue-credential/2.0/issue-credential"),
    DidcommOfferCredential("https://didcomm.org/issue-credential/2.0/offer-credential"),
    DidcommProposeCredential("https://didcomm.org/issue-credential/2.0/propose-credential"),
    DidcommRequestCredential("https://didcomm.org/issue-credential/2.0/request-credential"),
    DidcommconnectionRequest("https://atalaprism.io/mercury/connections/1.0/request"),
    DidcommconnectionResponse("https://atalaprism.io/mercury/connections/1.0/response"),
    Didcomminvitation("https://didcomm.org/out-of-band/2.0/invitation"),
    PrismOnboarding("https://atalaprism.io/did-request"),
    PickupRequest("https://didcomm.org/messagepickup/3.0/delivery-request"),
    PickupDelivery("https://didcomm.org/messagepickup/3.0/delivery"),
    PickupStatus("https://didcomm.org/messagepickup/3.0/status"),
    PickupReceived("https://didcomm.org/messagepickup/3.0/messages-received")
}
