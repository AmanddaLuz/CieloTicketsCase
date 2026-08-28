package br.com.amandaluz.cielotickets.data.local.dao

sealed interface PurchaseStatusUpdateDataResult {
    data object Updated : PurchaseStatusUpdateDataResult
    data object NotFound : PurchaseStatusUpdateDataResult
    data class StatusMismatch(val actualStatus: String) : PurchaseStatusUpdateDataResult
}

