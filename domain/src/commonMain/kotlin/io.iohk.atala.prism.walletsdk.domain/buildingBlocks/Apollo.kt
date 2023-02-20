package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.CompressedPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.SeedWords
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Apollo {

    fun createRandomMnemonics(): Array<String>

    fun createSeed(mnemonics: Array<String>, passphrase: String): Seed

    fun createRandomSeed(passphrase: String? = ""): SeedWords

    @JsName("createKeyPairFromKeyCurve")
    fun createKeyPair(seed: Seed, curve: KeyCurve): KeyPair

    @JsName("createKeyPairFromPrivateKey")
    fun createKeyPair(seed: Seed, privateKey: PrivateKey): KeyPair

    @JsName("compressedPublicKeyFromPublicKey")
    fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey

    @JsName("compressedPublicKeyFromCompresedData")
    fun compressedPublicKey(compressedData: ByteArray): CompressedPublicKey

    @JsName("publicKeyFromPoints")
    fun publicKey(curve: KeyCurve, x: ByteArray, y: ByteArray): PublicKey

    @JsName("publicKeyFromPoint")
    fun publicKey(curve: KeyCurve, x: ByteArray): PublicKey

    @JsName("signByteArrayMessage")
    fun signMessage(privateKey: PrivateKey, message: ByteArray): Signature

    @JsName("signStringMessage")
    fun signMessage(privateKey: PrivateKey, message: String): Signature

    fun verifySignature(publicKey: PublicKey, challenge: ByteArray, signature: Signature): Boolean
}
