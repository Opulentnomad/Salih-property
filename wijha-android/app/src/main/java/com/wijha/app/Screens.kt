@file:OptIn(ExperimentalLayoutApi::class)

package com.wijha.app

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import java.util.UUID

// ============ التنقل ============

sealed interface Screen {
    data object Main : Screen
    data class Detail(val destId: String) : Screen
    data class Builder(val destId: String, val days: Int) : Screen
    data class TripView(val tripId: String) : Screen
}

@Composable
fun AppRoot(vm: AppVm) {
    val s = vm.state
    if (!s.onboarded) {
        Onboarding(vm)
        return
    }
    var stack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Main)) }
    BackHandler(enabled = stack.size > 1) { stack = stack.dropLast(1) }

    when (val cur = stack.last()) {
        Screen.Main -> MainShell(
            vm,
            onOpenDest = { stack = stack + Screen.Detail(it) },
            onOpenTrip = { stack = stack + Screen.TripView(it) },
        )
        is Screen.Detail -> destById(cur.destId)?.let { d ->
            DetailScreen(vm, d, onBack = { stack = stack.dropLast(1) }, onBuild = { days -> stack = stack + Screen.Builder(d.id, days) })
        }
        is Screen.Builder -> destById(cur.destId)?.let { d ->
            BuilderScreen(vm, d, cur.days, onBack = { stack = stack.dropLast(1) },
                onSaved = { id -> stack = stack.dropLast(2) + Screen.TripView(id) })
        }
        is Screen.TripView -> TripScreen(vm, cur.tripId, onBack = { stack = stack.dropLast(1) })
    }
}

// ============ أيقونة الصور (Material "photo") ============

