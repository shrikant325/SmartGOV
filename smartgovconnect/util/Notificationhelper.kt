package com.example.smartgovconnect.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.smartgovconnect.R
import com.example.smartgovconnect.ui.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object NotificationHelper {

    private const val CHANNEL_ID = "new_schemes_channel"
    private const val CHANNEL_NAME = "New Schemes"
    private const val NOTIFICATION_ID = 1001

    private const val PREFS_NAME = "smartgov_prefs"
    private const val KEY_LAST_CHECK_PREFIX = "last_scheme_check_"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    "Notifies you when a new scheme relevant to your state or all users is published"
            }

            val manager =
                context.getSystemService(NotificationManager::class.java)

            manager?.createNotificationChannel(channel)
        }
    }

    fun getLastCheckTime(context: Context, uid: String): Long {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return prefs.getLong(
            KEY_LAST_CHECK_PREFIX + uid,
            0L
        )
    }

    fun setLastCheckTime(
        context: Context,
        uid: String,
        time: Long
    ) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putLong(KEY_LAST_CHECK_PREFIX + uid, time)
            .apply()
    }

    suspend fun checkForNewSchemes(
        context: Context,
        uid: String,
        userState: String
    ) {
        try {
            val lastCheck = getLastCheckTime(context, uid)

            val snapshot = FirebaseFirestore
                .getInstance()
                .collection("schemes")
                .get()
                .await()

            val allSchemes = snapshot.documents

            val matchingNewSchemes = allSchemes.filter { doc ->
                val createdAt = doc.getLong("createdAt") ?: 0L

                if (createdAt <= lastCheck) {
                    return@filter false
                }

                val level = doc.getString("level")
                    ?.trim()
                    ?.lowercase()
                    ?: "central"

                if (level == "central") {
                    true
                } else {
                    val schemeState =
                        doc.getString("state")
                            ?.trim()
                            ?: ""

                    schemeState.isNotEmpty() &&
                            schemeState.equals(
                                userState.trim(),
                                ignoreCase = true
                            )
                }
            }

            val latestSchemeTime =
                allSchemes.maxOfOrNull {
                    it.getLong("createdAt") ?: 0L
                } ?: lastCheck

            if (lastCheck > 0L && matchingNewSchemes.isNotEmpty()) {
                showNewSchemesNotification(
                    context,
                    matchingNewSchemes.map {
                        it.getString("title") ?: "New scheme"
                    }
                )
            }

            setLastCheckTime(
                context,
                uid,
                maxOf(lastCheck, latestSchemeTime)
            )
        } catch (_: Exception) {
        }
    }

    fun showNewSchemesNotification(
        context: Context,
        newSchemeTitles: List<String>
    ) {
        if (newSchemeTitles.isEmpty()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return
        }

        val count = newSchemeTitles.size

        val title =
            if (count == 1) {
                "New scheme available!"
            } else {
                "$count new schemes available!"
            }

        val text =
            if (count == 1) {
                "${newSchemeTitles.first()} is now available for you."
            } else {
                "${newSchemeTitles.take(2).joinToString(", ")}" +
                        if (count > 2) {
                            " and ${count - 2} more are now available."
                        } else {
                            " are now available."
                        }
            }

        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            NOTIFICATION_ID,
            notification
        )
    }
}