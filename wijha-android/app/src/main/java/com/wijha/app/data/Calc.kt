package com.wijha.app.data

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

fun num(n: Int): String = String.format(Locale.US, "%,d", n)
fun num(n: Double): String = num(n.roundToInt())
fun pad2(n: Int): String = n.toString().padStart(2, '0')

fun distanceKm(a: String, b: String): Int {
    val x = AIRPORT_BY_CODE[a] ?: return 0
    val y = AIRPORT_BY_CODE[b] ?: return 0
    val dl = Math.toRadians(y.lat - x.lat)
    val dn = Math.toRadians(y.lon - x.lon)
    val h = sin(dl / 2).let { it * it } +
        cos(Math.toRadians(x.lat)) * cos(Math.toRadians(y.lat)) * sin(dn / 2).let { it * it }
    return (12742 * asin(sqrt(h))).roundToInt()
}

fun hoursFor(km: Int): Double = km / (if (km < 1500) 700.0 else 830.0) + 0.45
fun fmtHours(h: Double): String {
    val m = (h * 60).roundToInt()
    return "${m / 60}:${pad2(m % 60)}"
}

fun Flight.km(): Int = distanceKm(from, to)
fun Flight.durationHours(): Double = hoursFor(km())
fun Flight.localDate(): LocalDate = LocalDate.parse(date)
fun Flight.depDateTime(): LocalDateTime = LocalDateTime.of(localDate(), LocalTime.parse(time.ifBlank { "09:00" }))
fun Flight.endDateTime(): LocalDateTime =
    depDateTime().plusMinutes((durationHours() * 60).toLong() + delay)

fun Flight.isPast(now: LocalDateTime): Boolean = endDateTime().isBefore(now)

/** وقت الإقلاع المعروض بعد احتساب التأخير. */
fun Flight.depText(): String {
    val (h, m) = time.ifBlank { "09:00" }.split(":").map { it.toInt() }
    val t = h * 60 + m + delay
    return "${pad2(t / 60 % 24)}:${pad2(t % 60)}"
}

/** وقت الوصول التقريبي بتوقيت المطار، مع عدد الأيام الإضافية. */
fun Flight.arrival(): Pair<String, Int> {
    val a = AIRPORT_BY_CODE[from]
    val b = AIRPORT_BY_CODE[to]
    val (h, m) = time.ifBlank { "09:00" }.split(":").map { it.toInt() }
    var t = h * 60 + m + delay + (durationHours() * 60).roundToInt() + ((b?.tz ?: 0) - (a?.tz ?: 0)) * 60
    val day = Math.floorDiv(t, 1440)
    t = ((t % 1440) + 1440) % 1440
    return "${pad2(t / 60)}:${pad2(t % 60)}" to day
}

enum class Phase { ONTIME, DELAYED, CANCELLED, LANDED, BOARDING }

fun Flight.phase(now: LocalDateTime): Phase {
    if (status == FlightStatus.CANCELLED) return Phase.CANCELLED
    if (isPast(now)) return Phase.LANDED
    if (status == FlightStatus.DELAYED) return Phase.DELAYED
    val mins = Duration.between(now, depDateTime()).toMinutes()
    return if (mins in 0..60) Phase.BOARDING else Phase.ONTIME
}

fun Phase.label(f: Flight): String = when (this) {
    Phase.ONTIME -> "في الموعد"
    Phase.DELAYED -> "متأخرة ${f.delay} د"
    Phase.CANCELLED -> "ملغاة"
    Phase.LANDED -> "هبطت"
    Phase.BOARDING -> "الصعود قريباً"
}

fun countdownText(f: Flight, now: LocalDateTime): String {
    val d = Duration.between(now, f.depDateTime())
    if (d.isNegative || d.isZero) return ""
    val days = d.toDays()
    val hours = d.toHours() % 24
    val mins = d.toMinutes() % 60
    return when {
        days > 0 -> "$days ${if (days == 1L) "يوم" else if (days < 11) "أيام" else "يوماً"} و$hours ساعة"
        else -> "$hours ساعة و$mins دقيقة"
    }
}

fun AppData.homeCc(): String = AIRPORT_BY_CODE[home]?.cc ?: "sa"

fun AppData.sortedFlights(): List<Flight> = flights.sortedBy { it.depDateTime() }
fun AppData.pastFlights(now: LocalDateTime): List<Flight> =
    sortedFlights().filter { it.isPast(now) && it.status != FlightStatus.CANCELLED }

