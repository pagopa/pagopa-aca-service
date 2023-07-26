package it.pagopa.aca.controller

import it.pagopa.aca.controllers.AcaController
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@WebFluxTest(AcaController::class)
class AcaControllerTests {

    @Mock private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec

    @Mock private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>

    @Mock private lateinit var responseSpec: WebClient.ResponseSpec

    @Test
    fun `warm up controller`() {
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
