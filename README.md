# Atala Prism Wallet SDK - Kotlin Multiplatform (Android/JVM)

[![Kotlin](https://img.shields.io/badge/kotlin-1.9.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
![android](https://camo.githubusercontent.com/b1d9ad56ab51c4ad1417e9a5ad2a8fe63bcc4755e584ec7defef83755c23f923/687474703a2f2f696d672e736869656c64732e696f2f62616467652f706c6174666f726d2d616e64726f69642d3645444238442e7376673f7374796c653d666c6174)
![jvm](https://camo.githubusercontent.com/700f5dcd442fd835875568c038ae5cd53518c80ae5a0cf12c7c5cf4743b5225b/687474703a2f2f696d672e736869656c64732e696f2f62616467652f706c6174666f726d2d6a766d2d4442343133442e7376673f7374796c653d666c6174)

![Atala Prism Logo](Logo.png)

# Welcome to Atala PRISM Kotlin multiplatform SDK

The following will explain how to use the SDK in your project, how to prepare your development environment if you wish to contribute and some basic considerations around the project.

This SDK provides a library and documentation for developers to build Android and JVM connected SSI applications with Atala PRISM.

## Basic considerations

### Atala PRISM

Atala PRISM is a self-sovereign identity (SSI) platform and service suite for verifiable data and digital identity. Built on Cardano, it offers core infrastructure for issuing DIDs (Decentralized identifiers) and verifiable credentials, alongside tools and frameworks to help expand your ecosystem.
The complete platform is separated in multiple repositories:

* [atala-prism-wallet-sdk-swift](https://github.com/input-output-hk/atala-prism-wallet-sdk-swift) - Repo that implements Atala PRISM for Apple platforms in Swift.
* [atala-prism-wallet-sdk-ts](https://github.com/input-output-hk/atala-prism-wallet-sdk-ts) - Repo that implements Atala PRISM for Browser and Node.js platforms in Typescript.
* [atala-prism-building-blocks](https://github.com/hyperledger-labs/open-enterprise-agent) - Repo that contains the platform Building Blocks.
* [atala-prism-mediator](https://github.com/input-output-hk/atala-prism-mediator) - Repo for DIDComm V2 Mediator

### Modules / APIs

Atala PRISM Kotlin multiplatform SDK provides the following building blocks to create, manage and resolve decentralized identifiers, issue, manage and verify verifiable credentials, establish and manage trusted, peer-to-peer connections and interactions between DIDs, and store, manage, and recover verifiable data linked to DIDs.

* __Apollo__: Building block that provides a suite of cryptographic operations.
* __Castor__: Building block that provides a suite of DID operations in a user-controlled manner.
* __Pollux__: Building block that provides a suite of credential operations in a privacy-preserving manner.
* __Mercury__: Building block that provides a set of secure, standards-based communications protocols in a transport-agnostic and interoperable manner.
* __Pluto__: Building block that provides an interface for storage operations in a portable, storage-agnostic manner.
* __PrismAgent__: PrismAgent using all the building blocks provides an agent that can provide a set of high level DID functionalities.

## Getting started

### Setup

To get started with the Atala PRISM kotlin multiplatform SDK, you can set up the SDK and start a new project or integrate it into an existing project. Before you start, make sure you have the following installed on your development machine:

- Android: API level 21 and above.
- Kotlin 1.9.10 or later.
- JVM: 11 or later.

### Integrating the SDK in an existing project

To integrate the SDK into an existing project, you have to import the SDK into your project:

`implementation("io.iohk.atala.prism:walletsdk:$sdk_version")`
