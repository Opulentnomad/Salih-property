package com.wijha.app.data

data class Airport(
    val code: String,
    val city: String,
    val name: String,
    val cc: String,
    val lat: Double,
    val lon: Double,
    val tz: Int,
)

val MONTHS = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")

val AIRPORTS = listOf(
    Airport("RUH", "الرياض", "مطار الملك خالد", "sa", 24.9576, 46.6988, 3),
    Airport("JED", "جدة", "مطار الملك عبدالعزيز", "sa", 21.6796, 39.1565, 3),
    Airport("DMM", "الدمام", "مطار الملك فهد", "sa", 26.4712, 49.7979, 3),
    Airport("MED", "المدينة المنورة", "مطار الأمير محمد بن عبدالعزيز", "sa", 24.5534, 39.7051, 3),
    Airport("AHB", "أبها", "مطار أبها", "sa", 18.2404, 42.6566, 3),
    Airport("ULH", "العُلا", "مطار الأمير عبدالمجيد", "sa", 26.4838, 38.1288, 3),
    Airport("DXB", "دبي", "مطار دبي الدولي", "ae", 25.2532, 55.3657, 4),
    Airport("AUH", "أبوظبي", "مطار زايد الدولي", "ae", 24.4330, 54.6511, 4),
    Airport("DOH", "الدوحة", "مطار حمد الدولي", "qa", 25.2731, 51.6081, 3),
    Airport("KWI", "الكويت", "مطار الكويت الدولي", "kw", 29.2266, 47.9689, 3),
    Airport("BAH", "المنامة", "مطار البحرين الدولي", "bh", 26.2708, 50.6336, 3),
    Airport("MCT", "مسقط", "مطار مسقط الدولي", "om", 23.5933, 58.2844, 4),
    Airport("SLL", "صلالة", "مطار صلالة", "om", 17.0387, 54.0913, 4),
    Airport("CAI", "القاهرة", "مطار القاهرة الدولي", "eg", 30.1219, 31.4056, 2),
    Airport("LXR", "الأقصر", "مطار الأقصر الدولي", "eg", 25.6710, 32.7066, 2),
    Airport("AMM", "عمّان", "مطار الملكة علياء", "jo", 31.7226, 35.9932, 3),
    Airport("BEY", "بيروت", "مطار رفيق الحريري", "lb", 33.8209, 35.4884, 2),
    Airport("IST", "إسطنبول", "مطار إسطنبول", "tr", 41.2753, 28.7519, 3),
    Airport("SAW", "إسطنبول", "مطار صبيحة غوكشن", "tr", 40.8986, 29.3092, 3),
    Airport("ASR", "قيصري", "مطار قيصري (كابادوكيا)", "tr", 38.7704, 35.4954, 3),
    Airport("TZX", "طرابزون", "مطار طرابزون", "tr", 40.9951, 39.7897, 3),
    Airport("ATH", "أثينا", "مطار أثينا الدولي", "gr", 37.9364, 23.9475, 2),
    Airport("JTR", "سانتوريني", "مطار سانتوريني", "gr", 36.3992, 25.4793, 2),
    Airport("JMK", "ميكونوس", "مطار ميكونوس", "gr", 37.4351, 25.3481, 2),
    Airport("FCO", "روما", "مطار فيوميتشينو", "it", 41.8003, 12.2389, 1),
    Airport("MXP", "ميلانو", "مطار مالبينسا", "it", 45.6306, 8.7281, 1),
    Airport("CDG", "باريس", "مطار شارل ديغول", "fr", 49.0097, 2.5479, 1),
    Airport("LHR", "لندن", "مطار هيثرو", "gb", 51.4700, -0.4543, 0),
    Airport("BCN", "برشلونة", "مطار برشلونة", "es", 41.2974, 2.0833, 1),
    Airport("MAD", "مدريد", "مطار مدريد باراخاس", "es", 40.4983, -3.5676, 1),
    Airport("FRA", "فرانكفورت", "مطار فرانكفورت", "de", 50.0379, 8.5622, 1),
    Airport("AMS", "أمستردام", "مطار سخيبول", "nl", 52.3105, 4.7683, 1),
    Airport("VIE", "فيينا", "مطار فيينا", "at", 48.1103, 16.5697, 1),
    Airport("ZRH", "زيورخ", "مطار زيورخ", "ch", 47.4647, 8.5492, 1),
    Airport("LIS", "لشبونة", "مطار لشبونة", "pt", 38.7742, -9.1342, 0),
    Airport("CMN", "الدار البيضاء", "مطار محمد الخامس", "ma", 33.3675, -7.5898, 1),
    Airport("TBS", "تبليسي", "مطار تبليسي", "ge", 41.6692, 44.9547, 4),
    Airport("KIX", "أوساكا (كيوتو)", "مطار كانساي", "jp", 34.4273, 135.2440, 9),
    Airport("HND", "طوكيو", "مطار هانيدا", "jp", 35.5494, 139.7798, 9),
    Airport("NRT", "طوكيو", "مطار ناريتا", "jp", 35.7720, 140.3929, 9),
    Airport("MLE", "ماليه", "مطار فيلانا الدولي", "mv", 4.1918, 73.5291, 5),
    Airport("SIN", "سنغافورة", "مطار تشانغي", "sg", 1.3644, 103.9915, 8),
    Airport("BKK", "بانكوك", "مطار سوفارنابومي", "th", 13.6900, 100.7501, 7),
    Airport("KUL", "كوالالمبور", "مطار كوالالمبور الدولي", "my", 2.7456, 101.7099, 8),
    Airport("JFK", "نيويورك", "مطار جون كينيدي", "us", 40.6413, -73.7781, -5),
    Airport("LAX", "لوس أنجلوس", "مطار لوس أنجلوس", "us", 33.9416, -118.4085, -8),
)