val PhotoIcon: ImageVector by lazy {
    ImageVector.Builder("photo", 24.dp, 24.dp, 24f, 24f).addPath(
        pathData = PathParser().parsePathString(
            "M21,19V5c0,-1.1 -0.9,-2 -2,-2H5c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2zM8.5,13.5l2.5,3.01L14.5,12l4.5,6H5l3.5,-4.5z",
        ).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()
}

// ============ التهيئة ============

@Composable
fun Onboarding(vm: AppVm) {
    val s = vm.state
    var picked by remember { mutableStateOf(setOf<String>()) }
    var budget by remember { mutableIntStateOf(2) }
    val lang = s.lang
    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
            .statusBarsPadding().navigationBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Pill("العربية", lang == "ar") { vm.update { it.copy(lang = "ar") } }
            Spacer(Modifier.width(8.dp))
            Pill("English", lang == "en") { vm.update { it.copy(lang = "en") } }
        }
        Text(tr("أهلاً بك في وجهة", "Welcome to Wijha"), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(
            tr("اختر ما تحب، وسنقترح عليك وجهات ونبني لك مساراً يناسب ذوقك. يمكنك التعديل لاحقاً.",
                "Pick what you love and we'll suggest destinations and build an itinerary that fits. You can change this later."),
            color = Mute, fontSize = 15.sp,
        )
        SectionTitle(tr("ما الذي تحب؟ (3 على الأقل)", "What do you like? (pick at least 3)"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ALL_TAGS.forEach { t ->
                Pill(t.label(lang), t.id in picked) { picked = if (t.id in picked) picked - t.id else picked + t.id }
            }
        }
        SectionTitle(tr("ميزانية الرحلة", "Trip budget"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(tr("اقتصادية", "Economy"), budget == 1) { budget = 1 }
            Pill(tr("متوسطة", "Mid-range"), budget == 2) { budget = 2 }
            Pill(tr("مرتفعة", "Premium"), budget == 3) { budget = 3 }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(tr("ابدأ الاستكشاف", "Start exploring"), Icons.Filled.Search, enabled = picked.size >= 3) {
            vm.update { it.copy(onboarded = true, budget = budget, weights = picked.associateWith { 0.8f }) }
        }
        Text(
            tr("بياناتك وتفضيلاتك تُحفظ على جهازك فقط.", "Your data and preferences are stored on your device only."),
            color = Mute, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ============ الهيكل الرئيسي ============

@Composable
fun MainShell(vm: AppVm, onOpenDest: (String) -> Unit, onOpenTrip: (String) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize().background(Bg)) {
        when (tab) {
            0 -> HomeTab(vm, onOpenDest)
            1 -> TripsTab(vm, onOpenTrip)
            2 -> PhotosTab(vm)
            else -> ProfileTab(vm)
        }
        val icons = listOf(Icons.Filled.Search, Icons.Filled.DateRange, PhotoIcon, Icons.Filled.Person)
        val labels = listOf(tr("استكشف", "Explore"), tr("رحلاتي", "Trips"), tr("الصور", "Photos"), tr("حسابي", "Profile"))
        Row(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp).fillMaxWidth().height(58.dp)
                .shadow(14.dp, CircleShape).clip(CircleShape).background(Ink),
            horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically,
        ) {
            icons.forEachIndexed { i, ic ->
                Column(
                    Modifier.clip(CircleShape).clickable { tab = i }.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(ic, labels[i], tint = if (tab == i) Color.White else Color(0xFF9D99A6), modifier = Modifier.size(22.dp))
                    Text(labels[i], fontSize = 10.sp, color = if (tab == i) Color.White else Color(0xFF9D99A6))
                }
            }
        }
    }
}

// ============ الاستكشاف ============

@Composable
fun HomeTab(vm: AppVm, onOpen: (String) -> Unit) {
    val s = vm.state
    val lang = s.lang
    var query by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf<String?>(null) }
    val ranked = remember(s.weights, s.budget, s.hidden, query, tag) { Recommender.rank(s, query, tag) }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(tr("صباح الخير", "Good morning"), color = Mute, fontSize = 13.sp)
                Text(tr("إلى أين هذه المرة؟", "Where to this time?"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                placeholder = { Text(tr("ابحث عن وجهة أو دولة…", "Search a destination or country…")) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                shape = CircleShape, modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                item { Pill(tr("الكل", "All"), tag == null) { tag = null } }
                items(ALL_TAGS) { t -> Pill(t.label(lang), tag == t.id) { tag = if (tag == t.id) null else t.id } }
            }
        }
        item { SectionTitle(tr("مقترحة لك", "Recommended for you")) }
        items(ranked, key = { it.dest.id }) { sc ->
            RecoCard(
                sc, liked = sc.dest.id in s.liked,
                onLike = { vm.toggleLike(sc.dest) },
                onDismiss = { vm.dismiss(sc.dest) },
                onOpen = { onOpen(sc.dest.id) },
            )
        }
        if (ranked.isEmpty()) {
            item { Text(tr("لا توجد نتائج. جرّب كلمة أخرى.", "No results. Try another word."), color = Mute) }
        }
    }
}

@Composable
fun RecoCard(sc: Scored, liked: Boolean, onLike: () -> Unit, onDismiss: () -> Unit, onOpen: () -> Unit) {
    val lang = LocalLang.current
    val d = sc.dest
    Box(
        Modifier.fillMaxWidth().height(240.dp)
            .shadow(14.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).clickable(onClick = onOpen),
    ) {
        DestPhoto(d, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)), startY = 220f),
            ),
        )
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.92f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                Text(tr("تطابق ${sc.match}٪", "${sc.match}% match"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.92f)).clickable(onClick = onDismiss)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) { Text(tr("ليس لي", "Not for me"), fontSize = 12.sp, color = Ink) }
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(Color.White).clickable(onClick = onLike),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null,
                        tint = if (liked) Heart else Ink, modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(d.name(lang), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(d.country(lang), color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }
                Text(tr("من ${d.priceSar} ر.س", "from SAR ${d.priceSar}"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                sc.reasons.forEach { r ->
                    val text = when (r) {
                        is Reason.TagReason -> tr("لأنك تحب ${tagById(r.tagId).ar}", "You like ${tagById(r.tagId).en}")
                        Reason.Budget -> tr("يناسب ميزانيتك", "Fits your budget")
                    }
                    Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 10.dp, vertical = 2.dp)) {
                        Text(text, fontSize = 11.sp, color = Ink)
                    }
                }
            }
        }
    }
}

