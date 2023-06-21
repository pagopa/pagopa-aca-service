package it.pagopa.aca.utils

import it.pagopa.aca.AcaTestUtils
import it.pagopa.aca.domain.Iupd
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AcaUtilsTests {

    companion object {
        private val acaUtils = AcaUtils()
        const val creditorInstitutionCode = "77777777777"
        const val iuv = "302001069073736640"
        val iupd = Iupd(creditorInstitutionCode, iuv)
    }
    @Test
    fun `check status ok`() = runTest {
        Assertions.assertEquals(
            false,
            acaUtils.checkStatus(PaymentPositionModelBaseResponseDto.StatusEnum.PUBLISHED)
        )
    }

    @Test
    fun `check status ko`() = runTest {
        Assertions.assertEquals(
            true,
            acaUtils.checkStatus(PaymentPositionModelBaseResponseDto.StatusEnum.PAID)
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
        val apiRequestBody = AcaTestUtils.createPositionRequestBody(iupd, 10)
        val newDebitPosition =
            acaUtils.newDebitPositionObject(apiRequestBody, iupd, "ITRUYRIITHYDSD", "CompanyName")
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
        val responseGetPosition = AcaTestUtils.responseGetPosition(iupd, 10, "ITRUYRIITHYDSD")
        val apiRequestBody = AcaTestUtils.createPositionRequestBody(iupd, 10)
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
