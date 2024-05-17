package it.pagopa.aca.controllers

import it.pagopa.aca.services.AcaService
import it.pagopa.aca.utils.AcaUtils
import it.pagopa.aca.warmup.annotations.WarmupFunction
import it.pagopa.aca.warmup.exceptions.WarmUpException
import it.pagopa.aca.warmup.utils.WarmupRequests
import it.pagopa.generated.aca.api.PaCreatePositionApi
import it.pagopa.generated.aca.model.DebtPositionResponseDto
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import it.pagopa.generated.gpd.model.PaymentPositionModelDto
import java.time.Duration
import java.time.temporal.ChronoUnit
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@RestController
class AcaController(private val webClient: WebClient = WebClient.create()) : PaCreatePositionApi {
    @Autowired private lateinit var acaService: AcaService
    @Autowired private lateinit var acaUtils: AcaUtils
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override suspend fun newDebtPosition(
        newDebtPositionRequestDto: NewDebtPositionRequestDto
    ): ResponseEntity<DebtPositionResponseDto> {
        logger.info("[ACA service] paCreatePosition")
        val paymentPositionModelDto: PaymentPositionModelDto =
            acaService.handleDebitPosition(newDebtPositionRequestDto)
                ?: return ResponseEntity.ok().build()
        val response: DebtPositionResponseDto =
            acaUtils.toDebtPositionResponse(
                newDebtPositionRequestDto.paFiscalCode,
                paymentPositionModelDto
            )
        return ResponseEntity.ok().body(response)
    }

    /** Controller warm up function, used to send a POST pa create position request */
    @WarmupFunction
    fun warmupPostCarts() {
        webClient
            .post()
            .uri("http://localhost:8080/paCreatePosition")
            .body(
                Mono.just(WarmupRequests.postPaCreatePosition()),
                NewDebtPositionRequestDto::class.java
            )
            .retrieve()
            .onStatus(
                { it.isError },
                { Mono.error(WarmUpException("AcaController", "warmupPaCreatePosition")) }
            )
            .toBodilessEntity()
            .block(Duration.of(10, ChronoUnit.SECONDS))
    }
}
