package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.prismagent.helpers.Api
import io.ktor.http.HttpStatusCode

class AgentApiMock(
    statusCode: HttpStatusCode,
    response: String
) : PrismAgent(ApolloMock(), CastorMock(), PlutoMock()) {
    override val api: Api = ApiMock(statusCode, response)
}
