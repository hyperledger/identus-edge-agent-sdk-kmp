# Module EdgeAgentSDK

Edge Agent KMP SDK is a library and documentation that helps developers build KMM/JVM/Android SSI (self-sovereign
identity) applications with Identus. This documentation will explain how to use the SDK in your project, how to
prepare your development environment if you wish to contribute, and some fundamental considerations around the project.

## What is Identus?

Identus is a self-sovereign identity (SSI) platform and service suite for verifiable data and digital identity.
Built on Cardano, it offers the core infrastructure for issuing DIDs and verifiable credentials alongside tools and
frameworks to help expand your ecosystem.

## Technical Considerations

The architecture of the SDK results from a careful evaluation of different software development methodologies and
patterns. We chose a modular, clean architecture based on interface/protocol-oriented programming, domain-oriented
programming principles, and dependency injection for several reasons that we will explain in this document.

### Overview

#### Modular Clean Architecture

Modular clean architecture is a software development methodology that emphasizes separating concerns and the creation of
independent modules that can be easily tested and maintained. This approach promotes using small, reusable components
that can be combined differently to create larger systems. The SDK architecture uses this approach to ensure that each
module can be developed and tested independently, reducing the risk of bugs and improving the overall quality of the
code.

#### Interface/Protocol-Oriented Programming

Protocol-oriented programming is a programming paradigm that focuses on the behaviour of objects rather than their
structure. This approach promotes the use of protocols to define the behaviour of objects, allowing for more flexible
and extensible code. The SDK architecture uses this approach to ensure the different modules can work together
seamlessly, regardless of the underlying implementation details.

#### Domain-Oriented Programming

Domain-oriented programming is a programming paradigm that focuses on a system's domain-specific requirements rather
than the implementation's technical details. This approach promotes the use of domain-specific models and concepts,
which can simplify the development process and improve the maintainability of the code. The SDK architecture uses this
approach to ensure that the different modules are designed around the specific needs of decentralized identity
management, making it easier for developers to build decentralized applications that are secure and scalable.

#### Dependency Injection

Dependency injection is a programming pattern that promotes loose coupling between different system components. This
approach encourages the use of interfaces and dependency injection containers to ensure that each element can be
developed and tested independently without relying on the implementation details of other components. The SDK
architecture uses this approach to ensure that each module can be developed and tested separately, making it easier for
developers to add new functionality to the system without affecting the existing code.

## Building Blocks

The building blocks are the core components of Identus, and they are designed to work together seamlessly to provide
a comprehensive identity management solution.

### Overview

Each building block serves a specific purpose, providing a solid foundation for building decentralized identity
applications.

Let's take a closer look at each building block:

- **Apollo**: Apollo is a building block that provides a suite of cryptographic operations. This includes secure hash
  algorithms, digital signatures, and encryption, all essential for creating a safe and tamper-proof identity system.
  Apollo ensures that all data within the Identus system is securely encrypted and digitally signed, making it
  resistant to tampering and unauthorized access.
- **Castor**: Castor is a building block that provides a suite of decentralized identifier (DID) operations in a
  user-controlled manner. DIDs are a vital component of decentralized identity, as they provide a way to uniquely
  identify individuals and entities in a decentralized manner. Castor allows users to create, manage, and control their
  DIDs and associated cryptographic keys.
- **Pollux**: Pollux is a building block that provides a suite of credential operations in a privacy-preserving manner.
  Credentials are a way to prove claims about an individual or entity, and they are an essential part of decentralized
  identity. Pollux allows users to create, manage, and share credentials in a privacy-preserving way to ensure that
  sensitive information is not revealed.
- **Mercury**: Mercury is a building block that provides a set of secure, standards-based communications protocols that
  are transport-agnostic and interoperable. Mercury allows different Identus components to communicate securely
  using HTTP, WebSocket, and MQTT protocols.
- **Pluto**: Pluto is a building block that provides an interface for storage operations in a portable, storage-agnostic
  manner. Pluto allows data to be stored and retrieved in a way independent of the underlying storage technology,
  allowing Identus to work with various storage solutions.

Together, these building blocks provide a solid foundation for building decentralized identity applications that are
secure, privacy-preserving, and interoperable. Using Identus, developers can focus on creating innovative identity
solutions without worrying about the underlying infrastructure.

### Edge Agent

Edge Agent is a comprehensive library that combines all the Prism platform's building blocks - Apollo, Castor, Pluto,
Mercury, and Pollux - to provide a seamless experience for developers working with decentralized identifiers (DIDs) on
the Identus platform.

## Documentation

#### General information and articles

- [Getting Started](https://docs.atalaprism.io/docs/getting-started)
- [What is identity?](https://docs.atalaprism.io/docs/concepts/what-is-identity)
- [Digital wallets](https://docs.atalaprism.io/docs/concepts/digital-wallets)
- [Identus Overview](https://docs.atalaprism.io/docs/atala-prism/overview)
