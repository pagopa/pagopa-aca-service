package it.pagopa.aca

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ApplicationTest {

    @Test
    fun contextLoading() {
        // check only if the context is loaded
        assertTrue(true)
    }
}
