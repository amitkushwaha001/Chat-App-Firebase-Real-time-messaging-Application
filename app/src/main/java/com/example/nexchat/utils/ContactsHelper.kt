package com.example.nexchat.utils

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils

object ContactsHelper {

    fun getContactList(context: Context): List<String> {
        val contactList = mutableListOf<String>()
        val phones = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        
        phones?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)
                val cleanNumber = formatPhoneNumber(number)
                if (cleanNumber.isNotEmpty()) {
                    contactList.add(cleanNumber)
                }
            }
        }
        return contactList.distinct()
    }

    private fun formatPhoneNumber(number: String): String {
        var clean = number.replace("[^0-9+]".toRegex(), "")
        if (clean.startsWith("00")) {
            clean = "+" + clean.substring(2)
        }
        // Normalize common Indian numbers if needed, or keep it generic
        // In a real app, you'd use libphonenumber
        return clean
    }
}
