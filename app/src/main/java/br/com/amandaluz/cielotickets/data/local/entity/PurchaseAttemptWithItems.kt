package br.com.amandaluz.cielotickets.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Projeção relacional de uma tentativa com seus itens.
 *
 * Não representa uma terceira tabela. O Room combina a entidade pai com as
 * entidades filhas cujo `attemptReference` corresponde à referência.
 *
 * @Embedded: Indica que os campos principais vêm da tentativa:
 */
data class PurchaseAttemptWithItems(
    @Embedded
    val attempt: PurchaseAttemptEntity,
    @Relation(
        parentColumn = "reference",
        entityColumn = "attemptReference",
    )
    val items: List<PurchaseItemEntity>,
)
