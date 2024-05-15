# Identus Wallet SDK - Kotlin Multiplatform (Android/JVM)

![badge-platform-android]
![badge-platform-jvm]
[![Coverage Status](https://coveralls.io/repos/github/input-output-hk/atala-prism-wallet-sdk-kmm/badge.svg?branch=main)](https://coveralls.io/github/input-output-hk/atala-prism-wallet-sdk-kmm?branch=main)

# Welcome to Identus Kotlin multiplatform SDK

The following will explain how to use the SDK in your project, how to prepare your development environment if you wish to contribute and some basic considerations around the project.

This SDK provides a library and documentation for developers to build Android and JVM connected SSI applications with Identus.

## Basic considerations

### Identus

Identus is a self-sovereign identity (SSI) platform and service suite for verifiable data and digital identity. Built on Cardano, it offers core infrastructure for issuing DIDs (Decentralized identifiers) and verifiable credentials, alongside tools and frameworks to help expand your ecosystem.
The complete platform is separated in multiple repositories:

* [atala-prism-wallet-sdk-swift](https://github.com/input-output-hk/atala-prism-wallet-sdk-swift) - Repo that implements Identus for Apple platforms in Swift.
* [atala-prism-wallet-sdk-ts](https://github.com/input-output-hk/atala-prism-wallet-sdk-ts) - Repo that implements Identus for Browser and Node.js platforms in Typescript.
* [atala-prism-building-blocks](https://github.com/hyperledger-labs/open-enterprise-agent) - Repo that contains the platform Building Blocks.
* [atala-prism-mediator](https://github.com/input-output-hk/atala-prism-mediator) - Repo for DIDComm V2 Mediator

### Modules / APIs

Identus Kotlin multiplatform SDK provides the following building blocks to create, manage and resolve decentralized identifiers, issue, manage and verify verifiable credentials, establish and manage trusted, peer-to-peer connections and interactions between DIDs, and store, manage, and recover verifiable data linked to DIDs.

* __Apollo__: Building block that provides a suite of cryptographic operations.
* __Castor__: Building block that provides a suite of DID operations in a user-controlled manner.
* __Pollux__: Building block that provides a suite of credential operations in a privacy-preserving manner.
* __Mercury__: Building block that provides a set of secure, standards-based communications protocols in a transport-agnostic and interoperable manner.
* __Pluto__: Building block that provides an interface for storage operations in a portable, storage-agnostic manner.
* __EdgeAgent__: EdgeAgent using all the building blocks provides an agent that can provide a set of high level DID functionalities.

## Getting started

### Setup

To get started with the Identus kotlin multiplatform SDK, you can set up the SDK and start a new project or integrate it into an existing project. Before you start, make sure you have the following installed on your development machine:

- Android: API level 21 and above.
- Kotlin 1.9.22 or later.
- JVM: 17 or later.

### Integrating the SDK in an existing project

To integrate the SDK into an existing project, you have to import the SDK into your project:

```kotlin
implementation("org.hyperledger.identus:edge-agent-sdk:<latest version>")
```

<!-- TAG_PLATFORMS -->
[badge-platform-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat
[badge-platform-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat
