package com.example.nexchat

import com.example.nexchat.models.CallLog
import org.junit.Test
import org.junit.Assert.*

class CallHistoryTest {

    @Test
    fun testCallLogParticipants() {
        val callerUid = "user_123"
        val receiverUid = "user_456"
        
        val callLog = CallLog(
            callId = "call_001",
            callerUid = callerUid,
            receiverUid = receiverUid,
            participants = listOf(callerUid, receiverUid)
        )
        
        assertTrue("Caller should be in participants", callLog.participants.contains(callerUid))
        assertTrue("Receiver should be in participants", callLog.participants.contains(receiverUid))
        assertEquals(2, callLog.participants.size)
    }

    @Test
    fun testMissedCallStatus() {
        val callLog = CallLog(isMissed = true)
        assertTrue("Call should be marked as missed", callLog.isMissed)
    }
}
