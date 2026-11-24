package app.thawani

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

private class Q(val q: String, val ok: String, val opts: List<String>, val why: String)

private fun buildQuiz(c: Card): List<Q> {
    val nums = listOf("997", "911", "998", "937", "993", "996")
    val qs = mutableListOf<Q>()
    qs += Q("ما الرقم المناسب للاتصال في هذه الحالة؟", c.phone, (listOf(c.phone) + nums.filter { it != c.phone }.shuffled().take(2)).shuffled(), "الرقم المناسب هو ${c.phone}.")
    if (c.avoid.isNotEmpty() && c.steps.size >= 2) { val a = c.avoid.random(); qs += Q("أي مما يلي يجب تجنبه في هذه الحالة؟", a, (listOf(a) + c.steps.shuffled().take(2)).shuffled(), "هذا ما يجب تجنبه: $a") }
    if (c.avoid.isNotEmpty()) { val s = c.steps.random(); qs += Q("أي مما يلي خطوة صحيحة في هذه الحالة؟", s, (listOf(s) + c.avoid.shuffled().take(2)).shuffled(), "الخطوة الصحيحة: $s") }
    return qs
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardScreen(app: AppState, id: String, startMode: String, speaker: Speaker) {
    val c = Data.card(id) ?: return
    val th = LocalTh.current
    val ctx = LocalContext.current
    var mode by remember(id, startMode) { mutableStateOf(startMode) }
    var lang by remember(id) { mutableStateOf("en") }
    var idx by remember(id) { mutableIntStateOf(0) }
    var talk by remember(id, startMode) { mutableStateOf(startMode == "guide" && app.autoread) }
    var qs by remember(id) { mutableStateOf(listOf<Q>()) }
    var qi by remember(id) { mutableIntStateOf(0) }
    var score by remember(id) { mutableIntStateOf(0) }
    var sel by remember(id) { mutableIntStateOf(-1) }
    val n = c.steps.size
    val col = lvColor(c.lv, th.dark)

    DisposableEffect(id) { onDispose { speaker.stop() } }
    LaunchedEffect(idx, talk, mode) {
        if (mode == "guide" && talk && idx <= n) speaker.speak(if (idx == 0) "القاعدة الأولى. ${c.golden}" else "الخطوة $idx. ${c.steps[idx - 1]}", app.rate) else if (!talk) speaker.stop()
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${Data.cats[c.cat] ?: ""}${if (c.lv == "c" || c.lv == "d") " · حالة عاجلة" else ""}", color = th.acc, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(c.title, color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
        // tabs
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.card).border(1.dp, th.line, RoundedCornerShape(14.dp)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TabBtn("البطاقة كاملة", mode == "over") { mode = "over"; talk = false }
            TabBtn("خطوة بخطوة وبالصوت", mode == "guide" || mode == "quiz") { mode = "guide"; if (app.autoread) talk = true }
            if (Data.en.containsKey(id)) TabBtn("English", mode == "lang") { mode = "lang"; talk = false }
        }

        when (mode) {
            "over" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Btn("مشاركة", { ctx.shareText(app.cardText(c)) }, kind = "ghost")
                    Btn("اتصل ${c.phone}", { ctx.dial(c.phone) }, kind = "red")
                }
                ThCard(Modifier.fillMaxWidth(), color = col.copy(alpha = .10f).compositeOver(th.card)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(c.level, color = th.ink2, fontSize = 16.sp)
                        Btn("اتصل الآن ${c.phone}", { ctx.dial(c.phone) }, kind = "red", modifier = Modifier.fillMaxWidth())
                    }
                }
                Banner("القاعدة الأولى\n${c.golden}")
                Text("الخطوات", color = th.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                c.steps.forEachIndexed { i, s ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.soft).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(th.acc), contentAlignment = Alignment.Center) { Text("${i + 1}", color = th.onAcc, fontWeight = FontWeight.Bold) }
                        Text(s, color = th.ink, fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.weight(1f))
                        if (Data.illus(id, i) != null) Box(Modifier.width(96.dp)) { Illus(id, i) }
                    }
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.redSoft).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("تجنب فوراً", color = th.redText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    c.avoid.forEach { Text("• $it", color = th.ink, fontSize = 15.sp, lineHeight = 22.sp) }
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.soft).padding(14.dp)) {
                    Text("التجهيزات وعلامات الخطر", color = th.ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    c.gear.forEach { g ->
                        var ck by remember(id, g) { mutableStateOf(false) }
                        Row(Modifier.fillMaxWidth().clickable { ck = !ck }, verticalAlignment = Alignment.Top) {
                            Checkbox(checked = ck, onCheckedChange = { ck = it }, colors = CheckboxDefaults.colors(checkedColor = th.acc))
                            Text(g, color = th.ink, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
                Sources(c)
                ThCard(Modifier.fillMaxWidth(), color = th.soft) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("📍 الموقع: ${app.family("loc", "منزل العائلة")}", color = th.ink, fontSize = 14.sp)
                        Text("📞 طوارئ العائلة: ${app.family("tel", "911 / 997")}", color = th.ink, fontSize = 14.sp)
                        Text("🏃 نقطة التجمع: ${app.family("pt", "خارج المبنى الرئيسي")}", color = th.ink, fontSize = 14.sp)
                        Text("🩺 الإسعافات والطفاية: ${app.family("kit", "مدخل المنزل / المطبخ")}", color = th.ink, fontSize = 14.sp)
                    }
                }
            }
            "guide" -> {
                if (idx > n) {
                    ThCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("أحسنت. هل اتصلت بـ ${c.phone}؟", color = th.ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("تابع حالة المصاب حتى وصول المسعفين.", color = th.ink2)
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.redSoft).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("تجنب فوراً", color = th.redText, fontWeight = FontWeight.Bold)
                                c.avoid.forEach { Text("• $it", color = th.ink, fontSize = 15.sp, lineHeight = 22.sp) }
                            }
                            Sources(c)
                            Btn("اتصل بـ ${c.phone}", { ctx.dial(c.phone) }, kind = "red", modifier = Modifier.fillMaxWidth())
                            Btn("اختبر نفسك", { qs = buildQuiz(c); qi = 0; score = 0; sel = -1; mode = "quiz"; talk = false }, kind = if (app.profile == "child") "solid" else "ghost", modifier = Modifier.fillMaxWidth())
                            Btn("إعادة الخطوات", { idx = 0 }, kind = "ghost", modifier = Modifier.fillMaxWidth())
                            Btn("عرض البطاقة كاملة", { mode = "over" }, kind = "ghost", modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    ThCard(Modifier.fillMaxWidth().pointerInput(id, idx) {
                        var dx = 0f
                        detectHorizontalDragGestures(onDragStart = { dx = 0f }, onDragEnd = { if (dx < -80f && idx <= n) idx++ else if (dx > 80f && idx > 0) idx-- }) { _, d -> dx += d }
                    }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (idx > 0 && Data.illus(id, idx - 1) != null) Illus(id, idx - 1)
                            else Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)).background(col.copy(alpha = .10f)), contentAlignment = Alignment.Center) { CardIcon(c, 90.dp) }
                            Text(if (idx == 0) "قبل أن تبدأ" else "الخطوة $idx من $n", color = th.acc, fontWeight = FontWeight.Bold)
                            if (idx == 0) Banner(c.golden) else Text(c.steps[idx - 1], color = th.ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold, lineHeight = 34.sp, modifier = Modifier.heightIn(min = 120.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                for (k in 0..n) Box(Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)).background(if (k <= idx) th.acc else th.line))
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Btn("→", { if (idx > 0) idx-- }, kind = "ghost", enabled = idx > 0, modifier = Modifier.width(64.dp))
                                Btn(if (talk && speaker.speaking) "⏹ إيقاف الصوت" else "🔊 تشغيل الصوت", {
                                    if (!speaker.arabicOk) ctx.toast("لم يُعثر على صوت عربي على جهازك. ثبّت بيانات الصوت العربي من إعدادات القراءة الصوتية.")
                                    if (talk && speaker.speaking) { talk = false; speaker.stop() } else talk = true
                                }, modifier = Modifier.weight(1f))
                                Btn(if (idx == n) "إنهاء ←" else "التالي ←", { idx++ }, modifier = Modifier.weight(1.2f))
                            }
                            if (id == "p3" || id == "p2") Btn(if (id == "p3") "افتح إيقاع الإنعاش" else "ابدأ مؤقت العاصبة", { app.go(Dest.CprD) }, kind = "ghost", modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Btn("اتصل الآن ${c.phone}", { ctx.dial(c.phone) }, kind = "red", modifier = Modifier.fillMaxWidth())
                }
            }
            "quiz" -> {
                if (qi >= qs.size) {
                    ThCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("★".repeat(score) + "☆".repeat(qs.size - score), color = Pal.gold, fontSize = 34.sp)
                            Text(if (score == qs.size) "ممتاز! أنت جاهز لهذه الحالة" else if (score > 0) "أحسنت، راجع الخطوات مرة أخرى" else "لا بأس، اقرأ الخطوات مرة ثانية وحاول", color = th.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Btn("اختبار جديد", { qs = buildQuiz(c); qi = 0; score = 0; sel = -1 }, modifier = Modifier.fillMaxWidth())
                            Btn("راجع الخطوات", { idx = 0; mode = "guide" }, kind = "ghost", modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    val q = qs[qi]
                    ThCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("سؤال ${qi + 1} من ${qs.size}", color = th.acc, fontWeight = FontWeight.Bold)
                            Text(q.q, color = th.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp)
                            q.opts.forEachIndexed { k, o ->
                                val done = sel >= 0
                                val bg = when { !done -> th.soft; o == q.ok -> Pal.cautD.copy(alpha = 0f).compositeOver(th.soft2); k == sel -> th.redSoft; else -> th.soft }
                                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg)
                                    .border(2.dp, if (done && o == q.ok) th.acc else if (done && k == sel) Pal.red else th.line, RoundedCornerShape(14.dp))
                                    .clickable(enabled = !done) { sel = k; if (q.opts[k] == q.ok) score++ }.padding(14.dp)) {
                                    Text(o, color = th.ink, fontSize = 16.sp, lineHeight = 24.sp)
                                }
                            }
                            if (sel >= 0) {
                                Text(if (q.opts[sel] == q.ok) "أحسنت! إجابة صحيحة." else q.why, color = th.ink, fontWeight = FontWeight.SemiBold)
                                Btn(if (qi + 1 >= qs.size) "النتيجة" else "السؤال التالي", {
                                    qi++; sel = -1
                                    if (qi >= qs.size) app.addStars(score)
                                }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
            "lang" -> {
                val e = Data.en[id]
                if (e != null) CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Banner("Draft translation for visitors who do not read Arabic. The Arabic card is the reference. Always call the emergency number first.")
                        Btn("Call now ${c.phone}", { ctx.dial(c.phone) }, kind = "red", modifier = Modifier.fillMaxWidth())
                        Text(e.title, color = th.ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
                        Text(e.level, color = th.ink2)
                        Banner("First rule\n${e.golden}")
                        Text("Steps", color = th.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        e.steps.forEachIndexed { i, s ->
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.soft).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(32.dp).clip(CircleShape).background(th.acc), contentAlignment = Alignment.Center) { Text("${i + 1}", color = th.onAcc, fontWeight = FontWeight.Bold) }
                                Text(s, color = th.ink, fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.weight(1f))
                            }
                        }
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(th.redSoft).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Avoid immediately", color = th.redText, fontWeight = FontWeight.Bold)
                            e.avoid.forEach { Text("• $it", color = th.ink, fontSize = 15.sp, lineHeight = 22.sp) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Sources(c: Card) {
    val th = LocalTh.current
    val ctx = LocalContext.current
    if (c.vids.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("فيديو توضيحي", color = th.ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        c.vids.forEach { Text("▶ ${it.title}", color = th.acc, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { ctx.openUrl(it.url) }.padding(vertical = 6.dp)) }
        Text("المشاهدة تحتاج اتصالاً بالإنترنت.", color = th.ink2, fontSize = 13.sp)
    }
    if (c.refs.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("المصادر", color = th.ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        c.refs.forEach { Text("${it.title} ↗", color = th.acc, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { ctx.openUrl(it.url) }.padding(vertical = 6.dp)) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabBtn(text: String, on: Boolean, f: () -> Unit) {
    val th = LocalTh.current
    Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(if (on) th.acc else Color.Transparent).clickable { f() }, contentAlignment = Alignment.Center) {
        Text(text, color = if (on) th.onAcc else th.ink2, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
