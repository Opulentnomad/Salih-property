package com.wijha.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/** تخزين محلي: ملف JSON للبيانات، ومجلد للصور المضغوطة. */
class Store(private val ctx: Context) {
    private val file = File(ctx.filesDir, "wijha.json")
    private val photosDir = File(ctx.filesDir, "photos").also { it.mkdirs() }

    fun photoFile(id: String) = File(photosDir, "$id.jpg")

    fun load(): AppData = try {
        decode(JSONObject(file.readText()))
    } catch (e: Exception) {
        AppData()
    }

    @Synchronized
    fun save(d: AppData) {
        val tmp = File(ctx.filesDir, "wijha.json.tmp")
        tmp.writeText(encode(d).toString())
        if (!tmp.renameTo(file)) {
            file.writeText(tmp.readText())
            tmp.delete()
        }
    }

    /** يحذف الصور التي لا تستخدمها أي تدوينة. */
    fun gc(d: AppData) {
        val used = HashSet<String>()
        d.posts.forEach { p ->
            if (p.cover.isNotEmpty()) used += p.cover
            p.blocks.forEach { b -> if (b is Block.Photos) b.items.forEach { used += it.id } }
        }
        photosDir.listFiles()?.forEach { f -> if (f.nameWithoutExtension !in used) f.delete() }
    }

    /** ينسخ صورة من معرض الجهاز مع ضغطها وتصحيح اتجاهها. */
    fun importUri(uri: Uri): String? = try {
        val src = ImageDecoder.createSource(ctx.contentResolver, uri)
        val bmp = ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
            val s = max(info.size.width, info.size.height)
            if (s > 1600) {
                val k = 1600f / s
                decoder.setTargetSize((info.size.width * k).toInt(), (info.size.height * k).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        val id = newId("m")
        FileOutputStream(photoFile(id)).use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        id
    } catch (e: Exception) {
        null
    }

    fun saveBytes(bytes: ByteArray): String {
        val id = newId("m")
        photoFile(id).writeBytes(bytes)
        return id
    }

    fun writeSampleImage(id: String, i: Int) {
        FileOutputStream(photoFile(id)).use { makeSampleImage(i).compress(Bitmap.CompressFormat.JPEG, 88, it) }
    }

    companion object {
        fun newId(prefix: String) = prefix + System.currentTimeMillis().toString(36) + Random.nextInt(100, 999)
    }

    // ---------- JSON ----------
    private fun encode(d: AppData) = JSONObject().apply {
        put("name", d.name); put("home", d.home); put("sample", d.sampleFlights); put("seeded", d.seeded)
        put("credits", JSONObject().also { o -> d.credits.forEach { (k, v) -> o.put(k, v) } })
        put("flights", JSONArray().also { a ->
            d.flights.forEach { f ->
                a.put(JSONObject().apply {
                    put("id", f.id); put("no", f.no); put("date", f.date); put("time", f.time); put("from", f.from); put("to", f.to)
                    put("trip", f.trip); put("status", f.status.name); put("delay", f.delay)
                    put("seat", f.seat); put("gate", f.gate); put("term", f.term)
                })
            }
        })
        put("posts", JSONArray().also { a -> d.posts.forEach { a.put(encodePost(it)) } })
    }

    private fun encodePost(p: Post) = JSONObject().apply {
        put("id", p.id); put("title", p.title); put("trip", p.trip); put("date", p.date); put("cover", p.cover)
        put("sample", p.sample); put("tags", JSONArray(p.tags))
        put("blocks", JSONArray().also { a ->
            p.blocks.forEach { b ->
                a.put(
                    when (b) {
                        is Block.Heading -> JSONObject().put("t", "h").put("text", b.text)
                        is Block.Paragraph -> JSONObject().put("t", "p").put("text", b.text)
                        is Block.Quote -> JSONObject().put("t", "q").put("text", b.text)
                        is Block.Tip -> JSONObject().put("t", "tip").put("text", b.text)
                        is Block.Place -> JSONObject().put("t", "place").put("name", b.name).put("kind", b.kind)
                            .put("rating", b.rating).put("cost", b.cost).put("note", b.note)
                        is Block.Photos -> JSONObject().put("t", "ph").put("tpl", b.template).put(
                            "items",
                            JSONArray().also { ia -> b.items.forEach { ia.put(JSONObject().put("id", it.id).put("cap", it.caption)) } },
                        )
                    },
                )
            }
        })
    }

    private fun decode(o: JSONObject): AppData {
        val credits = o.optJSONObject("credits")?.let { c -> c.keys().asSequence().associateWith { c.getString(it) } } ?: emptyMap()
        val flights = o.optJSONArray("flights")?.let { a ->
            (0 until a.length()).map { i ->
                val f = a.getJSONObject(i)
                Flight(
                    f.getString("id"), f.getString("no"), f.getString("date"), f.optString("time", "09:00"),
                    f.getString("from"), f.getString("to"), f.optString("trip"),
                    runCatching { FlightStatus.valueOf(f.optString("status", "ONTIME")) }.getOrDefault(FlightStatus.ONTIME),
                    f.optInt("delay"), f.optString("seat"), f.optString("gate"), f.optString("term"),
                )
            }
        } ?: emptyList()
        val posts = o.optJSONArray("posts")?.let { a -> (0 until a.length()).map { decodePost(a.getJSONObject(it)) } } ?: emptyList()
        return AppData(
            name = o.optString("name"), home = o.optString("home", "RUH"), flights = flights, posts = posts,
            credits = credits, sampleFlights = o.optBoolean("sample"), seeded = o.optBoolean("seeded"),
        )
    }

    private fun decodePost(p: JSONObject): Post {
        val blocks = p.optJSONArray("blocks")?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                val b = a.getJSONObject(i)
                when (b.optString("t")) {
                    "h" -> Block.Heading(b.optString("text"))
                    "p" -> Block.Paragraph(b.optString("text"))
                    "q" -> Block.Quote(b.optString("text"))
                    "tip" -> Block.Tip(b.optString("text"))
                    "place" -> Block.Place(b.optString("name"), b.optString("kind", "معلم"), b.optInt("rating"), b.optString("cost"), b.optString("note"))
                    "ph" -> Block.Photos(
                        b.optString("tpl", "duo"),
                        b.optJSONArray("items")?.let { ia -> (0 until ia.length()).map { PhotoItem(ia.getJSONObject(it).getString("id"), ia.getJSONObject(it).optString("cap")) } } ?: emptyList(),
                    )
                    else -> null
                }
            }
        } ?: emptyList()
        val tags = p.optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
        return Post(p.getString("id"), p.optString("title"), p.optString("trip"), p.optString("date"), tags, p.optString("cover"), blocks, p.optBoolean("sample"))
    }
}

