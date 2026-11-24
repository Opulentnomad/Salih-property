@file:OptIn(ExperimentalMaterial3Api::class)

package com.wijha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wijha.app.AppViewModel
import com.wijha.app.data.AIRPORT_BY_CODE
import com.wijha.app.data.AppData
import com.wijha.app.data.Flight
import com.wijha.app.data.FlightStatus
import com.wijha.app.data.airlineName
import com.wijha.app.data.arrival
import com.wijha.app.data.countdownText
import com.wijha.app.data.depDateTime
import com.wijha.app.data.depText
import com.wijha.app.data.durationHours
import com.wijha.app.data.flagEmoji
import com.wijha.app.data.fmtHours
import com.wijha.app.data.homeCc
import com.wijha.app.data.isPast
import com.wijha.app.data.km
import com.wijha.app.data.label
import com.wijha.app.data.localDate
import com.wijha.app.data.num
import com.wijha.app.data.phase
import com.wijha.app.data.sortedFlights
import com.wijha.app.data.wikiTitle
import com.wijha.app.ui.Bg
import com.wijha.app.ui.CodeStyle
import com.wijha.app.ui.CodeTiles
import com.wijha.app.ui.DateBubble
import com.wijha.app.ui.EmptyState
import com.wijha.app.ui.GlassChip
import com.wijha.app.ui.Ink
import com.wijha.app.ui.Mute
import com.wijha.app.ui.StatusChip
import com.wijha.app.ui.WikiImage
import com.wijha.app.ui.WarnFg
import com.wijha.app.ui.gradientFor
import java.time.LocalDateTime

data class TripGroup(val name: String, val flights: List<Flight>)

fun groupTrips(list: List<Flight>, newestFirst: Boolean): List<TripGroup> {
    val groups = list.groupBy { it.trip }.map { (n, fs) -> TripGroup(n, fs.sortedBy { it.depDateTime() }) }
    return if (newestFirst) groups.sortedByDescending { it.flights.first().depDateTime() }
    else groups.sortedBy { it.flights.first().depDateTime() }
}

private var heroAnimated = false

@Composable
fun FlightsScreen(
    vm: AppViewModel,
    now: LocalDateTime,
    padding: PaddingValues,
    onOpenTrip: (String) -> Unit,
    onEdit: (Flight) -> Unit,
    onAdd: () -> Unit,
    onNewPost: (String) -> Unit,
) {
    val data by vm.state.collectAsState()
    var seg by rememberSaveable { mutableIntStateOf(0) }
    val all = data.sortedFlights()
    val up = all.filter { !it.isPast(now) }
    val past = all.filter { it.isPast(now) }
    val next = up.firstOrNull { it.status != FlightStatus.CANCELLED } ?: up.firstOrNull()
    val animate = remember { !heroAnimated }.also { heroAnimated = true }
    val groups = groupTrips(if (seg == 0) up else past, newestFirst = seg == 1)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 12.dp, bottom = padding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(if (data.name.isBlank()) "رحلاتي" else "رحلاتي يا ${data.name}", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${up.size} قادمة · ${past.size} سابقة", style = MaterialTheme.typography.bodyMedium, color = Mute,
                )
            }
        }
        if (data.sampleFlights) item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("بيانات تجريبية. عدّلها أو احذفها.", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { vm.clearFlights() }) { Text("ابدأ من الصفر") }
                }
            }
        }
        item {
            if (next != null) NextFlight(next, data, now, animate, onEdit)
            else EmptyState("لا توجد رحلة قادمة", "أضف رقم رحلتك وتاريخها لتتابعها هنا، أو اختر وجهة من الاقتراحات.", "أضف رحلة", onAdd)
        }
        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("قادمة (${up.size})", "سابقة (${past.size})").forEachIndexed { i, t ->
                    SegmentedButton(
                        selected = seg == i, onClick = { seg = i },
                        shape = SegmentedButtonDefaults.itemShape(i, 2),
                    ) { Text(t) }
                }
            }
        }
        if (groups.isEmpty()) item { Text("لا توجد رحلات هنا بعد.", color = Mute, style = MaterialTheme.typography.bodyMedium) }
        items(groups, key = { it.name + seg }) { g ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TripHeader(g, data, onOpenTrip, onNewPost)
                g.flights.forEach { FlightRow(it, now, onEdit) }
            }
        }
    }
}

