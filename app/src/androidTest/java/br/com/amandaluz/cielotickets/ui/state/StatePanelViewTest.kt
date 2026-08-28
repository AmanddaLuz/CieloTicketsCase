package br.com.amandaluz.cielotickets.ui.state

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.MainActivity
import br.com.amandaluz.cielotickets.R
import com.google.android.material.button.MaterialButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatePanelViewTest {

    @Test
    fun rendersLoadingAndActionableMessage() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val view = StatePanelView(activity)
                view.render(StatePanelUiModel.Loading("Carregando"))

                assertEquals(
                    "Carregando",
                    view.findViewById<TextView>(R.id.stateMessage).text,
                )
                assertEquals(
                    View.VISIBLE,
                    view.findViewById<View>(R.id.progressIndicator).visibility,
                )

                var actionCalled = false
                view.render(
                    StatePanelUiModel.Message(
                        title = "Sem dados",
                        message = "Tente novamente",
                        iconRes = R.drawable.ic_history,
                        actionLabel = "Recarregar",
                    ),
                ) {
                    actionCalled = true
                }

                val action = view.findViewById<MaterialButton>(R.id.stateAction)
                assertEquals(
                    View.GONE,
                    view.findViewById<View>(R.id.progressIndicator).visibility,
                )
                assertEquals(View.VISIBLE, action.visibility)
                action.performClick()
                assertTrue(actionCalled)
            }
        }
    }
}
