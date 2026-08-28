package br.com.amandaluz.cielotickets.domain.usecase

import br.com.amandaluz.cielotickets.domain.model.Cart
import br.com.amandaluz.cielotickets.domain.model.PurchaseAttempt

interface CreatePurchaseAttemptUseCase {
    operator fun invoke(cart: Cart): PurchaseAttempt
}