// ============ تفاصيل الوجهة ============

@Composable
fun DetailScreen(vm: AppVm, d: Destination, onBack: () -> Unit, onBuild: (Int) -> Unit) {
    val lang = vm.state.lang
    val gal by rememberGallery(d)
    var days by remember { mutableIntStateOf(5) }
    Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(320.dp)) {
            DestPhoto(d, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
            Box(Modifier.statusBarsPadding().padding(16.dp)) {
                CircleBtn(Icons.AutoMirrored.Filled.ArrowBack, tr("رجوع", "Back"), onClick = onBack)
            }
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(d.name(lang), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(d.country(lang), color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (lang == "ar") d.blurbAr else d.blurbEn, fontSize = 16.sp, color = Ink)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                d.tags.entries.sortedByDescending { it.value }.take(5).forEach { Pill(tagById(it.key).label(lang)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoBox(tr("يبدأ من", "From"), tr("${d.priceSar} ر.س", "SAR ${d.priceSar}"), Modifier.weight(1f))
                InfoBox(tr("أفضل موسم", "Best season"), if (lang == "ar") d.bestAr else d.bestEn, Modifier.weight(1f))
                InfoBox(tr("أنشطة", "Activities"), d.pois.size.toString(), Modifier.weight(1f))
            }
            if (gal.isNotEmpty()) {
                SectionTitle(tr("صور من الوجهة", "Photos"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(gal) { url ->
                        AsyncImage(
                            model = url, contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.size(width = 170.dp, height = 120.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFE8E2EA)),
                        )
                    }
                }
            }
            SectionTitle(tr("ابنِ مسارك", "Build your itinerary"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 7).forEach { n -> Pill(tr("$n أيام", "$n days"), days == n) { days = n } }
            }
            PrimaryButton(tr("ولّد المسار", "Generate itinerary"), Icons.Filled.Refresh) { onBuild(days) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.shadow(6.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Color.White).padding(12.dp),
    ) {
        Text(label, fontSize = 11.sp, color = Mute)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}

// ============ منشئ المسار ============

@Composable
fun BuilderScreen(vm: AppVm, d: Destination, days0: Int, onBack: () -> Unit, onSaved: (String) -> Unit) {
    val s = vm.state
    val lang = s.lang
    var days by remember { mutableIntStateOf(days0) }
    var travelers by remember { mutableIntStateOf(2) }
    var offset by remember { mutableIntStateOf(30) }
    var seed by remember { mutableLongStateOf(1L) }
    var items by remember(days, seed) { mutableStateOf(Planner.plan(d, days, s.weights, seed)) }
    val start = LocalDate.now().toEpochDay() + offset
    val ctx = LocalContext.current

    Box(Modifier.fillMaxSize().background(Bg)) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircleBtn(Icons.AutoMirrored.Filled.ArrowBack, tr("رجوع", "Back"), onClick = onBack)
                    Column {
                        Text(tr("مسار ${d.ar}", "${d.en} itinerary"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(tr("مُولَّد حسب اهتماماتك", "Generated from your interests"), fontSize = 12.sp, color = Mute)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 7).forEach { n -> Pill(tr("$n أيام", "$n days"), days == n) { days = n } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(14 to tr("بعد أسبوعين", "In 2 weeks"), 30 to tr("بعد شهر", "In 1 month"), 60 to tr("بعد شهرين", "In 2 months"))
                            .forEach { (n, label) -> Pill(label, offset == n) { offset = n } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("المسافرون", "Travelers"), color = Mute, fontSize = 13.sp)
                        Pill("−") { if (travelers > 1) travelers-- }
                        Text(travelers.toString(), fontWeight = FontWeight.Bold)
                        Pill("+") { if (travelers < 10) travelers++ }
                    }
                }
            }
            item {
                DayPlan(d, items, start, days, onRemove = { rm -> items = items - rm }, onOpen = { openMap(ctx, d, it) })
            }
        }
        Row(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.height(52.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(Color.White)
                    .clickable { seed += 1 }.padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) { Text(tr("غيّر الاقتراح", "Reshuffle"), fontWeight = FontWeight.Bold, color = Ink) }
            PrimaryButton(tr("احفظ الرحلة", "Save trip"), Icons.Filled.Check, modifier = Modifier.weight(1f)) {
                val trip = Trip(UUID.randomUUID().toString(), d.id, start, days, travelers, items, emptyList())
                vm.update { it.copy(trips = it.trips + trip) }
                onSaved(trip.id)
            }
        }
    }
}

@Composable
fun DateBubble(epochDay: Long, idx: Int) {
    val (bg, fg) = Bubbles[idx % Bubbles.size]
    val date = LocalDate.ofEpochDay(epochDay)
    val month = date.month.getDisplayName(JTextStyle.SHORT, Locale.forLanguageTag(LocalLang.current))
    Column(
        Modifier.size(48.dp).clip(CircleShape).background(bg),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Text(month, fontSize = 9.sp, color = fg)
        Text(date.dayOfMonth.toString(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
fun DayPlan(
    d: Destination, items: List<TripItem>, startEpoch: Long, days: Int,
    onRemove: ((TripItem) -> Unit)?, onOpen: (Poi) -> Unit,
) {
    val lang = LocalLang.current
    val gal by rememberGallery(d)
    val hero by rememberHero(d)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (day in 0 until days) {
            val dayItems = items.filter { it.day == day }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateBubble(startEpoch + day, day)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr("اليوم ${day + 1}", "Day ${day + 1}"), fontWeight = FontWeight.Bold, color = Ink)
                    if (dayItems.isEmpty()) Text(tr("يوم حر", "Free day"), color = Mute, fontSize = 13.sp)
                    Planner.dayTimes(d, dayItems).forEach { (item, t) ->
                        val poi = d.pois.first { it.id == item.poiId }
                        val idx = d.pois.indexOf(poi)
                        val thumb = if (gal.isNotEmpty()) gal[idx % gal.size] else hero
                        Row(
                            Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp))
                                .background(Color.White).clickable { onOpen(poi) }.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(Planner.fmt(t), color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(poi.name(lang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                                Text("${tagById(poi.tag).label(lang)} · ${poi.hours}${tr(" س", " h")}", fontSize = 11.sp, color = Mute)
                            }
                            GradientBox(d, Modifier.size(width = 76.dp, height = 56.dp).clip(RoundedCornerShape(12.dp))) {
                                if (thumb != null) AsyncImage(thumb, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            if (onRemove != null) {
                                Icon(
                                    Icons.Filled.Close, tr("حذف", "Remove"), tint = Mute,
                                    modifier = Modifier.size(22.dp).clip(CircleShape).clickable { onRemove(item) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun openMap(ctx: android.content.Context, d: Destination, poi: Poi) {
    val q = Uri.encode("${poi.en}, ${d.en}")
    val intents = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$q")),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$q")),
    )
    for (i in intents) {
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
            return
        } catch (e: Exception) {
            // جرّب الخيار التالي
        }
    }
}

// ============ الرحلات ============

@Composable
fun TripsTab(vm: AppVm, onOpenTrip: (String) -> Unit) {
    val s = vm.state
    val lang = s.lang
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text(tr("رحلاتي", "My trips"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink) }
        if (s.trips.isEmpty()) {
            item {
                Text(
                    tr("لا توجد رحلات بعد. افتح وجهة من «استكشف» ثم اضغط «ولّد المسار».",
                        "No trips yet. Open a destination in Explore and tap “Generate itinerary”."),
                    color = Mute,
                )
            }
        }
        items(s.trips.reversed(), key = { it.id }) { t ->
            val d = destById(t.destId) ?: return@items
            Row(
                Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp))
                    .background(Color.White).clickable { onOpenTrip(t.id) }.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                DestPhoto(d, Modifier.size(84.dp).clip(RoundedCornerShape(16.dp)))
                Column(Modifier.weight(1f)) {
                    Text(tr("رحلة ${d.ar}", "${d.en} trip"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(dateRange(t, lang), fontSize = 12.sp, color = Mute)
                    Text(tr("${t.travelers} مسافرين · ${t.items.size} نشاطاً", "${t.travelers} travelers · ${t.items.size} activities"), fontSize = 12.sp, color = Mute)
                }
            }
        }
    }
}

fun dateRange(t: Trip, lang: String): String {
    val loc = Locale.forLanguageTag(lang)
    val a = LocalDate.ofEpochDay(t.startEpochDay)
    val b = a.plusDays((t.days - 1).toLong())
    val m1 = a.month.getDisplayName(JTextStyle.FULL, loc)
    val m2 = b.month.getDisplayName(JTextStyle.FULL, loc)
    return if (a.month == b.month) "${a.dayOfMonth} – ${b.dayOfMonth} $m1 ${b.year}" else "${a.dayOfMonth} $m1 – ${b.dayOfMonth} $m2 ${b.year}"
}

@Composable
fun TabPill(icon: ImageVector, label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.shadow(4.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
            .background(if (selected) Ink else Color.White).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = if (selected) Color.White else Ink, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 11.sp, color = if (selected) Color.White else Ink)
    }
}

@Composable
fun TripScreen(vm: AppVm, tripId: String, onBack: () -> Unit) {
    val s = vm.state
    val lang = s.lang
    val trip = s.trips.firstOrNull { it.id == tripId }
    val d = trip?.let { destById(it.destId) }
    if (trip == null || d == null) {
        Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) { Text("—") }
        return
    }
    val ctx = LocalContext.current
    val gal by rememberGallery(d)
    var tab by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    val today = LocalDate.now().toEpochDay()
    val end = trip.startEpochDay + trip.days - 1
    val shareText = tr("رحلتي إلى ${d.ar}: ${dateRange(trip, "ar")} — خُطّطت في تطبيق وجهة",
        "My trip to ${d.en}: ${dateRange(trip, "en")} — planned with Wijha")
    val status = when {
        trip.startEpochDay > today -> tr("قادمة", "Upcoming") to Color(0xFF5CC24A)
        today in trip.startEpochDay..end -> tr("جارية", "Ongoing") to Color(0xFFF2A23A)
        else -> tr("منتهية", "Completed") to Color(0xFF9D99A6)
    }

    Box(Modifier.fillMaxSize().background(Bg)) {
        DestPhoto(d, Modifier.fillMaxWidth().height(280.dp))
        Box(Modifier.fillMaxWidth().height(280.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.55f)))))
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            CircleBtn(Icons.AutoMirrored.Filled.ArrowBack, tr("رجوع", "Back"), onClick = onBack)
            CircleBtn(Icons.Filled.Share, tr("مشاركة", "Share")) {
                val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }
                ctx.startActivity(Intent.createChooser(send, null))
            }
        }
        Row(
            Modifier.padding(top = 120.dp, start = 16.dp, end = 16.dp).statusBarsPadding()
                .clip(CircleShape).background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(status.second))
            Text(status.first, fontSize = 12.sp, color = Ink)
        }
        Column(Modifier.padding(top = 178.dp, start = 20.dp, end = 20.dp).statusBarsPadding()) {
            Text(tr("رحلة ${d.ar}", "${d.en} Escape"), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "${dateRange(trip, lang)}  |  " + tr("${trip.travelers} مسافرين", "${trip.travelers} travelers"),
                color = Color.White.copy(alpha = 0.92f), fontSize = 13.sp,
            )
        }

        Column(
            Modifier.fillMaxSize().padding(top = 250.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Bg).padding(top = 14.dp),
        ) {
            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabPill(Icons.Filled.DateRange, tr("المسار", "Itinerary"), tab == 0, Modifier.weight(1f)) { tab = 0 }
                TabPill(Icons.Filled.Edit, tr("اليوميات", "Journal"), tab == 1, Modifier.weight(1f)) { tab = 1 }
                TabPill(PhotoIcon, tr("الصور", "Photos"), tab == 2, Modifier.weight(1f)) { tab = 2 }
                TabPill(Icons.Filled.Place, tr("الخريطة", "Map"), tab == 3, Modifier.weight(1f)) { tab = 3 }
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.weight(1f)) {
                when (tab) {
                    0 -> LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp)) {
                        item {
                            Text(tr("خطة الرحلة", "Trip plan"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                            Spacer(Modifier.height(10.dp))
                            DayPlan(d, trip.items, trip.startEpochDay, trip.days, null, onOpen = { openMap(ctx, d, it) })
                        }
                    }
                    1 -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (trip.notes.isEmpty()) item {
                            Text(tr("لا توجد ذكريات بعد. اضغط «إضافة ذكرى».", "No memories yet. Tap “Add memory”."), color = Mute)
                        }
                        items(trip.notes.reversed()) { n ->
                            Column(Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Color.White).padding(14.dp)) {
                                Text(LocalDate.ofEpochDay(n.epochDay).toString(), fontSize = 11.sp, color = Mute)
                                Text(n.text, fontSize = 15.sp, color = Ink)
                            }
                        }
                    }
                    2 -> PhotoGrid(
                        urls = gal, favs = s.favPhotos, onFav = { vm.toggleFavPhoto(it) },
                        modifier = Modifier.fillMaxSize(), bottomPad = 110.dp,
                        header = {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    if (gal.isEmpty()) tr("جارٍ تحميل الصور من الإنترنت…", "Loading photos from the internet…")
                                    else tr("${gal.size} صورة", "${gal.size} photos"),
                                    color = Mute, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                )
                            }
                        },
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(d.pois) { poi ->
                            Row(
                                Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp))
                                    .background(Color.White).clickable { openMap(ctx, d, poi) }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(Icons.Filled.LocationOn, null, tint = Rose)
                                Text(poi.name(lang), Modifier.weight(1f), fontWeight = FontWeight.Medium, color = Ink)
                                Text(tr("افتح في الخرائط", "Open in Maps"), fontSize = 12.sp, color = Blue)
                            }
                        }
                    }
                }
            }
        }
        PrimaryButton(
            tr("إضافة ذكرى", "Add memory"), Icons.Filled.Add,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
        ) { showAdd = true }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(tr("ذكرى جديدة", "New memory")) },
            text = {
                OutlinedTextField(
                    value = noteText, onValueChange = { noteText = it },
                    placeholder = { Text(tr("ماذا حدث اليوم؟", "What happened today?")) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (noteText.isNotBlank()) {
                        val n = Note(noteText.trim(), LocalDate.now().toEpochDay())
                        vm.update { st -> st.copy(trips = st.trips.map { if (it.id == trip.id) it.copy(notes = it.notes + n) else it }) }
                    }
                    noteText = ""
                    showAdd = false
                    tab = 1
                }) { Text(tr("حفظ", "Save")) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(tr("إلغاء", "Cancel")) } },
        )
    }
}