// ---------- بيانات تجريبية ----------

fun sampleFlights(today: LocalDate): List<Flight> {
    fun f(id: String, no: String, date: String, time: String, from: String, to: String, trip: String, extra: Flight.() -> Flight = { this }) =
        Flight(id, no, date, time, from, to, trip).extra()
    val d = { n: Long -> today.plusDays(n).toString() }
    return listOf(
        f("s1", "SV 731", "2022-10-05", "08:40", "RUH", "FCO", "روما 2022"), f("s2", "SV 732", "2022-10-12", "14:10", "FCO", "RUH", "روما 2022"),
        f("s3", "SV 1051", "2023-12-27", "07:20", "RUH", "DXB", "دبي 2023"), f("s4", "SV 1052", "2024-01-02", "19:05", "DXB", "RUH", "دبي 2023"),
        f("s5", "MS 612", "2024-01-09", "10:30", "RUH", "LXR", "الأقصر 2024"), f("s6", "MS 613", "2024-01-14", "16:45", "LXR", "RUH", "الأقصر 2024"),
        f("s7", "TK 145", "2024-04-12", "02:15", "RUH", "IST", "إسطنبول 2024"), f("s8", "TK 146", "2024-04-19", "11:50", "IST", "RUH", "إسطنبول 2024"),
        f("s9", "SV 215", "2025-06-18", "06:30", "RUH", "ATH", "سانتوريني 2025"), f("s10", "A3 308", "2025-06-18", "12:20", "ATH", "JTR", "سانتوريني 2025"),
        f("s11", "A3 309", "2025-06-24", "09:10", "JTR", "ATH", "سانتوريني 2025"), f("s12", "SV 216", "2025-06-24", "14:30", "ATH", "RUH", "سانتوريني 2025"),
        f("s13", "XY 641", "2025-08-14", "03:40", "RUH", "TBS", "تبليسي 2025"), f("s14", "XY 642", "2025-08-20", "21:15", "TBS", "RUH", "تبليسي 2025"),
        f("s15", "XY 735", "2025-10-03", "01:55", "RUH", "ASR", "كابادوكيا 2025"), f("s16", "XY 736", "2025-10-08", "15:25", "ASR", "RUH", "كابادوكيا 2025"),
        f("s17", "SV 811", d(21), "09:40", "RUH", "MLE", "المالديف") { copy(seat = "14A", gate = "B12", term = "5") },
        f("s18", "SV 812", d(28), "13:05", "MLE", "RUH", "المالديف"),
    )
}

