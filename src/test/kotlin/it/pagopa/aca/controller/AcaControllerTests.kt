package it.pagopa.aca.controller

import it.pagopa.aca.controllers.AcaController
import it.pagopa.aca.services.AcaService
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import java.time.OffsetDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@OptIn(ExperimentalCoroutinesApi::class)
@WebFluxTest(AcaController::class)
class AcaControllerTests {

    @Autowired lateinit var webClient: WebTestClient

    @MockBean lateinit var acaService: AcaService

    @Mock private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec

    @Mock private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>

    @Mock private lateinit var responseSpec: WebClient.ResponseSpec

    @Test
    fun `post paCreatePosition succeeded`() = runTest {
        val request =
            NewDebtPositionRequestDto(
                iuv = "302001069073736640",
                companyName = "company name",
                entityType = NewDebtPositionRequestDto.EntityType.F,
                entityFullName = "entity example full name",
                entityFiscalCode = "RYGFHDDDYR7FDFTR",
                description = "description test",
                amount = 10,
                expirationDate = OffsetDateTime.now(),
                paFiscalCode = "77777777777"
            )
        given(acaService.handleDebitPosition(request)).willReturn(Unit)
        webClient
            .post()
            .uri("/paCreatePosition")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `warm up controller`() = runTest {
        val webClient = mock(WebClient::class.java)
        given(webClient.post()).willReturn(requestBodyUriSpec)
        given(requestBodyUriSpec.uri(any(), any<Array<*>>())).willReturn(requestBodyUriSpec)
        given(requestBodyUriSpec.header(any(), any())).willReturn(requestBodyUriSpec)
        given(requestBodyUriSpec.body(any(), eq(NewDebtPositionRequestDto::class.java)))
            .willReturn(requestHeadersSpec)
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec)
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec)
        given(responseSpec.toBodilessEntity()).willReturn(Mono.empty())
        AcaController(webClient).warmupPostCarts()
        verify(webClient, times(1)).post()
    }
}
