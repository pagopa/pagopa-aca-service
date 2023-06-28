package it.pagopa.aca.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean(name = ["ibansApiClient"])
    fun ibansApiClient(
        @Value("\${apiConfig.ibans.uri}") baseUrl: String,
        @Value("\${apiConfig.ibans.readTimeout}") readTimeout: Int,
        @Value("\${apiConfig.ibans.connectionTimeout}") connectionTimeout: Int,
        @Value("\${apiConfig.ibans.apiKey}") apiKey: String
    ): it.pagopa.generated.apiconfig.api.IbansApi {
        val webClient =
            buildWebClient(
                baseUrl = baseUrl,
                readTimeout = readTimeout,
                connectionTimeout = connectionTimeout,
                it.pagopa.generated.apiconfig.ApiClient.buildWebClientBuilder()
            )
        val apiClient = it.pagopa.generated.apiconfig.ApiClient(webClient).setBasePath(baseUrl)
        apiClient.setApiKey(apiKey)
        return it.pagopa.generated.apiconfig.api.IbansApi(apiClient)
    }

    @Bean(name = ["gpdApiClient"])
    fun gpdClient(
        @Value("\${gpd.debitPosition.uri}") baseUrl: String,
        @Value("\${gpd.debitPosition.readTimeout}") readTimeout: Int,
        @Value("\${gpd.debitPosition.connectionTimeout}") connectionTimeout: Int,
        @Value("\${gpd.debitPosition.apiKey}") apiKey: String
    ): it.pagopa.generated.gpd.api.DebtPositionsApiApi {
        val webClient =
            buildWebClient(
                baseUrl = baseUrl,
                readTimeout = readTimeout,
                connectionTimeout = connectionTimeout,
                it.pagopa.generated.gpd.ApiClient.buildWebClientBuilder()
            )
        val apiClient = it.pagopa.generated.gpd.ApiClient(webClient).setBasePath(baseUrl)

        apiClient.setApiKey(apiKey)
        return it.pagopa.generated.gpd.api.DebtPositionsApiApi(apiClient)
    }

    @Bean(name = ["gpdApiClientActions"])
    fun gpdClientForInvalidate(
        @Value("\${gpd.debitPosition.uri}") baseUrl: String,
        @Value("\${gpd.debitPosition.readTimeout}") readTimeout: Int,
        @Value("\${gpd.debitPosition.connectionTimeout}") connectionTimeout: Int,
        @Value("\${gpd.debitPosition.apiKey}") apiKey: String
    ): it.pagopa.generated.gpd.api.DebtPositionActionsApiApi {
        val webClient =
            buildWebClient(
                baseUrl = baseUrl,
                readTimeout = readTimeout,
                connectionTimeout = connectionTimeout,
                it.pagopa.generated.gpd.ApiClient.buildWebClientBuilder()
            )
        val apiClient = it.pagopa.generated.gpd.ApiClient(webClient).setBasePath(baseUrl)
        apiClient.setApiKey(apiKey)
        return it.pagopa.generated.gpd.api.DebtPositionActionsApiApi(apiClient)
    }

    private fun buildWebClient(
        baseUrl: String,
        readTimeout: Int,
        connectionTimeout: Int,
        webClientBuilder: WebClient.Builder
    ): WebClient {
        val httpClient =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .doOnConnected { connection: Connection ->
                    connection.addHandlerLast(
                        ReadTimeoutHandler(readTimeout.toLong(), TimeUnit.MILLISECONDS)
                    )
                }
        return webClientBuilder
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(baseUrl)
            .build()
    }
}
