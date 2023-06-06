package it.pagopa.aca.controllers

import it.pagopa.aca.services.AcaService
import it.pagopa.generated.aca.api.PaCreatePositionApi
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
@Validated
class AcaController(
    @Autowired private val acaService: AcaService,
) : PaCreatePositionApi {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override suspend fun newDebtPosition(
        newDebtPositionRequestDto: NewDebtPositionRequestDto
    ): ResponseEntity<Unit> {
        logger.info("[ACA service] paCreatePosition")
        acaService.handleDebitPosition(newDebtPositionRequestDto)
        return ResponseEntity.ok().build()
    }
}
