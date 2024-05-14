# Package org.hyperledger.identus.walletsdk.pluto

Pluto is a powerful and flexible data storage interface library for working with decentralized identifiers. Whether you
are building a decentralized application that requires secure and private data storage or working with a more
traditional system that requires reliable and redundant storage for your DID-related data, Pluto provides the tools and
features you need to do the job with ease.

## A Secure DID Data Storage Interface Library

Pluto is a versatile and secure data storage interface library that provides developers with an easy way to store,
manage, and recover verifiable data linked to decentralized identifiers (DIDs). Pluto is designed to be
storage-agnostic. It can be used with many storage systems, from traditional databases to decentralized storage
networks.

Pluto supports a range of storage operations, including DID storage (including DIDPair), private key secure storage,
DIDComm message storage, and mediator storage. This allows developers to easily store and manage a wide range of
DID-related data, from basic identity information to more complex message and mediation records.

The default implementation of Pluto includes a secure and robust storage backend that uses Core Data and Keychain to
secure sensitive items. This means that developers can easily leverage the robust security features of these frameworks
without having to write complex code to manage the storage of sensitive data.

With Pluto, developers can easily store and manage verifiable data linked to DIDs securely and privately. Pluto provides
a range of advanced security features, including support for private key storage and encryption and support for multiple
storage backends for added redundancy and resilience.

Pluto also supports a range of DID-related data types, including DIDPairs, which store the public and private keys
associated with a DID, and DIDComm messages, which store the encrypted messages sent and received between DIDs.
