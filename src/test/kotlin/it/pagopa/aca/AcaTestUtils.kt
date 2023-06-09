package it.pagopa.aca

import it.pagopa.generated.aca.model.ProblemJsonDto
import it.pagopa.generated.apiconfig.model.IbanDto
import it.pagopa.generated.apiconfig.model.IbanLabelDto
import it.pagopa.generated.apiconfig.model.IbansDto
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
}
