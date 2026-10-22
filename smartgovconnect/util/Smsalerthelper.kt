package com.example.smartgovconnect.util

import android.telephony.SmsManager
import com.example.smartgovconnect.model.Scheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SmsAlertHelper {

    suspend fun notifyMatchingUsers(scheme: Scheme): Int {
        val db = FirebaseFirestore.getInstance()

        val snapshot = if (scheme.level.equals("state", true)) {
            db.collection("users")
                .whereEqualTo("state", scheme.state)
                .get()
                .await()
        } else {
            db.collection("users")
                .get()
                .await()
        }

        val smsManager = SmsManager.getDefault()
        var sentCount = 0

        for (doc in snapshot.documents) {
            val phone = doc.getString("phone") ?: continue
            val cleanPhone = phone.trim()

            if (cleanPhone.length != 10 || !cleanPhone.all { it.isDigit() }) {
                continue
            }

            val message =
                if (scheme.level.equals("state", true)) {
                    "SmartGov Connect: A new ${scheme.state} government scheme \"${scheme.title}\" is available. Open the app to view details and apply."
                } else {
                    "SmartGov Connect: A new government scheme \"${scheme.title}\" is available. Open the app to view details and apply."
                }

            try {
                val parts = smsManager.divideMessage(message)

                smsManager.sendMultipartTextMessage(
                    cleanPhone,
                    null,
                    parts,
                    null,
                    null
                )

                sentCount++
            } catch (_: Exception) {
            }
        }

        return sentCount
    }
}