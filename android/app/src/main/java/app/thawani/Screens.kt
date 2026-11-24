package app.thawani

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private fun fmt(s: Long) = "%02d:%02d".format(s / 60, s % 60)
private fun fmtH(s: Long) = "%02d:%02d:%02d".format(s / 3600, s % 3600 / 60, s % 60)

@Composable
private fun Page(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content(); Spacer(Modifier.height(24.dp)) }
}

@Composable
fun CprScreen(app: AppState, cpr: Cpr) {
    val th = LocalTh.current
    val ctx = LocalContext.current
    val sc by animateFloatAsState(if (cpr.beat) 1.25f else 1f, label = "beat")
    Page {
        Text("إيقاع الضغطات الصدرية", color = th.ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("110 ضغطات في الدقيقة، ضمن المعدل الموصى به (100 إلى 120). اضغط في منتصف الصدر مع كل نبضة.", color = th.ink2, fontSize = 15.sp, lineHeight = 22.sp)
        Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(20.dp)).background(th.soft2), contentAlignment = Alignment.Center) {
            Text("♥", color = Pal.red, fontSize = 84.sp, modifier = Modifier.scale(sc))
        }
        Btn(if (cpr.running) "إيقاف مؤقت" else if (cpr.count > 0) "استئناف" else "اضغط الآن", { cpr.toggle() },
            kind = if (cpr.running) "ghost" else "red", modifier = Modifier.fillMaxWidth().height(72.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("${cpr.count}" to "ضغطة", "110" to "نبضة/دقيقة", fmt(cpr.elapsed) to "الزمن").forEach { (v, l) ->
                Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(th.soft).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(v, color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold); Text(l, color = th.ink2, fontSize = 13.sp)
                }
            }
        }
        cpr.alert?.let { Banner(it) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn("اتصل 997", { ctx.dial("997") }, kind = "red")
            Btn("تصفير", { cpr.reset() }, kind = "ghost")
            Btn("خطوات الإنعاش", { app.go(Dest.CardD("p3", "guide")) }, kind = "ghost")
        }
        ThCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("مؤقت العاصبة", color = th.ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Text("سجّل وقت تركيبها وأبلغ به المسعفين عند وصولهم.", color = th.ink2, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                Text(fmtH(cpr.tSeconds), color = th.ink, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Text(cpr.tSince, color = th.ink2, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Btn(if (cpr.tRunning) "إيقاف مؤقت" else if (cpr.tSeconds > 0) "استئناف" else "بدء", { cpr.tToggle() })
                    Btn("تصفير", { cpr.tReset() }, kind = "ghost")
                    Btn("بطاقة النزيف", { app.go(Dest.CardD("p2", "guide")) }, kind = "ghost")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KitScreen(app: AppState) {
    val th = LocalTh.current
    val ctx = LocalContext.current
    var kitId by remember { mutableStateOf(Data.kits.first().id) }
    val kit = Data.kits.first { it.id == kitId }
    var checked by remember(kitId) { mutableStateOf(app.prefs.kitChecked(kitId)) }
    val pct = if (kit.items.isEmpty()) 0 else checked.size * 100 / kit.items.size
    Page {
        Text("قوائم التجهيز", color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("استعد قبل الحاجة. علّم ما جهّزته، وراجع المحتويات دورياً. علاماتك تُحفظ على جهازك فقط.", color = th.ink2, fontSize = 15.sp, lineHeight = 22.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Data.kits.forEach { k -> Chip(k.title, k.id == kitId) { kitId = k.id } } }
        ThCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("قائمة ${kit.title}", color = th.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(kit.note, color = th.ink2, fontSize = 14.sp) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("$pct%", color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("مكتمل", color = th.ink2, fontSize = 12.sp) }
                }
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)).background(th.line)) { Box(Modifier.fillMaxWidth(pct / 100f).height(10.dp).background(th.acc)) }
                kit.items.forEachIndexed { i, t ->
                    Row(Modifier.fillMaxWidth().clickable { checked = if (i in checked) checked - i else checked + i; app.prefs.setKit(kitId, checked) }, verticalAlignment = Alignment.Top) {
                        Checkbox(checked = i in checked, onCheckedChange = { checked = if (it) checked + i else checked - i; app.prefs.setKit(kitId, checked) }, colors = CheckboxDefaults.colors(checkedColor = th.acc))
                        Text(t, color = th.ink, fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.padding(top = 12.dp))
                    }
                }
                Text("${kit.refTitle} ↗", color = th.acc, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { ctx.openUrl(kit.refUrl) }.padding(vertical = 6.dp))
                Btn("إعادة ضبط هذه القائمة", { checked = emptySet(); app.prefs.setKit(kitId, emptySet()) }, kind = "ghost")
            }
        }
        Banner("الأدوية الشخصية تُحفظ حسب تعليمات الطبيب والملصق. لا تضع أدوية حساسة للحرارة في السيارة، ولا تعطِ دواءً لشخص آخر.")
    }
}

@Composable
fun MoreScreen(app: AppState) {
    val th = LocalTh.current
    @Composable fun Item(t: String, d: String, dest: Dest) {
        ThCard(Modifier.fillMaxWidth().clickable { app.go(dest) }) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(t, color = th.ink, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(d, color = th.ink2, fontSize = 14.sp) }
                Text("←", color = th.ink2, fontSize = 20.sp)
            }
        }
    }
    Page {
        Text("المزيد", color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Item("أرقام الطوارئ", "997 و911 و998 و937 و993 و996", Dest.NumsD)
        Item("أسئلة وخرافات", "صحيح أم خرافة؟ ${Data.faq.size} سؤالاً", Dest.FaqD)
        Item("خطتي وإعداداتي", "من يستخدم التطبيق، حجم الخط، المظهر، بيانات العائلة", Dest.Plan)
        Item("المراجع", "المصادر التي بُنيت عليها الإرشادات", Dest.RefsD)
        Item("عن ثواني", "الخصوصية وتنبيه المحتوى", Dest.About)
    }
}

