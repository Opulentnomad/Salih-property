@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.wijha.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wijha.app.AppViewModel
import com.wijha.app.data.AIRPORT_BY_CODE
import com.wijha.app.data.CONTINENT
import com.wijha.app.data.COUNTRY_NAMES
import com.wijha.app.data.flagEmoji
import com.wijha.app.data.levelOf
import com.wijha.app.data.pastFlights
import com.wijha.app.data.plannedCountries
import com.wijha.app.data.stamps
import com.wijha.app.data.visitedCountries
import com.wijha.app.ui.Acc
import com.wijha.app.ui.Bubbles
import com.wijha.app.ui.CodeStyle
import com.wijha.app.ui.GhostStamp
import com.wijha.app.ui.Ink
import com.wijha.app.ui.Line
import com.wijha.app.ui.Mute
import com.wijha.app.ui.Soft
import com.wijha.app.ui.StampView
import java.time.LocalDateTime

private val SLOTS = listOf(Triple(200, 4, 108), Triple(30, 92, 104), Triple(170, 186, 128), Triple(56, 286, 100))

@Composable
fun PassportScreen(vm: AppViewModel, now: LocalDateTime, padding: PaddingValues) {
    val data by vm.state.collectAsState()
    val stamps = data.stamps(now)
    val visited = data.visitedCountries(now)
    val planned = data.plannedCountries(now)
    val (lvl, lvlName) = levelOf(visited.size)
    val conts = visited.mapNotNull { CONTINENT[it] }.distinct().size
    var picking by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf("") }
    val pages = maxOf(1, (stamps.size + 3) / 4)
    val pager = rememberPagerState(pageCount = { pages })

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 12.dp, bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("جوازي", style = MaterialTheme.typography.headlineMedium)
                Text("تُضاف الأختام تلقائياً عند هبوط كل رحلة إلى دولة جديدة غير بلد إقامتك.", style = MaterialTheme.typography.bodyMedium, color = Mute)
            }
        }
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.fillMaxWidth().background(Ink).padding(horizontal = 18.dp, vertical = 10.dp)) {
                        Text("وجهة · جواز رحّالة", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            Modifier.size(80.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Acc, Ink))),
                            contentAlignment = Alignment.Center,
                        ) { Text(data.name.trim().take(1).ifEmpty { "؟" }, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold) }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = data.name, onValueChange = { vm.setName(it.take(24)) }, singleLine = true,
                                placeholder = { Text("اكتب اسمك") }, textStyle = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(),
                            )
                            Text("المستوى $lvl · $lvlName", color = Acc, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clip(CircleShape).background(Soft).padding(horizontal = 12.dp, vertical = 2.dp))
                        }
                    }
                    OutlinedButton(onClick = { picking = true }, modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        val a = AIRPORT_BY_CODE[data.home]
                        Text("مطار الإقامة: ${a?.let { flagEmoji(it.cc) + " " + it.city + " (" + it.code + ")" } ?: data.home}", Modifier.weight(1f))
                    }
                    Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("${visited.size}" to "دول", "${data.pastFlights(now).size}" to "رحلات", "$conts" to "قارات").forEachIndexed { i, (v, l) ->
                            val (bg, fg) = Bubbles[i]
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(bg).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(v, style = CodeStyle, fontSize = 30.sp, color = fg)
                                Text(l, style = MaterialTheme.typography.bodySmall, color = fg)
                            }
                        }
                    }
                    if (visited.isEmpty() && planned.isEmpty()) {
                        Text("ستظهر هنا أعلام الدول بعد أول رحلة دولية.", Modifier.padding(horizontal = 18.dp), color = Mute, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        FlowRow(Modifier.padding(horizontal = 18.dp).padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            visited.forEach { cc ->
                                val n = stamps.count { it.cc == cc }
                                FlagTile(cc, if (n > 1) "$n ختمان" else "ختم", 1f)
                            }
                            planned.forEach { cc -> FlagTile(cc, "قيد التخطيط", 0.5f) }
                        }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("صفحات الأختام", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("${stamps.size} أختام", style = MaterialTheme.typography.bodySmall, color = Mute)
            }
        }
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                Column {
                    HorizontalPager(pager, Modifier.fillMaxWidth().height(400.dp)) { page ->
                        Box(Modifier.fillMaxSize()) {
                            if (stamps.isEmpty()) Text(
                                "لا أختام بعد. تُضاف عند هبوط رحلتك إلى دولة جديدة.", Modifier.align(Alignment.Center).padding(24.dp),
                                color = Mute, textAlign = TextAlign.Center,
                            )
                            val slice = stamps.drop(page * 4).take(4)
                            slice.forEachIndexed { i, s ->
                                val (x, y, w) = SLOTS[i]
                                StampView(s, w.dp, Modifier.offset(x.dp, (y + 14).dp))
                            }
                            if (slice.size < 4 && page == pages - 1 && stamps.isNotEmpty()) {
                                val (x, y, w) = SLOTS[slice.size]
                                GhostStamp(w.dp, "ختمك القادم", Modifier.offset(x.dp, (y + 14).dp))
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.Center) {
                        repeat(pages) { i ->
                            Box(Modifier.padding(3.dp).size(8.dp).clip(CircleShape).background(if (pager.currentPage == i) Ink else Line))
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("عن تتبّع الرحلات", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "تُدخل حالة الرحلة والبوابة والمقعد يدوياً، وتصلك تنبيهات قبل الإقلاع بثلاث ساعات. التتبع الحي من المطارات يحتاج ربط التطبيق بخدمة بيانات رحلات. الأوقات والمسافات تقديرية.",
                        style = MaterialTheme.typography.bodyMedium, color = Mute,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { if (confirm == "clear") { vm.clearFlights(); confirm = "" } else confirm = "clear" }) {
                            Text(if (confirm == "clear") "اضغط للتأكيد" else "حذف كل الرحلات")
                        }
                        OutlinedButton(onClick = { if (confirm == "demo") { vm.restoreSampleFlights(); confirm = "" } else confirm = "demo" }) {
                            Text(if (confirm == "demo") "اضغط للتأكيد" else "استعادة التجريبية")
                        }
                    }
                }
            }
        }
    }
    if (picking) AirportPickerDialog("مطار الإقامة", { vm.setHome(it.code); picking = false }, { picking = false })
}

@Composable
private fun FlagTile(cc: String, sub: String, alpha: Float) {
    Column(
        Modifier.size(width = 96.dp, height = 84.dp).clip(RoundedCornerShape(16.dp)).background(com.wijha.app.ui.Bg),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Text(flagEmoji(cc), fontSize = 30.sp, modifier = Modifier.alpha(alpha))
        Text(COUNTRY_NAMES[cc] ?: cc, style = MaterialTheme.typography.labelLarge, color = Ink.copy(alpha = alpha))
        Text(sub, style = MaterialTheme.typography.bodySmall, color = Mute)
    }
}
