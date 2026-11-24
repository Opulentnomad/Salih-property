@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.wijha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wijha.app.AppViewModel
import com.wijha.app.data.Block
import com.wijha.app.data.MONTHS
import com.wijha.app.data.PhotoItem
import com.wijha.app.data.Post
import com.wijha.app.ui.EmptyState
import com.wijha.app.ui.Ink
import com.wijha.app.ui.Mute
import com.wijha.app.ui.Soft
import java.time.LocalDate

fun Post.photos(): List<PhotoItem> = blocks.filterIsInstance<Block.Photos>().flatMap { it.items }
fun Post.coverId(): String = cover.ifEmpty { photos().firstOrNull()?.id.orEmpty() }
fun Post.excerpt(): String = blocks.filterIsInstance<Block.Paragraph>().firstOrNull { it.text.isNotBlank() }?.text.orEmpty()

@Composable
fun JournalScreen(
    vm: AppViewModel,
    padding: PaddingValues,
    onOpenPost: (String) -> Unit,
    onOpenPhotos: (List<PhotoItem>, Int) -> Unit,
    onNew: () -> Unit,
) {
    val data by vm.state.collectAsState()
    var seg by rememberSaveable { mutableIntStateOf(0) }
    val posts = data.posts.sortedByDescending { it.date }
    val all = posts.flatMap { it.photos() }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 12.dp, bottom = padding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("مدونتي", style = MaterialTheme.typography.headlineMedium)
                Text("${posts.size} تدوينات · ${all.size} صورة", style = MaterialTheme.typography.bodyMedium, color = Mute)
            }
        }
        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("التدوينات", "الألبوم").forEachIndexed { i, t ->
                    SegmentedButton(selected = seg == i, onClick = { seg = i }, shape = SegmentedButtonDefaults.itemShape(i, 2)) { Text(t) }
                }
            }
        }
        if (seg == 0) {
            if (posts.isEmpty()) item { EmptyState("لا توجد تدوينات بعد", "اكتب عن رحلاتك: نصوص وأماكن زرتها وصور بقوالب جاهزة.", "تدوينة جديدة", onNew) }
            items(posts, key = { it.id }) { p -> PostCard(vm, p) { onOpenPost(p.id) } }
        } else {
            if (all.isEmpty()) item { EmptyState("الألبوم فارغ", "الصور التي تضيفها في تدويناتك تظهر هنا.") }
            items(all.chunked(2)) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { it ->
                        val idx = all.indexOf(it)
                        Box(
                            Modifier.weight(1f).aspectRatio(if (idx % 3 == 0) 0.8f else 1.1f).clip(RoundedCornerShape(18.dp))
                                .background(Soft).clickable { onOpenPhotos(all, idx) },
                        ) {
                            AsyncImage(vm.photoFile(it.id), it.caption.ifBlank { null }, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PostCard(vm: AppViewModel, p: Post, onClick: () -> Unit) {
    val d = runCatching { LocalDate.parse(p.date) }.getOrNull()
    Card(
        onClick = onClick, shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.height(116.dp)) {
            Box(Modifier.width(112.dp).fillMaxHeight().background(Soft)) {
                val c = p.coverId()
                if (c.isNotEmpty()) AsyncImage(vm.photoFile(c), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(p.title.ifBlank { "بدون عنوان" }, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(p.excerpt(), style = MaterialTheme.typography.bodySmall, color = Mute, maxLines = 2)
                Text(
                    listOfNotNull(
                        d?.let { "${it.dayOfMonth} ${MONTHS[it.monthValue - 1]} ${it.year}" }, "${p.photos().size} صورة", p.trip.ifBlank { null },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = Mute, maxLines = 1,
                )
            }
        }
    }
}

/** ورقة اختيار الرحلة وقالب البداية لتدوينة جديدة. */
@Composable
fun NewPostSheet(trips: List<String>, initialTrip: String, onStart: (String, String) -> Unit, onDismiss: () -> Unit) {
    var trip by remember { mutableStateOf(initialTrip) }
    var starter by remember { mutableStateOf("blank") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color.White) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("تدوينة جديدة", style = MaterialTheme.typography.headlineSmall)
            Text("عن أي رحلة؟", style = MaterialTheme.typography.labelLarge, color = Mute)
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = trip.isEmpty(), onClick = { trip = "" }, label = { Text("بدون رحلة") })
                trips.forEach { t -> FilterChip(selected = trip == t, onClick = { trip = t }, label = { Text(t) }) }
            }
            Text("ابدأ بقالب", style = MaterialTheme.typography.labelLarge, color = Mute)
            listOf(
                Triple("blank", "فارغة", "ابدأ بفقرة واحدة"), Triple("day", "يوميات يوم", "صباح وبعد الظهر وصور ومكان ونصيحة"),
                Triple("guide", "دليل مكان", "أماكن وصورة ونصيحة"), Triple("album", "ألبوم صور", "كولاج وبولارويد واقتباس"),
            ).chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (k, t, d) ->
                        Card(
                            onClick = { starter = k }, modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (starter == k) Ink else Soft),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(t, style = MaterialTheme.typography.titleMedium, color = if (starter == k) Color.White else Ink)
                                Text(d, style = MaterialTheme.typography.bodySmall, color = if (starter == k) Color.White.copy(alpha = 0.8f) else Mute)
                            }
                        }
                    }
                }
            }
            Button(onClick = { onStart(trip, starter) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Ink)) {
                Text("ابدأ الكتابة")
            }
        }
    }
}
