# [3.0.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.6.0...v3.0.0) (2024-03-05)


### Bug Fixes

* adds env var for maven central publication ([#134](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/134)) ([d049f35](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/d049f35663b1a92cb31857ceea4b882611d0a5db))
* release pipeline variable ([#135](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/135)) ([bbc8a38](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/bbc8a3833c49d029a9440763b43c1d8957279e3e))
* chore!: update did peer library version  (#128) ([ea08251](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/ea082513a801e6eb4ac9384b35c074cf34082dbc)), closes [#128](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/128)


### BREAKING CHANGES

* Support OEA 1.26+ due to updates on the PeerDID Specs changes.
  Signed-off-by: Ahmed Moussa <ahmed.moussa@iohk.io>

# [2.7.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.6.0...v2.7.0) (2024-01-31)


### Bug Fixes

* **DIDCommWrapper:** Crash when body is empty string ([#124](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/124)) ([ed537f6](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/ed537f6a3a165f06b1fe3c0b919213ddbaa8e9fb))


### Features

* receive and process presentation request ([#120](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/120)) ([1194d11](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/1194d11b36cab08e5bfa080a60fd2611b6c0ea39))

# [2.6.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.5.0...v2.6.0) (2023-11-29)


### Bug Fixes

* ATL-5864 pr changes ([#109](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/109)) ([1e32bb7](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/1e32bb79c9ed1970cdd6797736830c3be60f8258))
* fixes demo app fetching job ([#110](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/110)) ([8485b34](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/8485b34f24d7cb1e73692aceb752657bc4a7175c))
* replace antlr with regex to solve did creation issues ([#114](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/114)) ([854fc70](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/854fc70b1c0271e1bf5f4b6a63433d41a9d35a1f))


### Features

* document the latest development ([#115](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/115)) ([d435148](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/d4351480a1ccbacea503e31edc7ebdc220653149))
* implement derivableKey an include it into Secp256k1PrivateKey ([#112](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/112)) ([cecbbb1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/cecbbb16be1d2423d5a33edad3b549be12c7ab91))
* Importable/Exportable keys ([#107](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/107)) ([adc2b6a](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/adc2b6a37e107d8f4c09268886123c690ac0ebed))
* KeyRestoration ([#111](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/111)) ([ccfb584](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/ccfb5840f5db9694abfcb097b779ce3257f1c9a6))

# [2.5.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.4.0...v2.5.0) (2023-11-20)


### Features

* mediator DID can be updated from sample app UI ([#106](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/106)) ([f20e1bb](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/f20e1bbe98ab6990da69d0c26e733c3a3dc1c604))

# [2.4.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.3.2...v2.4.0) (2023-10-26)


### Features

* anoncreds receive and store ([#91](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/91)) ([5a757dd](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/5a757dd7add814bdb35901fee1e4d0f9cef3c1ad))
* cryptographic abstraction and apollo integration ([#100](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/100)) ([0549258](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/0549258251a69eebc3f032466263575966dbad21))

## [2.3.2](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.3.1...v2.3.2) (2023-09-19)


### Bug Fixes

* bump version for prism-agent v1.16 compatibility ([#99](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/99)) ([238a17c](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/238a17ccbd60253d862bad043bf23fccd87772a0))

## [2.3.1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.3.0...v2.3.1) (2023-08-30)


### Bug Fixes

* **pollux:** fix JWTPayload serlization & Update Error Handling ([#97](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/97)) ([9eb3927](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/9eb39274c119b293c26dd4e81be674d06fda9fd4))

# [2.3.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.2.0...v2.3.0) (2023-08-30)


### Features

* **agent:** add extra headers that were missing ([#96](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/96)) ([eaf8b23](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/eaf8b233d05feddbaf96893d1b941a8604e5a5b7))

# [2.2.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.1.1...v2.2.0) (2023-08-29)


### Bug Fixes

* pr change request ATL-4965 ([#90](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/90)) ([023f285](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/023f285f5b75ac0305db8e8b3a9e8f6f11f5d6d2))


### Features

* credential abstraction ([#88](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/88)) ([4354341](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/435434147457022ba86a42f970b7170b082330e4))
* **mercury:** add extra headers and add to pickup return route ([#94](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/94)) ([ae8fe21](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/ae8fe2155647c61c91998cac8df20d3f82ae33d1))

## [2.1.1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.1.0...v2.1.1) (2023-07-27)


### Bug Fixes

* message model id generates a duplicity issue ([#86](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/86)) ([f00b53a](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/f00b53a5a14f5c8026ca18fbf4b984e13e361f43))
* override received message if already exists ([#87](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/87)) ([7cac37c](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/7cac37c1b39328d1ed5ed52f492e4fb316000908))

# [2.1.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.0.2...v2.1.0) (2023-07-26)


### Bug Fixes

* base64 attachments and http correct request headers ([#85](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/85)) ([ad223b9](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/ad223b9914836a46127d4e6db66d94d57b9c1076))
* delete unwanted comment from README.md ([#84](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/84)) ([47e4a0a](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/47e4a0a05d72a0c23b691d396e3b344b7bb49d02))
* make forward message and its body non internal ([#82](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/82)) ([ae2caa8](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/ae2caa80de37133a7c3bb3ba59f20ab414eca7e9))


### Features

* Update README.md ([#83](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/83)) ([df70182](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/df701825c3565eda0477fdb3cef2d189b0fa4436))

## [2.0.2](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.0.1...v2.0.2) (2023-06-27)


### Bug Fixes

* **enhancements:** add @JvmOverloads for JWTJsonPayload ([#81](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/81)) ([2a7fe15](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/2a7fe15db68c080be18421082bb0a24fbe5045a1))
* JWTJsonPayload fields are optional except ([#80](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/80)) ([e095c49](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/e095c4914a8754b8324e57069901ccf10ee9d8a3))

## [2.0.1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v2.0.0...v2.0.1) (2023-06-25)


### Bug Fixes

* **ATL-4978:** fix OOB connection ([#79](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/79)) ([cd18709](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/cd187096e57b1a45d8eae01fea789a255a31e4ed))

# [2.0.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/compare/v1.0.0...v2.0.0) (2023-06-21)


### Bug Fixes

* agent start and mediation achieved ([#60](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/60)) ([e24f67a](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/e24f67a682b4d93f701fb31d6a5163f16bc919ec))
* create peer DID with updateMediator false does not ignore provided services ([#73](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/73)) ([662c845](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/662c8456ef8fd3e9e1730d3ddde1c2b9869cc14a))
* credentials duplicated when stored localy ([#69](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/69)) ([f1b6518](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/f1b651892edc72cebee0a6174448a9a75443eba6))
* **docs:** Add general docs & code docs & CI ([a339641](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/a339641db1b1dadb37868342d587cf7bbaf8cc53))
* Fix dependencies to fully integrate with latest version of packages in prism-protos + protosLib. ([1ed30cf](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/1ed30cffe5020ac0581013dfce9f3e2be0aa6139))
* Fix key pair creation from private key for ED25519 ([#56](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/56)) ([a8af225](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/a8af22505274b872fd186b801bf4193e6bbf1b4d))
* Fix key pair creation from private key for X25519 ([#57](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/57)) ([1cfc294](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/1cfc2945c456e22829dd977d296ea64c1a73d0e0))
* Fix key pair creation from private key using SECP256K1 ([#55](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/55)) ([8b48aa1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/8b48aa17c8ab08ad7b15333cceb424ee45a85ff2))
* Implement test for key pair creation using mnemonics and seed for curve SECP256K1 ([#54](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/54)) ([026dc0d](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/026dc0df9969c7817333c99b0b20290298312130))
* kmm agent up to date with swift public apis ([#67](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/67)) ([7a65b3a](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/7a65b3a74a013d2adf222914a6c2ced814b8a0d2))
* move hardcoded values into constants ([#72](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/72)) ([4577ecf](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/4577ecf7521e00f12fd7b1e405b796aff00c370b))
* pick up messages and mark as read ([#63](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/63)) ([087bb88](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/087bb882f743ed606d9f34b032133c66345f8818))
* remove private key storage duplicity ([#75](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/75)) ([549bbeb](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/549bbeb49c90ed348fec2068a48c9fe4b211ce00))
* request and achieve mediation ([#62](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/62)) ([73f98c5](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/73f98c50e8c9c757611c333832f4ef5aba351262))
* **sdk:** replace GlobalScope with correct coroutine scope ([e44ac86](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/e44ac86c03819bed8500cac4427a9c2601fb3106))


### Features

* [ATL-2994] [Wallet SDK] Define domain interfaces and models ([#3](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/3)) ([1e4faf8](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/1e4faf8c13aa2860634c57e055607a6a34fb16ca))
* add logging component to the sdk ([#77](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/77)) ([57ca4f0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/57ca4f001e67a3f46b6e3b8604a9cc46fdc1faed))
* add protobuf-gradle-plugin ([8eaf852](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/8eaf85217314769d78145adb190c91d3ea84a9ef))
* **agent:** add mediation and ability to send messages ([f7b5d7f](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/f7b5d7ff169d9ed904e55456d863b993fe41a67f))
* **agent:** add mediation grant message ([d53119c](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/d53119cb96968cce523231bb3b6a815cde57d15c))
* **agent:** add mediator request message ([617640c](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/617640cece9ae1090fe76c78b6b712eabd0f050e))
* **agent:** add mediator request message ([#5](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/5)) ([60cfd13](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/60cfd1368dcfeb8b287cc71d12d93dcb45d1aca4))
* **agent:** add prism agent and create did functionalities ([431201b](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/431201bc15492f3a66f0bfe742b2d644a1465e17))
* **Agent:** Implement Credential Issue Protocol in EdgeAgent ([#27](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/27)) ([0f635f3](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/0f635f34c94e3446c8beb4e38d572a9b0dd36d8c))
* **agent:** Implementation Onboarding invitation on Agent ([#18](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/18)) ([c6188ec](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/c6188ec259eb3347a231d47cece51c37d71fe12d))
* **Agent:** Logic to parse out of band invitations ([#25](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/25)) ([85535c5](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/85535c5d82792f50fe89ae82ef15a561222b66d8))
* **Agent:** Persist key pairs into local storage ([#22](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/22)) ([7cc738f](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/7cc738fe7708ca5ab3d0418dc963967c8f1e0821))
* **Apollo:** Ed25519 key pair generation ([d11a7a1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/d11a7a1bff94aa20ba86ae1ad773408edc8f80d9))
* **build:** remove grpc dependencies and simplify protobufs ([7e700e1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/7e700e196e0cc64ca73a607a8fb5e41bb4584d6a))
* **castor:** Add peerDID Create method + tests. ([#15](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/15)) ([3b9b495](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/3b9b495219d7416c6b7f6756a612f7e99c063d82))
* **castor:** Add peerDID resolver + tests. ([#14](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/14)) ([462764a](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/462764ace3bc3a6eee48b8076593cb7f1963a076))
* **castor:** Resolve LongFormatPrismDIDs in Castor ([#23](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/23)) ([1331923](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/13319234cbc07e2315ae5702f5bd22cefaa930b8))
* **didparser:** Adding amtlr4 grammar did parser with specification and tests ([#10](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/10)) ([0d7dac1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/0d7dac11265bc7c3c5abc929f2e5b39e925243cd))
* **didUrlParser:** add did url parser and g4 grammar ([#12](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/12)) ([2ee6ad9](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/2ee6ad94a9d0e57f720768b1208d1843766ade8f))
* document models and make some classes internal ([#78](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/78)) ([e6b9c0b](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/e6b9c0b0267e26e460def5a5020a83fd423adfe2))
* Implement ED25519 - Keypair generation ([#52](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/52)) ([6c4eab1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/6c4eab1e85088d53bc643dad87e26e66361e5603))
* Implement sign and verify for Ed25519 ([#59](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/59)) ([b0086a1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/b0086a11d698a4d5c9b264f12e88278ac6acd96f))
* Implement X25519 - Keypair generation ([bbc2394](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/bbc23946a456b26bab22081d6b1de3b736e7fe96))
* Implement X25519 - Keypair generation ([#53](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/53)) ([be0aa7e](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/be0aa7e809aed587dd257433e1a6aaf1952d2c35))
* improve error handling ([#74](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/74)) ([c14f157](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/c14f1575b093097002bd6645766592d5c48000d4))
* integrate authenticate-sdk, buildSrc (Deps + Version globals), Protos and basic dependencies from old SDK ([1b0e6b5](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/1b0e6b5570856af2b2ad26f57c4a0daafb581233))
* **mercury:** add default secrets resolver ([#34](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/34)) ([21449a1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/21449a14f26fa92a0186241a69e20c80aa3ca4aa))
* **Mercury:** Orchestration and tests ([#49](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/49)) ([cc1313c](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/cc1313ced7537fdb1eadd3707ad3728371f0915d))
* **pluto db:** Implement db ([#13](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/13)) ([9a82003](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/9a820030fc4e0aa9bbbe475078182893a5baae36))
* **Pluto:** Add back flows to add reactiveness to the DB  ([#38](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/38)) ([17dde4d](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/17dde4d6f9bf6ee061b44c0c046c1c795223ea1f))
* **pluto:** Implementation of pluto ([#17](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/17)) ([e6c2ed1](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/e6c2ed1fa74f51fc8cdd8ebd2840dccd33d39def))
* **pollux:** add create credential request and presentation jwt string and prism agent higher functionality ([9a38c18](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/9a38c182ee91eb322194d5282348fdb2123196c1))
* **pollux:** add create credential request and presentation jwt string and prism agent higher functionality ([231387d](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/231387ddfcca5327e9070ff90a2538afc6c4f5ae))
* **Prism Agent:** Add connection data persistency ([#37](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/37)) ([ad5132b](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/ad5132bf8d832c78461db53504f088fa161a255e))
* **EdgeAgent:** Implement message signature ([#21](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/21)) ([fec99aa](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/fec99aadd34138452d6fa1429d3631b1d02b6175))
* project init ([29f62bb](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/29f62bbc8a6fd779c5c7b3db5a10427e84527c53))
* release first production version ([99ce9e0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/99ce9e0129cbed436e01f315a2479634ec45da03))
* Sample app with full flow ([#66](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/66)) ([77ab0e4](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/77ab0e4ef67b88a79698c525cf437377757838c3))
* Wallet SDK init ([4eeea4c](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/4eeea4c953e59961bcfe95534ca267a7834b97eb))
* wallet-core module init ([1f03a35](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/1f03a35540a6ca5bfb8c3ce3081c82cf2c6b3c11))
* X25519 ([#48](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/issues/48)) ([5ac155b](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/commit/5ac155b42f2ccf37d8413dfd6f49b63131714599))


### BREAKING CHANGES

* first release version

## [1.0.0](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm/tree/v1.0.0)

Repository initialization.
