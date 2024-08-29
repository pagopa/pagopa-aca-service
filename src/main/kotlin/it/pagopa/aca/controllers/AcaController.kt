package it.pagopa.aca.controllers

import it.pagopa.aca.services.AcaService
import it.pagopa.aca.warmup.annotations.WarmupFunction
import it.pagopa.aca.warmup.exceptions.WarmUpException
import it.pagopa.aca.warmup.utils.WarmupRequests
import it.pagopa.generated.aca.api.PaCreatePositionApi
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import java.time.Duration
import java.time.temporal.ChronoUnit
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@RestController
class AcaController(private val webClient: WebClient = WebClient.create()) : PaCreatePositionApi {
    @Autowired private lateinit var acaService: AcaService
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override suspend fun newDebtPosition(
        newDebtPositionRequestDto: NewDebtPositionRequestDto,
        segregationCodes: String?
    ): ResponseEntity<Unit> {
        logger.info("[ACA service] paCreatePosition")
        // Check authorization
        if (!checkAuth(segregationCodes, newDebtPositionRequestDto.iuv))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        // Handle request
        acaService.handleDebitPosition(newDebtPositionRequestDto)
        return ResponseEntity.ok().build()
    }

    // Fun to check authorization based on IUV prefix
    private fun checkAuth(segregationCodes: String?, iuv: String): Boolean {
        return segregationCodes?.contains(iuv.substring(0, 2)) == true
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
