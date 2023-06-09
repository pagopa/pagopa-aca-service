package it.pagopa.aca.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.pagopa.aca.config.WebClientConfig
import it.pagopa.aca.creditorInstitutionResponseBody
import it.pagopa.aca.exceptions.ApiConfigException
import java.util.stream.Stream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpStatus

@OptIn(ExperimentalCoroutinesApi::class)
class CreditorInstitutionClientTest {

    companion object {

        val creditorInstitutionCode = "123456"

        lateinit var mockWebServer: MockWebServer

        val objectMapper: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

        @JvmStatic
        @BeforeAll
        fun setup() {
            mockWebServer = MockWebServer()
            mockWebServer.start(8080)
            println("Mock web server started on ${mockWebServer.hostName}:${mockWebServer.port}")
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            mockWebServer.shutdown()
            println("Mock web stopped")
        }

        @JvmStatic
        private fun errorsProvider() =
            Stream.of(
                Arguments.of(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Bad request"),
                Arguments.of(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"
                ),
                Arguments.of(
                    HttpStatus.FORBIDDEN,
                    HttpStatus.BAD_GATEWAY,
                    "Bad gateway, api config forbidden getCreditorInstitutionsIbans method"
                ),
                Arguments.of(
                    HttpStatus.NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "Creditor institution code: $creditorInstitutionCode not found"
                ),
                Arguments.of(
                    HttpStatus.TOO_MANY_REQUESTS,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests"
                ),
                Arguments.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "Bad gateway, api config internal server error"
                ),
                Arguments.of(
                    HttpStatus.CONFLICT,
                    HttpStatus.BAD_GATEWAY,
                    "Api config error: ${HttpStatus.CONFLICT}"
                ),
            )
    }

    private val creditorInstitutionsApi =
        WebClientConfig()
            .creditorInstitutionsClient(
                baseUrl = "http://${mockWebServer.hostName}:${mockWebServer.port}",
                apiKey = "apiKey",
                connectionTimeout = 1000,
                readTimeout = 1000
            )

    private val creditorInstitutionClient = CreditorInstitutionClient(creditorInstitutionsApi)

    @Test
    fun `Should retrieve creditor institution iban successfully`() = runTest {
        // pre-conditions
        val mockedResponse = creditorInstitutionResponseBody()
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(mockedResponse))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        )
        // test
        val (iban, companyName) =
            creditorInstitutionClient.getIban(creditorInstitutionCode, "requestId")
        // assertions
        assertEquals(mockedResponse.ibans[0].iban, iban)
        assertEquals(mockedResponse.ibans[0].companyName, companyName)
    }

    @ParameterizedTest
    @MethodSource("errorsProvider")
    fun `Should handle api config errors`(
        apiConfigErrorCode: HttpStatus,
        expectedStatusCode: HttpStatus,
        expectedDescription: String
    ) = runTest {
        // pre-conditions
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{}")
                .setResponseCode(apiConfigErrorCode.value())
                .addHeader("Content-Type", "application/json")
        )
        // test
        val exception =
            assertThrows<ApiConfigException> {
                creditorInstitutionClient.getIban(creditorInstitutionCode, "requestId")
            }
        assertEquals(expectedStatusCode, exception.toRestException().httpStatus)
        assertEquals(expectedDescription, exception.toRestException().description)
    }
}
