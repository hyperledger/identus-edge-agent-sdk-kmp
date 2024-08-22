# Cross-Platform Edge SDK Verification
## Requirements
1. A working Identus Mediator and an Identus Cloud Agent.
2. A holder who already has a JWT or Anoncred Credential issued by a known issuer (prism:did) [Holder A]
3. A holder who does not have credentials but aims to start the Verification [Holder B (verifier)]
4. Holder A shares its peerDID with holder B.
5. Holder B will initiate a presentation request

> NOTE:
>
> Please follow the [Quick started guide](https://docs.atalaprism.io/docs/quick-start) to establish mediation, 
> establish a connection as a holder and, issue a jwt credential.

## Specification
> NOTE:
> It follows the [Identity Foundation Presentation-exchange V2 protocol](https://identity.foundation/presentation-exchange/spec/v2.0.0/#input-descriptor)
>
> Claims can be for JWT:
> ```kotlin
> data class InputFieldFilter(
>    val type: String,
>    val pattern: String? = null,
>    val enum: List<Any>? = null,
>    val const: List<Any>? = null,
>    val value: Any? = null)
> ```
> 
> Claims can be for Anoncreds:
> ```kotlin
> data class AnoncredsPresentationClaims(
>    val predicates: Map<String, AnoncredsInputFieldFilter>,
>    val attributes: Map<String, RequestedAttributes>)
> ```

## Flow
1. Holder B Initiates the Presentation Request: creating a PresentationDefinitionRequest with specified requirements.
2. Holder A, will then create a Presentation which meets the criteria received as part of the request.
3. Holder B, will receive the Presentation and verify the following
    * [JWT] Holder A signed the JWT presentation with the correct signatures.
    * [JWT] Holder A signed the random challenge that required them to have the correct keys.
    * [JWT] Holder A includes a credential of its owns and not somebody else's.
    * [JWT] Holder A includes a credential with valid signatures, matching the issuer through the specified DID.
    * [JWT] (optional) Holder A has included a credential that the requested issuer has issued.
    * [JWT] (optional) Holder A has included a credential that satisfies the requested claims.
    * [Anoncreds] Holder A presentation meets the attributes and predicates requested.
4. Holder B can then verify at any point in time that presentation request and show feedback in UI.

## Code Reference
* toDID is the peer did of holder A, which has the credential that we aim to verify
* claims contain an object with all the claims we aim to validate; setting claims is internally used to help Holder A choose the proper credential and correctly verify the fields when Holder B receives the presentation.

* The Edge Agent Verifier (SDK) will then send the Presentation Request to the desired holder

Example for JWT
```kotlin
val claims = JWTPresentationClaims(
    claims = mapOf(
        "email" to InputFieldFilter(
            type = "string",
            pattern = "email@email.com"
        )
    )
)

agent.initiatePresentationRequest(
    type = CredentialType.JWT,
    toDID = toDID,
    presentationClaims = claims,
    domain = domain,
    challenge = UUID.randomUUID().toString()
)
```

Example for Anoncreds
```kotlin
val claims = AnoncredsPresentationClaims(
   predicates = mapOf(
      "0_age" to AnoncredsInputFieldFilter(
         type = "string",
         name = "age",
         gte = 18
      )
   ),
   attributes = mapOf(
      "0_name" to RequestedAttributes(
         "name",
         setOf("name"),
         emptyMap(),
         null
      )
   )
)

agent.initiatePresentationRequest(
    type = CredentialType.ANONCREDS_PROOF_REQUEST,
    toDID = toDID,
    presentationClaims = claims
)
```

* The Edge Agent Holder will be asked to choose what credential wants to be used for that Presentation Request

Example
```kotlin
val presentation = agent.preparePresentationForRequestProof(
   RequestPresentation.fromMessage(message),
   credential
)
agent.sendMessage(presentation.makeMessage())
```

* The Edge Agent Verifier (SDK) will then receive and validate the Credential as follows

Example
```kotlin
val isValid = agent.handlePresentation(message)
```



