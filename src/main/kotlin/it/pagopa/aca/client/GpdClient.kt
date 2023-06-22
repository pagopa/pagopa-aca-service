package it.pagopa.aca.client

import it.pagopa.aca.exceptions.GpdException
import it.pagopa.generated.gpd.api.DebtPositionActionsApiApi
import it.pagopa.generated.gpd.api.DebtPositionsApiApi
import it.pagopa.generated.gpd.model.PaymentPositionModelBaseResponseDto
import it.pagopa.generated.gpd.model.PaymentPositionModelDto
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class GpdClient(
    @Autowired @Qualifier("gpdApiClient") private val client: DebtPositionsApiApi,
    @Autowired
    @Qualifier("gpdApiClientActions")
    private val clientForActions: DebtPositionActionsApiApi
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getDebtPosition(
        creditorInstitutionCode: String,
        iupd: String
    ): Optional<Mono<PaymentPositionModelBaseResponseDto>> {
        val response: Mono<PaymentPositionModelBaseResponseDto> =
            try {
                logger.info(
                    "Querying gpd to retrieve debt position for creditorInstitutionCode: $creditorInstitutionCode, iupd: $iupd"
                )
                client.getOrganizationDebtPositionByIUPD(creditorInstitutionCode, iupd)
            } catch (e: WebClientResponseException) {
                if (e.statusCode == HttpStatus.NOT_FOUND) {
                    return Optional.empty()
                } else {
                    Mono.error(e)
                }
            }
        return Optional.of(
            response.onErrorMap(WebClientResponseException::class.java) {
                logger.error(
                    "Error communicating with gpd. Received response: ${it.responseBodyAsString}"
                )
                when (it.statusCode) {
                    HttpStatus.UNAUTHORIZED ->
                        GpdException(
                            description = "Error while call gpd unauthorized request",
                            httpStatusCode = HttpStatus.UNAUTHORIZED
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
        )
    }

    fun createDebtPosition(
        creditorInstitutionCode: String,
        debitPositionToCreate: PaymentPositionModelDto
    ): Mono<PaymentPositionModelDto> {
        val response: Mono<PaymentPositionModelDto> =
            try {
                logger.info(
                    "Querying gpd to create debt position for creditorInstitutionCode: $creditorInstitutionCode with iupd: ${debitPositionToCreate.iupd}"
                )
                client.createPosition(creditorInstitutionCode, debitPositionToCreate, true)
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response.onErrorMap(WebClientResponseException::class.java) {
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
                        description = "Error while create new debit position conflict into request",
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
    }

    fun updateDebtPosition(
        creditorInstitutionCode: String,
        iupd: String,
        debitPositionToUpdate: PaymentPositionModelDto
    ): Mono<PaymentPositionModelDto> {
        val response: Mono<PaymentPositionModelDto> =
            try {
                logger.info(
                    "Querying gpd to update debt position for creditorInstitutionCode: $creditorInstitutionCode with iupd: $iupd"
                )
                client.updatePosition(creditorInstitutionCode, iupd, debitPositionToUpdate, true)
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response.onErrorMap(WebClientResponseException::class.java) {
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
                        description = "Error while create new debit position conflict into request",
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
    }

    fun invalidateDebtPosition(
        creditorInstitutionCode: String,
        iupd: String
    ): Mono<PaymentPositionModelDto> {
        val response: Mono<PaymentPositionModelDto> =
            try {
                logger.info("Querying gpd to invalidate debt position with iupd: $iupd")
                clientForActions.invalidatePosition(creditorInstitutionCode, iupd)
            } catch (e: WebClientResponseException) {
                Mono.error(e)
            }
        return response.onErrorMap(WebClientResponseException::class.java) {
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
                            "Error while invalidate debit position. Debit position not found with iupd: $iupd",
                        httpStatusCode = HttpStatus.NOT_FOUND
                    )
                HttpStatus.CONFLICT ->
                    GpdException(
                        description = "Error while invalidate debit position conflict into request",
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
    }
}
