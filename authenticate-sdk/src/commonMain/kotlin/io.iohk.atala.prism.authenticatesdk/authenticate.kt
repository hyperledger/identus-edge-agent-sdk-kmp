package io.iohk.atala.prism.authenticatesdk

expect fun authenticate(did: String, signature: String, originalText: String): String
