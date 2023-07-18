package it.pagopa.aca.utils

import it.pagopa.aca.AcaTestUtils
import it.pagopa.aca.domain.Iupd
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import java.util.stream.Stream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AcaUtilsTests {

    companion object {
        private val acaUtils = AcaUtils()
        const val creditorInstitutionCode = "77777777777"
        const val iuv = "302001069073736640"
        val iupd = Iupd(creditorInstitutionCode, iuv)

        @JvmStatic
        private fun validStatusForExecuteOperation() =
            Stream.of(
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.PUBLISHED, false),
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.VALID, false),
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT, false),
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.PAID, true),
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.INVALID, true),
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.EXPIRED, true),
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.PARTIALLY_PAID, true),
                Arguments.of(PaymentPositionModelBaseResponseDto.StatusEnum.REPORTED, true),
            )
    }
    @ParameterizedTest
    @MethodSource("validStatusForExecuteOperation")
    fun `check status ok`(status: PaymentPositionModelBaseResponseDto.StatusEnum, result: Boolean) =
        runTest {
            Assertions.assertEquals(result, acaUtils.isInvalidStatusForExecuteOperation(status))
        }

    @Test
    fun `check invalidate amount ko`() = runTest {
        Assertions.assertEquals(false, acaUtils.isInvalidateAmount(10))
    }

    @Test
    fun `check invalidate amount ok`() = runTest {
        Assertions.assertEquals(true, acaUtils.isInvalidateAmount(0))
    }

    @Test
    fun `new debit position create successfully`() = runTest {
        val apiRequestBody = AcaTestUtils.createPositionRequestBody(iupd, 10)
        val newDebitPosition =
            acaUtils.toPaymentPositionModelDto(
                apiRequestBody,
                iupd,
                "ITRUYRIITHYDSD",
                "CompanyName"
            )
        Assertions.assertEquals(iupd.value(), newDebitPosition.iupd)
        Assertions.assertEquals(
            apiRequestBody.amount.toLong(),
            newDebitPosition.paymentOption?.get(0)?.amount
        )
        Assertions.assertEquals(
            apiRequestBody.description,
            newDebitPosition.paymentOption?.get(0)?.description
        )
        Assertions.assertEquals(apiRequestBody.entityType.value, newDebitPosition.type.value)
        Assertions.assertEquals(apiRequestBody.iuv, newDebitPosition.paymentOption?.get(0)?.iuv)
    }

    @Test
    fun `update old debit position successfully`() = runTest {
        val ibanUpdated = "ITRU0123456789"
        val responseGetPosition =
            AcaTestUtils.responseGetPosition(
                iupd,
                10,
                "ITRUYRIITHYDSD",
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val apiRequestBody = AcaTestUtils.createPositionRequestBody(iupd, 10)
        val objectUpdated =
            acaUtils.updateOldDebitPositionObject(
                responseGetPosition,
                apiRequestBody,
                iupd,
                ibanUpdated,
                creditorInstitutionCode
            )
        Assertions.assertEquals(iupd.value(), objectUpdated.iupd)
        Assertions.assertEquals(ibanUpdated, objectUpdated.paymentOption?.get(0)?.transfer?.get(0)?.iban)
        Assertions.assertEquals(null, objectUpdated.validityDate)
        Assertions.assertEquals(apiRequestBody.entityType.value, objectUpdated.type.value)
    }
}
