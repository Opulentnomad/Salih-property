@file:OptIn(ExperimentalMaterial3Api::class)

package com.wijha.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wijha.app.AppViewModel
import com.wijha.app.data.Block
import com.wijha.app.data.Flight
import com.wijha.app.data.PhotoItem
import com.wijha.app.data.Post
import com.wijha.app.data.Store
import com.wijha.app.data.depDateTime
import com.wijha.app.ui.screens.ExploreScreen
import com.wijha.app.ui.screens.FlightEditorSheet
import com.wijha.app.ui.screens.FlightsScreen
import com.wijha.app.ui.screens.JournalScreen
import com.wijha.app.ui.screens.Lightbox
import com.wijha.app.ui.screens.LightboxState
import com.wijha.app.ui.screens.NewPostSheet
import com.wijha.app.ui.screens.PassportScreen
import com.wijha.app.ui.screens.PostEditorScreen
import com.wijha.app.ui.screens.PostReaderScreen
import com.wijha.app.ui.screens.StatsScreen
import com.wijha.app.ui.screens.TripScreen
import java.time.LocalDate

private data class FlightEdit(val flight: Flight?, val presetTo: String?)
private data class NewPostRequest(val trip: String)

private enum class Tab(val label: String, val icon: ImageVector) {
    FLIGHTS("رحلاتي", Icons.Filled.Flight),
    JOURNAL("مدونتي", Icons.Filled.AutoStories),
    EXPLORE("اقتراحات", Icons.Filled.Explore),
    STATS("إحصاءات", Icons.Filled.BarChart),
    PASSPORT("جوازي", Icons.Filled.Badge),
}

@Composable
fun WijhaApp(vm: AppViewModel) {
    val nav = rememberNavController()
    val data by vm.state.collectAsState()
    var flightEdit by remember { mutableStateOf<FlightEdit?>(null) }
    var newPost by remember { mutableStateOf<NewPostRequest?>(null) }
    var lightbox by remember { mutableStateOf<LightboxState?>(null) }
    // مسودة تدوينة جديدة تُمرَّر إلى المحرر
    var pendingDraft by remember { mutableStateOf<Post?>(null) }

    val openPhotos: (List<PhotoItem>, Int) -> Unit = { items, i -> lightbox = LightboxState(items, i) }

    NavHost(nav, startDestination = "main") {
        composable("main") {
            MainScaffold(
                vm = vm,
                onEditFlight = { flightEdit = FlightEdit(it, null) },
                onNewFlight = { flightEdit = FlightEdit(null, it) },
                onOpenTrip = { nav.navigate("trip/" + Uri.encode(it)) },
                onOpenPost = { nav.navigate("post/$it") },
                onNewPost = { newPost = NewPostRequest(it) },
                onOpenPhotos = openPhotos,
            )
        }
        composable("trip/{name}", arguments = listOf(navArgument("name") { type = NavType.StringType })) { e ->
            TripScreen(
                vm = vm, tripName = e.arguments?.getString("name").orEmpty(),
                onBack = { nav.popBackStack() },
                onEditFlight = { flightEdit = FlightEdit(it, null) },
                onOpenPost = { nav.navigate("post/$it") },
                onNewPost = { newPost = NewPostRequest(it) },
                onOpenPhotos = openPhotos,
            )
        }
        composable("post/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            val id = e.arguments?.getString("id").orEmpty()
            PostReaderScreen(
                vm = vm, postId = id, onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("editor/$it") }, onOpenPhotos = openPhotos,
            )
        }
        composable("editor/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { e ->
            val id = e.arguments?.getString("id").orEmpty()
            val existing = data.posts.firstOrNull { it.id == id }
            val draft = existing ?: pendingDraft
            if (draft != null && draft.id == id) {
                PostEditorScreen(
                    vm = vm, initial = draft, isNew = existing == null,
                    onClose = { nav.popBackStack() },
                    onSaved = { savedId ->
                        pendingDraft = null
                        nav.popBackStack()
                        nav.navigate("post/$savedId") { launchSingleTop = true }
                    },
                )
            }
        }
    }

    flightEdit?.let { req ->
        FlightEditorSheet(
            data = data, flight = req.flight, presetTo = req.presetTo,
            onSave = { vm.saveFlight(it); flightEdit = null },
            onDelete = { vm.deleteFlight(it); flightEdit = null },
            onDismiss = { flightEdit = null },
        )
    }
    newPost?.let { req ->
        NewPostSheet(
            trips = data.flights.map { it.trip }.distinct(), initialTrip = req.trip,
            onStart = { trip, starter ->
                val first = data.flights.filter { it.trip == trip }.minByOrNull { it.depDateTime() }
                val p = Post(
                    id = Store.newId("p"), title = trip, trip = trip, date = first?.date ?: LocalDate.now().toString(),
                    blocks = starterBlocks(starter),
                )
                pendingDraft = p
                newPost = null
                nav.navigate("editor/${p.id}")
            },
            onDismiss = { newPost = null },
        )
    }
    lightbox?.let { Lightbox(vm, it) { lightbox = null } }
}

private fun starterBlocks(k: String): List<Block> = when (k) {
    "day" -> listOf(
        Block.Heading("الصباح"), Block.Paragraph(), Block.Photos("trio"), Block.Heading("بعد الظهر"), Block.Paragraph(),
        Block.Photos("duo"), Block.Place("", "مطعم"), Block.Tip(),
    )
    "guide" -> listOf(Block.Heading("دليل المكان"), Block.Place("", "معلم"), Block.Photos("single"), Block.Place("", "مطعم"), Block.Tip())
    "album" -> listOf(Block.Photos("mosaic"), Block.Photos("polaroid"), Block.Quote())
    else -> listOf(Block.Paragraph())
}

@Composable
private fun MainScaffold(
    vm: AppViewModel,
    onEditFlight: (Flight) -> Unit,
    onNewFlight: (String?) -> Unit,
    onOpenTrip: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onNewPost: (String) -> Unit,
    onOpenPhotos: (List<PhotoItem>, Int) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val now by rememberNow()
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Tab.entries.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i },
                        icon = { Icon(t.icon, null) }, label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, indicatorColor = Ink, selectedTextColor = Ink,
                            unselectedIconColor = Mute, unselectedTextColor = Mute,
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            when (tab) {
                0 -> ExtendedFloatingActionButton(onClick = { onNewFlight(null) }, containerColor = Ink, contentColor = Color.White) {
                    Icon(Icons.Filled.Add, null); Spacer(Modifier.padding(4.dp)); Text("أضف رحلة")
                }
                1 -> ExtendedFloatingActionButton(onClick = { onNewPost("") }, containerColor = Ink, contentColor = Color.White) {
                    Icon(Icons.Filled.Add, null); Spacer(Modifier.padding(4.dp)); Text("تدوينة جديدة")
                }
                else -> {}
            }
        },
    ) { pad: PaddingValues ->
        when (tab) {
            0 -> FlightsScreen(vm, now, pad, onOpenTrip, onEditFlight, { onNewFlight(null) }, onNewPost)
            1 -> JournalScreen(vm, pad, onOpenPost, onOpenPhotos) { onNewPost("") }
            2 -> ExploreScreen(pad) { onNewFlight(it) }
            3 -> StatsScreen(vm, now, pad)
            else -> PassportScreen(vm, now, pad)
        }
    }
}
