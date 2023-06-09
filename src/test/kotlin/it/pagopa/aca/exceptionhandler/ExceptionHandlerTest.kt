package it.pagopa.aca.exceptionhandler

import it.pagopa.aca.AcaTestUtils
import it.pagopa.aca.exceptions.ApiConfigException
import it.pagopa.aca.exceptions.RestApiException
import jakarta.xml.bind.ValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ExceptionHandlerTest {

    private val exceptionHandler = ExceptionHandler()

    @Test
    fun `Should handle RestApiException`() {
        val response =
            exceptionHandler.handleException(
                RestApiException(
                    httpStatus = HttpStatus.UNAUTHORIZED,
                    title = "title",
                    description = "description"
                )
            )
        assertEquals(
            AcaTestUtils.buildProblemJson(
                httpStatus = HttpStatus.UNAUTHORIZED,
                title = "title",
                description = "description"
            ),
            response.body
        )
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `Should handle ApiError`() {
        val exception =
            ApiConfigException(
                httpStatusCode = HttpStatus.UNAUTHORIZED,
                description = "description"
            )
        val response = exceptionHandler.handleException(exception)
        assertEquals(
            AcaTestUtils.buildProblemJson(
                httpStatus = HttpStatus.UNAUTHORIZED,
                title = "ApiConfig Invocation exception",
                description = "description"
            ),
            response.body
        )
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `Should handle ValidationExceptions`() {
        val exception = ValidationException("Invalid request")
        val response = exceptionHandler.handleRequestValidationException(exception)
        assertEquals(
            AcaTestUtils.buildProblemJson(
                httpStatus = HttpStatus.BAD_REQUEST,
                title = "Bad request",
                description = "Invalid request"
            ),
            response.body
        )
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `Should handle generic exception`() {
        val exception = NullPointerException("Nullpointer exception")
        val response = exceptionHandler.handleGenericException(exception)
        assertEquals(
            AcaTestUtils.buildProblemJson(
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                title = "Error processing the request",
                description = "Nullpointer exception"
            ),
            response.body
        )
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
