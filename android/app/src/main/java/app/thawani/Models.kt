package app.thawani

data class Link(val title: String, val url: String)

data class Card(
    val id: String, val cat: String, val title: String, val phone: String, val level: String, val lv: String,
    val golden: String, val steps: List<String>, val avoid: List<String>, val gear: List<String>,
    val ctx: List<String>, val refs: List<Link>, val vids: List<Link>
)

data class Checklist(val id: String, val title: String, val note: String, val refTitle: String, val refUrl: String, val items: List<String>)
data class Season(val id: String, val name: String, val d: String, val ids: List<String>)
data class Faq(val q: String, val v: String, val a: String, val card: String)
data class EnCard(val title: String, val level: String, val golden: String, val steps: List<String>, val avoid: List<String>)
data class RefItem(val id: String, val title: String, val url: String, val desc: String)
data class RefGroup(val title: String, val items: List<RefItem>)
data class Num(val n: String, val title: String, val desc: String)

object Data {
    val cards: List<Card> by lazy { cards1() + cards2() + cards3() + cards4() + cards5() + cards6() + cards7() + cards8() }
    val cats: Map<String, String> by lazy { catsMap() }
    val lv: Map<String, String> by lazy { lvMap() }
    val places: List<String> by lazy { placesList() }
    val kids: List<String> by lazy { kidsIds() }
    val kits: List<Checklist> by lazy { checklists() }
    val seasons: List<Season> by lazy { seasons() }
    val faq: List<Faq> by lazy { faqs() }
    val en: Map<String, EnCard> by lazy { enCards() }
    val refs: List<RefGroup> by lazy { refGroups() }
    val nums: List<Num> by lazy { numbers() }
    private val illus: Map<Pair<String, Int>, String> by lazy { illusList().associate { (it.first to it.second) to it.third } }
    fun illus(cardId: String, step: Int): String? = illus[cardId to step]
    fun card(id: String): Card? = cards.firstOrNull { it.id == id }
    val quick = listOf("p3", "p1", "p2", "p5", "p20", "p21", "p25", "p15")
    val quickLabel = mapOf(
        "p3" to "توقف القلب", "p1" to "اختناق بغصة", "p2" to "نزيف شديد", "p5" to "ألم صدر أو جلطة",
        "p20" to "رائحة غاز", "p21" to "حريق مطبخ", "p25" to "دخان فحم", "p15" to "طفل ابتلع سماً"
    )
}

fun norm(s: String): String = s.lowercase()
    .replace(Regex("[ً-ٰٟـ]"), "")
    .replace(Regex("[أإآ]"), "ا").replace("ة", "ه").replace("ى", "ي").trim()