// ============ الصور ============

@Composable
fun PhotosTab(vm: AppVm) {
    val s = vm.state
    val lang = s.lang
    val dests = remember(s.trips.size) {
        val ids = s.trips.map { it.destId }.distinct()
        if (ids.isEmpty()) CATALOG.take(4) else ids.mapNotNull { destById(it) }
    }
    val data by produceState(emptyMap<String, List<String>>(), dests) {
        value = dests.associate { it.id to ImageRepo.gallery(it) }
    }
    var sel by remember { mutableStateOf<String?>(null) }
    val urls = if (sel == null) data.values.flatten() else data[sel].orEmpty()

    PhotoGrid(
        urls = urls, favs = s.favPhotos, onFav = { vm.toggleFavPhoto(it) },
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        header = {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(tr("صور الرحلة", "Trip photos"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(
                            tr("${data.values.sumOf { it.size }} صورة من ${dests.size} أماكن", "${data.values.sumOf { it.size }} photos from ${dests.size} places"),
                            color = Mute, fontSize = 13.sp,
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        item { PlaceChip(tr("الكل", "All"), data.values.sumOf { it.size }, sel == null, null) { sel = null } }
                        items(dests) { d -> PlaceChip(d.name(lang), data[d.id]?.size ?: 0, sel == d.id, d) { sel = d.id } }
                    }
                    if (urls.isEmpty()) {
                        Text(tr("جارٍ تحميل الصور من الإنترنت…", "Loading photos from the internet…"), color = Mute, fontSize = 13.sp)
                    }
                }
            }
        },
    )
}

