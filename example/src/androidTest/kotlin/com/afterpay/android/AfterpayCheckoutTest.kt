package com.afterpay.android

import android.util.Log
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
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.afterpay.MainActivity
import com.example.afterpay.R
import com.example.afterpay.nav_graph
import com.example.afterpay.shopping.ShoppingListAdapter
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class AfterpayCheckoutTest {

    @get:Rule
    val disableAnimations = DisableAnimationsRule()

    @get:Rule
    val activityScenario = ActivityScenarioRule(MainActivity::class.java)

    private val idlingResource = BooleanIdlingResource()

    private val destinationObserver =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            idlingResource.isIdleNow(destination.id == nav_graph.dest.receipt)
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
            .perform(typeText("user@example.com"))

        onView(withId(R.id.cart_expressCheckBox)).check(matches(isNotChecked()))
        onView(withId(R.id.cart_buyNowCheckBox)).check(matches(isNotChecked()))
        onView(withId(R.id.cart_pickupCheckBox)).check(matches(isNotChecked()))
        onView(withId(R.id.cart_shippingOptionsRequiredCheckBox)).check(matches(isNotChecked()))

        onView(withId(R.id.cart_button_checkout)).perform(click())

        IdlingRegistry.getInstance().register(idlingResource)

        onView(withId(R.id.receipt_totalCost_label)).check(matches(isDisplayed()))

        IdlingRegistry.getInstance().unregister(idlingResource)
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

        override fun getName(): String = javaClass.name

        override fun isIdleNow(): Boolean = isIdle.get()

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = requireNotNull(callback)
        }

        fun isIdleNow(isIdle: Boolean) {
            this.isIdle.set(isIdle)
        }
    }
}
