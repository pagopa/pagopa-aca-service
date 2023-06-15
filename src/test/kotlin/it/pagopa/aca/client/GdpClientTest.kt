package it.pagopa.aca.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.pagopa.aca.AcaTestUtils
import it.pagopa.aca.config.WebClientConfig
import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.exceptions.GpdException
import it.pagopa.generated.gpd.api.DebtPositionsApiApi
import java.nio.charset.StandardCharsets
import java.util.stream.Stream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

@OptIn(ExperimentalCoroutinesApi::class)
class GdpClientTest {

    companion object {
        const val creditorInstitutionCode = "77777777777"
        const val iuv = "302001069073736640"
        val iupd = Iupd(creditorInstitutionCode, iuv)

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
        private fun errorsProviderGetDebtPosition() =
            Stream.of(
                Arguments.of(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED,
                    "Error while call gpd unauthorized request"
                ),
                Arguments.of(
                    HttpStatus.NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "No debt position found with Creditor institution code: $creditorInstitutionCode and iupd: ${iupd.value()}"
                ),
                Arguments.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "Bad gateway, error while execute request"
                ),
            )

        @JvmStatic
        private fun errorsProviderPostCreateDebtPosition() =
            Stream.of(
                Arguments.of(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED,
                    "Error while call gpd unauthorized request"
                ),
                Arguments.of(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Bad request"),
                Arguments.of(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT,
                    "Error while create new debit position conflict into request"
                ),
                Arguments.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "Bad gateway, error while execute request"
                ),
            )

