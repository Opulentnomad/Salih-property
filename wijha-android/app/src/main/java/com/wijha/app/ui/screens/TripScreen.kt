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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wijha.app.AppViewModel
import com.wijha.app.data.AIRPORT_BY_CODE
import com.wijha.app.data.MONTHS
import com.wijha.app.data.PhotoItem
import com.wijha.app.data.airlineName
import com.wijha.app.data.Block
import com.wijha.app.data.dateRangeText
import com.wijha.app.data.depDateTime
import com.wijha.app.data.depText
import com.wijha.app.data.endDateTime
import com.wijha.app.data.flagEmoji
import com.wijha.app.data.homeCc
import com.wijha.app.data.km
import com.wijha.app.data.label
import com.wijha.app.data.localDate
import com.wijha.app.data.num
import com.wijha.app.data.phase
import com.wijha.app.data.wikiTitle
import com.wijha.app.ui.Bg
import com.wijha.app.ui.CodeStyle
import com.wijha.app.ui.DateBubble
import com.wijha.app.ui.EmptyState
import com.wijha.app.ui.GlassChip
import com.wijha.app.ui.Ink
import com.wijha.app.ui.Mute
import com.wijha.app.ui.StatusChip
import com.wijha.app.ui.WikiImage
import com.wijha.app.ui.gradientFor
import com.wijha.app.ui.openMaps
import com.wijha.app.ui.rememberNow
import com.wijha.app.ui.shareText
import java.time.LocalDateTime

