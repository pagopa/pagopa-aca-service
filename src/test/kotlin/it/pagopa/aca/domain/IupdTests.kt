package it.pagopa.aca.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IupdTests {

    private val fiscalCode = "77777777777"

    private val iuv = "302000100000009424"

    @Test
    fun shouldCreateTransactionIdSuccessfully() {
        val iupd = "ACA_${fiscalCode}_${iuv}"
        assertEquals(iupd, Iupd(fiscalCode, iuv).value())
    }
}
