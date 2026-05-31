package com.example.nexchat

import com.example.nexchat.models.Status
import com.example.nexchat.models.UserStatus
import org.junit.Test
import org.junit.Assert.*

class StatusTest {

    @Test
    fun testStatusObject() {
        val timestamp = System.currentTimeMillis()
        val status = Status(
            statusId = "s1",
            uid = "u1",
            imageUrl = "url1",
            timestamp = timestamp
        )
        
        assertEquals("s1", status.statusId)
        assertEquals("u1", status.uid)
        assertEquals("url1", status.imageUrl)
        assertEquals(timestamp, status.timestamp)
    }

    @Test
    fun testUserStatusAggregation() {
        val statuses = listOf(
            Status(statusId = "1", imageUrl = "url1"),
            Status(statusId = "2", imageUrl = "url2")
        )
        
        val userStatus = UserStatus(
            name = "Amit",
            profileImage = "p1",
            lastUpdated = 12345L,
            statuses = statuses
        )
        
        assertEquals(2, userStatus.statuses.size)
        assertEquals("Amit", userStatus.name)
    }
}
