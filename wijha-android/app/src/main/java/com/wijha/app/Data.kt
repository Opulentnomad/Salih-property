package com.wijha.app

data class Tag(val id: String, val ar: String, val en: String)

val ALL_TAGS = listOf(
    Tag("beach", "شواطئ", "Beaches"),
    Tag("quiet", "هدوء", "Quiet"),
    Tag("photo", "تصوير", "Photography"),
    Tag("food", "مأكولات محلية", "Local food"),
    Tag("culture", "ثقافة وتاريخ", "Culture"),
    Tag("family", "عائلي", "Family"),
    Tag("adventure", "مغامرة", "Adventure"),
    Tag("night", "حياة ليلية", "Nightlife"),
    Tag("luxury", "فخامة", "Luxury"),
    Tag("nature", "طبيعة", "Nature"),
)

fun tagById(id: String): Tag = ALL_TAGS.first { it.id == id }

/** slot: 0 صباح، 1 ظهر، 2 عصر، 3 غروب/مساء */
data class Poi(
    val id: String,
    val ar: String,
    val en: String,
    val tag: String,
    val slot: Int,
    val hours: Float,
)

data class Destination(
    val id: String,
    val ar: String,
    val en: String,
    val countryAr: String,
    val countryEn: String,
    /** عنوان صفحة ويكيبيديا، تُجلب منه صورة الغلاف مباشرة من الإنترنت */
    val wiki: String,
    /** عبارة بحث في ويكيميديا كومنز لجلب معرض الصور */
    val commonsQuery: String,
    val tags: Map<String, Float>,
    val priceSar: Int,
    val bestAr: String,
    val bestEn: String,
    val blurbAr: String,
    val blurbEn: String,
    val c1: Long,
    val c2: Long,
    val pois: List<Poi>,
)

fun Destination.name(lang: String) = if (lang == "ar") ar else en
fun Destination.country(lang: String) = if (lang == "ar") countryAr else countryEn
fun Poi.name(lang: String) = if (lang == "ar") ar else en
fun Tag.label(lang: String) = if (lang == "ar") ar else en

// ---- حالة المستخدم والرحلات ----
data class Note(val text: String, val epochDay: Long)
data class TripItem(val day: Int, val poiId: String)
data class Trip(
    val id: String,
    val destId: String,
    val startEpochDay: Long,
    val days: Int,
    val travelers: Int,
    val items: List<TripItem>,
    val notes: List<Note>,
)

data class UserState(
    val lang: String = "ar",
    val onboarded: Boolean = false,
    val weights: Map<String, Float> = emptyMap(),
    val budget: Int = 2,
    val liked: Set<String> = emptySet(),
    val hidden: Set<String> = emptySet(),
    val favPhotos: Set<String> = emptySet(),
    val trips: List<Trip> = emptyList(),
)

private fun p(id: String, ar: String, en: String, tag: String, slot: Int, hours: Float) =
    Poi(id, ar, en, tag, slot, hours)

