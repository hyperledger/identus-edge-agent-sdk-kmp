package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.prismagent.helpers.Api
import io.iohk.atala.prism.walletsdk.prismagent.helpers.ApiImpl
import io.iohk.atala.prism.walletsdk.prismagent.helpers.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@JsExport
actual class PrismAgent actual constructor(
    apollo: Apollo,
    castor: Castor,
    pluto: Pluto,
    seed: Seed?,
    api: Api?
) {
    actual enum class State {
        STOPED, STARTING, RUNNING, STOPING
    }

    actual var state: State = State.STOPED
    actual val seed: Seed
    actual val apollo: Apollo
    actual val castor: Castor
    actual val pluto: Pluto

    private val api: Api

    init {
        this.apollo = apollo
        this.castor = castor
        this.pluto = pluto
        this.seed = seed ?: apollo.createRandomSeed().second
        this.api = api ?: ApiImpl(
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            isLenient = true
                        }
                    )
                }
            }
        )
    }
}
