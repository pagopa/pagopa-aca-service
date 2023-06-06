package it.pagopa.aca.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IupdTests {

    private val fiscalCode = "77777777777"

    private val iuv = "302000100000009424"

    @Test
    fun shouldCreateTransactionIdSuccessfully() {
        val iupd =
            StringBuilder().append("ACA_").append(fiscalCode).append("_").append(iuv).toString()
        assertEquals(iupd, Iupd(fiscalCode, iuv).value())
    }
}