val CATALOG: List<Destination> = listOf(
    Destination(
        id = "santorini", ar = "سانتوريني", en = "Santorini", countryAr = "اليونان", countryEn = "Greece",
        wiki = "Santorini", commonsQuery = "Santorini Oia",
        tags = mapOf("photo" to 1f, "beach" to .7f, "quiet" to .6f, "food" to .6f, "culture" to .5f, "luxury" to .7f, "nature" to .3f),
        priceSar = 4200, bestAr = "مايو – أكتوبر", bestEn = "May – Oct",
        blurbAr = "قباب زرقاء وبيوت بيضاء على حافة بركان، وأشهر غروب في المتوسط.",
        blurbEn = "Blue domes and white houses on a volcanic rim, with the Mediterranean's best sunsets.",
        c1 = 0xFF6F9BF0, c2 = 0xFFF6C4D8,
        pois = listOf(
            p("oia_sunset", "غروب قرية أويا", "Oia sunset", "photo", 3, 2f),
            p("fira", "جولة في فيرا", "Fira old town walk", "culture", 0, 2.5f),
            p("red_beach", "الشاطئ الأحمر", "Red Beach", "beach", 2, 2.5f),
            p("caldera", "رحلة بحرية في الكالديرا", "Caldera cruise", "nature", 1, 4f),
            p("akrotiri", "موقع أكروتيري الأثري", "Akrotiri archaeological site", "culture", 0, 2f),
            p("winery", "تذوق النبيذ", "Winery tasting", "food", 2, 2f),
            p("imerovigli", "ممشى فيرا إلى إيمروفيجلي", "Fira to Imerovigli hike", "adventure", 0, 3f),
            p("ammoudi", "عشاء سمك في خليج عمودي", "Seafood at Ammoudi Bay", "food", 3, 2f),
        ),
    ),
    Destination(
        id = "mykonos", ar = "ميكونوس", en = "Mykonos", countryAr = "اليونان", countryEn = "Greece",
        wiki = "Mykonos", commonsQuery = "Mykonos",
        tags = mapOf("beach" to 1f, "night" to .9f, "luxury" to .8f, "photo" to .7f, "food" to .6f, "quiet" to .2f),
        priceSar = 4800, bestAr = "يونيو – سبتمبر", bestEn = "Jun – Sep",
        blurbAr = "شواطئ ذهبية وأزقة بيضاء وحياة ليلية لا تنام.",
        blurbEn = "Golden beaches, whitewashed lanes and a nightlife that never sleeps.",
        c1 = 0xFF7FB6E8, c2 = 0xFFF3DDC2,
        pois = listOf(
            p("little_venice", "ليتل فينيس", "Little Venice", "photo", 3, 2f),
            p("windmills", "طواحين كاتو ميلي", "Kato Mili windmills", "photo", 2, 1f),
            p("paradise", "شاطئ باراديس", "Paradise Beach", "beach", 1, 4f),
            p("super_paradise", "شاطئ سوبر باراديس", "Super Paradise Beach", "night", 2, 3f),
            p("old_town_myk", "أزقة مدينة ميكونوس", "Mykonos Town alleys", "culture", 0, 2.5f),
            p("delos", "رحلة إلى جزيرة ديلوس", "Delos island trip", "culture", 0, 4f),
            p("psarou", "غداء على شاطئ بسارو", "Lunch at Psarou", "food", 1, 2f),
            p("armenistis", "منارة أرمينيستيس", "Armenistis Lighthouse", "quiet", 3, 1.5f),
        ),
    ),
    Destination(
        id = "istanbul", ar = "إسطنبول", en = "Istanbul", countryAr = "تركيا", countryEn = "Türkiye",
        wiki = "Istanbul", commonsQuery = "Istanbul Bosphorus",
        tags = mapOf("culture" to 1f, "food" to 1f, "photo" to .7f, "family" to .6f, "night" to .6f, "luxury" to .3f),
        priceSar = 3300, bestAr = "أبريل – يونيو", bestEn = "Apr – Jun",
        blurbAr = "مدينة بين قارتين: مساجد وأسواق ومضيق ومطبخ لا يُنسى.",
        blurbEn = "A city on two continents: mosques, bazaars, the Bosphorus and unforgettable food.",
        c1 = 0xFF8FA8E6, c2 = 0xFFFFB98A,
        pois = listOf(
            p("hagia", "آيا صوفيا", "Hagia Sophia", "culture", 0, 2f),
            p("blue_mosque", "المسجد الأزرق", "Blue Mosque", "culture", 0, 1.5f),
            p("grand_bazaar", "البازار الكبير", "Grand Bazaar", "food", 1, 3f),
            p("bosphorus", "رحلة البوسفور", "Bosphorus cruise", "nature", 2, 2f),
            p("galata", "برج غلطة", "Galata Tower", "photo", 3, 1.5f),
            p("topkapi", "قصر توب كابي", "Topkapi Palace", "culture", 1, 3f),
            p("spice", "السوق المصري والتوابل", "Spice Bazaar", "food", 1, 2f),
            p("karakoy", "مقاهي كاراكوي", "Karaköy cafés", "food", 2, 2f),
        ),
    ),
    Destination(
        id = "maldives", ar = "المالديف", en = "Maldives", countryAr = "المالديف", countryEn = "Maldives",
        wiki = "Maldives", commonsQuery = "Maldives island",
        tags = mapOf("beach" to 1f, "quiet" to 1f, "luxury" to 1f, "nature" to .8f, "photo" to .7f, "family" to .5f, "adventure" to .5f),
        priceSar = 9500, bestAr = "نوفمبر – أبريل", bestEn = "Nov – Apr",
        blurbAr = "جزر مرجانية ومياه فيروزية وفيلات فوق الماء.",
        blurbEn = "Coral islands, turquoise lagoons and overwater villas.",
        c1 = 0xFF7FE4E2, c2 = 0xFF1B9AC4,
        pois = listOf(
            p("snorkel", "غطس سطحي في الشعاب", "Reef snorkeling", "adventure", 0, 2.5f),
            p("sandbank", "رحلة إلى الجزيرة الرملية", "Sandbank trip", "beach", 1, 3f),
            p("dolphin", "الدلافين عند الغروب", "Sunset dolphin cruise", "nature", 3, 2f),
            p("male", "جولة في ماليه", "Malé city tour", "culture", 1, 3f),
            p("spa", "سبا فوق الماء", "Overwater spa", "luxury", 2, 2f),
            p("dive", "غوص مع الأسماك", "Scuba dive", "adventure", 0, 3f),
            p("maafushi", "جزيرة محلية: ماافوشي", "Maafushi local island", "food", 2, 3f),
            p("glow", "شاطئ المياه المضيئة", "Glowing beach at night", "photo", 3, 1.5f),
        ),
    ),
    Destination(
        id = "tbilisi", ar = "تبليسي", en = "Tbilisi", countryAr = "جورجيا", countryEn = "Georgia",
        wiki = "Tbilisi", commonsQuery = "Tbilisi",
        tags = mapOf("culture" to .9f, "food" to .9f, "nature" to .6f, "night" to .6f, "quiet" to .5f, "family" to .5f, "adventure" to .5f),
        priceSar = 2600, bestAr = "مايو – سبتمبر", bestEn = "May – Sep",
        blurbAr = "عاصمة هادئة بحمامات كبريت وجبال قريبة ومطبخ شهي.",
        blurbEn = "A relaxed capital with sulfur baths, nearby mountains and a delicious cuisine.",
        c1 = 0xFFA9C7F0, c2 = 0xFFE6D2C0,
        pois = listOf(
            p("old_tbilisi", "المدينة القديمة", "Old Town", "culture", 0, 3f),
            p("sulfur", "حمامات الكبريت", "Sulfur baths", "quiet", 2, 2f),
            p("narikala", "قلعة ناريكالا بالتلفريك", "Narikala Fortress", "photo", 1, 2f),
            p("khinkali", "تذوّق الخينكالي", "Khinkali tasting", "food", 1, 1.5f),
            p("mtatsminda", "حديقة متاتسميندا", "Mtatsminda Park", "family", 2, 3f),
            p("kazbegi", "رحلة يوم إلى كازبيجي", "Kazbegi day trip", "adventure", 0, 9f),
            p("peace", "جسر السلام", "Bridge of Peace", "photo", 3, 1f),
            p("wine_eve", "أمسية نبيذ جورجي", "Georgian wine evening", "night", 3, 2.5f),
        ),
    ),
    Destination(
        id = "cappadocia", ar = "كابادوكيا", en = "Cappadocia", countryAr = "تركيا", countryEn = "Türkiye",
        wiki = "Cappadocia", commonsQuery = "Cappadocia balloons",
        tags = mapOf("photo" to 1f, "adventure" to .8f, "nature" to .8f, "culture" to .7f, "quiet" to .6f, "luxury" to .4f),
        priceSar = 3200, bestAr = "أبريل – أكتوبر", bestEn = "Apr – Oct",
        blurbAr = "وديان صخرية ومناطيد عند الشروق وفنادق في الكهوف.",
        blurbEn = "Rock valleys, sunrise balloons and cave hotels.",
        c1 = 0xFFF2A6A0, c2 = 0xFFFFD9A8,
        pois = listOf(
            p("balloon", "المنطاد عند الشروق", "Sunrise balloon ride", "photo", 0, 3f),
            p("goreme", "متحف غوريمي المفتوح", "Göreme Open-Air Museum", "culture", 1, 2.5f),
            p("uchisar", "قلعة أُوتشيسار", "Uçhisar Castle", "photo", 3, 1.5f),
            p("derinkuyu", "المدينة تحت الأرض", "Derinkuyu underground city", "culture", 1, 2f),
            p("rose_valley", "وادي الورد", "Rose Valley hike", "adventure", 2, 3f),
            p("atv", "جولة دراجات رباعية", "ATV tour", "adventure", 2, 2f),
            p("avanos", "ورشة الفخار في أفانوس", "Avanos pottery workshop", "culture", 1, 2f),
            p("cave_dinner", "عشاء في مطعم كهفي", "Cave restaurant dinner", "food", 3, 2f),
        ),
    ),
    Destination(
        id = "barcelona", ar = "برشلونة", en = "Barcelona", countryAr = "إسبانيا", countryEn = "Spain",
        wiki = "Barcelona", commonsQuery = "Barcelona Sagrada Familia",
        tags = mapOf("culture" to .9f, "food" to .9f, "night" to .9f, "photo" to .8f, "beach" to .7f, "family" to .6f),
        priceSar = 5200, bestAr = "مايو – سبتمبر", bestEn = "May – Sep",
        blurbAr = "عمارة غاودي والبحر والتاباس في مدينة واحدة.",
        blurbEn = "Gaudí's architecture, the sea and tapas in one city.",
        c1 = 0xFF8FC3EE, c2 = 0xFFFFD6A5,
        pois = listOf(
            p("sagrada", "ساغرادا فاميليا", "Sagrada Família", "culture", 0, 2.5f),
            p("park_guell", "حديقة غويل", "Park Güell", "photo", 1, 2.5f),
            p("boqueria", "سوق لا بوكيريا", "La Boqueria market", "food", 1, 1.5f),
            p("gothic", "الحي القوطي", "Gothic Quarter", "culture", 2, 2.5f),
            p("barceloneta", "شاطئ باركيلونيتا", "Barceloneta Beach", "beach", 2, 3f),
            p("casa_batllo", "كاسا باتلو", "Casa Batlló", "culture", 0, 1.5f),
            p("bunkers", "غروب من مخابئ الكارمل", "Bunkers del Carmel sunset", "photo", 3, 2f),
            p("tapas", "جولة تاباس مسائية", "Evening tapas crawl", "night", 3, 3f),
        ),
    ),
    Destination(
        id = "alula", ar = "العُلا", en = "AlUla", countryAr = "السعودية", countryEn = "Saudi Arabia",
        wiki = "AlUla", commonsQuery = "AlUla Hegra",
        tags = mapOf("culture" to .9f, "nature" to .9f, "photo" to .9f, "quiet" to .8f, "adventure" to .7f, "luxury" to .6f),
        priceSar = 2800, bestAr = "أكتوبر – مارس", bestEn = "Oct – Mar",
        blurbAr = "حضارات قديمة وصخور شاهقة وسماء صافية للنجوم.",
        blurbEn = "Ancient civilisations, towering sandstone and star-filled skies.",
        c1 = 0xFFF0B27A, c2 = 0xFFF8E0C0,
        pois = listOf(
            p("hegra", "الحِجر (مدائن صالح)", "Hegra (Madain Salih)", "culture", 0, 3f),
            p("elephant", "جبل الفيل", "Elephant Rock", "photo", 3, 1.5f),
            p("alula_old", "البلدة القديمة", "AlUla Old Town", "culture", 1, 2f),
            p("maraya", "مرايا", "Maraya", "photo", 2, 1.5f),
            p("dadan", "موقع دادان", "Dadan", "culture", 0, 2f),
            p("alula_balloon", "منطاد العُلا", "AlUla hot air balloon", "adventure", 0, 2f),
            p("harrat", "حرة عويرض", "Harrat Uwayrid", "nature", 2, 3f),
            p("stars", "مراقبة النجوم", "Desert stargazing", "quiet", 3, 2.5f),
        ),
    ),
)

fun destById(id: String): Destination? = CATALOG.firstOrNull { it.id == id }