val AIRPORT_BY_CODE: Map<String, Airport> = AIRPORTS.associateBy { it.code }

val COUNTRY_NAMES = mapOf(
    "sa" to "السعودية", "ae" to "الإمارات", "qa" to "قطر", "kw" to "الكويت", "bh" to "البحرين", "om" to "عُمان",
    "eg" to "مصر", "jo" to "الأردن", "lb" to "لبنان", "tr" to "تركيا", "gr" to "اليونان", "it" to "إيطاليا",
    "fr" to "فرنسا", "gb" to "بريطانيا", "es" to "إسبانيا", "de" to "ألمانيا", "nl" to "هولندا", "at" to "النمسا",
    "ch" to "سويسرا", "pt" to "البرتغال", "ma" to "المغرب", "ge" to "جورجيا", "jp" to "اليابان", "mv" to "المالديف",
    "sg" to "سنغافورة", "th" to "تايلاند", "my" to "ماليزيا", "us" to "أمريكا",
)

val CONTINENT = mapOf(
    "sa" to "as", "ae" to "as", "qa" to "as", "kw" to "as", "bh" to "as", "om" to "as", "jo" to "as", "lb" to "as",
    "tr" to "as", "ge" to "as", "jp" to "as", "mv" to "as", "sg" to "as", "th" to "as", "my" to "as",
    "eg" to "af", "ma" to "af",
    "gr" to "eu", "it" to "eu", "fr" to "eu", "gb" to "eu", "es" to "eu", "de" to "eu", "nl" to "eu", "at" to "eu",
    "ch" to "eu", "pt" to "eu",
    "us" to "na",
)

/** علم الدولة بإيموجي النظام (مؤشرات إقليمية). */
fun flagEmoji(cc: String): String =
    if (cc.length != 2) "🏳" else String(Character.toChars(0x1F1E6 + (cc[0].lowercaseChar() - 'a'))) +
        String(Character.toChars(0x1F1E6 + (cc[1].lowercaseChar() - 'a')))

