package com.wijha.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/** الصور من الإنترنت: ويكيبيديا لغلاف الوجهة، وويكيميديا كومنز للبحث في صور المدونة. */
object Net {
    private const val UA = "WijhaApp/2.0 (Android travel journal)"

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain -> chain.proceed(chain.request().newBuilder().header("User-Agent", UA).build()) }
        .build()

    private val heroCache = ConcurrentHashMap<String, String>()
    private val bad = Regex("map|logo|flag|coat|locator|diagram|plan|svg|chart|stamp", RegexOption.IGNORE_CASE)

    private fun get(url: String): String? = try {
        client.newCall(Request.Builder().url(url).header("Accept", "application/json").build()).execute().use { r ->
            if (r.isSuccessful) r.body?.string() else null
        }
    } catch (e: Exception) {
        null
    }

    suspend fun hero(title: String): String? = withContext(Dispatchers.IO) {
        heroCache[title]?.let { return@withContext it.ifEmpty { null } }
        val body = get("https://en.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(title, "UTF-8"))
        val url = try {
            body?.let { JSONObject(it).optJSONObject("thumbnail")?.optString("source")?.replace(Regex("/\\d+px-"), "/960px-") }
        } catch (e: Exception) {
            null
        }
        if (url != null) heroCache[title] = url
        url
    }

    suspend fun commons(query: String): List<CommonsPhoto> = withContext(Dispatchers.IO) {
        val q = URLEncoder.encode("$query filetype:bitmap", "UTF-8")
        val body = get(
            "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrsearch=$q" +
                "&gsrnamespace=6&gsrlimit=30&prop=imageinfo&iiprop=url%7Csize%7Cmime%7Cextmetadata&iiurlwidth=1000",
        ) ?: throw java.io.IOException("offline")
        val pages = JSONObject(body).optJSONObject("query")?.optJSONObject("pages") ?: return@withContext emptyList()
        val tag = Regex("<[^>]*>")
        val out = ArrayList<Pair<Int, CommonsPhoto>>()
        for (key in pages.keys()) {
            val p = pages.getJSONObject(key)
            if (bad.containsMatchIn(p.optString("title"))) continue
            val info = p.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
            if (info.optString("mime") != "image/jpeg" || info.optInt("width") < 1200) continue
            val thumb = info.optString("thumburl")
            if (thumb.isEmpty()) continue
            val meta = info.optJSONObject("extmetadata")
            val artist = meta?.optJSONObject("Artist")?.optString("value").orEmpty().replace(tag, "").replace(Regex("\\s+"), " ").trim()
            val lic = meta?.optJSONObject("LicenseShortName")?.optString("value").orEmpty().replace(tag, "").trim()
            val credit = listOf(artist, lic).filter { it.isNotEmpty() }.joinToString(" · ").ifEmpty { "ويكيميديا كومنز" }
            out += p.optInt("index") to CommonsPhoto(thumb, credit)
        }
        out.sortedBy { it.first }.map { it.second }.take(24)
    }

    suspend fun download(url: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            client.newCall(Request.Builder().url(url).build()).execute().use { r -> if (r.isSuccessful) r.body?.bytes() else null }
        } catch (e: Exception) {
            null
        }
    }
}
