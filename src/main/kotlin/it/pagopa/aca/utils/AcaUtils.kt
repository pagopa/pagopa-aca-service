package it.pagopa.aca.utils

import it.pagopa.aca.domain.Iupd
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import it.pagopa.generated.gpd.model.PaymentOptionModelDto
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import it.pagopa.generated.gpd.model.PaymentPositionModelDto
import it.pagopa.generated.gpd.model.TransferModelDto
import org.springframework.stereotype.Component

@Component
class AcaUtils {

    companion object {
        const val STAND_IN_CONSTANT: String = "STAND-IN"
    }
    fun isValidStatusForExecuteOperation(status: PaymentPositionModelBaseResponseDto.StatusEnum?): Boolean {
        return status != PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT &&
            status != PaymentPositionModelBaseResponseDto.StatusEnum.PUBLISHED &&
            status != PaymentPositionModelBaseResponseDto.StatusEnum.VALID
    }

    fun checkInvalidateAmount(amount: Int): Boolean {
        return amount == 0
    }

    fun newDebitPositionObject(
        newDebtPositionRequestDto: NewDebtPositionRequestDto,
        iupd: Iupd,
        iban: String,
        companyName: String?
    ): PaymentPositionModelDto {
        return PaymentPositionModelDto()
            .iupd(iupd.value())
            .fiscalCode(newDebtPositionRequestDto.entityFiscalCode)
            .type(
                PaymentPositionModelDto.TypeEnum.valueOf(newDebtPositionRequestDto.entityType.value)
            )
            .fullName(newDebtPositionRequestDto.entityFullName)
            .companyName(companyName)
            .paymentOption(
                listOf(
                    PaymentOptionModelDto()
                        .iuv(newDebtPositionRequestDto.iuv)
                        .amount(newDebtPositionRequestDto.amount.toLong())
                        .description(newDebtPositionRequestDto.description)
                        .isPartialPayment(false)
                        .dueDate(newDebtPositionRequestDto.expirationDate)
                        .transfer(
                            listOf(
                                TransferModelDto()
                                    .amount(newDebtPositionRequestDto.amount.toLong())
                                    .idTransfer(TransferModelDto.IdTransferEnum._1)
                                    .category(STAND_IN_CONSTANT)
                                    .iban(iban)
                            )
                        )
                )
            )
    }

    fun updateOldDebitPositionObject(
        oldDebitPosition: PaymentPositionModelBaseResponseDto,
        newDebtPositionRequestDto: NewDebtPositionRequestDto,
        iupd: Iupd,
        companyName: String?
    ): PaymentPositionModelDto {
        return PaymentPositionModelDto()
            .iupd(oldDebitPosition.iupd)
            .fiscalCode(newDebtPositionRequestDto.entityFiscalCode)
            .officeName(oldDebitPosition.officeName)
            .validityDate(null)
            .type(
                PaymentPositionModelDto.TypeEnum.valueOf(newDebtPositionRequestDto.entityType.value)
            )
            .fullName(newDebtPositionRequestDto.entityFullName)
            .companyName(companyName)
            .paymentOption(
                oldDebitPosition.paymentOption
                    ?.stream()
                    ?.map {
                        PaymentOptionModelDto()
                            .iuv(newDebtPositionRequestDto.iuv)
                            .amount(newDebtPositionRequestDto.amount.toLong())
                            .description(newDebtPositionRequestDto.description)
                            .dueDate(newDebtPositionRequestDto.expirationDate)
                            .transfer(
                                it.transfer
                                    ?.stream()
                                    ?.map { transfer ->
                                        TransferModelDto()
                                            .amount(newDebtPositionRequestDto.amount.toLong())
                                            .idTransfer(TransferModelDto.IdTransferEnum._1)
                                            .category(transfer.category)
                                            .iban(transfer.iban)
                                    }
                                    ?.toList()
                            )
                    }
                    ?.toList()
            )
    }
}
