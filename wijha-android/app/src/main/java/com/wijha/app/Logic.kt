package com.wijha.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

// ===================== التوصية =====================

sealed interface Reason {
    data class TagReason(val tagId: String) : Reason
    data object Budget : Reason
}

data class Scored(val dest: Destination, val match: Int, val reasons: List<Reason>)

object Recommender {
    fun rank(s: UserState, query: String, tagFilter: String?): List<Scored> {
        val q = query.trim().lowercase()
        return CATALOG
            .filter { it.id !in s.hidden }
            .filter {
                q.isEmpty() || it.ar.contains(q) || it.en.lowercase().contains(q) ||
                    it.countryAr.contains(q) || it.countryEn.lowercase().contains(q)
            }
            .filter { tagFilter == null || (it.tags[tagFilter] ?: 0f) >= 0.5f }
            .map { score(it, s) }
            .sortedByDescending { it.match }
    }

    fun budgetLimit(level: Int): Int = when (level) {
        1 -> 3000
        2 -> 6000
        else -> Int.MAX_VALUE
    }

    fun score(d: Destination, s: UserState): Scored {
        val w = s.weights
        val dot = d.tags.entries.sumOf { ((w[it.key] ?: 0f) * it.value).toDouble() }
        val nw = sqrt(w.values.sumOf { (it * it).toDouble() })
        val nd = sqrt(d.tags.values.sumOf { (it * it).toDouble() })
        val cos = if (nw == 0.0 || nd == 0.0) 0.0 else dot / (nw * nd)
        val fits = d.priceSar <= budgetLimit(s.budget)
        val m = (50 + 46 * cos + (if (fits) 4 else -14)).roundToInt().coerceIn(35, 98)

        val reasons = mutableListOf<Reason>()
        d.tags.entries
            .map { it.key to (w[it.key] ?: 0f) * it.value }
            .filter { it.second > 0.15f }
            .sortedByDescending { it.second }
            .take(2)
            .forEach { reasons += Reason.TagReason(it.first) }
        if (fits && reasons.size < 2) reasons += Reason.Budget
        return Scored(d, m, reasons)
    }
}

// ===================== مولّد المسار =====================

object Planner {
    private val SLOT_START = intArrayOf(9 * 60 + 30, 13 * 60, 16 * 60 + 30, 18 * 60 + 30)

    /** يختار الأنشطة الأعلى ملاءمة لتفضيلاتك، ثم يوزّعها على الأيام بحيث يحتوي كل يوم على أوقات متنوعة. */
    fun plan(d: Destination, days: Int, weights: Map<String, Float>, seed: Long): List<TripItem> {
        val rnd = Random(seed)
        val n = minOf(d.pois.size, days * 3)
        val chosen = d.pois
            .map { it to ((weights[it.tag] ?: 0f) + 0.3f + rnd.nextFloat() * 0.45f) }
            .sortedByDescending { it.second }
            .take(n)
            .map { it.first }
            .sortedBy { it.slot }
        return chosen.mapIndexed { i, poi -> TripItem(i % days, poi.id) }
    }

    /** أوقات البدء لعناصر يوم واحد، دون تداخل (فاصل 90 دقيقة على الأقل). */
    fun dayTimes(d: Destination, items: List<TripItem>): List<Pair<TripItem, Int>> {
        var last = -1
        return items
            .map { it to d.pois.first { p -> p.id == it.poiId } }
            .sortedBy { it.second.slot }
            .map { (item, poi) ->
                var t = SLOT_START[poi.slot]
                if (t <= last) t = last + 90
                last = t
                item to t
            }
    }

    fun fmt(minutes: Int): String = "%02d:%02d".format(minutes / 60 % 24, minutes % 60)
}

// ===================== الصور من الإنترنت =====================

/**
 * الصور تُجلب مباشرة من ويكيبيديا وويكيميديا كومنز (مرخّصة للاستخدام العام):
 *  - الغلاف: صورة المقال الرئيسية لكل وجهة عبر Wikipedia REST API.
 *  - المعرض: بحث في ويكيميديا كومنز عن صور JPEG عالية الدقة مع استبعاد الخرائط والشعارات.
 */
object ImageRepo {
    private const val UA = "WijhaApp/1.0 (Android travel planner)"
    private val heroCache = ConcurrentHashMap<String, String>()
    private val galleryCache = ConcurrentHashMap<String, List<String>>()
    private val badTitle = Regex("map|logo|flag|coat|locator|diagram|plan|panorama of the|svg|chart|stamp", RegexOption.IGNORE_CASE)

    private fun get(url: String): String? = try {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000
        c.readTimeout = 15_000
        c.setRequestProperty("User-Agent", UA)
        c.setRequestProperty("Accept", "application/json")
        if (c.responseCode in 200..299) c.inputStream.bufferedReader().use { it.readText() } else null
    } catch (e: Exception) {
        null
    }

