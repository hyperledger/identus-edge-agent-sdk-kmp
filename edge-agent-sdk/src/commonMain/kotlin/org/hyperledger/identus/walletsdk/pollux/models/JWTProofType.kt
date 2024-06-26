package org.hyperledger.identus.walletsdk.pollux.models

enum class JWTProofType(val value: String) {
    ECDSASECP256K1Signature2019("EcdsaSecp256k1Signature2019"),
    DataIntegrityProof("DataIntegrityProof"),
    Unknown("Unknown")
}

enum class VerificationKeyType(val value: String) {
    Ed25519VerificationKey2018("Ed25519VerificationKey2018"),
    Ed25519VerificationKey2020("Ed25519VerificationKey2020"),
    X25519KeyAgreementKey2019("X25519KeyAgreementKey2019"),
    X25519KeyAgreementKey2020("X25519KeyAgreementKey2020"),
    EcdsaSecp256k1VerificationKey2019("EcdsaSecp256k1VerificationKey2019")
}
