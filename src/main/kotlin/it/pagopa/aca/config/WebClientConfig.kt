package it.pagopa.aca.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean(name = ["creditorInstitutionsClient"])
    fun creditorInstitutionsClient(
        @Value("\${apiConfig.creditorInstitutions.uri}") baseUrl: String,
        @Value("\${apiConfig.creditorInstitutions.readTimeout}") readTimeout: Int,
        @Value("\${apiConfig.creditorInstitutions.connectionTimeout}") connectionTimeout: Int,
        @Value("\${apiConfig.apiConfig.creditorInstitutions.apiKey}") apiKey: String
    ): it.pagopa.generated.apiconfig.api.CreditorInstitutionsApi {
        val httpClient =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .doOnConnected { connection: Connection ->
                    connection.addHandlerLast(
                        ReadTimeoutHandler(readTimeout.toLong(), TimeUnit.MILLISECONDS)
                    )
                }
        val webClient =
            it.pagopa.generated.apiconfig.ApiClient.buildWebClientBuilder()
                .clientConnector(ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build()
        val apiClient = it.pagopa.generated.apiconfig.ApiClient(webClient).setBasePath(baseUrl)
        apiClient.setApiKey(apiKey)
        return it.pagopa.generated.apiconfig.api.CreditorInstitutionsApi(apiClient)
    }
}
