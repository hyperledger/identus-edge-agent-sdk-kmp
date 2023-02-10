package io.iohk.atala.prism.walletsdk.prismagent.shared
fun mapFromKeyValueArray(array: Array<KeyValue>): Map<String, String> {
    val response = mutableMapOf<String, String>()
    array.forEach {
        response[it.key] = it.value
    }
    return response
}
