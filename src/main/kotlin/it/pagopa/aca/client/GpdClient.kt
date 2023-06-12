package it.pagopa.aca.client

import it.pagopa.aca.exceptions.GpdException
import it.pagopa.generated.gpd.api.DebtPositionsApiApi
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import it.pagopa.generated.gpd.model.PaymentPositionModelDto
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class GpdClient(@Autowired @Qualifier("gpdApiClient") private val client: DebtPositionsApiApi) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun getDebtPosition(
        creditorInstitutionCode: String,
        iupd: String
    ): PaymentPositionModelBaseResponseDto {
        val response: Mono<PaymentPositionModelBaseResponseDto> =
            try {
                logger.info(
                    "Querying gpd to retrieve debt position for creditorInstitutionCode: $creditorInstitutionCode, iupd: $iupd"
                )
                client.getOrganizationDebtPositionByIUPD(creditorInstitutionCode, iupd)
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response
            .onErrorMap(WebClientResponseException::class.java) {
                logger.error(
                    "Error communicating with gpd. Received response: ${it.responseBodyAsString}"
                )
                when (it.statusCode) {
                    HttpStatus.UNAUTHORIZED ->
                        GpdException(
                            description = "Error while call gpd unauthorized request",
                            httpStatusCode = HttpStatus.UNAUTHORIZED
                        )
                    HttpStatus.NOT_FOUND ->
                        GpdException(
                            description =
                                "No debt position found with Creditor institution code: $creditorInstitutionCode and iupd: $iupd",
                            httpStatusCode = HttpStatus.NOT_FOUND
                        )
                    HttpStatus.INTERNAL_SERVER_ERROR ->
                        GpdException(
                            description = "Bad gateway, error while execute request",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                    else ->
                        GpdException(
                            description = "Gpd error: ${it.statusCode}",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                }
            }
            .awaitSingle()
    }

    suspend fun createDebtPosition(
        creditorInstitutionCode: String,
        debitPositionToCreate: PaymentPositionModelDto
    ): PaymentPositionModelDto {
        val response: Mono<PaymentPositionModelDto> =
            try {
                logger.info(
                    "Querying gpd to create debt position for creditorInstitutionCode: $creditorInstitutionCode with iupd: ${debitPositionToCreate.iupd}"
                )
                client.createPosition(creditorInstitutionCode, debitPositionToCreate, true)
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response
            .onErrorMap(WebClientResponseException::class.java) {
                logger.error(
                    "Error communicating with gpd. Received response: ${it.responseBodyAsString}"
                )
                when (it.statusCode) {
                    HttpStatus.BAD_REQUEST ->
                        GpdException(
                            description = "Bad request",
                            httpStatusCode = HttpStatus.BAD_REQUEST
                        )
                    HttpStatus.UNAUTHORIZED ->
                        GpdException(
                            description = "Error while call gpd unauthorized request",
                            httpStatusCode = HttpStatus.UNAUTHORIZED
                        )
                    HttpStatus.CONFLICT ->
                        GpdException(
                            description =
                                "Error while create new debit position conflict into request",
                            httpStatusCode = HttpStatus.CONFLICT
                        )
                    HttpStatus.INTERNAL_SERVER_ERROR ->
                        GpdException(
                            description = "Bad gateway, error while execute request",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                    else ->
                        GpdException(
                            description = "Gpd error: ${it.statusCode}",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                }
            }
            .awaitSingle()
    }

    suspend fun updateDebtPosition(
        creditorInstitutionCode: String,
        iupd: String,
        debitPositionToUpdate: PaymentPositionModelDto
    ): PaymentPositionModelDto {
        val response: Mono<PaymentPositionModelDto> =
            try {
                logger.info(
                    "Querying gpd to update debt position for creditorInstitutionCode: $creditorInstitutionCode with iupd: $iupd"
                )
                client.updatePosition(creditorInstitutionCode, iupd, debitPositionToUpdate, true)
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response
            .onErrorMap(WebClientResponseException::class.java) {
                logger.error(
                    "Error communicating with gpd. Received response: ${it.responseBodyAsString}"
                )
                when (it.statusCode) {
                    HttpStatus.BAD_REQUEST ->
                        GpdException(
                            description = "Bad request",
                            httpStatusCode = HttpStatus.BAD_REQUEST
                        )
                    HttpStatus.UNAUTHORIZED ->
                        GpdException(
                            description = "Error while call gpd unauthorized request",
                            httpStatusCode = HttpStatus.UNAUTHORIZED
                        )
                    HttpStatus.NOT_FOUND ->
                        GpdException(
                            description =
                                "No debt position found with Creditor institution code: $creditorInstitutionCode and iupd: $iupd",
                            httpStatusCode = HttpStatus.NOT_FOUND
                        )
                    HttpStatus.CONFLICT ->
                        GpdException(
                            description =
                                "Error while create new debit position conflict into request",
                            httpStatusCode = HttpStatus.CONFLICT
                        )
                    HttpStatus.INTERNAL_SERVER_ERROR ->
                        GpdException(
                            description = "Bad gateway, error while execute request",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                    else ->
                        GpdException(
                            description = "Gpd error: ${it.statusCode}",
                            httpStatusCode = HttpStatus.BAD_GATEWAY
                        )
                }
            }
            .awaitSingle()
    }
}