@Composable
fun PlaceChip(label: String, count: Int, selected: Boolean, dest: Destination?, onClick: () -> Unit) {
    Column(
        Modifier.width(82.dp).shadow(6.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp))
            .background(if (selected) Ink else Color.White).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (dest != null) DestPhoto(dest, Modifier.fillMaxWidth().height(38.dp))
        else Box(Modifier.fillMaxWidth().height(38.dp), contentAlignment = Alignment.Center) {
            Icon(PhotoIcon, null, tint = if (selected) Color.White else Ink, modifier = Modifier.size(22.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else Ink, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
        Text(count.toString(), fontSize = 11.sp, color = if (selected) Color(0xFFCFCBD6) else Mute, modifier = Modifier.padding(bottom = 6.dp))
    }
}

// ============ الحساب ============

@Composable
fun ProfileTab(vm: AppVm) {
    val s = vm.state
    val lang = s.lang
    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(tr("حسابي", "Profile"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
        SectionTitle(tr("اللغة", "Language"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("العربية", lang == "ar") { vm.update { it.copy(lang = "ar") } }
            Pill("English", lang == "en") { vm.update { it.copy(lang = "en") } }
        }
        SectionTitle(tr("الميزانية", "Budget"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(tr("اقتصادية", "Economy"), s.budget == 1) { vm.update { it.copy(budget = 1) } }
            Pill(tr("متوسطة", "Mid-range"), s.budget == 2) { vm.update { it.copy(budget = 2) } }
            Pill(tr("مرتفعة", "Premium"), s.budget == 3) { vm.update { it.copy(budget = 3) } }
        }
        SectionTitle(tr("اهتماماتي", "My interests"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ALL_TAGS.forEach { t ->
                val on = (s.weights[t.id] ?: 0f) >= 0.5f
                Pill(t.label(lang), on) {
                    vm.update { st ->
                        val w = st.weights.toMutableMap()
                        w[t.id] = if (on) 0f else 0.8f
                        st.copy(weights = w)
                    }
                }
            }
        }
        SectionTitle(tr("ما تعلّمه التطبيق منك", "What the app learned"))
        val top = s.weights.entries.filter { it.value > 0f }.sortedByDescending { it.value }.take(5)
        if (top.isEmpty()) Text(tr("لا توجد إشارات بعد.", "No signals yet."), color = Mute)
        top.forEach { (id, w) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tagById(id).label(lang), Modifier.width(110.dp), fontSize = 13.sp, color = Ink)
                Box(Modifier.weight(1f).height(8.dp).clip(CircleShape).background(Color(0xFFE8E2EA))) {
                    Box(Modifier.fillMaxWidth(w.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(Blue))
                }
            }
        }
        Text(
            tr("يتحسن الترتيب كلما ضغطت قلباً أو «ليس لي».", "Ranking improves each time you tap the heart or “Not for me”."),
            color = Mute, fontSize = 12.sp,
        )
        if (s.hidden.isNotEmpty()) {
            Pill(tr("إظهار الوجهات المخفية (${s.hidden.size})", "Show hidden destinations (${s.hidden.size})")) {
                vm.update { it.copy(hidden = emptySet()) }
            }
        }
        SectionTitle(tr("الخصوصية", "Privacy"))
        Text(
            tr("تفضيلاتك ورحلاتك وذكرياتك محفوظة على هذا الجهاز فقط. الصور تُجلب من ويكيبيديا وويكيميديا كومنز ولا نرسل إليهما أي بيانات عنك.",
                "Your preferences, trips and memories stay on this device. Photos are fetched from Wikipedia and Wikimedia Commons; no personal data is sent to them."),
            color = Mute, fontSize = 13.sp,
        )
        Pill(tr("إعادة ضبط كل شيء", "Reset everything")) { vm.update { UserState(lang = it.lang) } }
    }
}
