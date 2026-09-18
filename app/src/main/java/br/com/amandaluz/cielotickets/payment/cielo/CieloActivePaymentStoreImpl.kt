package br.com.amandaluz.cielotickets.payment.cielo

import android.content.Context
import androidx.core.content.edit

class CieloActivePaymentStoreImpl(
    context: Context,
) : CieloActivePaymentStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    override fun claim(reference: String): Boolean {
        require(reference.isNotBlank()) { "Payment reference must not be blank" }
        return synchronized(lock) {
            val current = currentReference()
            when {
                current != null && current != reference -> false
                else -> {
                    preferences.edit {
                        putString(KEY_REFERENCE, reference)
                    }
                    true
                }
            }
        }
    }

    override fun currentReference(): String? =
        preferences.getString(KEY_REFERENCE, null)
            ?.takeIf(String::isNotBlank)

    override fun replace(
        expectedReference: String,
        newReference: String,
    ): Boolean {
        require(expectedReference.isNotBlank()) {
            "Expected payment reference must not be blank"
        }
        require(newReference.isNotBlank()) {
            "New payment reference must not be blank"
        }
        return synchronized(lock) {
            if (currentReference() != expectedReference) {
                false
            } else {
                preferences.edit {
                    putString(KEY_REFERENCE, newReference)
                }
                true
            }
        }
    }

    override fun clear(reference: String) {
        require(reference.isNotBlank()) { "Payment reference must not be blank" }
        synchronized(lock) {
            if (currentReference() == reference) {
                preferences.edit {
                    remove(KEY_REFERENCE)
                }
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "cielo_active_payment"
        const val KEY_REFERENCE = "reference"
        val lock = Any()
    }
}
