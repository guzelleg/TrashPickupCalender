package com.guzel1018.trashpickupcalender.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.guzel1018.trashpickupcalender.R
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import com.guzel1018.trashpickupcalender.ui.ReminderDayOption
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object ReminderScheduler {
    
    private const val CHANNEL_ID = "trash_pickup_reminders"
    private const val CHANNEL_NAME = "Müllabfuhr Erinnerungen"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Benachrichtigungen für Müllabfuhr Termine"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleReminders(
        context: Context,
        events: List<DatedCalendarItem>,
        dayOption: ReminderDayOption,
        time: LocalTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel all existing reminders first
        cancelAllReminders(context, events.size)
        
        events.forEachIndexed { index, event ->
            val reminderDateTime = calculateReminderDateTime(event.date, dayOption, time)
            
            // Only schedule if the reminder is in the future
            if (reminderDateTime.isAfter(LocalDateTime.now())) {
                scheduleReminder(
                    context,
                    alarmManager,
                    reminderDateTime,
                    event.kind,
                    index
                )
            }
        }
    }
    
    private fun calculateReminderDateTime(
        eventDate: LocalDate,
        dayOption: ReminderDayOption,
        time: LocalTime
    ): LocalDateTime {
        val reminderDate = when (dayOption) {
            ReminderDayOption.DAY_BEFORE -> eventDate.minusDays(1)
            ReminderDayOption.TWO_DAYS_BEFORE -> eventDate.minusDays(2)
            ReminderDayOption.THREE_DAYS_BEFORE -> eventDate.minusDays(3)
            ReminderDayOption.PREVIOUS_SUNDAY -> {
                // Find the Sunday before the event
                if (eventDate.dayOfWeek == DayOfWeek.SUNDAY) {
                    eventDate.minusWeeks(1)
                } else {
                    eventDate.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
                }
            }
        }
        
        return LocalDateTime.of(reminderDate, time)
    }
    
    private fun scheduleReminder(
        context: Context,
        alarmManager: AlarmManager,
        reminderDateTime: LocalDateTime,
        eventKind: String,
        requestCode: Int
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("event_kind", eventKind)
            putExtra("request_code", requestCode)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = reminderDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        // Use setExactAndAllowWhileIdle for better reliability
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    fun cancelAllReminders(context: Context, count: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        for (i in 0 until count) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}

