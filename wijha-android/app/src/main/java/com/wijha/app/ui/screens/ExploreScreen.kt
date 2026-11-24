package com.wijha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wijha.app.data.MONTHS
import com.wijha.app.data.MONTH_SUGGESTIONS
import com.wijha.app.data.PLACE_BY_KEY
import com.wijha.app.data.COUNTRY_NAMES
import com.wijha.app.data.flagEmoji
import com.wijha.app.data.wikiTitle
import com.wijha.app.ui.Acc
import com.wijha.app.ui.CodeStyle
import com.wijha.app.ui.Mute
import com.wijha.app.ui.Soft
import com.wijha.app.ui.WikiImage
import com.wijha.app.ui.gradientFor
import java.time.LocalDate

@Composable
fun ExploreScreen(padding: PaddingValues, onPlan: (String) -> Unit) {
    var month by rememberSaveable { mutableIntStateOf(LocalDate.now().monthValue - 1) }
    val chips = rememberLazyListState()
    LaunchedEffect(Unit) { chips.scrollToItem(maxOf(0, month - 2)) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 12.dp, bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("وجهات ${MONTHS[month]}", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "أفضل وجهات الشهر المختار. أفكار فقط للسفر القادم، بلا حجوزات.",
                    style = MaterialTheme.typography.bodyMedium, color = Mute,
                )
            }
        }
        item {
            LazyRow(state = chips, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(12) { i -> FilterChip(selected = month == i, onClick = { month = i }, label = { Text(MONTHS[i]) }) }
            }
        }
        items(MONTH_SUGGESTIONS[month]) { s ->
            val place = PLACE_BY_KEY[s.place] ?: return@items
            Card(
                shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Row(Modifier.height(140.dp)) {
                    WikiImage(wikiTitle(place.airport), Modifier.width(110.dp).fillMaxHeight(), gradientFor(place.cc))
                    Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(place.name, style = MaterialTheme.typography.titleLarge)
                        Text("${flagEmoji(place.cc)} ${COUNTRY_NAMES[place.cc].orEmpty()}", style = MaterialTheme.typography.bodySmall, color = Mute)
                        Text(s.reason, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = { onPlan(place.airport) }, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(36.dp)) {
                            Icon(Icons.Filled.Flight, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("خطّط رحلة")
                        }
                    }
                    Column(
                        Modifier.width(70.dp).fillMaxHeight().background(Soft),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                    ) {
                        Text("${s.temp}°", style = CodeStyle, fontSize = 30.sp, color = Acc)
                        Text("تقريباً", style = MaterialTheme.typography.bodySmall, color = Mute)
                    }
                }
            }
        }
        item { Text("درجات الحرارة تقريبية وتتغير من سنة لأخرى.", style = MaterialTheme.typography.bodySmall, color = Mute) }
    }
}

