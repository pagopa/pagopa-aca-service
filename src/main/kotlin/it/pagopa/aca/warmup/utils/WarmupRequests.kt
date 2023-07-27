package it.pagopa.aca.warmup.utils

import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import java.time.OffsetDateTime

object WarmupRequests {

    fun postPaCreatePosition() =
        NewDebtPositionRequestDto(
            iuv = "302001069073736640",
            entityType = NewDebtPositionRequestDto.EntityType.F,
            entityFullName = "entity example full name",
            entityFiscalCode = "RYGFHDDDYR7FDFTR",
            description = "description test",
            amount = 10,
            expirationDate = OffsetDateTime.now(),
            paFiscalCode = "77777777777"
        )
}
