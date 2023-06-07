package it.pagopa.aca.services

import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AcaService {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun handleDebitPosition(newDebtPositionRequestDto: NewDebtPositionRequestDto) {
        // TODO add implementation here
        logger.info("Handle debit position for amount ${newDebtPositionRequestDto.amount}")
    }
}
