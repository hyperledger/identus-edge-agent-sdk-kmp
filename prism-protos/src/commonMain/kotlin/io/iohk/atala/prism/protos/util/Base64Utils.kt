package io.iohk.atala.prism.protos.util

// TODO("Use Apollo Base64")
public expect object Base64Utils {
    /**
     * Returns a URL-safe Base64 encoding without padding (i.e. no trailing '='s).
     */
    public fun encode(bytes: ByteArray): String

    /**
     * Decodes a URL-safe Base64 encoding without padding into a list of bytes.
     */
    public fun decode(src: String): ByteArray
}
