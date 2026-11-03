package com.rihla.english.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rihla.english.MainActivity
import com.rihla.english.R
import com.rihla.english.RihlaApplication
import com.rihla.english.core.journey.Journey
import com.rihla.english.core.journey.Script
import com.rihla.english.core.season.Season
import com.rihla.english.core.season.SeasonCalendar
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/** Schedules the once-a-day study reminder at the learner's chosen time. */
object ReminderScheduler {
    private const val WORK_NAME = "daily_study_reminder"
    const val CHANNEL_ID = "daily_reminders"

    fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next).toMinutes().coerceAtLeast(1)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
}

/**
 * Fires once a day. Skips the notification if today's goal is already met; otherwise picks a
 * message that fits: streak at risk, Ramadan, or a friendly nudge.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as RihlaApplication).container
        val profile = container.journeyStore.profile.first()
        if (!profile.remindersEnabled || !profile.onboarded) return Result.success()

        val progress = container.journeyStore.progress.first()
        val today = container.clock.todayEpochDay()
        // Already studied today: no nudge.
        if (Journey.minutesToday(progress, today) >= profile.dailyMinutes) return Result.success()

        val ctx = applicationContext
        val name = profile.name.ifBlank { ctx.getString(R.string.reminder_friend) }
        val next = Journey.nextScene(Script.chapters, progress)
        val days = Journey.commitmentDays(progress, today)
        val (title, body) = when {
            SeasonCalendar.seasonOn(today) == Season.RAMADAN ->
                ctx.getString(R.string.reminder_ramadan_title) to ctx.getString(R.string.reminder_ramadan_body, name)
            days > 1 ->
                ctx.getString(R.string.reminder_streak_title, days) to ctx.getString(R.string.reminder_streak_body, name)
            next != null ->
                ctx.getString(R.string.reminder_scene_title) to ctx.getString(R.string.reminder_scene_body, name, next.titleAr)
            else -> ctx.getString(R.string.reminder_review_title) to ctx.getString(R.string.reminder_review_body, name)
        }
        notify(title, body)
        return Result.success()
    }

    private fun notify(title: String, body: String) {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        ReminderScheduler.ensureChannel(ctx)
        val open = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(ctx, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val NOTIFICATION_ID = 1001
    }
}
