package br.com.amandaluz.cielotickets.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.amandaluz.cielotickets.MainActivity
import br.com.amandaluz.cielotickets.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationTest {

    @Test
    fun homeActionsDoNotOverlapHeader() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val subtitle = activity.findViewById<android.view.View>(R.id.appSubtitle)
                val sellButton = activity.findViewById<android.view.View>(R.id.sellButton)
                val historyButton = activity.findViewById<android.view.View>(R.id.historyButton)

                assertTrue(sellButton.top >= subtitle.bottom)
                assertTrue(historyButton.top >= sellButton.bottom)
            }
        }
    }

    @Test
    fun navigatesFromHomeToEventsAndBack() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.sellButton)).perform(click())
            onView(withText(R.string.events_title)).check(matches(isDisplayed()))
            onView(withText("Festival Gastronômico")).check(matches(isDisplayed()))

            pressBack()

            onView(withId(R.id.sellButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun addsTicketAndOpensCartBottomSheet() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.sellButton)).perform(click())
            onView(
                withContentDescription(
                    "Adicionar ingresso para Festival Gastronômico",
                ),
            ).perform(click())
            onView(withId(R.id.cartButton)).perform(click())

            onView(withText(R.string.your_cart)).check(matches(isDisplayed()))
            onView(withText("Festival Gastronômico")).check(matches(isDisplayed()))
            onView(
                allOf(
                    withId(R.id.cartTotal),
                    withText(containsString("35,00")),
                ),
            ).check(matches(isDisplayed()))
        }
    }

    @Test
    fun navigatesFromHomeToHistory() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.historyButton)).perform(click())

            onView(withText(R.string.history_title)).check(matches(isDisplayed()))
            onView(withText(R.string.history_next_title)).check(matches(isDisplayed()))
        }
    }
}
