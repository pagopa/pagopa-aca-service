package it.pagopa.aca

import it.pagopa.aca.domain.Iupd
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import it.pagopa.generated.aca.model.ProblemJsonDto
import it.pagopa.generated.apiconfig.model.IbanEnhancedDto
import it.pagopa.generated.apiconfig.model.IbanLabelDto
import it.pagopa.generated.apiconfig.model.IbansEnhancedDto
import it.pagopa.generated.gpd.model.*
import java.time.OffsetDateTime
import org.springframework.http.HttpStatus

object ObjectTestUtils {
    fun creditorInstitutionResponseBody(): IbansEnhancedDto =
        IbansEnhancedDto()
            .ibansEnhanced(
                listOf(
                    IbanEnhancedDto("ciOwner", "companyName", OffsetDateTime.now())
                        .description("ibanDtoDescription")
                        .isActive(true)
                        .iban("IT99C0222211111000000000000")
                        .addLabelsItem(
                            IbanLabelDto().description("IbanLabelDtoDescription").name("ACA")
                        )
                        .validityDate(OffsetDateTime.now())
                )
            )

    fun buildProblemJson(
        httpStatus: HttpStatus,
        title: String,
        description: String
    ): ProblemJsonDto =
        ProblemJsonDto(status = httpStatus.value(), detail = description, title = title)

    fun debitPositionResponseBody(): PaymentPositionModelBaseResponseDto =
        PaymentPositionModelBaseResponseDto()
            .iupd("ACA_77777777777_302001069073736640")
            .companyName("companyName")
            .organizationFiscalCode("77777777777")
            .type(PaymentPositionModelBaseResponseDto.TypeEnum.F)
            .insertedDate(OffsetDateTime.now())
            .publishDate(OffsetDateTime.now())
            .validityDate(OffsetDateTime.now())
            .paymentDate(OffsetDateTime.now())
            .status(PaymentPositionModelBaseResponseDto.StatusEnum.PUBLISHED)
            .lastUpdatedDate(OffsetDateTime.now())
            .paymentOption(
                listOf(
                    PaymentOptionModelResponseDto()
                        .iuv("302001069073736640")
                        .organizationFiscalCode("77777777777")
                        .amount(100)
                        .isPartialPayment(false)
                )
            )

    fun debitPositionModelResponse(iupd: Iupd): PaymentPositionModelDto =
        PaymentPositionModelDto()
            .iupd(iupd.value())
            .companyName("companyName")
            .type(PaymentPositionModelDto.TypeEnum.F)
            .paymentOption(
                listOf(
                    PaymentOptionModelDto()
                        .iuv("302001069073736640")
                        .amount(100)
                        .isPartialPayment(false)
                )
            )

    fun debitPositionRequestBody(iupd: Iupd): PaymentPositionModelDto =
        PaymentPositionModelDto()
            .iupd(iupd.value())
            .companyName("companyName")
            .fiscalCode("XXXYYY00X11Y123Z")
            .type(PaymentPositionModelDto.TypeEnum.F)
            .fullName("Entity Test")

    fun createPositionRequestBody(iupd: Iupd, amount: Int): NewDebtPositionRequestDto =
        NewDebtPositionRequestDto(
            iupd.fiscalCode,
            NewDebtPositionRequestDto.EntityType.F,
            "XXXYYY00X11Y123Z",
            "entityFullName",
            iupd.iuv,
            amount,
            "description",
            OffsetDateTime.now()
        )

    fun responseGetPosition(
        iupd: Iupd,
        amount: Int,
        iban: String
    ): PaymentPositionModelBaseResponseDto =
        PaymentPositionModelBaseResponseDto()
            .iupd(iupd.value())
            .validityDate(OffsetDateTime.now())
            .type(PaymentPositionModelBaseResponseDto.TypeEnum.F)
            .paymentOption(
                listOf(
                    PaymentOptionModelResponseDto()
                        .amount(amount.toLong())
                        .organizationFiscalCode("XXXYYY00X11Y123Z")
                        .isPartialPayment(false)
                        .transfer(
                            listOf(TransferModelResponseDto().amount(amount.toLong()).iban(iban))
                        )
                )
            )
}
