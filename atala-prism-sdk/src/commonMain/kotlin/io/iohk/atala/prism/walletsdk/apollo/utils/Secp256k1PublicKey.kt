package io.iohk.atala.prism.walletsdk.apollo.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.base63.toJavaBigInteger
import io.iohk.atala.prism.apollo.ecdsa.ECDSAType
import io.iohk.atala.prism.apollo.ecdsa.KMMECDSA
import io.iohk.atala.prism.apollo.utils.ECPublicKeyInitializationException
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PublicKey
import io.iohk.atala.prism.apollo.utils.KMMEllipticCurve
import io.iohk.atala.prism.walletsdk.apollo.config.ECConfig
import io.iohk.atala.prism.walletsdk.apollo.utils.ec.KMMECPoint
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CustomKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.VerifiableKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import java.security.KeyFactory
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import kotlin.experimental.and

class Secp256k1PublicKey(nativeValue: ByteArray) : PublicKey(), VerifiableKey {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        if (size == ECConfig.PUBLIC_KEY_COMPRESSED_BYTE_SIZE) {
            keySpecification[CustomKey("compressed").property] = "true"
        } else {
            keySpecification[CustomKey("compressed").property] = "false"
        }
        keySpecification[CurveKey().property] = Curve.SECP256K1.value
    }

    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromBytes(raw)
        return KMMECDSA.verify(
            type = ECDSAType.ECDSA_SHA256,
            data = message,
            publicKey = kmmPublicKey,
            signature = signature
        )
    }

    fun isCompressed(): Boolean {
        return (
            keySpecification.containsKey("compressed") &&
                keySpecification[CustomKey("compressed").property] == "true"
            )
    }

    fun getEncodedCompressed(): ByteArray {
        if (isCompressed()) {
            return raw
        }
        val size = io.iohk.atala.prism.apollo.utils.ECConfig.PRIVATE_KEY_BYTE_SIZE
        val curvePoint = computeCurvePoint(bcecpublicKeyFromSecp256K1())
        val yArr = curvePoint.y.bytes()
        val xArr = curvePoint.x.bytes()
        val prefix = 2 + (yArr[yArr.size - 1] and 1)
        val arr = ByteArray(1 + size)
        arr[0] = prefix.toByte()
        xArr.copyInto(arr, 1)
        return arr
    }

    private fun bcecpublicKeyFromSecp256K1(): BCECPublicKey {
        val ecParameterSpec = ECNamedCurveTable.getParameterSpec(KMMEllipticCurve.SECP256k1.value)
        val ecNamedCurveSpec: ECParameterSpec = ECNamedCurveSpec(
            ecParameterSpec.name,
            ecParameterSpec.curve,
            ecParameterSpec.g,
            ecParameterSpec.n
        )

        val bouncyCastlePoint = ecParameterSpec.curve.decodePoint(raw)
        val point =
            ECPoint(bouncyCastlePoint.xCoord.toBigInteger(), bouncyCastlePoint.yCoord.toBigInteger())
        val spec = ECPublicKeySpec(point, ecNamedCurveSpec)
        val provider = BouncyCastleProvider()
        val keyFactory = KeyFactory.getInstance("EC", provider)
        return keyFactory.generatePublic(spec) as BCECPublicKey
    }

    companion object {
        fun computeCurvePoint(key: BCECPublicKey): KMMECPoint {
            val javaPoint = key.w
            return KMMECPoint(javaPoint.affineX.toKotlinBigInteger(), javaPoint.affineY.toKotlinBigInteger())
        }

        fun secp256k1FromCompressed(compressed: ByteArray): Secp256k1PublicKey {
            require(compressed.size == ECConfig.PUBLIC_KEY_COMPRESSED_BYTE_SIZE) {
                "Compressed byte array's expected length is ${ECConfig.PUBLIC_KEY_COMPRESSED_BYTE_SIZE}, but got ${compressed.size}"
            }
            val ecParameterSpec = ECNamedCurveTable.getParameterSpec(KMMEllipticCurve.SECP256k1.value)
            val bouncyCastlePoint = ecParameterSpec.curve.decodePoint(compressed)
            val point =
                ECPoint(bouncyCastlePoint.xCoord.toBigInteger(), bouncyCastlePoint.yCoord.toBigInteger())
            return secp256k1FromBigIntegerCoordinates(
                point.affineX.toKotlinBigInteger(),
                point.affineY.toKotlinBigInteger()
            )
        }

        fun secp256k1FromBigIntegerCoordinates(x: BigInteger, y: BigInteger): Secp256k1PublicKey {
            val ecPoint = ECPoint(x.toJavaBigInteger(), y.toJavaBigInteger())
            if (!KMMECSecp256k1PublicKey.isPointOnSecp256k1Curve(KMMECPoint(x, y))) {
                throw ECPublicKeyInitializationException("ECPoint corresponding to a public key doesn't belong to Secp256k1 curve")
            }
            val ecParameterSpec = ECNamedCurveTable.getParameterSpec(KMMEllipticCurve.SECP256k1.value)
            val ecNamedCurveSpec: ECParameterSpec = ECNamedCurveSpec(
                ecParameterSpec.name,
                ecParameterSpec.curve,
                ecParameterSpec.g,
                ecParameterSpec.n
            )
            val spec = ECPublicKeySpec(ecPoint, ecNamedCurveSpec)
            val provider = BouncyCastleProvider()
            val keyFactory = KeyFactory.getInstance("EC", provider)
            val publicKey = keyFactory.generatePublic(spec) as BCECPublicKey

            return Secp256k1PublicKey(publicKey.encoded)
        }
    }
}
