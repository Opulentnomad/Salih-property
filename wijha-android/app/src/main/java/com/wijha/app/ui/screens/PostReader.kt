@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.wijha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wijha.app.AppViewModel
import com.wijha.app.data.Block
import com.wijha.app.data.MONTHS
import com.wijha.app.data.PhotoItem
import com.wijha.app.data.airlineName
import com.wijha.app.data.depDateTime
import com.wijha.app.data.localDate
import com.wijha.app.data.num
import com.wijha.app.data.km
import com.wijha.app.ui.Acc
import com.wijha.app.ui.Ink
import com.wijha.app.ui.Mute
import com.wijha.app.ui.Soft
import com.wijha.app.ui.shareText
import java.time.LocalDate

@Composable
fun PostReaderScreen(
    vm: AppViewModel,
    postId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenPhotos: (List<PhotoItem>, Int) -> Unit,
) {
    val data by vm.state.collectAsState()
    val p = data.posts.firstOrNull { it.id == postId }
    val ctx = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    if (p == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val d = runCatching { LocalDate.parse(p.date) }.getOrNull()
    val flights = data.flights.filter { it.trip == p.trip && p.trip.isNotEmpty() }.sortedBy { it.depDateTime() }
    val credits = p.photos().mapNotNull { data.credits[it.id] }.distinct()

    Box(Modifier.fillMaxSize().background(com.wijha.app.ui.Bg)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(Soft)) {
                    val c = p.coverId()
                    if (c.isNotEmpty()) AsyncImage(vm.photoFile(c), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(0f to Color(0x55081028), 0.4f to Color.Transparent, 1f to Color(0xDD081028)),
                        ),
                    )
                    Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                        Text(p.title.ifBlank { "بدون عنوان" }, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            listOfNotNull(d?.let { "${it.dayOfMonth} ${MONTHS[it.monthValue - 1]} ${it.year}" }, p.trip.ifBlank { null }).joinToString(" · "),
                            color = Color(0xFFE8EEFB), style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (flights.isNotEmpty()) item {
                Column(
                    Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("${flights.size} رحلات · ${num(flights.sumOf { it.km() })} كم", style = MaterialTheme.typography.labelLarge, color = Mute)
                    flights.forEach { f ->
                        val dt = f.localDate()
                        Text(
                            "${f.from} → ${f.to} · ${f.no}${airlineName(f.no).let { if (it.isEmpty()) "" else " · $it" }} · ${dt.dayOfMonth} ${MONTHS[dt.monthValue - 1]}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            p.blocks.forEach { b ->
                item { Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { BlockView(vm, b, onOpenPhotos) } }
            }
            if (p.tags.isNotEmpty()) item {
                FlowRow(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    p.tags.forEach { SuggestionChip(onClick = {}, label = { Text(it) }) }
                }
            }
            if (credits.isNotEmpty()) item {
                Text("الصور: ${credits.joinToString(" · ")}", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = Mute)
            }
        }
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            RoundIcon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoundIcon(Icons.Filled.Share, "مشاركة") {
                    shareText(ctx, p.title + "\n\n" + p.blocks.mapNotNull {
                        when (it) { is Block.Paragraph -> it.text; is Block.Heading -> it.text; is Block.Quote -> it.text; is Block.Tip -> it.text; else -> null }
                    }.filter { it.isNotBlank() }.joinToString("\n\n"))
                }
                RoundIcon(Icons.Filled.Edit, "تعديل") { onEdit(p.id) }
                RoundIcon(Icons.Filled.Delete, "حذف") { confirm = true }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("حذف التدوينة؟") },
            text = { Text("ستُحذف التدوينة وصورها من الجهاز.") },
            confirmButton = { TextButton(onClick = { confirm = false; vm.deletePost(p.id); onBack() }) { Text("حذف", color = com.wijha.app.ui.BadFg) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("إلغاء") } },
        )
    }
}

@Composable
fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.92f), contentColor = Ink),
    ) { Icon(icon, desc) }
}

@Composable
private fun BlockView(vm: AppViewModel, b: Block, onOpenPhotos: (List<PhotoItem>, Int) -> Unit) {
    when (b) {
        is Block.Heading -> if (b.text.isNotBlank()) Text(b.text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        is Block.Paragraph -> if (b.text.isNotBlank()) Text(b.text, style = MaterialTheme.typography.bodyLarge)
        is Block.Quote -> if (b.text.isNotBlank()) Row(Modifier.padding(vertical = 8.dp)) {
            Box(Modifier.width(4.dp).height(48.dp).background(Acc))
            Text(b.text, Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleMedium, color = Acc, fontWeight = FontWeight.Medium)
        }
        is Block.Tip -> if (b.text.isNotBlank()) Column(
            Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(20.dp)).background(Soft).padding(14.dp),
        ) {
            Text("نصيحة", style = MaterialTheme.typography.labelLarge, color = Acc)
            Text(b.text, style = MaterialTheme.typography.bodyMedium)
        }
        is Block.Place -> Column(
            Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(b.name.ifBlank { "مكان" }, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(b.kind, style = MaterialTheme.typography.labelMedium, color = Acc, modifier = Modifier.clip(CircleShape).background(Soft).padding(horizontal = 10.dp, vertical = 2.dp))
                if (b.rating > 0) Text("★".repeat(b.rating) + "☆".repeat(5 - b.rating), color = Acc)
                if (b.cost.isNotBlank()) Text(b.cost, style = MaterialTheme.typography.bodySmall, color = Mute)
            }
            if (b.note.isNotBlank()) Text(b.note, style = MaterialTheme.typography.bodyMedium)
        }
        is Block.Photos -> Box(Modifier.padding(vertical = 6.dp)) {
            com.wijha.app.ui.PhotoLayout(b.template, b.items, vm::photoFile) { i -> onOpenPhotos(b.items, i) }
        }
    }
}