@Composable
fun NumsScreen(app: AppState) {
    val th = LocalTh.current
    val ctx = LocalContext.current
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) shareLoc(ctx) else ctx.toast("لم يُمنح إذن الموقع")
    }
    val colors = listOf(Color(0xFFC5483A), Color(0xFF8E1B2B), Color(0xFFBA7A20), Color(0xFF087E68), Color(0xFF527CB3), Color(0xFF527CB3))
    Page {
        Text("أرقام الطوارئ في المملكة", color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Data.nums.forEachIndexed { i, n ->
            ThCard(Modifier.fillMaxWidth().clickable { ctx.dial(n.n) }) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(n.n, color = colors[i % colors.size], fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width3())
                    Column { Text(n.title, color = th.ink, fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Text(n.desc, color = th.ink2, fontSize = 14.sp) }
                }
            }
        }
        Btn("شارك موقعي", {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) shareLoc(ctx) else perm.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }, modifier = Modifier.fillMaxWidth())
        Text("يفتح قائمة المشاركة (واتساب وغيره) برابط موقعك الأخير على الخريطة.", color = th.ink2, fontSize = 13.sp)
    }
}

private fun Modifier.width3() = this.then(Modifier.padding(end = 4.dp))

fun shareLoc(ctx: android.content.Context) {
    val l = ctx.lastLocation()
    if (l == null) { ctx.toast("تعذر تحديد الموقع. فعّل الموقع وجرّب مرة أخرى."); return }
    ctx.shareText("أحتاج مساعدة عاجلة. موقعي: https://maps.google.com/?q=${"%.6f".format(java.util.Locale.US, l.latitude)},${"%.6f".format(java.util.Locale.US, l.longitude)}")
}

