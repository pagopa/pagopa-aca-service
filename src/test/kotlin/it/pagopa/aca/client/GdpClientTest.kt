package it.pagopa.aca.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import it.pagopa.aca.AcaTestUtils
import it.pagopa.aca.config.WebClientConfig
import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.exceptions.GpdException
import it.pagopa.aca.exceptions.GpdPositionNotFoundException
import it.pagopa.generated.gpd.api.DebtPositionActionsApiApi
import it.pagopa.generated.gpd.api.DebtPositionsApiApi
import java.nio.charset.StandardCharsets
import java.util.*
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
import reactor.test.StepVerifier

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
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"
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
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"
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
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"
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

        @JvmStatic
        private fun errorsProviderInvalidateDebtPosition() =
            Stream.of(
                Arguments.of(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal server error"
                ),
                Arguments.of(
                    HttpStatus.NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "Error while invalidate debit position. Debit position not found with iupd: ${iupd.value()}"
                ),
                Arguments.of(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT,
                    "Error while invalidate debit position conflict into request",
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

    private val gpdApiForInvalidate =
        WebClientConfig()
            .gpdClientForInvalidate(
                baseUrl = "http://${mockWebServer.hostName}:${mockWebServer.port}",
                apiKey = "apiKey",
                connectionTimeout = 1000,
                readTimeout = 1000
            )
    private val gpdClient = GpdClient(gpdApi, gpdApiForInvalidate)

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
        val response = gpdClient.getDebtPosition(creditorInstitutionCode, iupd.value()).block()
        // assertions
        Assertions.assertEquals(mockedResponse.iupd, response?.iupd)
        Assertions.assertEquals(
            mockedResponse.organizationFiscalCode,
            response?.organizationFiscalCode
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
        StepVerifier.create(gpdClient.getDebtPosition(creditorInstitutionCode, iupd.value()))
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == expectedDescription
                it.toRestException().httpStatus == expectedStatusCode
            }
            .verify()
    }

    @Test
    fun `Should handle get debit position errors 404`() = runTest {
        // pre-conditions
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{}")
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .addHeader("Content-Type", "application/json")
        )
        // test
        StepVerifier.create(gpdClient.getDebtPosition(creditorInstitutionCode, iupd.value()))
            .expectErrorMatches {
                it as GpdPositionNotFoundException
                it.toRestException().description == "Debit position not found"
                it.toRestException().httpStatus == HttpStatus.NOT_FOUND
            }
            .verify()
    }

    @Test
    fun `Should handle exception invoking get debit position`() = runTest {
        // pre-conditions
        val gpdApi = mock<DebtPositionsApiApi>()
        val gpdApiForInvalidate = mock<DebtPositionActionsApiApi>()
        val gpdClient = GpdClient(gpdApi, gpdApiForInvalidate)
        val httpErrorStatusCode = HttpStatus.CONFLICT
        val requestId = UUID.randomUUID().toString()
        given(
                gpdApi.getOrganizationDebtPositionByIUPD(
                    creditorInstitutionCode,
                    iupd.value(),
                    requestId
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
        StepVerifier.create(
                gpdClient.getDebtPosition(creditorInstitutionCode, iupd.value(), requestId)
            )
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == "Gpd error: $httpErrorStatusCode"
                it.toRestException().httpStatus == HttpStatus.BAD_GATEWAY
            }
            .verify()
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
        val response = gpdClient.createDebtPosition(creditorInstitutionCode, mockedRequest).block()
        // assertions
        Assertions.assertEquals(response?.iupd, mockedRequest.iupd)
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
        StepVerifier.create(
                gpdClient.createDebtPosition(
                    creditorInstitutionCode,
                    AcaTestUtils.debitPositionRequestBody(iupd)
                )
            )
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == expectedDescription
                it.toRestException().httpStatus == expectedStatusCode
            }
            .verify()
    }

    @Test
    fun `Should handle exception invoking post create debit position`() = runTest {
        // pre-conditions
        val gpdApi = mock<DebtPositionsApiApi>()
        val gpdApiForInvalidate = mock<DebtPositionActionsApiApi>()
        val gpdClient = GpdClient(gpdApi, gpdApiForInvalidate)
        val httpErrorStatusCode = HttpStatus.NOT_FOUND
        val requestId = UUID.randomUUID().toString()
        given(
                gpdApi.createPosition(
                    creditorInstitutionCode,
                    AcaTestUtils.debitPositionRequestBody(iupd),
                    requestId,
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
        StepVerifier.create(
                gpdClient.createDebtPosition(
                    creditorInstitutionCode,
                    AcaTestUtils.debitPositionRequestBody(iupd),
                    requestId
                )
            )
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == "Gpd error: $httpErrorStatusCode"
                it.toRestException().httpStatus == HttpStatus.BAD_GATEWAY
            }
            .verify()
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
            gpdClient
                .updateDebtPosition(creditorInstitutionCode, iupd.value(), mockedRequest)
                .block()
        // assertions
        Assertions.assertEquals(response?.iupd, mockedRequest.iupd)
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
        StepVerifier.create(
                gpdClient.updateDebtPosition(
                    creditorInstitutionCode,
                    iupd.value(),
                    AcaTestUtils.debitPositionRequestBody(iupd)
                )
            )
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == expectedDescription
                it.toRestException().httpStatus == expectedStatusCode
            }
            .verify()
    }

    @Test
    fun `Should handle exception invoking updated of debit position`() = runTest {
        // pre-conditions
        val gpdApi = mock<DebtPositionsApiApi>()
        val gpdApiForInvalidate = mock<DebtPositionActionsApiApi>()
        val gpdClient = GpdClient(gpdApi, gpdApiForInvalidate)
        val httpErrorStatusCode = HttpStatus.UNPROCESSABLE_ENTITY
        val requestId = UUID.randomUUID().toString()
        given(
                gpdApi.updatePosition(
                    creditorInstitutionCode,
                    iupd.value(),
                    AcaTestUtils.debitPositionRequestBody(iupd),
                    requestId,
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
        StepVerifier.create(
                gpdClient.updateDebtPosition(
                    creditorInstitutionCode,
                    iupd.value(),
                    AcaTestUtils.debitPositionRequestBody(iupd),
                    requestId
                )
            )
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == "Gpd error: $httpErrorStatusCode"
                it.toRestException().httpStatus == HttpStatus.BAD_GATEWAY
            }
            .verify()
    }

    @Test
    fun `Should invalidate debit position successfully`() = runTest {
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
            gpdClient.invalidateDebtPosition(creditorInstitutionCode, iupd.value()).block()
        // assertions
        Assertions.assertEquals(response?.iupd, mockedRequest.iupd)
    }

    @ParameterizedTest
    @MethodSource("errorsProviderInvalidateDebtPosition")
    fun `Should handle invalidate debit position errors`(
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
        StepVerifier.create(gpdClient.invalidateDebtPosition(creditorInstitutionCode, iupd.value()))
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == expectedDescription
                it.toRestException().httpStatus == expectedStatusCode
            }
            .verify()
    }

    @Test
    fun `Should handle exception invoking invalidate action of debit position`() = runTest {
        // pre-conditions
        val gpdApi = mock<DebtPositionsApiApi>()
        val gpdApiForInvalidate = mock<DebtPositionActionsApiApi>()
        val gpdClient = GpdClient(gpdApi, gpdApiForInvalidate)
        val httpErrorStatusCode = HttpStatus.UNPROCESSABLE_ENTITY
        val requestId = UUID.randomUUID().toString()
        given(
                gpdApiForInvalidate.invalidatePosition(
                    creditorInstitutionCode,
                    iupd.value(),
                    requestId
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
        StepVerifier.create(
                gpdClient.invalidateDebtPosition(creditorInstitutionCode, iupd.value(), requestId)
            )
            .expectErrorMatches {
                it as GpdException
                it.toRestException().description == "Gpd error: $httpErrorStatusCode"
                it.toRestException().httpStatus == HttpStatus.BAD_GATEWAY
            }
            .verify()
    }
}
