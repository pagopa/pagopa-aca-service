package it.pagopa.aca

import it.pagopa.aca.domain.Iupd
import it.pagopa.generated.aca.model.ProblemJsonDto
import it.pagopa.generated.apiconfig.model.IbanDto
import it.pagopa.generated.apiconfig.model.IbanLabelDto
import it.pagopa.generated.apiconfig.model.IbansDto
import it.pagopa.generated.gpd.model.PaymentOptionModelResponseDto
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import it.pagopa.generated.gpd.model.PaymentPositionModelDto
import java.time.OffsetDateTime
import org.springframework.http.HttpStatus

object AcaTestUtils {
    fun creditorInstitutionResponseBody(): IbansDto =
        IbansDto()
            .ibans(
                listOf(
                    IbanDto("ciOwner", "companyName", 0L, OffsetDateTime.now())
                        .description("ibanDtoDescription")
                        .isActive(true)
                        .iban("IT99C0222211111000000000000")
                        .addLabelsItem(
                            IbanLabelDto()
                                .description("IbanLabelDtoDescription")
                                .name(IbanLabelDto.NameEnum.ACA)
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

    fun debitPositionRequestBody(iupd: Iupd): PaymentPositionModelDto =
        PaymentPositionModelDto()
            .iupd(iupd.value())
            .companyName("companyName")
            .fiscalCode("XXXYYY00X11Y123Z")
            .type(PaymentPositionModelDto.TypeEnum.F)
            .fullName("Entity Test")
}
