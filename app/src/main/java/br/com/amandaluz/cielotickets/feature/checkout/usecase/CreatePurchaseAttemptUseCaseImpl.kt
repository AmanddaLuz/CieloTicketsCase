package br.com.amandaluz.cielotickets.feature.checkout.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import java.util.UUID

class CreatePurchaseAttemptUseCaseImpl(
    private val generateReference: () -> String = { UUID.randomUUID().toString() },
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : CreatePurchaseAttemptUseCase {
    override fun invoke(
        cart: Cart,
        paymentMethod: PaymentMethod,
    ): PurchaseAttempt = PurchaseAttempt.create(
        reference = generateReference(),
        cart = cart,
        paymentMethod = paymentMethod,
        createdAt = currentTimeMillis(),
    )
}
