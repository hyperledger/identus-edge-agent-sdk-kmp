package io.iohk.atala.prism.walletsdk.prismagent.helpers

@OptIn(ExperimentalJsExport::class)
@JsExport
/**
 *
 * This class has no useful logic; it will help making a version of Kotlin *Pair* available in js.
 *
 * @param T the type of the first member.
 * @param K the type of the second member.
 */
data class KMMPair<T, K>(val first: T, val second: K)
