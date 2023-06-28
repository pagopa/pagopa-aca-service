package it.pagopa.aca.exceptions

import org.springframework.http.HttpStatus

class GpdException(private val description: String, private val httpStatusCode: HttpStatus) :
    ApiError(description) {
    override fun toRestException() =
        RestApiException(
            httpStatus = httpStatusCode,
            description = description,
            title = "Invocation exception"
        )
}
