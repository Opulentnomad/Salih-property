package app.thawani

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.chrono.HijrahChronology
import java.time.temporal.ChronoField

private fun hijriMonth(): Int = try { HijrahChronology.INSTANCE.dateNow().get(ChronoField.MONTH_OF_YEAR) } catch (_: Exception) { 0 }

private val catColors = mapOf(
    "first_aid" to Color(0xFFC5483A), "kids_elderly" to Color(0xFFC87B35), "home_safety" to Color(0xFF53866A),
    "vehicle_road" to Color(0xFF527CB3), "weather_outdoor" to Color(0xFF43849A)
)
private val catDesc = mapOf(
    "first_aid" to "نزيف، غصة، توقف قلب", "kids_elderly" to "غصة رضيع، تسمم، سقوط", "home_safety" to "غاز، حريق، كهرباء، مصعد",
    "vehicle_road" to "تعطل، إطار، حوادث، حريق", "weather_outdoor" to "ضربة شمس، لدغات، سيول"
)

@Composable
fun CardRow(c: Card, onClick: () -> Unit) {
    val th = LocalTh.current
    ThCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CardIcon(c)
            Column(Modifier.weight(1f)) {
                Text(Data.cats[c.cat] ?: "", color = th.ink2, fontSize = 12.sp)
                Text(c.title, color = th.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
                Text(c.level, color = th.ink2, fontSize = 13.sp, lineHeight = 18.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(99.dp)).background(lvColor(c.lv, th.dark).copy(alpha = .18f)).padding(horizontal = 10.dp, vertical = 2.dp)) {
                Text(c.phone, color = lvColor(c.lv, th.dark), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(app: AppState, onOpen: (String) -> Unit) {
    val th = LocalTh.current
    val ctx = LocalContext.current
    val m = hijriMonth()
    val season = when { m == 9 -> Data.seasons.first { it.id == "ramadan" }; m == 11 || m == 12 -> Data.seasons.first { it.id == "hajj" }; else -> null }
    val tiles = Data.quick.take(if (app.simple) 6 else 8).mapNotNull { Data.card(it) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Column(Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                Text("دليل إسعافات أولية مبسّط", color = th.acc, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Text(buildAnnotatedString {
                    append("في لحظة طارئة،\n")
                    withStyle(SpanStyle(color = th.acc)) { append("كل ثانية") }
                    append(" تصنع فرقاً.")
                }, color = th.ink, fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 46.sp)
                Spacer(Modifier.height(8.dp))
                Text("خطوات واضحة تساعدك على التصرف بهدوء إلى أن تصل المساعدة. اختر الحالة التي أمامك.", color = th.ink2, fontSize = 17.sp, lineHeight = 26.sp)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Btn("اعثر على الحالة ←", { app.go(Dest.Library) })
                    Row(Modifier.clickable { ctx.dial("997") }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(th.redSoft), contentAlignment = Alignment.Center) { Text("↗", color = th.redText, fontSize = 20.sp) }
                        Column { Text("اتصال بالإسعاف", color = th.ink, fontWeight = FontWeight.SemiBold); Text("الهلال الأحمر · 997", color = th.ink2, fontSize = 13.sp) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("✳ إذا كان هناك خطر مباشر، ابتعد إلى مكان آمن واتصل بالطوارئ.", color = th.ink2, fontSize = 13.sp)
            }
        }
        if (season != null && !app.simple) item {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(th.caut).border(1.dp, th.cautLine, RoundedCornerShape(16.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) { Text("موسم ${season.name}", color = th.ink, fontWeight = FontWeight.Bold); Text(season.d, color = th.ink2, fontSize = 14.sp) }
                Btn("عرض بطاقات الموسم", { app.season = season.id; app.cat = "all"; app.place = "all"; app.query = ""; app.go(Dest.Library) })
            }
        }
        item {
            SectionTitle("ماذا يحدث الآن؟", "ابدأ من هنا")
            ThCard(Modifier.fillMaxWidth().clickable { app.focusSearch = true; app.go(Dest.Library) }) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⌕", color = th.ink2, fontSize = 20.sp); Spacer(Modifier.size(10.dp))
                    Text("ابحث عن حالة، مثل: نزيف أو حريق", color = th.ink2, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text("🎙", fontSize = 20.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        items(tiles.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { c ->
                    ThCard(Modifier.weight(1f).clickable { onOpen(c.id) }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CardIcon(c)
                            Text(Data.quickLabel[c.id] ?: c.title, color = th.ink, fontSize = if (app.simple) 20.sp else 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp)
                            Text(c.level, color = th.ink2, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2)
                            if (c.lv == "c" || c.lv == "d") Text("حالة عاجلة", color = th.redText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (!app.simple) {
            item {
                SectionTitle("أين أنت؟")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Data.places.forEach { pl -> Chip(pl, false) { app.place = pl; app.cat = "all"; app.season = ""; app.query = ""; app.go(Dest.Library) } }
                }
            }
            item { SectionTitle("التصنيفات") }
            items(Data.cats.keys.toList()) { k ->
                val col = catColors[k] ?: th.acc
                val n = Data.cards.count { it.cat == k }
                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(18.dp)).background(col)
                    .clickable { app.cat = k; app.place = "all"; app.season = ""; app.query = ""; app.go(Dest.Library) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(Data.cats[k] ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(catDesc[k] ?: "", color = Color.White.copy(alpha = .9f), fontSize = 14.sp)
                    }
                    Box(Modifier.clip(RoundedCornerShape(99.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 2.dp)) {
                        Text("$n بطاقة", color = Color(0xFF0F2A22), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            Text("البطاقات للتوعية ولا تغني عن الاتصال بالإسعاف أو دورة إسعافات معتمدة.", color = th.ink2, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(app: AppState, onOpen: (String) -> Unit, onVoice: () -> Unit) {
    val th = LocalTh.current
    val list = app.filtered()
    val focus = remember { FocusRequester() }
    LaunchedEffect(app.focusSearch) { if (app.focusSearch) { app.focusSearch = false; runCatching { focus.requestFocus() } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            TextField(
                value = app.query, onValueChange = { app.query = it; if (it.isNotEmpty()) { app.cat = "all"; app.place = "all"; app.season = "" } },
                modifier = Modifier.fillMaxWidth().focusRequester(focus), singleLine = true, shape = RoundedCornerShape(16.dp),
                placeholder = { Text("ابحث: غصة، لدغة، حريق…") },
                leadingIcon = { Text("⌕", fontSize = 20.sp) },
                trailingIcon = { Text("🎙", fontSize = 20.sp, modifier = Modifier.clickable { onVoice() }.padding(12.dp)) },
                colors = TextFieldDefaults.colors(focusedContainerColor = th.card, unfocusedContainerColor = th.card, focusedIndicatorColor = th.acc, unfocusedIndicatorColor = th.line,
                    focusedTextColor = th.ink, unfocusedTextColor = th.ink)
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Data.seasons.forEach { s -> Chip(s.name, app.season == s.id) { app.season = if (app.season == s.id) "" else s.id; app.cat = "all"; app.place = "all"; app.query = "" } }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("الكل (${Data.cards.size})", app.cat == "all") { app.cat = "all" }
                Data.cats.forEach { (k, v) -> Chip("$v (${Data.cards.count { it.cat == k }})", app.cat == k) { app.cat = k } }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("كل الأماكن", app.place == "all") { app.place = "all" }
                Data.places.forEach { p -> Chip(p, app.place == p) { app.place = p } }
            }
        }
        item { Text("${list.size} بطاقة", color = th.ink2, fontWeight = FontWeight.SemiBold) }
        if (app.profile == "child" && !app.kidsAll) item {
            Banner("تعرض لك بطاقات مناسبة لعمرك. اطلب من شخص كبير مساعدتك في الباقي.")
            Spacer(Modifier.height(8.dp)); Btn("عرض كل البطاقات", { app.kidsAll = true }, kind = "ghost")
        }
        if (list.isEmpty()) item { ThCard(Modifier.fillMaxWidth()) { Text("لا توجد حالة بهذا الاسم. جرّب كلمة أخرى.", color = th.ink2, modifier = Modifier.padding(24.dp)) } }
        items(list, key = { it.id }) { c -> CardRow(c) { onOpen(c.id) } }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
