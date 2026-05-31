package com.example.nexchat

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nexchat.activities.ChatActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatActivityTest {

    @Test
    fun testChatUIElements() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ChatActivity::class.java).apply {
            putExtra("name", "Test User")
            putExtra("uid", "test_uid")
        }
        
        ActivityScenario.launch<ChatActivity>(intent).use {
            onView(withId(R.id.tvUserName)).check(matches(withText("Test User")))
            onView(withId(R.id.etMessage)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSend)).check(matches(isDisplayed()))
            onView(withId(R.id.ivAttach)).check(matches(isDisplayed()))
        }
    }
}
