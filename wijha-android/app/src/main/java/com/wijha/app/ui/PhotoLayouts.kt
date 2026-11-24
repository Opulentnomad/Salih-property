package com.wijha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wijha.app.data.PhotoItem
import java.io.File

@Composable
private fun Tile(item: PhotoItem, file: File, modifier: Modifier, onClick: () -> Unit, caption: Boolean = true) {
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(Soft).clickable(onClick = onClick)) {
        AsyncImage(
            model = file, contentDescription = item.caption.ifBlank { null },
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
        )
        if (caption && item.caption.isNotBlank()) {
            Box(
                Modifier.align(Alignment.BottomStart).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.66f))))
                    .padding(start = 10.dp, end = 10.dp, top = 18.dp, bottom = 6.dp),
            ) { Text(item.caption, color = Color.White, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

/** قوالب عرض الصور السبعة. onClick يستقبل ترتيب الصورة داخل القالب. */
@Composable
fun PhotoLayout(
    template: String,
    items: List<PhotoItem>,
    fileOf: (String) -> File,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {},
) {
    if (items.isEmpty()) return
    val indexed = items.mapIndexed { i, it -> i to it }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        when (template) {
            "single" -> indexed.forEach { (i, it) ->
                Tile(it, fileOf(it.id), Modifier.fillMaxWidth().aspectRatio(4f / 3f), { onClick(i) })
            }
            "duo" -> indexed.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(1f).aspectRatio(3f / 4f), { onClick(i) }) }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
            "trio" -> {
                val head = indexed.take(3)
                Row(Modifier.fillMaxWidth().aspectRatio(4f / 3f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    head.getOrNull(0)?.let { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(1.25f).fillMaxHeight(), { onClick(i) }) }
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        head.drop(1).forEach { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(1f).fillMaxWidth(), { onClick(i) }) }
                    }
                }
                indexed.drop(3).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(1f).aspectRatio(1f), { onClick(i) }) }
                        if (row.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }
            "grid" -> indexed.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(1f).aspectRatio(1f), { onClick(i) }) }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
            "mosaic" -> indexed.chunked(5).forEach { group ->
                val big = group.getOrNull(0)
                Row(Modifier.fillMaxWidth().height(200.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    big?.let { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(2f).fillMaxHeight(), { onClick(i) }) }
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        group.drop(1).take(2).forEach { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(1f).fillMaxWidth(), { onClick(i) }) }
                    }
                }
                if (group.size > 3) Row(Modifier.fillMaxWidth().height(96.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    group.drop(3).forEach { (i, it) -> Tile(it, fileOf(it.id), Modifier.weight(1f).fillMaxHeight(), { onClick(i) }) }
                }
            }
            "polaroid" -> indexed.chunked(2).forEachIndexed { r, row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEachIndexed { c, (i, it) ->
                        val rot = if ((r + c) % 2 == 0) -3f else 2.5f
                        Column(
                            Modifier.weight(1f).graphicsLayer { rotationZ = rot }.shadow(6.dp, RoundedCornerShape(3.dp))
                                .background(Color.White).padding(7.dp).clickable { onClick(i) },
                        ) {
                            AsyncImage(
                                model = fileOf(it.id), contentDescription = it.caption.ifBlank { null },
                                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                            )
                            Text(
                                it.caption, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                                color = Ink, modifier = Modifier.fillMaxWidth().height(28.dp).padding(top = 6.dp),
                                maxLines = 1,
                            )
                        }
                    }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
            else -> Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF141A2C)).padding(vertical = 18.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(items) { i, it ->
                        Tile(it, fileOf(it.id), Modifier.width(190.dp).aspectRatio(3f / 2f), { onClick(i) })
                    }
                }
            }
        }
    }
}
