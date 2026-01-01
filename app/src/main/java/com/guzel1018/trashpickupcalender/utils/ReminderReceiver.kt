package com.guzel1018.trashpickupcalender.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.guzel1018.trashpickupcalender.MainActivity
import com.guzel1018.trashpickupcalender.R

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val eventKind = intent.getStringExtra("event_kind") ?: return
        val requestCode = intent.getIntExtra("request_code", 0)
        
        showNotification(context, eventKind, requestCode)
    }
    
    private fun showNotification(context: Context, eventKind: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Using Material 3 design principles
        val notification = NotificationCompat.Builder(context, "trash_pickup_reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Müllabfuhr Erinnerung")
            .setContentText("Morgen: $eventKind")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Vergessen Sie nicht, Ihre Mülltonne rauszustellen!\n\nMorgen wird abgeholt: $eventKind"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(Color.parseColor("#4CAF50")) // Material Green
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
}

