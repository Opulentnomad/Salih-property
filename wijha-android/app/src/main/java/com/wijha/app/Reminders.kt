package com.wijha.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.wijha.app.data.AIRPORT_BY_CODE
import com.wijha.app.data.AppData
import com.wijha.app.data.Flight
import com.wijha.app.data.FlightStatus
import com.wijha.app.data.depDateTime
import com.wijha.app.data.depText
import java.time.ZoneId

/** تنبيهات أصلية قبل الإقلاع بثلاث ساعات. */
object Reminders {
    const val CHANNEL = "flights"

    fun createChannel(ctx: Context) {
        val ch = NotificationChannel(CHANNEL, "تذكير الرحلات", NotificationManager.IMPORTANCE_HIGH)
        ch.description = "تنبيه قبل إقلاع رحلتك بثلاث ساعات"
        ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun intentFor(ctx: Context, f: Flight): PendingIntent {
        val a = AIRPORT_BY_CODE[f.from]?.city ?: f.from
        val b = AIRPORT_BY_CODE[f.to]?.city ?: f.to
        val i = Intent(ctx, ReminderReceiver::class.java)
            .putExtra("title", "رحلتك ${f.no} بعد ثلاث ساعات")
            .putExtra("text", "$a ← $b · الإقلاع ${f.depText()}")
        return PendingIntent.getBroadcast(ctx, f.id.hashCode(), i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun schedule(ctx: Context, f: Flight) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val pi = intentFor(ctx, f)
        val at = runCatching { f.depDateTime().minusHours(3).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrDefault(0L)
        if (f.status == FlightStatus.CANCELLED || at <= System.currentTimeMillis()) {
            am.cancel(pi)
            return
        }
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    fun cancel(ctx: Context, f: Flight) {
        ctx.getSystemService(AlarmManager::class.java).cancel(intentFor(ctx, f))
    }

    fun rescheduleAll(ctx: Context, data: AppData) = data.flights.forEach { schedule(ctx, it) }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(context, Reminders.CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_plane)
            .setContentTitle(intent.getStringExtra("title"))
            .setContentText(intent.getStringExtra("text"))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % 100000).toInt(), n)
    }
}
