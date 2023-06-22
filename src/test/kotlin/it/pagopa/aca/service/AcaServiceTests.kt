package it.pagopa.aca.service

import it.pagopa.aca.ObjectTestUtils
import it.pagopa.aca.client.GpdClient
import it.pagopa.aca.client.IbansClient
import it.pagopa.aca.domain.Iupd
import it.pagopa.aca.services.AcaService
import it.pagopa.aca.utils.AcaUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import java.util.*

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AcaServiceTests {
    private val gpdClient: GpdClient = mock()
    private val ibansClient: IbansClient = mock()
    private val acaUtils = AcaUtils()
    private val acaService = AcaService(gpdClient, ibansClient, acaUtils)

    companion object {
        private const val creditorInstitutionCode = "77777777777"
        private const val iuv = "302001069073736640"
        val iupd = Iupd(creditorInstitutionCode, iuv)
    }
    @Test
    fun `create position successfully`() = runTest {
        val requestCreatePosition = ObjectTestUtils.createPositionRequestBody(iupd, 10)
        val responseCreate = ObjectTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any())).willReturn(Optional.empty())
        given(ibansClient.getIban(any(), any())).willReturn(Mono.just(Pair("ITORITORITORIT", "entityName")))

        given(gpdClient.createDebtPosition(anyOrNull(), anyOrNull())).willReturn(Mono.just(responseCreate))
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1)).getDebtPosition(any(), any())
        verify(ibansClient, Mockito.times(1)).getIban(any(), any())
        verify(gpdClient, Mockito.times(1)).createDebtPosition(any(), anyOrNull())
        // mocking
        assertEquals(iupd.value(), responseCreate.iupd)
    }

    @Test
    fun `create position amount error`() = runTest {
        val requestCreatePosition = ObjectTestUtils.createPositionRequestBody(iupd, 0)
        val responseCreate = ObjectTestUtils.debitPositionModelResponse(iupd)
        /* preconditions */
        given(gpdClient.getDebtPosition(any(), any())).willReturn(Optional.empty())
        /* tests */
        acaService.handleDebitPosition(requestCreatePosition)
        /* Asserts */
        verify(gpdClient, Mockito.times(1)).getDebtPosition(any(), any())
        verify(gpdClient, Mockito.times(0)).createDebtPosition(any(), any())
        verify(ibansClient, Mockito.times(0)).getIban(any(), any())
        // mocking
        assertEquals(iupd.value(), responseCreate.iupd)
    }
}
