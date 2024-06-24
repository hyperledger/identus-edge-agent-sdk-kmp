# SDk JWT revocation verification
## Requirements
1. A working Identus Mediator and an Identus Cloud Agent.
2. A holder who already has a JWT Credential issued

> NOTE:
>
> Please follow the [Quick started guide](https://docs.atalaprism.io/docs/quick-start) to establish mediation,
> establish a connection as a holder, issue a jwt credential.

## Specification
> NOTE:
> Only JWT credentials are supported for the moment.

## Flow
1. Holder has a JWT credential
2. This credential Holder has, is used to verify if is revoked.


## Code reference

* Edge Agent will do the appropriate validations and will update the credential record on the database.
* The UI must be updated based on the pluto data.

Example
```kotlin
val credential = JWTCredential()
agent.isCredentialRevoked(credential)
```