fun samplePost(ids: List<String>): Post {
    fun ph(tpl: String, idx: List<Int>, caps: List<String> = emptyList()) =
        Block.Photos(tpl, idx.mapIndexed { j, i -> PhotoItem(ids[i], caps.getOrElse(j) { "" }) })
    return Post(
        id = "p_demo", title = "أربعة أيام في إسطنبول", trip = "إسطنبول 2024", date = "2024-04-12",
        tags = listOf("ثقافة", "مأكولات محلية", "تصوير"), cover = ids[2], sample = true,
        blocks = listOf(
            Block.Paragraph("وصلنا قبل الفجر وكانت المدينة لا تزال نائمة. هذه التدوينة تجريبية، اكتب فوقها أو احذفها وأضف تدوينتك."),
            ph("trio", listOf(0, 1, 2), listOf("الصباح الأول", "", "غروب على البوسفور")),
            Block.Heading("أين أكلنا"),
            Block.Place("مطعم السمك عند الجسر", "مطعم", 5, "≈ 180 ل.ت للشخص", "احجز قبل الغروب. طلبنا السمك المشوي مع سلطة الراكي."),
            ph("polaroid", listOf(3, 4, 5), listOf("جلسة الشاي", "أزقة قديمة", "الليلة الأخيرة")),
            Block.Quote("أجمل ما في المدينة أنك تضيع فيها ثم تجد نفسك في مكان أجمل."),
            Block.Tip("اشترِ بطاقة النقل من أول يوم، فهي توفر وقتاً ومالاً."),
            ph("strip", listOf(1, 3, 5, 0, 2)),
        ),
    )
}

/** صورة تجريبية مرسومة (غروب وتلال ومعالم) حتى تظهر قوالب الصور فوراً. */
fun makeSampleImage(i: Int): Bitmap {
    val w = 1200
    val h = 800
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val pal = listOf(
        intArrayOf(0xFFF6A57A.toInt(), 0xFFF7D7A0.toInt(), 0xFF2F4F7F.toInt()),
        intArrayOf(0xFF7FA7F2.toInt(), 0xFFCFE4F6.toInt(), 0xFF2D4CA3.toInt()),
        intArrayOf(0xFF2B3A67.toInt(), 0xFFF0A060.toInt(), 0xFF1A2440.toInt()),
        intArrayOf(0xFF9AD4C8.toInt(), 0xFFF5EFD2.toInt(), 0xFF2C7A7B.toInt()),
        intArrayOf(0xFFE8B4C8.toInt(), 0xFFFFD7A8.toInt(), 0xFF5B3A8C.toInt()),
        intArrayOf(0xFFF0C36A.toInt(), 0xFFF7E7B4.toInt(), 0xFF7A4A2A.toInt()),
    )[i % 6]
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.shader = LinearGradient(0f, 0f, 0f, h * 0.72f, pal[0], pal[1], Shader.TileMode.CLAMP)
    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    p.shader = null
    p.color = pal[1]
    c.drawCircle(260f + i * 130f, 250f, 74f, p)
    p.color = pal[2]
    p.alpha = 140
    val hills = Path().apply {
        moveTo(0f, h.toFloat()); lineTo(0f, 540f)
        var x = 0
        while (x <= w) { lineTo(x.toFloat(), 500f + sin(x / 120.0 + i).toFloat() * 50f); x += 40 }
        lineTo(w.toFloat(), h.toFloat()); close()
    }
    c.drawPath(hills, p)
    p.alpha = 255
    when (i % 3) {
        0 -> for (k in 0..2) {
            val x = 420f + k * 190f
            c.drawRect(x - 60f, 560f, x + 60f, 800f, p)
            c.drawArc(x - 60f, 500f, x + 60f, 620f, 180f, 180f, true, p)
            c.drawRect(x - 4f, 470f, x + 4f, 510f, p)
        }
        1 -> for (k in 0..1) {
            val x = 520f + k * 260f
            c.drawPath(Path().apply { moveTo(x, 360f); lineTo(x, 700f); lineTo(x + 170f, 700f); close() }, p)
            c.drawRect(x - 60f, 700f, x + 200f, 720f, p)
        }
        else -> {
            for (k in 0..1) {
                val x = 300f + k * 560f
                c.drawRect(x, 330f, x + 40f, 800f, p)
                c.drawPath(Path().apply { moveTo(x - 6f, 330f); lineTo(x + 20f, 250f); lineTo(x + 46f, 330f); close() }, p)
            }
            c.drawArc(450f, 570f, 750f, 870f, 180f, 180f, true, p)
        }
    }
    return bmp
}