@Composable
private fun TripHeader(g: TripGroup, data: AppData, onOpen: (String) -> Unit, onNewPost: (String) -> Unit) {
    val flags = g.flights.mapNotNull { AIRPORT_BY_CODE[it.to]?.cc }.distinct().filter { it != data.homeCc() }
    val km = g.flights.sumOf { it.km() }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onOpen(g.name) }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(flags.joinToString("") { flagEmoji(it) }, fontSize = 18.sp)
            Column {
                Text(g.name, style = MaterialTheme.typography.titleMedium)
                Text("${g.flights.size} رحلات · ${num(km)} كم", style = MaterialTheme.typography.bodySmall, color = Mute)
            }
            Icon(Icons.Filled.KeyboardArrowLeft, null, tint = Mute)
        }
        OutlinedButton(onClick = { onNewPost(g.name) }, contentPadding = PaddingValues(horizontal = 12.dp)) {
            Icon(Icons.Filled.Edit, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("تدوينة")
        }
    }
}

@Composable
fun FlightRow(f: Flight, now: LocalDateTime, onClick: (Flight) -> Unit) {
    val ph = f.phase(now)
    Card(
        onClick = { onClick(f) }, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateBubble(f.localDate())
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(f.from, style = CodeStyle, fontSize = 22.sp)
                    Icon(Icons.Filled.Flight, null, Modifier.size(16.dp).rotate(90f), tint = Mute)
                    Text(f.to, style = CodeStyle, fontSize = 22.sp)
                }
                Text(
                    "${f.no}${airlineName(f.no).let { if (it.isEmpty()) "" else " · $it" }} · ${num(f.km())} كم · ${fmtHours(f.durationHours())}",
                    style = MaterialTheme.typography.bodySmall, color = Mute,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(ph, ph.label(f))
                Text(f.depText(), style = MaterialTheme.typography.bodySmall, color = Mute)
            }
        }
    }
}

@Composable
private fun NextFlight(f: Flight, data: AppData, now: LocalDateTime, animate: Boolean, onEdit: (Flight) -> Unit) {
    val a = AIRPORT_BY_CODE[f.from]
    val b = AIRPORT_BY_CODE[f.to]
    val dest = if (b != null && b.cc != data.homeCc()) b else a ?: b
    val ph = f.phase(now)
    val cd = countdownText(f, now)
    Column {
        Box(Modifier.fillMaxWidth().height(236.dp).clip(RoundedCornerShape(30.dp))) {
            WikiImage(wikiTitle(dest?.code ?: f.to), Modifier.fillMaxSize(), gradientFor(dest?.cc ?: ""))
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Transparent, 0.45f to Color.Transparent, 1f to Color(0xD0081028)),
                ),
            )
            Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                if (cd.isNotEmpty()) GlassChip("بعد $cd") else Spacer(Modifier.width(1.dp))
                StatusChip(ph, ph.label(f))
            }
            Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CodeTiles(f.from, 44, animate, tile = Color.White.copy(alpha = 0.18f))
                    Icon(Icons.Filled.Flight, null, Modifier.size(26.dp).rotate(90f), tint = Color.White)
                    CodeTiles(f.to, 44, animate, tile = Color.White.copy(alpha = 0.18f))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${a?.city ?: f.from} ← ${b?.city ?: f.to} · ${f.no}${airlineName(f.no).let { if (it.isEmpty()) "" else " · $it" }}",
                    color = Color.White, style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val ar = f.arrival()
                val dt = f.localDate()
                Row(Modifier.fillMaxWidth()) {
                    Info("التاريخ", "${dt.dayOfMonth} ${com.wijha.app.data.MONTHS[dt.monthValue - 1]}", Modifier.weight(1f))
                    Info("الإقلاع", f.depText(), Modifier.weight(1f), code = true)
                    Info("الوصول ≈", ar.first + if (ar.second != 0) " +${ar.second}" else "", Modifier.weight(1f), code = true)
                }
                Row(Modifier.fillMaxWidth()) {
                    Info("المدة ≈", fmtHours(f.durationHours()), Modifier.weight(1f), code = true)
                    Info("المسافة", "${num(f.km())} كم", Modifier.weight(1f))
                    Info("المقعد · البوابة", "${f.seat.ifBlank { "—" }} · ${f.gate.ifBlank { "—" }}", Modifier.weight(1f), code = true)
                }
                if (f.status == FlightStatus.DELAYED && f.delay > 0) {
                    Text("متأخرة ${f.delay} دقيقة عن ${f.time}", color = WarnFg, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { onEdit(f) }, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink),
                ) { Text("تحديث الحالة والتفاصيل") }
            }
        }
    }
}

@Composable
private fun Info(label: String, value: String, modifier: Modifier = Modifier, code: Boolean = false) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Mute)
        if (code) Text(value, style = CodeStyle, fontSize = 20.sp)
        else Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

