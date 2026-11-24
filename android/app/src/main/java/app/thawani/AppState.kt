package app.thawani

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed interface Dest {
    data object Home : Dest
    data object Library : Dest
    data class CardD(val id: String, val mode: String = "over") : Dest
    data object CprD : Dest
    data object Kit : Dest
    data object More : Dest
    data object FaqD : Dest
    data object NumsD : Dest
    data object Plan : Dest
    data object RefsD : Dest
    data object About : Dest
}

class AppState(val prefs: Prefs) {
    var query by mutableStateOf("")
    var cat by mutableStateOf("all")
    var place by mutableStateOf("all")
    var season by mutableStateOf("")
    var kidsAll by mutableStateOf(false)
    var profile by mutableStateOf(prefs.profile)
    var scale by mutableFloatStateOf(prefs.scale)
    var theme by mutableStateOf(prefs.theme)
    var rate by mutableFloatStateOf(prefs.rate)
    var autoread by mutableStateOf(prefs.autoread)
    var simple by mutableStateOf(prefs.simple)
    var stars by mutableIntStateOf(prefs.stars)
    var emergency by mutableStateOf(false)
    var focusSearch by mutableStateOf(false)
    var showWelcome by mutableStateOf(prefs.profile.isEmpty())
    val stack = mutableStateListOf<Dest>(Dest.Home)
    val current: Dest get() = stack.last()

    fun go(d: Dest) { stack.add(d) }
    fun tab(d: Dest) { stack.clear(); stack.add(Dest.Home); if (d != Dest.Home) stack.add(d) }
    fun back(): Boolean { if (stack.size > 1) { stack.removeAt(stack.lastIndex); return true }; return false }

    fun updScale(v: Float) { scale = v; prefs.scale = v }
    fun updTheme(v: String) { theme = v; prefs.theme = v }
    fun updRate(v: Float) { rate = v; prefs.rate = v }
    fun addStars(n: Int) { stars += n; prefs.stars = stars }
    fun updSimple(v: Boolean) { simple = v; prefs.simple = v }
    fun updAutoread(v: Boolean) { autoread = v; prefs.autoread = v }

    fun applyProfile(p: String) {
        profile = p; prefs.profile = p; showWelcome = false
        when (p) {
            "senior" -> { updSimple(true); updAutoread(true); if (scale < 1.25f) updScale(1.25f) }
            else -> { updSimple(false); updAutoread(false) }
        }
        if (p == "child") kidsAll = false
    }

    fun filtered(): List<Card> {
        val q = norm(query)
        val sIds = Data.seasons.firstOrNull { it.id == season }?.ids
        return Data.cards.filter { c ->
            (cat == "all" || c.cat == cat) && (place == "all" || c.ctx.contains(place)) &&
                (sIds == null || sIds.contains(c.id)) && (profile != "child" || kidsAll || Data.kids.contains(c.id)) &&
                (q.isEmpty() || norm(listOf(c.title, c.level, c.golden, Data.cats[c.cat] ?: "", c.steps.joinToString(" "), c.avoid.joinToString(" "), c.gear.joinToString(" "), c.ctx.joinToString(" ")).joinToString(" ")).contains(q))
        }
    }

    fun family(key: String, def: String) = prefs.family(key, def)

    fun cardText(c: Card): String {
        val f = { k: String, d: String -> family(k, d) }
        return listOf(
            "🚨 *${c.title}*", "📞 رقم الطوارئ: ${c.phone}  |  ${c.level}", "", "⭐ *القاعدة الأولى:*", c.golden, "",
            "⚡ *الخطوات:*", *c.steps.mapIndexed { i, s -> "${i + 1}. $s" }.toTypedArray(), "",
            "⛔ *تجنب فوراً:*", *c.avoid.map { "• $it" }.toTypedArray(), "",
            "🧰 *التجهيزات وعلامات الخطر:*", *c.gear.map { "• $it" }.toTypedArray(), "",
            "🏠 *بيانات العائلة*", "📍 الموقع: ${f("loc", "منزل العائلة")}", "📞 طوارئ العائلة: ${f("tel", "911 / 997")}",
            "🏃 نقطة التجمع: ${f("pt", "خارج المبنى الرئيسي")}", "🩺 الإسعافات/الطفاية: ${f("kit", "مدخل المنزل / المطبخ")}", "", "— ثواني"
        ).joinToString("\n")
    }
}
