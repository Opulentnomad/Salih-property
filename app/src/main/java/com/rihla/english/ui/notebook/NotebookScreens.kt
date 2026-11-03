package com.rihla.english.ui.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.journey.NotebookEntry
import com.rihla.english.ui.components.AudioPills
import com.rihla.english.ui.components.CineButton
import com.rihla.english.ui.components.CineIcon
import com.rihla.english.ui.components.CineIconView
import com.rihla.english.ui.components.CineOutlinedButton
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.Ltr
import com.rihla.english.ui.journey.JourneyViewModel
import com.rihla.english.ui.theme.Cine
import com.rihla.english.ui.theme.EnglishTextStyles

private enum class Filter(val label: Int) { ALL(R.string.filter_all), DUE(R.string.filter_due), KNOWN(R.string.filter_known) }

/** "دفتري": every phrase met on the journey, in its sentence, with spaced review. */
@Composable
fun NotebookScreen(viewModel: JourneyViewModel, onStartReview: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(Filter.ALL) }
    val entries = state.progress.notebook.values
        .filter {
            when (filter) {
                Filter.ALL -> true
                Filter.DUE -> it.isDue(state.today)
                Filter.KNOWN -> it.mastered
            }
        }
        .sortedByDescending { it.savedDay }

    LazyColumn(
        Modifier.fillMaxSize().background(Cine.Ink).statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.notebook_title), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.notebook_count, state.progress.notebook.size), style = MaterialTheme.typography.bodyMedium, color = Cine.Muted)
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (state.dueCount > 0) Cine.Amber else Cine.Panel)
                    .clickable(enabled = state.dueCount > 0, role = Role.Button, onClick = onStartReview)
                    .padding(18.dp),
            ) {
                Text(
                    if (state.dueCount > 0) stringResource(R.string.review_cta, minOf(state.dueCount, ReviewViewModel.SESSION_SIZE))
                    else stringResource(R.string.review_cta_none),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.dueCount > 0) Cine.Ink else Cine.Text,
                )
                Text(stringResource(R.string.review_sub), style = MaterialTheme.typography.labelMedium, color = if (state.dueCount > 0) Cine.Ink.copy(alpha = 0.75f) else Cine.Muted)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(stringResource(f.label)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cine.AmberTint,
                            selectedLabelColor = Cine.AmberSoft,
                            labelColor = Cine.TextSoft,
                        ),
                    )
                }
            }
        }
        if (entries.isEmpty()) {
            item { Text(stringResource(R.string.notebook_empty), style = MaterialTheme.typography.bodyMedium, color = Cine.Muted, modifier = Modifier.padding(top = 24.dp)) }
        }
        items(entries, key = { it.chunkId }) { EntryCard(it, state.today) }
    }
}

@Composable
private fun EntryCard(e: NotebookEntry, today: Long) {
    val player = LocalSpeechPlayer.current
    val (tag, tagColor, tagBg) = when {
        e.mastered -> Triple(R.string.tag_known, Cine.BlueSoft, Cine.BlueTint)
        e.reviews == 0 -> Triple(R.string.tag_new, Cine.TextSoft, Cine.PanelBorder)
        e.isDue(today) -> Triple(R.string.tag_due, Cine.AmberSoft, Cine.AmberTint)
        else -> Triple(R.string.tag_new, Cine.TextSoft, Cine.PanelBorder)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Cine.Panel)
            .border(1.dp, Cine.PanelBorder, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button) { player.speak(e.context) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EnglishText(e.english, style = EnglishTextStyles.option.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize), color = Cine.Text, modifier = Modifier.weight(1f))
            Text(stringResource(tag), style = MaterialTheme.typography.labelMedium, color = tagColor, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(tagBg).padding(horizontal = 8.dp, vertical = 3.dp))
        }
        Text(e.arabic, style = MaterialTheme.typography.bodyMedium, color = Cine.Muted)
        EnglishText("“${e.context}”", style = EnglishTextStyles.small, color = Cine.Muted)
        Text(stringResource(R.string.from_source, e.sourceAr), style = MaterialTheme.typography.labelMedium, color = Cine.Faint)
    }
}

/** Review session: the phrase in its sentence → recall the meaning → self-grade (SM-2). */
@Composable
fun ReviewScreen(viewModel: ReviewViewModel, onExit: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player = LocalSpeechPlayer.current
    val current = state.current
    LaunchedEffect(state.index, state.loading) { current?.let { player.speak(it.context) } }

    Column(
        Modifier.fillMaxSize().background(Cine.Ink).statusBarsPadding().navigationBarsPadding().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val close = stringResource(R.string.close)
            IconButton(onClick = onExit, modifier = Modifier.semantics { contentDescription = close }) {
                CineIconView(CineIcon.Close, Cine.Text, Modifier.size(22.dp))
            }
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { if (state.queue.isEmpty()) 1f else state.index.toFloat() / state.queue.size },
                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Cine.Amber,
                trackColor = Cine.Panel,
            )
            if (current != null) {
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.review_progress, state.index + 1, state.queue.size), style = MaterialTheme.typography.labelMedium, color = Cine.Muted)
            }
        }
        when {
            state.loading -> Unit
            state.finished -> {
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(72.dp).clip(CircleShape).background(Cine.BlueTint).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                    CineIconView(CineIcon.Check, Cine.BlueSoft, Modifier.size(36.dp))
                }
                Text(stringResource(R.string.review_done_title), style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.review_done_body, state.queue.size), style = MaterialTheme.typography.bodyLarge, color = Cine.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f))
                CineButton(stringResource(R.string.back_to_journey), onClick = onExit)
            }
            current != null -> {
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.review_question), style = MaterialTheme.typography.titleMedium, color = Cine.Muted)
                val annotated = buildAnnotatedString {
                    val ctx = current.context
                    val start = ctx.indexOf(current.english).coerceAtLeast(0)
                    if (ctx.contains(current.english)) {
                        append(ctx.substring(0, start))
                        withStyle(SpanStyle(color = Cine.Amber, textDecoration = TextDecoration.Underline)) { append(current.english) }
                        append(ctx.substring(start + current.english.length))
                    } else append(current.english)
                }
                Ltr { Text(annotated, style = EnglishTextStyles.hero.copy(color = Cine.Text, textDirection = TextDirection.Ltr), modifier = Modifier.fillMaxWidth()) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { AudioPills(current.context) }
                if (state.revealed) {
                    Text(current.arabic, style = MaterialTheme.typography.headlineMedium, color = Cine.AmberSoft)
                    Text(stringResource(R.string.from_source, current.sourceAr), style = MaterialTheme.typography.labelMedium, color = Cine.Faint)
                }
                Spacer(Modifier.weight(1f))
                if (!state.revealed) {
                    CineButton(stringResource(R.string.review_reveal), onClick = viewModel::reveal)
                } else {
                    CineButton(stringResource(R.string.review_knew), onClick = { viewModel.grade(5) }, color = Cine.Blue)
                    CineOutlinedButton(stringResource(R.string.review_almost), onClick = { viewModel.grade(3) })
                    CineOutlinedButton(stringResource(R.string.review_forgot), onClick = { viewModel.grade(1) }, contentColor = Cine.AmberSoft)
                }
            }
        }
    }
}

