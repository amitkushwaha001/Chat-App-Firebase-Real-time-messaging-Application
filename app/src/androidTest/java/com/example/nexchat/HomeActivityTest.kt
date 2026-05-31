package com.example.nexchat

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nexchat.activities.HomeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Test
    fun testTabsPresence() {
        onView(withText("CHATS")).check(matches(isDisplayed()))
        onView(withText("STATUS")).check(matches(isDisplayed()))
        onView(withText("CALLS")).check(matches(isDisplayed()))
    }

    @Test
    fun testFabPresence() {
        onView(withId(R.id.fabNewChat)).check(matches(isDisplayed()))
    }
}
