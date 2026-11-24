package com.wijha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wijha.app.AppViewModel
import com.wijha.app.data.AIRLINES
import com.wijha.app.data.AIRPORT_BY_CODE
import com.wijha.app.data.MONTHS
import com.wijha.app.data.flagEmoji
import com.wijha.app.data.fmtHours
import com.wijha.app.data.km
import com.wijha.app.data.localDate
import com.wijha.app.data.num
import com.wijha.app.data.pastFlights
import com.wijha.app.data.stats
import com.wijha.app.ui.Acc
import com.wijha.app.ui.Bubbles
import com.wijha.app.ui.CodeStyle
import com.wijha.app.ui.EmptyState
import com.wijha.app.ui.Mute
import com.wijha.app.ui.Soft
import java.time.LocalDateTime

@Composable
fun StatsScreen(vm: AppViewModel, now: LocalDateTime, padding: PaddingValues) {
    val data by vm.state.collectAsState()
    var year by remember { mutableStateOf<Int?>(null) }
    var metricKm by remember { mutableStateOf(false) }
    val years = data.pastFlights(now).map { it.localDate().year }.distinct().sortedDescending()
    if (year != null && year !in years) year = null
    val s = data.stats(year, now)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 12.dp, bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("إحصاءات الطيران", style = MaterialTheme.typography.headlineMedium)
                Text("تُحسب من رحلاتك السابقة. المسافات بالخط المستقيم بين المطارات.", style = MaterialTheme.typography.bodyMedium, color = Mute)
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = year == null, onClick = { year = null }, label = { Text("الكل") }) }
                items(years) { y -> FilterChip(selected = year == y, onClick = { year = y }, label = { Text("$y") }) }
            }
        }
        if (s.flights.isEmpty()) {
            item { EmptyState("لا توجد رحلات سابقة", "بعد أن تهبط رحلتك الأولى تظهر الأرقام هنا.") }
            return@LazyColumn
        }
        val earth = s.km / 40075.0
        val moon = s.km / 384400.0 * 100
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(num(s.km), style = CodeStyle, fontSize = 64.sp, color = Acc)
                        Text(" كم", style = MaterialTheme.typography.titleMedium, color = Mute, modifier = Modifier.padding(bottom = 10.dp))
                    }
                    Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Soft)) {
                        Box(Modifier.fillMaxWidth((moon / 100).coerceIn(0.0, 1.0).toFloat()).fillMaxHeight().clip(CircleShape).background(Acc))
                    }
                    Text(
                        "تعادل ${String.format(java.util.Locale.US, "%.2f", earth)} مرة حول الأرض، و${String.format(java.util.Locale.US, "%.1f", moon)}٪ من الطريق إلى القمر.",
                        style = MaterialTheme.typography.bodyMedium, color = Mute,
                    )
                }
            }
        }
        item {
            val tiles = listOf(
                "${s.flights.size}" to "رحلة", fmtHours(s.hours) to "ساعة في الجو ≈", "${s.countries}" to "دولة",
                "${s.airports}" to "مطار", "${s.airlines}" to "شركة طيران", num(s.km / s.flights.size) to "كم متوسط الرحلة",
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tiles.chunked(2).forEachIndexed { r, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEachIndexed { c, (v, l) ->
                            val (bg, fg) = Bubbles[(r * 2 + c) % 3]
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(bg).padding(14.dp)) {
                                Text(v, style = CodeStyle, fontSize = 32.sp, color = fg)
                                Text(l, style = MaterialTheme.typography.bodyMedium, color = fg)
                            }
                        }
                    }
                }
            }
        }
        item {
            val items: List<Pair<String, Int>> = if (year == null) {
                s.flights.groupBy { it.localDate().year }.toSortedMap().map { (y, fs) -> "$y" to (if (metricKm) fs.sumOf { it.km() } else fs.size) }
            } else {
                (1..12).map { m -> MONTHS[m - 1].take(3) to s.flights.filter { it.localDate().monthValue == m }.sumOf { if (metricKm) it.km() else 1 } }
            }
            val max = (items.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (year == null) "حسب السنة" else "حسب الشهر", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !metricKm, onClick = { metricKm = false }, label = { Text("عدد الرحلات") })
                        FilterChip(selected = metricKm, onClick = { metricKm = true }, label = { Text("المسافة") })
                    }
                    Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        items.forEach { (label, v) ->
                            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                Text(if (metricKm && v >= 1000) "${v / 1000}k" else "$v", style = CodeStyle, fontSize = 12.sp)
                                Box(Modifier.fillMaxWidth().height((v.toFloat() / max * 90f).coerceAtLeast(3f).dp).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp)).background(Acc))
                                Text(label, style = MaterialTheme.typography.bodySmall, color = Mute, maxLines = 1, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        item { TopList("أكثر المسارات", s.routes.take(4).map { (r, n) -> val (a, b) = r.split("-"); "$a ⇄ $b" to "${AIRPORT_BY_CODE[a]?.city.orEmpty()} – ${AIRPORT_BY_CODE[b]?.city.orEmpty()}" to n }) }
        item { TopList("أكثر المطارات", s.airportsTop.take(4).map { (a, n) -> "${flagEmoji(AIRPORT_BY_CODE[a]?.cc.orEmpty())} $a" to AIRPORT_BY_CODE[a]?.city.orEmpty() to n }) }
        if (s.airlinesTop.isNotEmpty()) item { TopList("شركات الطيران", s.airlinesTop.take(4).map { (a, n) -> a to AIRLINES[a].orEmpty() to n }) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                s.longest?.let { (f, k) -> Mini(Modifier.weight(1f), "أطول رحلة", "${num(k)} كم", "${f.from}→${f.to} · ${f.no}") }
                s.shortest?.let { (f, k) -> Mini(Modifier.weight(1f), "أقصر رحلة", "${num(k)} كم", "${f.from}→${f.to} · ${f.no}") }
            }
        }
    }
}

@Composable
private fun Mini(modifier: Modifier, title: String, value: String, sub: String) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(14.dp)) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = Mute)
        Text(value, style = CodeStyle, fontSize = 28.sp, color = Acc)
        Text(sub, style = MaterialTheme.typography.bodySmall, color = Mute)
    }
}

@Composable
private fun TopList(title: String, rows: List<Pair<Pair<String, String>, Int>>) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            rows.forEach { (t, n) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(t.first, style = CodeStyle, fontSize = 19.sp)
                    Text(t.second, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Mute)
                    Text("$n×", style = CodeStyle, fontSize = 18.sp, color = Acc, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
