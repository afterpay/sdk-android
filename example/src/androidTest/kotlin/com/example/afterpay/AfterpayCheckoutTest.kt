package com.example.afterpay

import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webContent
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.matcher.DomMatchers.hasElementWithId
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.afterpay.android.DisableAnimationsRule
import com.example.afterpay.shopping.ShoppingListAdapter
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

private const val WIDGET_CONTAINER_ID = "afterpay-widget-container"

@RunWith(AndroidJUnit4::class)
class AfterpayCheckoutTest {

    @get:Rule
    val disableAnimations = DisableAnimationsRule()

    @get:Rule
    val activityScenario = ActivityScenarioRule(MainActivity::class.java)

    private val idlingResource = BooleanIdlingResource()

    private val destinationObserver =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            idlingResource.isIdleNow(destination.id == NavGraph.dest.receipt)
        }

    @Before
    fun setup() {
        activityScenario.scenario.onActivity {
            it.findNavController(R.id.nav_host_fragment)
                .addOnDestinationChangedListener(destinationObserver)
        }
    }

    @After
    fun tearDown() {
        activityScenario.scenario.onActivity {
            it.findNavController(R.id.nav_host_fragment)
                .removeOnDestinationChangedListener(destinationObserver)
        }
    }

    @Test
    fun givenCheckoutPerformed_whenCheckoutSucceeds_thenPresentPaymentSchedule() {
        onView(withId(R.id.shopping_recyclerView)).perform(
            actionOnItemAtPosition<ShoppingListAdapter.ViewHolder>(
                0,
                clickChildWithId(R.id.shoppingItem_button_addToCart)
            )
        )

        onView(withId(R.id.shopping_button_viewCart)).perform(click())

        onView(withId(R.id.cart_editText_emailAddress))
            .perform(replaceText("user@example.com"))

        onView(withId(R.id.cart_expressCheckBox)).check(matches(isNotChecked()))
        onView(withId(R.id.cart_buyNowCheckBox)).check(matches(isNotChecked()))
        onView(withId(R.id.cart_pickupCheckBox)).check(matches(isNotChecked()))
        onView(withId(R.id.cart_shippingOptionsRequiredCheckBox)).check(matches(isNotChecked()))

        onView(withId(R.id.cart_button_checkout)).perform(click())
        IdlingRegistry.getInstance().register(idlingResource)

        onView(withId(R.id.receipt_afterpayWidget_container)).check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(idlingResource)

        onWebView(withId(R.id.receipt_afterpayWidget))
            .check(webContent(hasElementWithId(WIDGET_CONTAINER_ID)))

        val widgetContainer = onWebView(withId(R.id.receipt_afterpayWidget))
            .withElement(findElement(Locator.ID, WIDGET_CONTAINER_ID))

        val expectedInitialContent =
            """{"token":"thisIsAnOrderToken","amount":null,"locale":"en_AU","style":{"logo":true,"heading":true}}"""
        widgetContainer.check(webMatches(getText(), containsString(expectedInitialContent)))

        onView(withId(R.id.receipt_totalCost)).perform(typeText("50"))

        // This is the lesser of two evils (the greater being to pollute our production code with
        // an idling resource); it accounts for a debounce in the collection of text input when
        // updating the widget with a new amount.
        Thread.sleep(800)

        val expectedUpdatedContent = """Update called with {"amount":"50","currency":"AUD"}"""
        widgetContainer.check(webMatches(getText(), containsString(expectedUpdatedContent)))
    }

    private fun clickChildWithId(@IdRes viewId: Int) = object : ViewAction {

        override fun getConstraints(): Matcher<View>? = null

        override fun getDescription(): String = "Click on the child view with the given ID."

        override fun perform(uiController: UiController?, view: View?) {
            view?.findViewById<View>(viewId)?.performClick()
        }
    }

    private inner class BooleanIdlingResource : IdlingResource {

        private lateinit var callback: IdlingResource.ResourceCallback

        private val isIdle = AtomicBoolean(false)

        override fun getName(): String {
            return javaClass.name
        }

        override fun isIdleNow(): Boolean = isIdle.get()

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = requireNotNull(callback)
        }

        fun isIdleNow(isIdle: Boolean) {
            if (isIdle) {
                this.callback.onTransitionToIdle()
            }
            this.isIdle.set(isIdle)
        }
    }
}
