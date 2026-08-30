package br.com.amandaluz.cielotickets.feature.checkout.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.PaymentMethod
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

interface CreatePurchaseAttemptUseCase {
    operator fun invoke(cart: Cart, paymentMethod: PaymentMethod): PurchaseAttempt
}