        @JvmStatic
        private fun errorsProviderUpdateDebtPosition() =
            Stream.of(
                Arguments.of(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED,
                    "Error while call gpd unauthorized request"
                ),
                Arguments.of(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Bad request"),
                Arguments.of(
                    HttpStatus.NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "No debt position found with Creditor institution code: $creditorInstitutionCode and iupd: ${iupd.value()}"
                ),
                Arguments.of(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT,
                    "Error while create new debit position conflict into request"
                ),
                Arguments.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "Bad gateway, error while execute request"
                ),
            )
    }

    private val gpdApi =
        WebClientConfig()
            .gpdClient(
                baseUrl = "http://${mockWebServer.hostName}:${mockWebServer.port}",
                apiKey = "apiKey",
                connectionTimeout = 1000,
                readTimeout = 1000
            )

    private val gpdClient = GpdClient(gpdApi)

    @Test
    fun `Should retrieve debit position successfully`() = runTest {
        // pre-conditions
        val mockedResponse = AcaTestUtils.debitPositionResponseBody()
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(mockedResponse))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        )
        // test
        val response = gpdClient.getDebtPosition(creditorInstitutionCode, iupd.value())
        // assertions
        Assertions.assertEquals(mockedResponse.iupd, response.iupd)
        Assertions.assertEquals(
            mockedResponse.organizationFiscalCode,
            response.organizationFiscalCode
        )
    }

    @ParameterizedTest
    @MethodSource("errorsProviderGetDebtPosition")
    fun `Should handle get debit position errors`(
        godErrorCode: HttpStatus,
        expectedStatusCode: HttpStatus,
        expectedDescription: String
    ) = runTest {
        // pre-conditions
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{}")
                .setResponseCode(godErrorCode.value())
                .addHeader("Content-Type", "application/json")
        )
        // test
        val exception =
            assertThrows<GpdException> {
                gpdClient.getDebtPosition(creditorInstitutionCode, iupd.value())
            }
        Assertions.assertEquals(expectedStatusCode, exception.toRestException().httpStatus)
        Assertions.assertEquals(expectedDescription, exception.toRestException().description)
    }

    @Test
    fun `Should handle exception invoking get debit position`() = runTest {
        // pre-conditions
        val gpdApi = mock<DebtPositionsApiApi>()
        val gpdClient = GpdClient(gpdApi)
        val httpErrorStatusCode = HttpStatus.CONFLICT
        given(gpdApi.getOrganizationDebtPositionByIUPD(creditorInstitutionCode, iupd.value()))
            .willThrow(
                WebClientResponseException.create(
                    httpErrorStatusCode.value(),
                    "Test exception",
                    HttpHeaders.EMPTY,
                    ByteArray(0),
                    StandardCharsets.UTF_8
                )
            )
        // test
        val exception =
            assertThrows<GpdException> {
                gpdClient.getDebtPosition(creditorInstitutionCode, iupd.value())
            }
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY, exception.toRestException().httpStatus)
        Assertions.assertEquals(
            "Gpd error: $httpErrorStatusCode",
            exception.toRestException().description
        )
    }

    @Test
    fun `Should create debit position successfully`() = runTest {
        // pre-conditions
        val mockedResponse = AcaTestUtils.debitPositionResponseBody()
        val mockedRequest = AcaTestUtils.debitPositionRequestBody(iupd)
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(mockedResponse))
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
        )
        // test
        val response = gpdClient.createDebtPosition(creditorInstitutionCode, mockedRequest)
        // assertions
        Assertions.assertEquals(response.iupd, mockedRequest.iupd)
    }

    @ParameterizedTest
    @MethodSource("errorsProviderPostCreateDebtPosition")
    fun `Should handle post create debit position errors`(
        godErrorCode: HttpStatus,
        expectedStatusCode: HttpStatus,
        expectedDescription: String
    ) = runTest {
        // pre-conditions
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{}")
                .setResponseCode(godErrorCode.value())
                .addHeader("Content-Type", "application/json")
        )
        // test
        val exception =
            assertThrows<GpdException> {
                gpdClient.createDebtPosition(
                    creditorInstitutionCode,
                    AcaTestUtils.debitPositionRequestBody(iupd)
                )
            }
        Assertions.assertEquals(expectedStatusCode, exception.toRestException().httpStatus)
        Assertions.assertEquals(expectedDescription, exception.toRestException().description)
    }

    @Test
    fun `Should handle exception invoking post create debit position`() = runTest {
        // pre-conditions
        val gpdApi = mock<DebtPositionsApiApi>()
        val gpdClient = GpdClient(gpdApi)
        val httpErrorStatusCode = HttpStatus.NOT_FOUND
        given(
                gpdApi.createPosition(
                    creditorInstitutionCode,
                    AcaTestUtils.debitPositionRequestBody(iupd),
                    true
                )
            )
            .willThrow(
                WebClientResponseException.create(
                    httpErrorStatusCode.value(),
                    "Test exception",
                    HttpHeaders.EMPTY,
                    ByteArray(0),
                    StandardCharsets.UTF_8
                )
            )
        // test
        val exception =
            assertThrows<GpdException> {
                gpdClient.createDebtPosition(
                    creditorInstitutionCode,
                    AcaTestUtils.debitPositionRequestBody(iupd)
                )
            }
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY, exception.toRestException().httpStatus)
        Assertions.assertEquals(
            "Gpd error: $httpErrorStatusCode",
            exception.toRestException().description
        )
    }

    @Test
    fun `Should update debit position successfully`() = runTest {
        // pre-conditions
        val mockedResponse = AcaTestUtils.debitPositionResponseBody()
        val mockedRequest = AcaTestUtils.debitPositionRequestBody(iupd)
        mockWebServer.enqueue(
            MockResponse()
                .setBody(objectMapper.writeValueAsString(mockedResponse))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        )
        // test
        val response =
            gpdClient.updateDebtPosition(creditorInstitutionCode, iupd.value(), mockedRequest)
        // assertions
        Assertions.assertEquals(response.iupd, mockedRequest.iupd)
    }

    @ParameterizedTest
    @MethodSource("errorsProviderUpdateDebtPosition")
    fun `Should handle update debit position errors`(
        godErrorCode: HttpStatus,
        expectedStatusCode: HttpStatus,
        expectedDescription: String
    ) = runTest {
        // pre-conditions
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{}")
                .setResponseCode(godErrorCode.value())
                .addHeader("Content-Type", "application/json")
        )
        // test
        val exception =
            assertThrows<GpdException> {
                gpdClient.updateDebtPosition(
                    creditorInstitutionCode,
                    iupd.value(),
                    AcaTestUtils.debitPositionRequestBody(iupd)
                )
            }
        Assertions.assertEquals(expectedStatusCode, exception.toRestException().httpStatus)
        Assertions.assertEquals(expectedDescription, exception.toRestException().description)
    }

    @Test
    fun `Should handle exception invoking updated of debit position`() = runTest {
        // pre-conditions
        val gpdApi = mock<DebtPositionsApiApi>()
        val gpdClient = GpdClient(gpdApi)
        val httpErrorStatusCode = HttpStatus.UNPROCESSABLE_ENTITY
        given(
                gpdApi.updatePosition(
                    creditorInstitutionCode,
                    iupd.value(),
                    AcaTestUtils.debitPositionRequestBody(iupd),
                    true
                )
            )
            .willThrow(
                WebClientResponseException.create(
                    httpErrorStatusCode.value(),
                    "Test exception",
                    HttpHeaders.EMPTY,
                    ByteArray(0),
                    StandardCharsets.UTF_8
                )
            )
        // test
        val exception =
            assertThrows<GpdException> {
                gpdClient.updateDebtPosition(
                    creditorInstitutionCode,
                    iupd.value(),
                    AcaTestUtils.debitPositionRequestBody(iupd)
                )
            }
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY, exception.toRestException().httpStatus)
        Assertions.assertEquals(
            "Gpd error: $httpErrorStatusCode",
            exception.toRestException().description
        )
    }
}