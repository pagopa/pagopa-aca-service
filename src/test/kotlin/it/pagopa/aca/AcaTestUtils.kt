package it.pagopa.aca

import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.service.AcaServiceTests
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import it.pagopa.generated.aca.model.ProblemJsonDto
import it.pagopa.generated.apiconfig.model.CreditorInstitutionDetailsDto
import it.pagopa.generated.apiconfig.model.IbanEnhancedDto
import it.pagopa.generated.apiconfig.model.IbanLabelDto
import it.pagopa.generated.apiconfig.model.IbansEnhancedDto
import it.pagopa.generated.gpd.model.*
import java.time.LocalDateTime
import java.time.OffsetDateTime
import org.springframework.http.HttpStatus

object AcaTestUtils {
    fun creditorInstitutionIbanResponseBody(): IbansEnhancedDto =
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

    fun creditorInstitutionResponseBody(): CreditorInstitutionDetailsDto =
        CreditorInstitutionDetailsDto()
            .creditorInstitutionCode("123456")
            .businessName("companyName")

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
            .insertedDate(LocalDateTime.now())
            .publishDate(LocalDateTime.now())
            .validityDate(LocalDateTime.now())
            .paymentDate(LocalDateTime.now())
            .status(PaymentPositionModelBaseResponseDto.StatusEnum.PUBLISHED)
            .lastUpdatedDate(LocalDateTime.now())
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
            "company name",
            NewDebtPositionRequestDto.EntityType.F,
            "XXXYYY00X11Y123Z",
            "entityFullName",
            iupd.iuv,
            amount,
            "description",
            OffsetDateTime.now()
        )

    fun createPositionRequestBody(
        iupd: Iupd,
        amount: Int,
        iban: String? = null,
        postalIban: String? = null,
        switchToExpired: Boolean
    ): NewDebtPositionRequestDto =
        NewDebtPositionRequestDto(
            iupd.fiscalCode,
            "company name",
            NewDebtPositionRequestDto.EntityType.F,
            "XXXYYY00X11Y123Z",
            "entityFullName",
            iupd.iuv,
            amount,
            "description",
            OffsetDateTime.now(),
            "3" + AcaServiceTests.iupd.iuv,
            iban,
            postalIban,
            switchToExpired
        )

    fun responseGetPosition(
        iupd: Iupd,
        amount: Int,
        iban: String,
        status: PaymentPositionModelBaseResponseDto.StatusEnum
    ): PaymentPositionModelBaseResponseDto =
        PaymentPositionModelBaseResponseDto()
            .iupd(iupd.value())
            .validityDate(LocalDateTime.now())
            .type(PaymentPositionModelBaseResponseDto.TypeEnum.F)
            .status(status)
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
