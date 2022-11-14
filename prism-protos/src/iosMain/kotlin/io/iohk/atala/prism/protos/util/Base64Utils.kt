package io.iohk.atala.prism.protos.util

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.posix.memcpy

public actual object Base64Utils {
    public actual fun encode(bytes: ByteArray): String {
        val nsData = memScoped {
            NSData.create(
                bytes = allocArrayOf(bytes),
                length = bytes.size.convert()
            )
        }
        val base64Encoded = nsData.base64EncodedStringWithOptions(0)
        // Make Base64 representation URL-safe
        return base64Encoded.replace('/', '_').replace('+', '-').dropLastWhile { it == '=' }
    }

    public actual fun decode(src: String): ByteArray {
        val expectedLength = (src.length + 3) / 4 * 4
        val base64encoded =
            src.replace('_', '/').replace('-', '+').padEnd(expectedLength, '=')
        val data = NSData.create(base64encoded, 0)!!
        return ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
    }
}