    suspend fun hero(d: Destination): String? {
        heroCache[d.id]?.let { return it }
        val url = withContext(Dispatchers.IO) {
            val body = get("https://en.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(d.wiki, "UTF-8"))
                ?: return@withContext null
            try {
                val o = JSONObject(body)
                val orig = o.optJSONObject("originalimage")
                val thumb = o.optJSONObject("thumbnail")
                when {
                    orig != null && orig.optInt("width") in 1..2400 -> orig.optString("source")
                    thumb != null -> thumb.optString("source").replace(Regex("/\\d+px-"), "/1000px-")
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
        if (!url.isNullOrBlank()) heroCache[d.id] = url
        return url?.takeIf { it.isNotBlank() } ?: gallery(d).firstOrNull()
    }

    suspend fun gallery(d: Destination): List<String> {
        galleryCache[d.id]?.let { return it }
        val list = withContext(Dispatchers.IO) {
            val q = URLEncoder.encode(d.commonsQuery + " filetype:bitmap", "UTF-8")
            val url = "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search" +
                "&gsrsearch=$q&gsrnamespace=6&gsrlimit=30&prop=imageinfo&iiprop=url%7Csize%7Cmime&iiurlwidth=800"
            val body = get(url) ?: return@withContext emptyList<String>()
            try {
                val pages = JSONObject(body).optJSONObject("query")?.optJSONObject("pages")
                    ?: return@withContext emptyList<String>()
                val out = mutableListOf<Pair<Int, String>>()
                for (key in pages.keys()) {
                    val page = pages.getJSONObject(key)
                    if (badTitle.containsMatchIn(page.optString("title"))) continue
                    val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                    if (info.optString("mime") != "image/jpeg") continue
                    if (info.optInt("width") < 1200) continue
                    val thumb = info.optString("thumburl")
                    if (thumb.isNotBlank()) out += page.optInt("index") to thumb
                }
                out.sortedBy { it.first }.map { it.second }.take(20)
            } catch (e: Exception) {
                emptyList()
            }
        }
        if (list.isNotEmpty()) galleryCache[d.id] = list
        return list
    }
}

// ===================== التخزين المحلي =====================

class Store(context: Context) {
    private val sp = context.getSharedPreferences("wijha", Context.MODE_PRIVATE)

    fun load(): UserState {
        val raw = sp.getString("state", null) ?: return UserState()
        return try {
            decode(JSONObject(raw))
        } catch (e: Exception) {
            UserState()
        }
    }

    fun save(s: UserState) {
        sp.edit().putString("state", encode(s).toString()).apply()
    }

    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }

    private fun encode(s: UserState) = JSONObject().apply {
        put("lang", s.lang)
        put("onboarded", s.onboarded)
        put("budget", s.budget)
        put("weights", JSONObject().also { o -> s.weights.forEach { (k, v) -> o.put(k, v.toDouble()) } })
        put("liked", JSONArray(s.liked.toList()))
        put("hidden", JSONArray(s.hidden.toList()))
        put("favPhotos", JSONArray(s.favPhotos.toList()))
        put("trips", JSONArray().also { arr ->
            s.trips.forEach { t ->
                arr.put(JSONObject().apply {
                    put("id", t.id); put("destId", t.destId); put("start", t.startEpochDay)
                    put("days", t.days); put("travelers", t.travelers)
                    put("items", JSONArray().also { a ->
                        t.items.forEach { a.put(JSONObject().put("day", it.day).put("poi", it.poiId)) }
                    })
                    put("notes", JSONArray().also { a ->
                        t.notes.forEach { a.put(JSONObject().put("text", it.text).put("day", it.epochDay)) }
                    })
                })
            }
        })
    }

    private fun decode(o: JSONObject): UserState {
        val w = o.optJSONObject("weights")
        val weights = buildMap<String, Float> {
            w?.keys()?.forEach { k -> put(k, w.getDouble(k).toFloat()) }
        }
        val trips = o.optJSONArray("trips")?.let { arr ->
            (0 until arr.length()).map { i ->
                val t = arr.getJSONObject(i)
                val items = t.getJSONArray("items").let { a ->
                    (0 until a.length()).map { j -> a.getJSONObject(j).let { TripItem(it.getInt("day"), it.getString("poi")) } }
                }
                val notes = t.getJSONArray("notes").let { a ->
                    (0 until a.length()).map { j -> a.getJSONObject(j).let { Note(it.getString("text"), it.getLong("day")) } }
                }
                Trip(t.getString("id"), t.getString("destId"), t.getLong("start"), t.getInt("days"), t.getInt("travelers"), items, notes)
            }
        } ?: emptyList()
        return UserState(
            lang = o.optString("lang", "ar"),
            onboarded = o.optBoolean("onboarded", false),
            weights = weights,
            budget = o.optInt("budget", 2),
            liked = o.optJSONArray("liked")?.strings()?.toSet() ?: emptySet(),
            hidden = o.optJSONArray("hidden")?.strings()?.toSet() ?: emptySet(),
            favPhotos = o.optJSONArray("favPhotos")?.strings()?.toSet() ?: emptySet(),
            trips = trips,
        )
    }
}
