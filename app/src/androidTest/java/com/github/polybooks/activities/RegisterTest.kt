package com.github.polybooks.activities

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.polybooks.R
import com.github.polybooks.utils.GlobalVariables.EXTRA_USERNAME
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)

class RegisterTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(RegisterActivity::class.java)

    @Before
    fun before() {
        Intents.init()
    }

    @After
    fun after() {
        Firebase.auth.currentUser?.delete() // Par securité
        Intents.release()
    }

    @Test
    fun fillAndRegister() {
        onView(withId(R.id.username_field)).perform(scrollTo(), typeText("TestTestTest"), closeSoftKeyboard())
        onView(withId(R.id.email_field)).perform(scrollTo(), typeText("test@test.test"), closeSoftKeyboard())
        onView(withId(R.id.password1_field)).perform(scrollTo(), typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.password2_field)).perform(scrollTo(), typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.button_reg)).perform(scrollTo(), click())
        Thread.sleep(1500)
        Intents.intended(hasComponent(UserProfileActivity::class.java.name))
        Intents.intended(toPackage("com.github.polybooks"))
        Intents.intended(hasExtra(EXTRA_USERNAME, "TestTestTest"))
        Thread.sleep(1500)
        Firebase.auth.currentUser?.delete()
    }

    @Test
    fun navBarSales() {
        onView(withId(R.id.sales)).perform(click())
        Intents.intended(hasComponent(FilteringSalesActivity::class.java.name))
    }

    @Test
    fun navBarProfile() {
        onView(withId(R.id.user_profile)).perform(click())
        //Espresso.onView(ViewMatchers.withId(R.id.recyclerView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun navBarBooks() {
        onView(withId(R.id.books)).perform(click())
        Intents.intended(hasComponent(FilteringBooksActivity::class.java.name))
    }

    @Test
    fun navBarDefault() {
        onView(withId(R.id.default_selected)).check(
            ViewAssertions.matches(
                withEffectiveVisibility(
                    Visibility.GONE
                )
            )
        )
        onView(withId(R.id.default_selected))
            .check(ViewAssertions.matches(Matchers.not(isEnabled())))
    }

    @Test
    fun navBarSelected() {
        onView(withId(R.id.user_profile))
            .check(ViewAssertions.matches(isSelected()))
    }

    @Test
    fun navBarHome() {
        onView(withId(R.id.home)).perform(click())
        Intents.intended(hasComponent(MainActivity::class.java.name))
    }
}
