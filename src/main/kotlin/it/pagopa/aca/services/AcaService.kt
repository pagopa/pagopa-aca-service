package it.pagopa.aca.services

import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@Slf4j
class AcaService {

    fun handleDebitPosition(newDebtPositionRequestDto: NewDebtPositionRequestDto): Mono<Void> {
        return Mono.just("data").then()
    }
}
