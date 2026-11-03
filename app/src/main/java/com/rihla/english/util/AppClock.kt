package com.rihla.english.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Time source in the device's time zone. Injected so ViewModels are testable. */
interface AppClock {
    fun nowMillis(): Long
    fun todayEpochDay(): Long

    /** 0..1 fraction of the current local day elapsed (for league pacing). */
    fun fractionOfDay(): Double
}

class SystemAppClock : AppClock {
    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun todayEpochDay(): Long = LocalDate.now(ZoneId.systemDefault()).toEpochDay()

    override fun fractionOfDay(): Double {
        val zone = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMillis()).atZone(zone)
        return now.toLocalTime().toSecondOfDay() / 86_400.0
    }
}

/** "mm:ss" or "h:mm:ss" countdown text with Latin digits (readable inside Arabic UI). */
fun formatCountdown(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
