package com.example.nexchat

import com.example.nexchat.models.Message
import org.junit.Test
import org.junit.Assert.*

class MessageModelTest {

    @Test
    fun testMessageHiddenByLogic() {
        val message = Message(
            messageId = "m1",
            hiddenBy = listOf("userA", "userB")
        )
        
        assertTrue("Message should be hidden for userA", message.hiddenBy.contains("userA"))
        assertFalse("Message should not be hidden for userC", message.hiddenBy.contains("userC"))
    }

    @Test
    fun testMessageTypeDefaults() {
        val message = Message(message = "Hello")
        assertEquals("text", message.type)
        assertFalse(message.seen)
    }
}
