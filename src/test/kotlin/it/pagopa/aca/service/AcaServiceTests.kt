package it.pagopa.aca.service

import it.pagopa.aca.ObjectTestUtils
import it.pagopa.aca.client.GpdClient
import it.pagopa.aca.client.IbansClient
import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.exceptions.GpdPositionNotFoundException
import it.pagopa.aca.exceptions.RestApiException
import it.pagopa.aca.services.AcaService
import it.pagopa.aca.utils.AcaUtils
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AcaServiceTests {
    private val gpdClient: GpdClient = mock()
    private val ibansClient: IbansClient = mock()
    private val acaUtils = AcaUtils()
    private val acaService = AcaService(gpdClient, ibansClient, acaUtils)

    companion object {
        private const val paFiscalCode = "77777777777"
        private const val iuv = "302001069073736640"
        val iupd = Iupd(paFiscalCode, iuv)
        const val ibanTest = "IT55555555555555"
        const val companyName = "companyNameTests"
    }
    @Test
    fun `create position successfully`() = runTest {
        val requestCreatePosition = ObjectTestUtils.createPositionRequestBody(iupd, 10)
        val responseCreate = ObjectTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any()))
            .willReturn(Mono.error(GpdPositionNotFoundException()))
        given(ibansClient.getIban(any(), any())).willReturn(Mono.just(Pair(ibanTest, companyName)))
        given(gpdClient.createDebtPosition(any(), any())).willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(1)).getIban(eq(requestCreatePosition.paFiscalCode), any())
        verify(gpdClient, Mockito.times(1))
            .createDebtPosition(
                requestCreatePosition.paFiscalCode,
                acaUtils.newDebitPositionObject(requestCreatePosition, iupd, ibanTest, companyName)
            )
        assertEquals(iupd.value(), responseCreate.iupd)
    }

    @Test
    fun `create position amount error`() = runTest {
        val requestCreatePosition = ObjectTestUtils.createPositionRequestBody(iupd, 0)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any()))
            .willReturn(Mono.error(GpdPositionNotFoundException()))
        /* Asserts */
        assertThrows<RestApiException> { acaService.handleDebitPosition(requestCreatePosition) }
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any())
        verify(ibansClient, Mockito.times(0)).getIban(any(), any())
    }

    @Test
    fun `exception position status error`() = runTest {
        val requestCreatePosition = ObjectTestUtils.createPositionRequestBody(iupd, 10)
        val responseGetPosition =
            ObjectTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.PAID
            )
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any())).willReturn(Mono.just(responseGetPosition))
        /* Asserts */
        assertThrows<RestApiException> { acaService.handleDebitPosition(requestCreatePosition) }
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any())
        verify(ibansClient, Mockito.times(0)).getIban(any(), any())
    }

    @Test
    fun `invalidate position successfully`() = runTest {
        val requestCreatePosition = ObjectTestUtils.createPositionRequestBody(iupd, 0)
        val responseGetPosition =
            ObjectTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val responseInvalidate = ObjectTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any())).willReturn(Mono.just(responseGetPosition))
        given(gpdClient.invalidateDebtPosition(any(), any()))
            .willReturn(Mono.just(responseInvalidate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(gpdClient, Mockito.times(1))
            .invalidateDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(0)).getIban(any(), any())
        verify(gpdClient, Mockito.times(0))
            .createDebtPosition(
                requestCreatePosition.paFiscalCode,
                acaUtils.newDebitPositionObject(requestCreatePosition, iupd, ibanTest, companyName)
            )
    }

    @Test
    fun `update position successfully`() = runTest {
        val requestCreatePosition = ObjectTestUtils.createPositionRequestBody(iupd, 10)
        val responseGetPosition =
            ObjectTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val responseCreate = ObjectTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any())).willReturn(Mono.just(responseGetPosition))
        given(ibansClient.getIban(any(), any())).willReturn(Mono.just(Pair(ibanTest, companyName)))
        given(gpdClient.updateDebtPosition(any(), any(), any()))
            .willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(1)).getIban(eq(requestCreatePosition.paFiscalCode), any())
        verify(gpdClient, Mockito.times(1))
            .updateDebtPosition(
                requestCreatePosition.paFiscalCode,
                iupd.value(),
                acaUtils.updateOldDebitPositionObject(
                    responseGetPosition,
                    requestCreatePosition,
                    iupd,
                    companyName
                )
            )
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any())
        verify(gpdClient, Mockito.times(0))
            .invalidateDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
    }
}
