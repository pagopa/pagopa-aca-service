package it.pagopa.aca.client

import it.pagopa.aca.exceptions.ApiConfigException
import it.pagopa.generated.apiconfig.api.CreditorInstitutionsApi
import it.pagopa.generated.apiconfig.model.CreditorInstitutionDetailsDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class CreditorInstitutionClient(
    @Autowired
    @Qualifier("creditorInstitutionApiClient")
    private val client: CreditorInstitutionsApi
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val CREDITOR_INSTITUTION_LABEL = "ACA"
    }

    fun getCreditorInstitution(
        creditorInstitutionCode: String,
        requestId: String
    ): Mono<Pair<String, String>> {
        val response: Mono<CreditorInstitutionDetailsDto> =
            try {
                logger.info(
                    "Querying api config to retrieve creditorInstitution: $creditorInstitutionCode, request id: $requestId"
                )
                client.getCreditorInstitution(
                    creditorInstitutionCode,
                    requestId,
                )
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response
            .map {
                val companyName = it.businessName
                Pair(creditorInstitutionCode, companyName)
            }
            .onErrorMap(WebClientResponseException::class.java) {
                logger.error(
                    "Error communicating with api config during get creditor institution. Received response code: ${it.statusCode} and response: ${it.responseBodyAsString}"
                )
                when (it.statusCode) {
                    HttpStatus.BAD_REQUEST ->
                        ApiConfigException(
                            description = "Bad request",
                            httpStatusCode = HttpStatus.BAD_REQUEST
                        )
                    HttpStatus.FORBIDDEN ->
                        ApiConfigException(
                            description =
                                "Bad gateway, api config forbidden getCreditorInstitution method",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                    HttpStatus.UNAUTHORIZED ->
                        ApiConfigException(
                            description = "Internal server error, unauthorized",
                            httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        )
                    HttpStatus.TOO_MANY_REQUESTS ->
                        ApiConfigException(
                            description = "Internal server error, too many requests",
                            httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        )
                    HttpStatus.NOT_FOUND ->
                        ApiConfigException(
                            description =
                                "Creditor institution code: $creditorInstitutionCode not found",
                            httpStatusCode = HttpStatus.NOT_FOUND
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
