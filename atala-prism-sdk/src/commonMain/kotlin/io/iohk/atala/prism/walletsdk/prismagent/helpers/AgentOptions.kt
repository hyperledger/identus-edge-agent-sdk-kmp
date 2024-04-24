package io.iohk.atala.prism.walletsdk.prismagent.helpers

data class AgentOptions(val experiments: Experiments = Experiments())

data class Experiments(val liveMode: Boolean = false)
