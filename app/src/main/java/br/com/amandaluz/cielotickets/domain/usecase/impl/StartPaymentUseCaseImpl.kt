package br.com.amandaluz.cielotickets.domain.usecase.impl

import br.com.amandaluz.cielotickets.domain.gateway.PaymentGateway
import br.com.amandaluz.cielotickets.domain.model.PaymentStatus
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.usecase.StartPaymentUseCase
import br.com.amandaluz.cielotickets.domain.usecase.UpdatePurchaseStatusUseCase

class StartPaymentUseCaseImpl(
    private val paymentGateway: PaymentGateway,
    private val updatePurchaseStatus: UpdatePurchaseStatusUseCase,
) : StartPaymentUseCase {

    override suspend fun invoke(attempt: PurchaseAttempt): StartPaymentUseCase.Result =
        when (
            val processingResult = updatePurchaseStatus(
                attempt.reference,
                PaymentStatus.PROCESSING,
            )
        ) {
            is UpdatePurchaseStatusUseCase.Result.Updated -> startGateway(attempt)
            is UpdatePurchaseStatusUseCase.Result.Unchanged -> {
                StartPaymentUseCase.Result.AlreadyProcessing(attempt.reference)
            }
            is UpdatePurchaseStatusUseCase.Result.NotFound -> {
                StartPaymentUseCase.Result.NotFound(attempt.reference)
            }
            is UpdatePurchaseStatusUseCase.Result.InvalidTransition -> {
                StartPaymentUseCase.Result.InvalidStatus(
                    reference = attempt.reference,
                    currentStatus = processingResult.currentStatus,
                )
            }
        }

    private suspend fun startGateway(
        attempt: PurchaseAttempt,
    ): StartPaymentUseCase.Result =
        when (paymentGateway.initiatePayment(attempt)) {
            PaymentGateway.Result.Initiated -> {
                StartPaymentUseCase.Result.Started(attempt.reference)
            }
            PaymentGateway.Result.AppNotAvailable -> {
                resolveGatewayFailure(
                    reference = attempt.reference,
                    gatewayResult = StartPaymentUseCase.Result.AppNotAvailable(
                        attempt.reference,
                    ),
                )
            }
            PaymentGateway.Result.CredentialsNotConfigured -> {
                resolveGatewayFailure(
                    reference = attempt.reference,
                    gatewayResult = StartPaymentUseCase.Result.CredentialsNotConfigured(
                        attempt.reference,
                    ),
                )
            }
            PaymentGateway.Result.TechnicalFailure -> {
                resolveGatewayFailure(
                    reference = attempt.reference,
                    gatewayResult = StartPaymentUseCase.Result.TechnicalFailure(
                        attempt.reference,
                    ),
                )
            }
        }

    private suspend fun resolveGatewayFailure(
        reference: String,
        gatewayResult: StartPaymentUseCase.Result,
    ): StartPaymentUseCase.Result =
        when (val result = updatePurchaseStatus(reference, PaymentStatus.ERROR)) {
            is UpdatePurchaseStatusUseCase.Result.Updated,
            is UpdatePurchaseStatusUseCase.Result.Unchanged,
            -> gatewayResult
            is UpdatePurchaseStatusUseCase.Result.NotFound -> {
                StartPaymentUseCase.Result.NotFound(reference)
            }
            is UpdatePurchaseStatusUseCase.Result.InvalidTransition -> {
                StartPaymentUseCase.Result.InvalidStatus(
                    reference = reference,
                    currentStatus = result.currentStatus,
                )
            }
        }
}
