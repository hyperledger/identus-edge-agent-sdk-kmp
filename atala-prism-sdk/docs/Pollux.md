# Package org.hyperledger.identus.walletsdk.pollux

A credential contains a set of claims about the subject of the credential. Those claims are made by a single authority
called the credential issuer. The entity (person, organization) to whom the credential is issued, i.e., the one who
keeps it in their digital wallet, is called the credential holder. Apart from claims, the credential contains the
subject identifier (DID) to whom the credential is issued.

- **Verifiable Credential**: a digital credential that can be cryptographically signed and verified.
- **Issuer**: The entity that issues VCs to holders
- **Holder**: The entity that is currently holding the VC
- **Subject**: An entity to which the credential is issued. Often the holder will be the subject of the credential.
- **Verifier**: An entity that receives and verifies the credential validity and credential ownership of the holder if
  necessary
- **Wallet**: a hardware/software that enables an issuer to manage credentials (create them, issue, revoke), a holder to
  receive and store credentials, and a verifier to verify them. A single wallet application can support all the
  functions.

There can be wallets for issuers, holders, and verifiers, accessible via mobile/ web apps, browser extensions, or
desktop applications. The wallet can enable the functionality of all parties (Issuer, verifier, holder) or be intended
to be used by only one party. One entity (person, organization) can be an issuer, verifier, or holder in different
scenarios if their wallet supports all these functionalities.
