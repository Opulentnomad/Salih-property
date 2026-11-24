@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.wijha.app.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.wijha.app.AppViewModel
import com.wijha.app.data.Block
import com.wijha.app.data.CommonsPhoto
import com.wijha.app.data.MONTHS
import com.wijha.app.data.Net
import com.wijha.app.data.PHOTO_TEMPLATES
import com.wijha.app.data.PLACE_KINDS
import com.wijha.app.data.POST_TAGS
import com.wijha.app.data.PhotoItem
import com.wijha.app.data.Post
import com.wijha.app.data.wikiTitle
import com.wijha.app.ui.Acc
import com.wijha.app.ui.Bg
import com.wijha.app.ui.Ink
import com.wijha.app.ui.Mute
import com.wijha.app.ui.PhotoLayout
import com.wijha.app.ui.Soft
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PostEditorScreen(vm: AppViewModel, initial: Post, isNew: Boolean, onClose: () -> Unit, onSaved: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf(initial) }
    var pickTarget by remember { mutableStateOf(-1) }
    var netTarget by remember { mutableStateOf(-1) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val data by vm.state.collectAsState()
    val trips = data.flights.map { it.trip }.distinct()

    fun setBlock(i: Int, b: Block) { draft = draft.copy(blocks = draft.blocks.toMutableList().also { it[i] = b }) }
    fun addPhotos(i: Int, items: List<PhotoItem>) {
        val b = draft.blocks.getOrNull(i) as? Block.Photos ?: return
        setBlock(i, b.copy(items = b.items + items))
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(30)) { uris ->
        val i = pickTarget
        if (uris.isNotEmpty() && i >= 0) {
            busy = true
            scope.launch {
                val items = vm.importPhotos(uris)
                addPhotos(i, items)
                busy = false
                if (items.size < uris.size) error = "تعذّر قراءة بعض الصور."
            }
        }
    }

    Scaffold(
        containerColor = Bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isNew) "تدوينة جديدة" else "تعديل التدوينة") },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "إغلاق") } },
                actions = {
                    TextButton(onClick = {
                        if (draft.title.isBlank()) error = "اكتب عنواناً للتدوينة."
                        else { vm.savePost(draft.copy(sample = false)); onSaved(draft.id) }
                    }) { Text("حفظ") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Bg),
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                LazyRow(
                    Modifier.navigationBarsPadding().padding(vertical = 8.dp), contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val adds = listOf<Pair<String, () -> Block>>(
                        "فقرة" to { Block.Paragraph() }, "صور" to { Block.Photos("duo") }, "عنوان" to { Block.Heading() },
                        "مكان" to { Block.Place() }, "نصيحة" to { Block.Tip() }, "اقتباس" to { Block.Quote() },
                    )
                    items(adds.size) { k ->
                        AssistChip(onClick = { draft = draft.copy(blocks = draft.blocks + adds[k].second()) }, label = { Text("+ ${adds[k].first}") })
                    }
                }
            }
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = draft.title, onValueChange = { draft = draft.copy(title = it.take(80)) }, label = { Text("العنوان") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.titleLarge,
                )
            }
            if (error.isNotEmpty()) item { Text(error, color = com.wijha.app.ui.BadFg) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val d = runCatching { LocalDate.parse(draft.date) }.getOrDefault(LocalDate.now())
                    OutlinedButton(onClick = {
                        DatePickerDialog(ctx, { _, y, m, day -> draft = draft.copy(date = LocalDate.of(y, m + 1, day).toString()) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
                    }) {
                        Icon(Icons.Filled.CalendarMonth, null)
                        Spacer(Modifier.width(8.dp))
                        Text("${d.dayOfMonth} ${MONTHS[d.monthValue - 1]} ${d.year}")
                    }
                    Text("الرحلة", style = MaterialTheme.typography.labelLarge, color = Mute)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = draft.trip.isEmpty(), onClick = { draft = draft.copy(trip = "") }, label = { Text("بدون رحلة") })
                        trips.forEach { t -> FilterChip(selected = draft.trip == t, onClick = { draft = draft.copy(trip = t) }, label = { Text(t) }) }
                    }
                    Text("الوسوم", style = MaterialTheme.typography.labelLarge, color = Mute)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        POST_TAGS.forEach { t ->
                            FilterChip(
                                selected = t in draft.tags,
                                onClick = { draft = draft.copy(tags = if (t in draft.tags) draft.tags - t else draft.tags + t) },
                                label = { Text(t) },
                            )
                        }
                    }
                }
            }
            itemsIndexed(draft.blocks) { i, b ->
                BlockCard(
                    block = b, index = i, count = draft.blocks.size,
                    onChange = { setBlock(i, it) },
                    onMove = { dir ->
                        val j = i + dir
                        if (j in draft.blocks.indices) draft = draft.copy(blocks = draft.blocks.toMutableList().also { l -> val t = l[i]; l[i] = l[j]; l[j] = t })
                    },
                    onDelete = { draft = draft.copy(blocks = draft.blocks.filterIndexed { k, _ -> k != i }) },
                    vm = vm,
                    onPickGallery = { pickTarget = i; picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onPickNet = { netTarget = i },
                )
            }
        }
    }
    if (busy) Dialog(onDismissRequest = {}) { Surface(shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Text("جارٍ إضافة الصور…") } } }
    if (netTarget >= 0) {
        val query = remember { guessQuery(data, draft.trip) }
        InternetPhotoDialog(query, { netTarget = -1 }) { picked ->
            val i = netTarget
            netTarget = -1
            busy = true
            scope.launch {
                val items = vm.importRemote(picked)
                addPhotos(i, items)
                busy = false
                if (items.isEmpty()) error = "تعذّر تنزيل الصور. تأكد من الاتصال."
            }
        }
    }
}