val AIRLINES = mapOf(
    "SV" to "السعودية", "XY" to "طيران ناس", "F3" to "فلاي أديل", "EK" to "طيران الإمارات", "EY" to "الاتحاد للطيران",
    "FZ" to "فلاي دبي", "G9" to "العربية للطيران", "QR" to "القطرية", "WY" to "الطيران العُماني", "GF" to "طيران الخليج",
    "KU" to "الكويتية", "J9" to "طيران الجزيرة", "MS" to "مصر للطيران", "RJ" to "الملكية الأردنية",
    "ME" to "طيران الشرق الأوسط", "TK" to "الخطوط التركية", "PC" to "بيغاسوس", "A3" to "إيجيان", "AZ" to "إيتا",
    "AF" to "الفرنسية", "BA" to "البريطانية", "IB" to "إيبيريا", "LH" to "لوفتهانزا", "KL" to "كي إل إم",
    "JL" to "اليابانية", "NH" to "أنا", "SQ" to "السنغافورية", "TG" to "التايلاندية", "MH" to "الماليزية", "LX" to "سويس",
)

fun airlineName(no: String): String = AIRLINES[no.trim().take(2).uppercase()] ?: ""

/** عنوان مقال ويكيبيديا لمدينة المطار، لجلب صورة الغلاف. */
val WIKI_TITLE = mapOf(
    "RUH" to "Riyadh", "JED" to "Jeddah", "DMM" to "Dammam", "MED" to "Medina", "AHB" to "Abha", "ULH" to "AlUla",
    "DXB" to "Dubai", "AUH" to "Abu Dhabi", "DOH" to "Doha", "KWI" to "Kuwait City", "BAH" to "Manama", "MCT" to "Muscat",
    "SLL" to "Salalah", "CAI" to "Cairo", "LXR" to "Luxor", "AMM" to "Amman", "BEY" to "Beirut", "IST" to "Istanbul",
    "SAW" to "Istanbul", "ASR" to "Cappadocia", "TZX" to "Trabzon", "ATH" to "Athens", "JTR" to "Santorini",
    "JMK" to "Mykonos", "FCO" to "Rome", "MXP" to "Milan", "CDG" to "Paris", "LHR" to "London", "BCN" to "Barcelona",
    "MAD" to "Madrid", "FRA" to "Frankfurt", "AMS" to "Amsterdam", "VIE" to "Vienna", "ZRH" to "Zurich", "LIS" to "Lisbon",
    "CMN" to "Casablanca", "TBS" to "Tbilisi", "KIX" to "Kyoto", "HND" to "Tokyo", "NRT" to "Tokyo", "MLE" to "Maldives",
    "SIN" to "Singapore", "BKK" to "Bangkok", "KUL" to "Kuala Lumpur", "JFK" to "New York City", "LAX" to "Los Angeles",
)

fun wikiTitle(code: String): String = WIKI_TITLE[code] ?: AIRPORT_BY_CODE[code]?.city ?: code

/** وجهة مقترحة: المفتاح والاسم والدولة ومطار الوصول. */
data class Place(val key: String, val name: String, val cc: String, val airport: String)

val PLACES = listOf(
    Place("mv", "المالديف", "mv", "MLE"), Place("alula", "العُلا", "sa", "ULH"), Place("luxor", "الأقصر", "eg", "LXR"),
    Place("dubai", "دبي", "ae", "DXB"), Place("istanbul", "إسطنبول", "tr", "IST"), Place("cappa", "كابادوكيا", "tr", "ASR"),
    Place("santorini", "سانتوريني", "gr", "JTR"), Place("tbilisi", "تبليسي", "ge", "TBS"), Place("rome", "روما", "it", "FCO"),
    Place("paris", "باريس", "fr", "CDG"), Place("barcelona", "برشلونة", "es", "BCN"), Place("kyoto", "كيوتو", "jp", "KIX"),
    Place("salalah", "صلالة", "om", "SLL"), Place("abha", "أبها", "sa", "AHB"), Place("trabzon", "طرابزون", "tr", "TZX"),
)

