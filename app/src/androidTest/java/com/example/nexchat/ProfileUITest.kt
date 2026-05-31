package com.example.nexchat

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nexchat.activities.ProfileActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(ProfileActivity::class.java)

    @Test
    fun testProfileScreenComponents() {
        // Check if profile fields are displayed
        onView(withId(R.id.ivProfile)).check(matches(isDisplayed()))
        onView(withId(R.id.etName)).check(matches(isDisplayed()))
        onView(withId(R.id.etAbout)).check(matches(isDisplayed()))
        onView(withId(R.id.tvUserId)).check(matches(isDisplayed()))
        onView(withId(R.id.ivCopyId)).check(matches(isDisplayed()))
    }
}