@Composable
private fun BlockCard(
    block: Block, index: Int, count: Int, vm: AppViewModel,
    onChange: (Block) -> Unit, onMove: (Int) -> Unit, onDelete: () -> Unit,
    onPickGallery: () -> Unit, onPickNet: () -> Unit,
) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when (block) { is Block.Heading -> "عنوان"; is Block.Paragraph -> "فقرة"; is Block.Quote -> "اقتباس"; is Block.Tip -> "نصيحة"; is Block.Place -> "مكان"; is Block.Photos -> "صور" },
                    style = MaterialTheme.typography.labelLarge, color = Acc, modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onMove(-1) }, enabled = index > 0) { Icon(Icons.Filled.ArrowUpward, "أعلى") }
                IconButton(onClick = { onMove(1) }, enabled = index < count - 1) { Icon(Icons.Filled.ArrowDownward, "أسفل") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "حذف الكتلة") }
            }
            when (block) {
                is Block.Heading -> OutlinedTextField(block.text, { onChange(block.copy(text = it)) }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("عنوان القسم") })
                is Block.Paragraph -> OutlinedTextField(block.text, { onChange(block.copy(text = it)) }, Modifier.fillMaxWidth(), minLines = 3, placeholder = { Text("اكتب هنا ما حدث وما رأيته…") })
                is Block.Quote -> OutlinedTextField(block.text, { onChange(block.copy(text = it)) }, Modifier.fillMaxWidth(), minLines = 2, placeholder = { Text("عبارة لا تنساها") })
                is Block.Tip -> OutlinedTextField(block.text, { onChange(block.copy(text = it)) }, Modifier.fillMaxWidth(), minLines = 2, placeholder = { Text("نصيحة لمن يزور المكان") })
                is Block.Place -> {
                    OutlinedTextField(block.name, { onChange(block.copy(name = it)) }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("اسم المكان") })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PLACE_KINDS.forEach { k -> FilterChip(selected = block.kind == k, onClick = { onChange(block.copy(kind = k)) }, label = { Text(k) }) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("التقييم", color = Mute, modifier = Modifier.padding(end = 6.dp))
                        (1..5).forEach { s ->
                            FilterChip(selected = block.rating == s, onClick = { onChange(block.copy(rating = if (block.rating == s) 0 else s)) }, label = { Text("$s") })
                        }
                    }
                    OutlinedTextField(block.cost, { onChange(block.copy(cost = it)) }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("التكلفة (اختياري)") })
                    OutlinedTextField(block.note, { onChange(block.copy(note = it)) }, Modifier.fillMaxWidth(), minLines = 2, label = { Text("ملاحظاتك") })
                }
                is Block.Photos -> {
                    Text("قالب العرض", style = MaterialTheme.typography.labelMedium, color = Mute)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PHOTO_TEMPLATES.forEach { (k, l) -> FilterChip(selected = block.template == k, onClick = { onChange(block.copy(template = k)) }, label = { Text(l) }) }
                    }
                    block.items.forEachIndexed { j, it ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AsyncImage(vm.photoFile(it.id), null, Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Soft), contentScale = ContentScale.Crop)
                            OutlinedTextField(
                                it.caption, { c -> onChange(block.copy(items = block.items.toMutableList().also { l -> l[j] = it.copy(caption = c.take(80)) })) },
                                Modifier.weight(1f), singleLine = true, placeholder = { Text("تعليق على الصورة") },
                            )
                            IconButton(onClick = { onChange(block.copy(items = block.items.toMutableList().also { l -> val x = l.removeAt(j); if (j > 0) l.add(j - 1, x) else l.add(0, x) })) }, enabled = j > 0) { Icon(Icons.Filled.ArrowUpward, "قبل") }
                            IconButton(onClick = { onChange(block.copy(items = block.items.filterIndexed { k, _ -> k != j })) }) { Icon(Icons.Filled.Close, "إزالة الصورة") }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onPickGallery, colors = ButtonDefaults.buttonColors(containerColor = Ink)) {
                            Icon(Icons.Filled.AddPhotoAlternate, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("من المعرض")
                        }
                        OutlinedButton(onClick = onPickNet) { Icon(Icons.Filled.Public, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("من الإنترنت") }
                    }
                    if (block.items.isNotEmpty()) {
                        Box(Modifier.clip(RoundedCornerShape(14.dp)).background(Bg).padding(8.dp)) {
                            PhotoLayout(block.template, block.items, vm::photoFile)
                        }
                    }
                }
            }
        }
    }
}

