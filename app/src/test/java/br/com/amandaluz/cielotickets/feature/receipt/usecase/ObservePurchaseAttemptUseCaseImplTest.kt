package br.com.amandaluz.cielotickets.feature.receipt.usecase

import br.com.amandaluz.cielotickets.testfake.FakePurchaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ObservePurchaseAttemptUseCaseImplTest {

    @Test
    fun observesUnknownReferenceAsNull() = runTest {
        val useCase = ObservePurchaseAttemptUseCaseImpl(
            FakePurchaseRepository(),
        )

        assertNull(useCase("unknown-reference").first())
    }

    @Test
    fun rejectsBlankReference() {
        val useCase = ObservePurchaseAttemptUseCaseImpl(
            FakePurchaseRepository(),
        )

        assertThrows(IllegalArgumentException::class.java) {
            useCase(" ")
        }
    }
}
