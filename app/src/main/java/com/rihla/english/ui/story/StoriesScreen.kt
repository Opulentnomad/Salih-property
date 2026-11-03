package com.rihla.english.ui.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.components.ScreenHeader
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.gamification.StoryCardUi
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.Night
import com.rihla.english.ui.theme.Saffron
import kotlinx.coroutines.launch

/** "مواقف من حياتك": interactive dialogue stories set in everyday Gulf situations. */
@Composable
fun StoriesScreen(viewModel: GamificationViewModel, onOpenStory: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lessonTitles = remember(state.units) { state.units.flatMap { it.nodes }.associate { it.node.id to it.node.titleAr } }
    val lockedTemplate = stringResource(R.string.story_locked, "%s")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ScreenHeader(stringResource(R.string.stories_title), stringResource(R.string.stories_subtitle))
            }
            items(state.stories, key = { it.story.id }) { card ->
                StoryCard(card) {
                    if (card.locked) {
                        val title = lessonTitles[card.story.requiredNodeId].orEmpty()
                        scope.launch { snackbar.showSnackbar(lockedTemplate.replace("%s", title)) }
                    } else {
                        onOpenStory(card.story.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryCard(card: StoryCardUi, onClick: () -> Unit) {
    val color = Color(card.story.colorHex)
    PressableSurface(
        onClick = onClick,
        color = color,
        shadowColor = Night.copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        depth = 6.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .alpha(if (card.locked) 0.55f else 1f)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(68.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) { Text(if (card.locked) "🔒" else card.story.emoji, fontSize = 34.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(card.story.titleAr, style = MaterialTheme.typography.titleLarge, color = Color.White)
                EnglishText(card.story.titleEn, style = EnglishTextStyles.chip.copy(fontSize = 15.sp), color = Color.White.copy(alpha = 0.85f))
                Text(card.story.scenarioAr, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f), maxLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) { i ->
                        Text("★", fontSize = 20.sp, color = if (i < card.progress.bestStars) Saffron else Color.White.copy(alpha = 0.35f))
                    }
                }
            }
        }
    }
}
