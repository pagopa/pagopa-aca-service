package it.pagopa.aca.service

import it.pagopa.aca.AcaTestUtils
import it.pagopa.aca.client.GpdClient
import it.pagopa.aca.client.IbansClient
import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.exceptions.GpdPositionNotFoundException
import it.pagopa.aca.exceptions.RestApiException
import it.pagopa.aca.services.AcaService
import it.pagopa.aca.utils.AcaUtils
import it.pagopa.generated.aca.model.NewDebtPositionRequestDto
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import it.pagopa.generated.gpd.model.PaymentPositionModelDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AcaServiceTests {
    private val gpdClient: GpdClient = mock()
    private val ibansClient: IbansClient = mock()
    private val acaUtils = AcaUtils()
    private val acaService = AcaService(gpdClient, ibansClient, acaUtils)
    private val paymentPositionModelDtoCaptor: KArgumentCaptor<PaymentPositionModelDto> =
        argumentCaptor<PaymentPositionModelDto>()

    companion object {
        private const val paFiscalCode = "77777777777"
        private const val iuv = "302001069073736640"
        val iupd = Iupd(paFiscalCode, iuv)
        const val ibanTest = "IT55555555555555"
        const val ibanTestUpdate = "IT66666666666666"
        const val companyName = "company name"
    }
    @Test
    fun `create position successfully`() = runTest {
        val requestCreatePosition = AcaTestUtils.createPositionRequestBody(iupd, 10)
        val responseCreate = AcaTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.error(GpdPositionNotFoundException()))
        given(ibansClient.getIban(any(), any(), any(), any()))
            .willReturn(Mono.just(Pair(ibanTest, companyName)))
        given(gpdClient.createDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(1))
            .getIban(any(), any(), eq(requestCreatePosition.paFiscalCode), anyOrNull())
        verify(gpdClient, Mockito.times(1))
            .createDebtPosition(
                requestCreatePosition.paFiscalCode,
                acaUtils.toPaymentPositionModelDto(
                    requestCreatePosition,
                    iupd,
                    ibanTest,
                    companyName
                )
            )
        assertEquals(iupd.value(), responseCreate.iupd)
    }

    @Test
    fun `create position full params successfully`() = runTest {
        val iban: String = "IT99C0222211111000000000000"
        val postalIban: String = "IT99C0222211111000000000000"
        val requestCreatePosition =
            AcaTestUtils.createPositionRequestBody(iupd, 10, iban, postalIban, true)
        executeFullParamTest(requestCreatePosition, iban, postalIban)
    }

    @Test
    fun `create position full params (except postalIban) successfully`() = runTest {
        val iban: String = "IT99C0222211111000000000000"
        val requestCreatePosition =
            AcaTestUtils.createPositionRequestBody(iupd, 10, iban, postalIban = null, true)
        executeFullParamTest(requestCreatePosition, iban, expectedPostalIban = null)
    }

    @Test
    fun `create position full params (except iban) successfully`() = runTest {
        val iban: String = "IT99C0222211111000000000000"
        val postalIban: String = "IT99C0222211111000000000000"
        val requestCreatePosition =
            AcaTestUtils.createPositionRequestBody(iupd, 10, iban, postalIban, true)
        executeFullParamTest(requestCreatePosition, expectedIban = iban, postalIban)
    }

    @Test
    fun `create position amount error`() = runTest {
        val requestCreatePosition = AcaTestUtils.createPositionRequestBody(iupd, 0)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.error(GpdPositionNotFoundException()))
        /* Asserts */
        val exception =
            assertThrows<RestApiException> { acaService.handleDebitPosition(requestCreatePosition) }
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.httpStatus)
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any(), anyOrNull())
        verify(ibansClient, Mockito.times(0)).getIban(any(), any(), any(), any())
    }

    @Test
    fun `exception position status error`() = runTest {
        val requestCreatePosition = AcaTestUtils.createPositionRequestBody(iupd, 10)
        val responseGetPosition =
            AcaTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.PAID
            )
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseGetPosition))
        /* Asserts */
        val exception =
            assertThrows<RestApiException> { acaService.handleDebitPosition(requestCreatePosition) }
        assertEquals(HttpStatus.CONFLICT, exception.httpStatus)
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any(), anyOrNull())
        verify(ibansClient, Mockito.times(0)).getIban(any(), any(), any(), any())
    }

    @Test
    fun `invalidate position successfully`() = runTest {
        val requestCreatePosition = AcaTestUtils.createPositionRequestBody(iupd, 0)
        val responseGetPosition =
            AcaTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val responseInvalidate = AcaTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseGetPosition))
        given(gpdClient.invalidateDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseInvalidate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(gpdClient, Mockito.times(1))
            .invalidateDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(0)).getIban(any(), any(), any(), any())
        verify(gpdClient, Mockito.times(0))
            .createDebtPosition(
                requestCreatePosition.paFiscalCode,
                acaUtils.toPaymentPositionModelDto(
                    requestCreatePosition,
                    iupd,
                    ibanTest,
                    companyName
                )
            )
    }

    @Test
    fun `update position (no iban, no postalIban) successfully`() = runTest {
        val requestCreatePosition = AcaTestUtils.createPositionRequestBody(iupd, 10)
        val responseGetPosition =
            AcaTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val responseCreate = AcaTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseGetPosition))
        given(ibansClient.getIban(any(), any(), any(), any()))
            .willReturn(Mono.just(Pair(ibanTestUpdate, companyName)))
        given(
                gpdClient.updateDebtPosition(
                    any(),
                    any(),
                    paymentPositionModelDtoCaptor.capture(),
                    anyOrNull()
                )
            )
            .willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(1))
            .getIban(any(), any(), eq(requestCreatePosition.paFiscalCode), anyOrNull())
        verify(gpdClient, Mockito.times(1))
            .updateDebtPosition(
                requestCreatePosition.paFiscalCode,
                iupd.value(),
                acaUtils.updateOldDebitPositionObject(
                    responseGetPosition,
                    requestCreatePosition,
                    iupd,
                    ibanTestUpdate,
                    companyName
                )
            )
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any(), anyOrNull())
        verify(gpdClient, Mockito.times(0))
            .invalidateDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        assertEquals(
            ibanTestUpdate,
            paymentPositionModelDtoCaptor.firstValue.paymentOption?.get(0)?.transfer?.get(0)?.iban
        )
    }

    @Test
    fun `update position (no postalIban) successfully`() = runTest {
        val requestCreatePosition =
            AcaTestUtils.createPositionRequestBody(
                iupd,
                10,
                ibanTestUpdate,
                postalIban = null,
                true
            )
        val responseGetPosition =
            AcaTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val responseCreate = AcaTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseGetPosition))
        given(ibansClient.getIban(any(), any(), any(), any()))
            .willReturn(Mono.just(Pair(ibanTestUpdate, companyName)))
        given(
                gpdClient.updateDebtPosition(
                    any(),
                    any(),
                    paymentPositionModelDtoCaptor.capture(),
                    anyOrNull()
                )
            )
            .willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(0))
            .getIban(any(), any(), eq(requestCreatePosition.paFiscalCode), anyOrNull())
        verify(gpdClient, Mockito.times(1))
            .updateDebtPosition(
                requestCreatePosition.paFiscalCode,
                iupd.value(),
                acaUtils.updateOldDebitPositionObject(
                    responseGetPosition,
                    requestCreatePosition,
                    iupd,
                    ibanTestUpdate,
                    companyName
                )
            )
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any(), anyOrNull())
        verify(gpdClient, Mockito.times(0))
            .invalidateDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        assertEquals(
            ibanTestUpdate,
            paymentPositionModelDtoCaptor.firstValue.paymentOption?.get(0)?.transfer?.get(0)?.iban
        )
    }

    @Test
    fun `update position (no iban) successfully`() = runTest {
        val postalIbanUpdate: String = "IT99C0222211111000000000000"
        val requestCreatePosition =
            AcaTestUtils.createPositionRequestBody(iupd, 10, iban = null, postalIbanUpdate, true)
        val responseGetPosition =
            AcaTestUtils.responseGetPosition(
                iupd,
                10,
                ibanTest,
                PaymentPositionModelBaseResponseDto.StatusEnum.DRAFT
            )
        val responseCreate = AcaTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseGetPosition))
        given(ibansClient.getIban(any(), any(), any(), any()))
            .willReturn(Mono.just(Pair(ibanTestUpdate, companyName)))
        given(
                gpdClient.updateDebtPosition(
                    any(),
                    any(),
                    paymentPositionModelDtoCaptor.capture(),
                    anyOrNull()
                )
            )
            .willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1))
            .getDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        verify(ibansClient, Mockito.times(1))
            .getIban(any(), any(), eq(requestCreatePosition.paFiscalCode), anyOrNull())
        verify(gpdClient, Mockito.times(1))
            .updateDebtPosition(
                requestCreatePosition.paFiscalCode,
                iupd.value(),
                acaUtils.updateOldDebitPositionObject(
                    responseGetPosition,
                    requestCreatePosition,
                    iupd,
                    ibanTestUpdate,
                    companyName,
                    postalIbanUpdate
                )
            )
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any(), anyOrNull())
        verify(gpdClient, Mockito.times(0))
            .invalidateDebtPosition(requestCreatePosition.paFiscalCode, iupd.value())
        assertEquals(
            ibanTestUpdate,
            paymentPositionModelDtoCaptor.firstValue.paymentOption?.get(0)?.transfer?.get(0)?.iban
        )
    }

    private suspend fun executeFullParamTest(
        requestCreatePosition: NewDebtPositionRequestDto,
        expectedIban: String?,
        expectedPostalIban: String?
    ) {
        val responseCreate = AcaTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.error(GpdPositionNotFoundException()))
        given(ibansClient.getIban(any(), any(), any(), any()))
            .willReturn(Mono.just(Pair(ibanTest, companyName)))
        given(gpdClient.createDebtPosition(any(), any(), anyOrNull()))
            .willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1)).getDebtPosition(any(), any(), anyOrNull())
        verify(ibansClient, Mockito.times(0))
            .getIban(any(), any(), eq(requestCreatePosition.paFiscalCode), anyOrNull())
        verify(gpdClient, Mockito.times(1))
            .createDebtPosition(
                requestCreatePosition.paFiscalCode,
                acaUtils.toPaymentPositionModelDto(
                    requestCreatePosition,
                    iupd,
                    expectedIban,
                    requestCreatePosition.companyName,
                    expectedPostalIban
                )
            )
        assertEquals(iupd.value(), responseCreate.iupd)
    }
}
