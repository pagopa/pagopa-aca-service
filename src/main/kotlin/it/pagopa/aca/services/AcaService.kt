package it.pagopa.aca.services

import it.pagopa.aca.client.GpdClient
import it.pagopa.aca.client.IbansClient
import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.exceptions.RestApiException
import it.pagopa.aca.utils.AcaUtils
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import java.util.UUID
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AcaService(
    @Autowired private val gpdClient: GpdClient,
    @Autowired private val ibansClient: IbansClient,
    @Autowired private val acaUtils: AcaUtils
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun handleDebitPosition(newDebtPositionRequestDto: NewDebtPositionRequestDto) {
        logger.info(
            "Handle debit position for amount: ${newDebtPositionRequestDto.amount} and iuv: ${newDebtPositionRequestDto.iuv}"
        )
        val requestId = UUID.randomUUID().toString()
        val iupd = Iupd(newDebtPositionRequestDto.paFiscalCode, newDebtPositionRequestDto.iuv)
        val paFiscalCode = newDebtPositionRequestDto.paFiscalCode
        gpdClient
            .getDebtPosition(paFiscalCode, iupd.value())
            .map { oldDebitPosition ->
                oldDebitPosition
                    .filter { acaUtils.checkStatus(it.status) }
                    .switchIfEmpty(
                        Mono.error(
                            RestApiException(
                                HttpStatus.CONFLICT,
                                "Unauthorized action",
                                "Unauthorized action on debt position with iuv: ${newDebtPositionRequestDto.iuv}"
                            )
                        )
                    )
                    .flatMap { debitPosition ->
                        if (acaUtils.checkInvalidateAmount(newDebtPositionRequestDto.amount)) {
                            logger.info("Invalidate debit position with iupd: ${iupd.value()}")
                            gpdClient.invalidateDebtPosition(paFiscalCode, iupd.value())
                        } else {
                            logger.info("Update debit position with iupd: ${iupd.value()}")
                            ibansClient
                                .getIban(paFiscalCode, requestId)
                                .map {
                                    acaUtils.updateOldDebitPositionObject(
                                        debitPosition,
                                        newDebtPositionRequestDto,
                                        iupd,
                                        it.second
                                    )
                                }
                                .flatMap { updatedDebitPosition ->
                                    gpdClient.updateDebtPosition(
                                        paFiscalCode,
                                        iupd.value(),
                                        updatedDebitPosition
                                    )
                                }
                        }
                    }
            }
            .orElse(
                if (acaUtils.checkInvalidateAmount(newDebtPositionRequestDto.amount)) {
                    Mono.error(
                        RestApiException(
                            HttpStatus.BAD_REQUEST,
                            "Unauthorized action",
                            "Amount not compatible with the creation request"
                        )
                    )
                } else {
                    ibansClient
                        .getIban(paFiscalCode, requestId)
                        .doOnError { logger.info("error") }
                        .doOnSuccess { logger.info("success") }
                        .map { response ->
                            acaUtils.newDebitPositionObject(
                                newDebtPositionRequestDto,
                                iupd,
                                response.first,
                                response.second
                            )
                        }
                        .flatMap { newDebitPosition ->
                            logger.info("Create new debit position with iupd: ${iupd.value()}")
                            gpdClient.createDebtPosition(paFiscalCode, newDebitPosition)
                        }
                }
            )
            .awaitSingle()
    }
}
