package com.example.nexchat

import org.junit.Test
import org.junit.Assert.*

class ChatLogicTest {

    @Test
    fun testUnifiedRoomIdGeneration() {
        val uid1 = "userA"
        val uid2 = "userB"
        
        // The logic used in ChatActivity: 
        // val roomIds = listOf(senderId, receiverId!!).sorted()
        // val unifiedRoomId = "${roomIds[0]}_${roomIds[1]}"
        
        val list1 = listOf(uid1, uid2).sorted()
        val roomId1 = "${list1[0]}_${list1[1]}"
        
        val list2 = listOf(uid2, uid1).sorted()
        val roomId2 = "${list2[0]}_${list2[1]}"
        
        assertEquals("Room IDs should be identical regardless of who is sender", roomId1, roomId2)
        assertEquals("userA_userB", roomId1)
    }

    @Test
    fun testPhoneNumberFormatting() {
        val rawNumber = "+91 87005-30415"
        val cleanNumber = rawNumber.replace("[^0-9]".toRegex(), "")
        
        // Test our logic in ChatsFragment.isNumberInContacts
        // it.replace("[^0-9]".toRegex(), "").endsWith(clean.takeLast(10))
        
        val storedNumber = "8700530415"
        assertTrue(rawNumber.replace("[^0-9]".toRegex(), "").endsWith(storedNumber.takeLast(10)))
    }
}