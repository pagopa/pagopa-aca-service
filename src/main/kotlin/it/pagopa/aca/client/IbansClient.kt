package it.pagopa.aca.client

import it.pagopa.aca.exceptions.ApiConfigException
import it.pagopa.generated.apiconfig.api.IbansApi
import it.pagopa.generated.apiconfig.model.IbansEnhancedDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class IbansClient(@Autowired @Qualifier("ibansApiClient") private val client: IbansApi) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val CREDITOR_INSTITUTION_LABEL = "ACA"
    }

    fun getIban(creditorInstitutionCode: String, requestId: String): Mono<Pair<String, String?>> {
        val response: Mono<IbansEnhancedDto> =
            try {
                logger.info(
                    "Querying api config to retrieve iban for creditorInstitutionCode: $creditorInstitutionCode, request id: $requestId"
                )
                client.getIbans(
                    0,
                    creditorInstitutionCode,
                    requestId,
                    5,
                    CREDITOR_INSTITUTION_LABEL
                )
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response
            .map {
                // TODO here we have to just take the first returned IbanDto object?
                val ibanDto = it.ibansEnhanced[0]
                Pair(ibanDto.iban, ibanDto.companyName)
            }
            .onErrorMap(WebClientResponseException::class.java) {
                logger.error(
                    "Error communicating with api config during get iban. Received response code: ${it.statusCode} and response: ${it.responseBodyAsString}"
                )
                when (it.statusCode) {
                    HttpStatus.BAD_REQUEST ->
                        ApiConfigException(
                            description = "Bad request",
                            httpStatusCode = HttpStatus.BAD_REQUEST
                        )
                    HttpStatus.UNAUTHORIZED ->
                        ApiConfigException(
                            description = "Internal server error",
                            httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        )
                    HttpStatus.FORBIDDEN ->
                        ApiConfigException(
                            description =
                                "Bad gateway, api config forbidden getCreditorInstitutionsIbansEnhanced method",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                    HttpStatus.NOT_FOUND ->
                        ApiConfigException(
                            description =
                                "Creditor institution code: $creditorInstitutionCode not found",
                            httpStatusCode = HttpStatus.NOT_FOUND
                        )
                    HttpStatus.TOO_MANY_REQUESTS ->
                        ApiConfigException(
                            description = "Internal server error",
                            httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        )
                    HttpStatus.INTERNAL_SERVER_ERROR ->
                        ApiConfigException(
                            description = "Bad gateway, api config internal server error",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                    else ->
                        ApiConfigException(
                            description = "Api config error: ${it.statusCode}",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                }
            }
    }
}
