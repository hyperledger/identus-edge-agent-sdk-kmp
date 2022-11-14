package io.iohk.atala.prism.protos.util

import java.util.*

public actual object Base64Utils {
    public actual fun encode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    public actual fun decode(src: String): ByteArray =
        Base64.getUrlDecoder().decode(src)
}
