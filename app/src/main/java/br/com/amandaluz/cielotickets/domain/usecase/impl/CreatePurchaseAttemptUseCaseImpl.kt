package br.com.amandaluz.cielotickets.domain.usecase.impl

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt
import br.com.amandaluz.cielotickets.domain.usecase.CreatePurchaseAttemptUseCase
import java.util.UUID

class CreatePurchaseAttemptUseCaseImpl(
    private val generateReference: () -> String = { UUID.randomUUID().toString() },
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : CreatePurchaseAttemptUseCase {
    override fun invoke(cart: Cart): PurchaseAttempt = PurchaseAttempt.create(
        reference = generateReference(),
        cart = cart,
        createdAt = currentTimeMillis(),
    )
}