@Composable
fun FaqScreen(app: AppState) {
    val th = LocalTh.current
    val ctx = LocalContext.current
    var open by remember { mutableStateOf(-1) }
    val v = mapOf("myth" to "خرافة", "true" to "صحيح", "depends" to "يعتمد")
    Page {
        Text("أسئلة وخرافات شائعة", color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Data.faq.forEachIndexed { i, f ->
            val tint = when (f.v) { "myth" -> th.redSoft; "true" -> th.soft2; else -> th.caut }
            ThCard(Modifier.fillMaxWidth().clickable { open = if (open == i) -1 else i }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.clip(RoundedCornerShape(99.dp)).background(tint).padding(horizontal = 10.dp, vertical = 2.dp)) { Text(v[f.v] ?: "", color = th.ink, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        Text(f.q, color = th.ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                        Text(if (open == i) "−" else "+", color = th.ink2, fontSize = 22.sp)
                    }
                    if (open == i) {
                        Text(f.a, color = th.ink2, fontSize = 15.sp, lineHeight = 23.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Btn("اقرأ البطاقة", { app.go(Dest.CardD(f.card)) })
                            Btn("مشاركة", { ctx.shareText("❓ ${f.q}\n${v[f.v]}: ${f.a}\n— ثواني") }, kind = "ghost")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanScreen(app: AppState) {
    val th = LocalTh.current
    Page {
        Text("خطتي وإعداداتي", color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        SectionTitle("من يستخدم ثواني؟")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("طفل أو مراهق", app.profile == "child") { app.applyProfile("child") }
            Chip("بالغ", app.profile == "adult") { app.applyProfile("adult") }
            Chip("كبير سن (عرض مبسّط وصوت)", app.profile == "senior") { app.applyProfile("senior") }
        }
        if (app.stars > 0) Text("نجومك من الاختبارات: ${app.stars} ★", color = th.ink2)
        SectionTitle("حجم الخط")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1f to "عادي", 1.12f to "كبير", 1.25f to "كبير جداً", 1.4f to "الأكبر").forEach { (v, l) -> Chip(l, app.scale == v) { app.updScale(v) } }
        }
        SectionTitle("المظهر")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("auto" to "تلقائي", "light" to "فاتح", "dark" to "داكن").forEach { (v, l) -> Chip(l, app.theme == v) { app.updTheme(v) } }
        }
        SectionTitle("القراءة الصوتية")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.8f to "أبطأ", 0.95f to "عادية", 1.1f to "أسرع").forEach { (v, l) -> Chip(l, app.rate == v) { app.updRate(v) } }
            Chip(if (app.autoread) "القراءة التلقائية: مفعّلة" else "القراءة التلقائية: متوقفة", app.autoread) { app.updAutoread(!app.autoread) }
        }
        SectionTitle("بيانات العائلة", "تظهر أسفل كل بطاقة وفي المشاركة، وتُحفظ على جهازك فقط")
        listOf("loc" to "الموقع / المنزل", "tel" to "رقم طوارئ العائلة البديل", "pt" to "نقطة التجمع الآمنة", "kit" to "موقع حقيبة الإسعافات والطفاية").forEach { (k, label) ->
            var v by remember(k) { mutableStateOf(app.prefs.familyRaw(k)) }
            OutlinedTextField(value = v, onValueChange = { v = it; app.prefs.setFamily(k, it) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
    }
}

@Composable
fun RefsScreen() {
    val th = LocalTh.current
    val ctx = LocalContext.current
    Page {
        Text("المراجع", color = th.ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("اقرأ التفاصيل من مصدرها. لكل بطاقة مصادرها الخاصة داخلها.", color = th.ink2, fontSize = 15.sp)
        Data.refs.forEach { g ->
            Text(g.title, color = th.ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            g.items.forEach { r ->
                ThCard(Modifier.fillMaxWidth().clickable { ctx.openUrl(r.url) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text("[${r.id.drop(1)}] ${r.title}", color = th.acc, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp)
                        Text(r.desc, color = th.ink2, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    val th = LocalTh.current
    Page {
        Text("ثواني", color = th.ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("دليل توعوي مبسّط للتصرف في مواقف الطوارئ في المملكة. يعرض خطوات عامة إلى حين وصول المساعدة، ولا يستبدل الاتصال بالطوارئ ولا التدريب المعتمد. يعمل بدون إنترنت، وبياناتك (خطة العائلة وقوائم التجهيز) تُحفظ على جهازك فقط.", color = th.ink, fontSize = 16.sp, lineHeight = 25.sp)
        Banner("للتعلم العملي راجع برامج التدريب الرسمية في الهلال الأحمر السعودي. ثواني مبادرة توعوية غير رسمية.")
        Text("الخصوصية: التطبيق لا يجمع أي بيانات ولا يرسل شيئاً إلى أي خادم. الموقع لا يُقرأ إلا عندما تضغط «شارك موقعي»، ويُرسل عبر قائمة المشاركة التي تختارها أنت.", color = th.ink2, fontSize = 15.sp, lineHeight = 23.sp)
        Text("المحتوى لم يراجعه مختص إسعاف بعد. راجعه مع جهة معتمدة قبل الاعتماد عليه.", color = th.ink2, fontSize = 14.sp)
        Text("الإصدار 1.0.0", color = th.ink2, fontSize = 13.sp)
    }
}