/** بحث صور من ويكيميديا كومنز واختيار عدة صور. */
@Composable
private fun InternetPhotoDialog(initialQuery: String, onDismiss: () -> Unit, onPick: (List<CommonsPhoto>) -> Unit) {
    var q by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<CommonsPhoto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<Int>()) }
    val scope = rememberCoroutineScope()
    fun search() {
        if (q.isBlank()) return
        loading = true; error = ""; selected = emptySet()
        scope.launch {
            try {
                results = Net.commons(q.trim())
                if (results.isEmpty()) error = "لا توجد نتائج. جرّب كلمة إنجليزية مثل Istanbul."
            } catch (e: Exception) {
                results = emptyList()
                error = "تعذّر الاتصال بالإنترنت. تأكد من الاتصال ثم أعد المحاولة."
            }
            loading = false
        }
    }
    LaunchedEffect(Unit) { if (initialQuery.isNotBlank()) search() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("صور من الإنترنت", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(q, { q = it }, Modifier.weight(1f), singleLine = true, placeholder = { Text("Istanbul") }, leadingIcon = { Icon(Icons.Filled.Search, null) })
                    Button(onClick = { search() }, colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("بحث") }
                }
                Text("من ويكيميديا كومنز. يُحفظ اسم المصوّر والترخيص مع الصورة.", style = MaterialTheme.typography.bodySmall, color = Mute)
                if (loading) CircularProgressIndicator(Modifier.size(28.dp))
                if (error.isNotEmpty()) Text(error, color = com.wijha.app.ui.BadFg, style = MaterialTheme.typography.bodyMedium)
                LazyVerticalGrid(GridCells.Fixed(3), Modifier.heightIn(max = 340.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(results) { i, r ->
                        Box(
                            Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Soft).clickable { selected = if (i in selected) selected - i else selected + i },
                        ) {
                            AsyncImage(r.url, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            if (i in selected) Box(Modifier.fillMaxSize().background(Acc.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                                Text("✓", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onPick(selected.sorted().map { results[it] }) }, enabled = selected.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink),
                    ) { Text("أضف (${selected.size})") }
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                }
            }
        }
    }
}

/** عبارة بحث مناسبة لصور الرحلة: مدينة أول وجهة أجنبية فيها. */
fun guessQuery(data: com.wijha.app.data.AppData, trip: String): String {
    val home = com.wijha.app.data.AIRPORT_BY_CODE[data.home]?.cc
    val code = data.flights.filter { it.trip == trip }.map { it.to }.firstOrNull { com.wijha.app.data.AIRPORT_BY_CODE[it]?.cc != home }
    return code?.let { wikiTitle(it) } ?: ""
}
