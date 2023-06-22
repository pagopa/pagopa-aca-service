package it.pagopa.aca.exceptions

import org.springframework.http.HttpStatus

class GpdPositionNotFoundException() : ApiError("Debit position not found") {
    override fun toRestException() =
        RestApiException(
            httpStatus = HttpStatus.NOT_FOUND,
            description = "Debit position not found",
            title = "Gpd Invocation exception"
        )
}