/** ختم تلقائي عند هبوط رحلة إلى دولة غير بلد الإقامة وغير دولة الإقلاع. */
fun AppData.stamps(now: LocalDateTime): List<Stamp> = pastFlights(now).mapNotNull { f ->
    val a = AIRPORT_BY_CODE[f.from] ?: return@mapNotNull null
    val b = AIRPORT_BY_CODE[f.to] ?: return@mapNotNull null
    if (a.cc == b.cc || b.cc == homeCc()) return@mapNotNull null
    val h = (f.id + f.no).fold(7) { acc, c -> acc * 31 + c.code }.let { Math.abs(it) }
    Stamp(b.cc, b.city, f.localDate(), h % 3, (h shr 3) % 4, ((h % 17) - 8).toFloat())
}

fun AppData.visitedCountries(now: LocalDateTime): List<String> = stamps(now).map { it.cc }.distinct()

fun AppData.plannedCountries(now: LocalDateTime): List<String> {
    val visited = visitedCountries(now)
    return sortedFlights().filter { !it.isPast(now) && it.status != FlightStatus.CANCELLED }
        .mapNotNull { AIRPORT_BY_CODE[it.to]?.cc }
        .distinct()
        .filter { it != homeCc() && it !in visited }
}

data class FlightStats(
    val flights: List<Flight>,
    val km: Int,
    val hours: Double,
    val airports: Int,
    val airlines: Int,
    val countries: Int,
    val routes: List<Pair<String, Int>>,
    val airportsTop: List<Pair<String, Int>>,
    val airlinesTop: List<Pair<String, Int>>,
    val longest: Pair<Flight, Int>?,
    val shortest: Pair<Flight, Int>?,
)

fun AppData.stats(year: Int?, now: LocalDateTime): FlightStats {
    val list = pastFlights(now).filter { year == null || it.localDate().year == year }
    val kms = list.map { it.km() }
    val ap = HashSet<String>()
    val al = HashSet<String>()
    val ct = HashSet<String>()
    list.forEach { f ->
        ap += f.from; ap += f.to
        if (airlineName(f.no).isNotEmpty()) al += f.no.take(2).uppercase()
        listOf(f.from, f.to).forEach { c -> AIRPORT_BY_CODE[c]?.cc?.let { if (it != homeCc()) ct += it } }
    }
    fun <T> top(items: List<T>): List<Pair<T, Int>> =
        items.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key to it.value }
    val lg = list.maxByOrNull { it.km() }?.let { it to it.km() }
    val sh = list.minByOrNull { it.km() }?.let { it to it.km() }
    return FlightStats(
        flights = list, km = kms.sum(), hours = list.sumOf { it.durationHours() }, airports = ap.size, airlines = al.size,
        countries = ct.size,
        routes = top(list.map { listOf(it.from, it.to).sorted().joinToString("-") }),
        airportsTop = top(list.flatMap { listOf(it.from, it.to) }),
        airlinesTop = top(list.filter { airlineName(it.no).isNotEmpty() }.map { it.no.take(2).uppercase() }),
        longest = lg, shortest = sh,
    )
}

fun tripLabelFor(data: AppData, to: String, from: String, date: String): String {
    val home = data.homeCc()
    val dest = AIRPORT_BY_CODE[to]?.takeIf { it.cc != home } ?: AIRPORT_BY_CODE[from]
    val year = runCatching { LocalDate.parse(date).year }.getOrDefault(LocalDate.now().year)
    return "${dest?.city ?: to} $year"
}

fun dateRangeText(from: LocalDate, to: LocalDate): String =
    if (from.month == to.month && from.year == to.year) "${from.dayOfMonth} – ${to.dayOfMonth} ${MONTHS[to.monthValue - 1]} ${to.year}"
    else "${from.dayOfMonth} ${MONTHS[from.monthValue - 1]} – ${to.dayOfMonth} ${MONTHS[to.monthValue - 1]} ${to.year}"

fun levelOf(countries: Int): Pair<Int, String> = when {
    countries >= 10 -> 4 to "جوّاب"
    countries >= 6 -> 3 to "ترحال"
    countries >= 3 -> 2 to "استكشاف"
    else -> 1 to "انطلاقة"
}

fun maxOf2(a: Int, b: Int) = max(a, b)
