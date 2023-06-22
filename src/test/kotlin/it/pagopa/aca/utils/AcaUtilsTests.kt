package it.pagopa.aca.utils

import it.pagopa.aca.ObjectTestUtils
import it.pagopa.aca.domain.Iupd
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import java.util.stream.Stream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
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
                PaymentPositionModelBaseResponseDto.StatusEnum.PUBLISHED,
                PaymentPositionModelBaseResponseDto.StatusEnum.VALID,
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
    }
    @ParameterizedTest
    @MethodSource("validStatusForExecuteOperation")
    fun `check status ok`(status: PaymentPositionModelBaseResponseDto.StatusEnum) = runTest {
        Assertions.assertEquals(false, acaUtils.isValidStatusForExecuteOperation(status))
    }

    @Test
    fun `check status ko`() = runTest {
        Assertions.assertEquals(
            true,
            acaUtils.isValidStatusForExecuteOperation(PaymentPositionModelBaseResponseDto.StatusEnum.PAID)
        )
    }

    @Test
    fun `check invalidate amount ko`() = runTest {
        Assertions.assertEquals(false, acaUtils.checkInvalidateAmount(10))
    }

    @Test
    fun `check invalidate amount ok`() = runTest {
        Assertions.assertEquals(true, acaUtils.checkInvalidateAmount(0))
    }

    @Test
    fun `new debit position create successfully`() = runTest {
        val apiRequestBody = ObjectTestUtils.createPositionRequestBody(iupd, 10)
        val newDebitPosition =
            acaUtils.toPaymentPositionModelDto(apiRequestBody, iupd, "ITRUYRIITHYDSD", "CompanyName")
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
        val responseGetPosition =
            ObjectTestUtils.responseGetPosition(
                iupd,
                10,
                "ITRUYRIITHYDSD",
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val apiRequestBody = ObjectTestUtils.createPositionRequestBody(iupd, 10)
        val objectUpdated =
            acaUtils.updateOldDebitPositionObject(
                responseGetPosition,
                apiRequestBody,
                iupd,
                creditorInstitutionCode
            )
        Assertions.assertEquals(iupd.value(), objectUpdated.iupd)
        Assertions.assertEquals(null, objectUpdated.validityDate)
        Assertions.assertEquals(apiRequestBody.entityType.value, objectUpdated.type.value)
    }
}
