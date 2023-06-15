package it.pagopa.aca.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class WebClientConfigTest {

    private val webClientConfig = WebClientConfig()

    private val apiKey = "apiKey"
    private val readTimeout = 1000
    private val connectionTimeout = 1000
    private val baseUrl = "http://creditorInstitutionUrl"

    @Test
    fun `Should build Api config client successfully`() {
        val creditorInstitutionsApi =
            webClientConfig.ibansApiClient(
                baseUrl = baseUrl,
                apiKey = apiKey,
                readTimeout = readTimeout,
                connectionTimeout = connectionTimeout
            )
        val authKey = creditorInstitutionsApi.apiClient.getAuthentication("ApiKey")
        assertNotNull(authKey)
        assertEquals(apiKey, (authKey as it.pagopa.generated.apiconfig.auth.ApiKeyAuth).apiKey)
        assertEquals(baseUrl, creditorInstitutionsApi.apiClient.basePath)
    }

    @Test
    fun `Should build Gpd client successfully`() {
        val gpdApi =
            webClientConfig.gpdClient(
                baseUrl = baseUrl,
                apiKey = apiKey,
                readTimeout = readTimeout,
                connectionTimeout = connectionTimeout
            )
        val authKey = gpdApi.apiClient.getAuthentication("ApiKey")
        assertNotNull(authKey)
        assertEquals(apiKey, (authKey as it.pagopa.generated.gpd.auth.ApiKeyAuth).apiKey)
        assertEquals(baseUrl, gpdApi.apiClient.basePath)
    }
}
