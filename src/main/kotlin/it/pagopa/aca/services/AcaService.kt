package it.pagopa.aca.services

import it.pagopa.aca.client.CreditorInstitutionClient
import it.pagopa.aca.client.GpdClient
import it.pagopa.aca.client.IbansClient
import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.exceptions.GpdPositionNotFoundException
import it.pagopa.aca.exceptions.RestApiException
import it.pagopa.aca.utils.AcaUtils
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import it.pagopa.generated.gpd.model.PaymentPositionModelDto
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
    @Autowired private val creditorInstitutionClient: CreditorInstitutionClient,
    @Autowired private val acaUtils: AcaUtils
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun handleDebitPosition(
        newDebtPositionRequestDto: NewDebtPositionRequestDto
    ): PaymentPositionModelDto? {
        logger.info(
            "Handle debit position for amount: ${newDebtPositionRequestDto.amount} and iuv: ${newDebtPositionRequestDto.iuv}"
        )
        val requestId = UUID.randomUUID().toString()
        val iupd = Iupd(newDebtPositionRequestDto.paFiscalCode, newDebtPositionRequestDto.iuv)
        val paFiscalCode = newDebtPositionRequestDto.paFiscalCode
        return gpdClient
            .getDebtPosition(paFiscalCode, iupd.value())
            .filter { !acaUtils.isInvalidStatusForExecuteOperation(it.status) }
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
                if (acaUtils.isInvalidateAmount(newDebtPositionRequestDto.amount)) {
                    logger.info("Invalidate debit position with iupd: ${iupd.value()}")
                    gpdClient.invalidateDebtPosition(paFiscalCode, iupd.value())
                } else {
                    if (newDebtPositionRequestDto.iban == null) {
                        logger.info("Update debit position with iupd: ${iupd.value()}")
                        ibansClient
                            .getIban(0, 1, paFiscalCode, requestId) // only the first iban is used
                            .map {
                                acaUtils.updateOldDebitPositionObject(
                                    debitPosition,
                                    newDebtPositionRequestDto,
                                    iupd,
                                    iban = it.first,
                                    companyName = it.second,
                                    newDebtPositionRequestDto.postalIban
                                )
                            }
                            .flatMap { updatedDebitPosition ->
                                gpdClient.updateDebtPosition(
                                    paFiscalCode,
                                    iupd.value(),
                                    updatedDebitPosition
                                )
                            }
                    } else {
                        creditorInstitutionClient
                            .getCreditorInstitution(paFiscalCode, requestId)
                            .map {
                                acaUtils.updateOldDebitPositionObject(
                                    debitPosition,
                                    newDebtPositionRequestDto,
                                    iupd,
                                    newDebtPositionRequestDto.iban,
                                    companyName = it.second,
                                    newDebtPositionRequestDto.postalIban
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
            .onErrorResume(GpdPositionNotFoundException::class.java) {
                if (acaUtils.isInvalidateAmount(newDebtPositionRequestDto.amount)) {
                    logger.debug("Amount not compatible with the creation request")
                    Mono.error(
                        RestApiException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "Unprocessable request",
                            "Can not perform the requested action on debit position"
                        )
                    )
                } else {
                    if (newDebtPositionRequestDto.iban == null) {
                        ibansClient
                            .getIban(0, 1, paFiscalCode, requestId) // only the first iban is used
                            .map { response ->
                                acaUtils.toPaymentPositionModelDto(
                                    newDebtPositionRequestDto,
                                    iupd,
                                    iban = response.first,
                                    companyName = response.second,
                                    newDebtPositionRequestDto.postalIban
                                )
                            }
                            .flatMap { newDebitPosition ->
                                logger.info("Create new debit position with iupd: ${iupd.value()}")
                                gpdClient.createDebtPosition(paFiscalCode, newDebitPosition)
                            }
                    } else {
                        creditorInstitutionClient
                            .getCreditorInstitution(paFiscalCode, requestId)
                            .map { response ->
                                acaUtils.toPaymentPositionModelDto(
                                    newDebtPositionRequestDto,
                                    iupd,
                                    newDebtPositionRequestDto.iban,
                                    companyName = response.second,
                                    newDebtPositionRequestDto.postalIban
                                )
                            }
                            .flatMap { newDebitPosition ->
                                logger.info("Create new debit position with iupd: ${iupd.value()}")
                                gpdClient.createDebtPosition(paFiscalCode, newDebitPosition)
                            }
                    }
                }
            }
            .awaitSingle()
    }
}