val PLACE_BY_KEY: Map<String, Place> = PLACES.associateBy { it.key }

data class Suggestion(val place: String, val reason: String, val temp: Int)

/** ثلاث وجهات لكل شهر مع سبب مختصر وحرارة تقريبية. */
val MONTH_SUGGESTIONS: List<List<Suggestion>> = listOf(
    listOf(Suggestion("mv", "جافّ ومشمس وبحر صافٍ", 30), Suggestion("luxor", "شتاء دافئ لزيارة المعابد", 23), Suggestion("alula", "أجواء معتدلة وفعاليات الشتاء", 20)),
    listOf(Suggestion("mv", "ذروة الموسم الجاف", 30), Suggestion("alula", "نهار لطيف وليل بارد", 22), Suggestion("dubai", "طقس ربيعي على الشاطئ", 26)),
    listOf(Suggestion("dubai", "آخر أسابيع الجو المعتدل", 28), Suggestion("kyoto", "بداية تفتّح أزهار الكرز", 14), Suggestion("alula", "ربيع قبل الحرّ", 25)),
    listOf(Suggestion("kyoto", "أزهار الكرز في آخر أوجها", 17), Suggestion("istanbul", "مهرجان التيوليب في الحدائق", 16), Suggestion("rome", "ربيع معتدل وزحام أقل", 19)),
    listOf(Suggestion("santorini", "افتتاح الموسم وبحر دافئ", 23), Suggestion("barcelona", "شواطئ ومدينة بجو مثالي", 22), Suggestion("rome", "أيام طويلة وطقس لطيف", 23)),
    listOf(Suggestion("santorini", "غروب طويل وبحر دافئ", 27), Suggestion("tbilisi", "صيف أخضر وحمّامات كبريت", 28), Suggestion("abha", "ضباب وجبال خضراء", 24)),
    listOf(Suggestion("salalah", "موسم الخريف الجنوبي", 27), Suggestion("abha", "أبرد من مدن الصيف", 25), Suggestion("trabzon", "غابات ومرتفعات خضراء", 26)),
    listOf(Suggestion("salalah", "رذاذ وأخضر على الجبال", 26), Suggestion("trabzon", "هروب من الحرّ", 26), Suggestion("abha", "سهرات بجو لطيف", 24)),
    listOf(Suggestion("santorini", "بحر دافئ وزحام أقل", 26), Suggestion("barcelona", "نهاية الصيف دون ذروة الزحام", 25), Suggestion("tbilisi", "موسم قطف العنب والنبيذ", 24)),
    listOf(Suggestion("cappa", "مناطيد بسماء صافية", 17), Suggestion("rome", "خريف دافئ", 21), Suggestion("alula", "عودة الجو المعتدل", 30)),
    listOf(Suggestion("kyoto", "أوراق الخريف الحمراء", 14), Suggestion("luxor", "بداية موسم الأقصر", 27), Suggestion("mv", "بداية الموسم الجاف", 30)),
    listOf(Suggestion("mv", "بحر هادئ وسماء صافية", 30), Suggestion("paris", "أضواء الأعياد", 6), Suggestion("dubai", "أجمل شتاء في الخليج", 25)),
)

val PHOTO_TEMPLATES = listOf(
    "single" to "صورة كبيرة", "duo" to "صورتان", "trio" to "ثلاث صور", "grid" to "شبكة",
    "mosaic" to "كولاج", "polaroid" to "بولارويد", "strip" to "شريط فيلم",
)
val PLACE_KINDS = listOf("معلم", "مطعم", "مقهى", "فندق", "شاطئ", "سوق", "نشاط", "متحف")
val POST_TAGS = listOf("شواطئ", "هدوء", "تصوير", "مأكولات محلية", "ثقافة", "عائلي", "مغامرة", "تسوّق")