/** تفاصيل الرحلة: غلاف صورة وتبويبات المسار والمدونة والصور والخريطة. */
@Composable
fun TripScreen(
    vm: AppViewModel,
    tripName: String,
    onBack: () -> Unit,
    onEditFlight: (com.wijha.app.data.Flight) -> Unit,
    onOpenPost: (String) -> Unit,
    onNewPost: (String) -> Unit,
    onOpenPhotos: (List<PhotoItem>, Int) -> Unit,
) {
    val data by vm.state.collectAsState()
    val now by rememberNow()
    val ctx = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val fs = data.flights.filter { it.trip == tripName }.sortedBy { it.depDateTime() }
    if (fs.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val home = data.homeCc()
    val dest = fs.map { it.to }.firstOrNull { AIRPORT_BY_CODE[it]?.cc != home } ?: fs.first().to
    val first = fs.first().depDateTime()
    val last = fs.maxOf { it.endDateTime() }
    val status = when {
        now.isBefore(first) -> "قادمة" to Color(0xFF4FBF6B)
        now.isBefore(last) -> "جارية" to Color(0xFFF2A23A)
        else -> "منتهية" to Color(0xFF9AA6BD)
    }
    val ccs = fs.mapNotNull { AIRPORT_BY_CODE[it.to]?.cc }.distinct().filter { it != home }
    val posts = data.posts.filter { it.trip == tripName }.sortedByDescending { it.date }
    val photos = posts.flatMap { p -> p.blocks.filterIsInstance<Block.Photos>().flatMap { it.items } }

    Scaffold(
        containerColor = Bg, contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onNewPost(tripName) }, containerColor = Ink, contentColor = Color.White) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("إضافة ذكرى")
            }
        },
    ) { pad ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = pad.calculateBottomPadding() + 96.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(290.dp)) {
                        WikiImage(wikiTitle(dest), Modifier.fillMaxSize(), gradientFor(AIRPORT_BY_CODE[dest]?.cc ?: ""))
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color(0x55081028), 0.4f to Color.Transparent, 1f to Color(0xCC081028))))
                        Column(Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 18.dp, bottom = 46.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            GlassChip(status.first, dot = status.second)
                            Text(tripName, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(ccs.joinToString("") { flagEmoji(it) }, fontSize = 16.sp)
                                Text(
                                    "${dateRangeText(first.toLocalDate(), last.toLocalDate())} · ${fs.size} رحلات · ${num(fs.sumOf { it.km() })} كم",
                                    color = Color(0xFFE8EEFB), style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                item {
                    Column(
                        Modifier.padding(top = 0.dp).clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)),
                    ) {
                        TabRow(selectedTabIndex = tab, containerColor = Bg, contentColor = Ink) {
                            listOf("المسار" to Icons.Filled.CalendarMonth, "المدونة" to Icons.Filled.AutoStories, "الصور" to Icons.Filled.PhotoLibrary, "الخريطة" to Icons.Filled.Map)
                                .forEachIndexed { i, (t, ic) ->
                                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }, icon = { Icon(ic, null) })
                                }
                        }
                    }
                }
                when (tab) {
                    0 -> items(fs.size) { i ->
                        val f = fs[i]
                        val ph = f.phase(now)
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DateBubble(f.localDate(), modifier = Modifier.padding(top = 4.dp))
                            Card(
                                onClick = { onEditFlight(f) }, Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp),
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(f.from, style = CodeStyle, fontSize = 22.sp)
                                        Icon(Icons.Filled.Flight, null, Modifier.size(16.dp).rotate(90f), tint = Mute)
                                        Text(f.to, style = CodeStyle, fontSize = 22.sp)
                                    }
                                    Text(
                                        "${f.no}${airlineName(f.no).let { if (it.isEmpty()) "" else " · $it" }} · ${f.depText()} · ${num(f.km())} كم",
                                        style = MaterialTheme.typography.bodySmall, color = Mute,
                                    )
                                    StatusChip(ph, ph.label(f))
                                }
                            }
                        }
                    }
                    1 -> {
                        if (posts.isEmpty()) item { Box(Modifier.padding(16.dp)) { EmptyState("لا توجد تدوينات لهذه الرحلة", "اكتب عن الأماكن والأكل والصور.", "تدوينة جديدة") { onNewPost(tripName) } } }
                        items(posts.size) { i -> Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) { PostCard(vm, posts[i]) { onOpenPost(posts[i].id) } } }
                    }
                    2 -> {
                        if (photos.isEmpty()) item { Box(Modifier.padding(16.dp)) { EmptyState("لا توجد صور بعد", "الصور التي تضيفها في تدويناتك لهذه الرحلة تظهر هنا.") } }
                        items(photos.chunked(3).size) { r ->
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                photos.chunked(3)[r].forEachIndexed { c, it ->
                                    Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(Bg).clickable { onOpenPhotos(photos, r * 3 + c) }) {
                                        AsyncImage(vm.photoFile(it.id), it.caption.ifBlank { null }, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    }
                                }
                                repeat(3 - photos.chunked(3)[r].size) { Box(Modifier.weight(1f)) }
                            }
                        }
                    }
                    else -> {
                        val cities = fs.map { it.to }.distinct()
                        val places = posts.flatMap { p -> p.blocks.filterIsInstance<Block.Place>().filter { it.name.isNotBlank() } }
                        items(cities.size) { i ->
                            val a = AIRPORT_BY_CODE[cities[i]]!!
                            PlaceRow(flagEmoji(a.cc), "${a.city} (${a.code})", a.name) { openMaps(ctx, "${a.name} ${a.city}") }
                        }
                        items(places.size) { i ->
                            val p = places[i]
                            PlaceRow("📍", p.name, p.kind) { openMaps(ctx, "${p.name} ${AIRPORT_BY_CODE[dest]?.city.orEmpty()}") }
                        }
                        if (places.isEmpty()) item { Text("أماكن بطاقات المكان في تدويناتك تظهر هنا بروابط الخرائط.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = Mute) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                RoundIcon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", onBack)
                RoundIcon(Icons.Filled.Share, "مشاركة") {
                    shareText(ctx, "رحلتي: $tripName — ${fs.size} رحلات · ${num(fs.sumOf { it.km() })} كم. خُطّطت في وجهة")
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(emoji: String, title: String, sub: String, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(emoji, fontSize = 20.sp)
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = Mute)
        }
    }
}
