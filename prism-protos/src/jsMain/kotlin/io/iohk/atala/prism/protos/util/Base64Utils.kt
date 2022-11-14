package io.iohk.atala.prism.protos.util

public actual object Base64Utils {
    // https://regexland.com/base64/
    private val base64Regex = """^(?:[A-Za-z\d+/]{4})*(?:[A-Za-z\d+/]{3}=|[A-Za-z\d+/]{2}==)?$""".toRegex()

    private val bufferImport = js("require('buffer')")

    public actual fun encode(bytes: ByteArray): String {
        val buffer = bufferImport.Buffer.from(bytes)
        val result = buffer.toString("base64") as String
        return result.replace('/', '_').replace('+', '-').dropLastWhile { it == '=' }
    }

    public actual fun decode(src: String): ByteArray {
        val expectedLength = (src.length + 3) / 4 * 4
        val base64encoded =
            src.replace('_', '/').replace('-', '+').padEnd(expectedLength, '=')
        if (!base64Regex.matches(base64encoded)) {
            throw Exception("\"$base64encoded\" is not a valid base64 string")
        }
        val decoded = bufferImport.Buffer.from(base64encoded, "base64")
        val result = ByteArray(decoded.length as Int)
        for (i in result.indices) {
            result[i] = (decoded[i] as Int).toByte()
        }
        return result
    }
}
