package br.com.amandaluz.cielotickets.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentStatusTest {

    @Test
    fun createdTransitionsOnlyToProcessing() {
        assertTrue(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.PROCESSING))
        assertFalse(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.APPROVED))
    }

    @Test
    fun processingTransitionsToEveryTerminalStatus() {
        assertTrue(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.APPROVED))
        assertTrue(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.DENIED))
        assertTrue(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.CANCELLED))
        assertTrue(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.ERROR))
    }

    @Test
    fun terminalStatusesCannotTransitionToAnotherStatus() {
        assertFalse(PaymentStatus.APPROVED.canTransitionTo(PaymentStatus.ERROR))
        assertFalse(PaymentStatus.DENIED.canTransitionTo(PaymentStatus.PROCESSING))
        assertFalse(PaymentStatus.CANCELLED.canTransitionTo(PaymentStatus.APPROVED))
        assertFalse(PaymentStatus.ERROR.canTransitionTo(PaymentStatus.PROCESSING))
    }

    @Test
    fun repeatedStatusIsNotAStateTransition() {
        PaymentStatus.entries.forEach { status ->
            assertFalse(status.canTransitionTo(status))
        }
    }
}
