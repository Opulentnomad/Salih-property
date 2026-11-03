package com.rihla.english.ui.story

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.audio.RecognitionEvent
import com.rihla.english.core.engine.AnswerMatcher
import com.rihla.english.core.story.ReplyQuality
import com.rihla.english.core.story.Story
import com.rihla.english.core.story.StoryOption
import com.rihla.english.core.story.StoryState
import com.rihla.english.core.story.StoryStep
import com.rihla.english.core.story.TranscriptItem
import com.rihla.english.ui.components.ArabicText
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.LocalSpeechRecognizer
import com.rihla.english.ui.components.MiniSpeaker
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.components.RihlaOutlinedButton
import com.rihla.english.ui.lesson.AnimatedProgressBar
import com.rihla.english.ui.lesson.exercises.ChoiceCard
import com.rihla.english.ui.theme.Coral
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.Night
import com.rihla.english.ui.theme.Oasis
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.ui.theme.Saffron
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Chat-style story player: characters' lines appear as speech bubbles (read aloud by TTS), and at
 * each choice point the learner taps — or says — their English reply and gets instant coaching.
 */
@Composable
fun StoryScreen(viewModel: StoryViewModel, onExit: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val play = state.play
    if (play == null) {
        LaunchedEffect(Unit) { onExit() }
        return
    }
    val player = LocalSpeechPlayer.current
    val listState = rememberLazyListState()

    // Read each new spoken line aloud and keep the latest bubble in view.
    LaunchedEffect(play.transcript.size) {
        val last = play.transcript.lastOrNull()
        if (last is TranscriptItem.Spoken) player.speak(last.line.english)
        if (play.transcript.isNotEmpty()) listState.animateScrollToItem(play.transcript.size)
    }
    // Auto-advance spoken lines, pacing them to the length of the previous line.
    LaunchedEffect(play.stepIndex, play.transcript.size) {
        if (play.currentStep is StoryStep.Line) {
            val previous = (play.transcript.lastOrNull() as? TranscriptItem.Spoken)?.line?.english.orEmpty()
            delay(if (play.transcript.isEmpty()) 600L else 900L + previous.length * 55L)
            viewModel.advance()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onExit) { Text("✕", fontSize = 24.sp, color = RihlaTheme.colors.textSecondary) }
            Text("${play.story.emoji} ${play.story.titleAr}", style = MaterialTheme.typography.titleMedium, color = RihlaTheme.colors.textPrimary)
            Spacer(Modifier.width(12.dp))
            AnimatedProgressBar(play.progress, Modifier.weight(1f))
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    play.story.scenarioAr,
                    style = MaterialTheme.typography.bodyLarge,
                    color = RihlaTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RihlaTheme.colors.cardBackground)
                        .padding(14.dp),
                )
            }
            itemsIndexed(play.transcript) { _, item -> Bubble(play.story, item) }
            if (play.currentStep is StoryStep.Line && play.transcript.isNotEmpty()) {
                item { TypingIndicator(onTap = viewModel::advance) }
            }
        }

        val reward = state.reward
        when {
            reward != null -> StoryRewardPanel(play, reward, onReplay = viewModel::restart, onDone = onExit)
            play.currentStep is StoryStep.Choice -> ChoicePanel(play, onChoose = viewModel::choose)
        }
    }
}

@Composable
private fun Bubble(story: Story, item: TranscriptItem) {
    val colors = RihlaTheme.colors
    var showArabic by remember { mutableStateOf(false) }
    val mine = item is TranscriptItem.MyReply
    val english = when (item) {
        is TranscriptItem.Spoken -> item.line.english
        is TranscriptItem.MyReply -> item.option.english
    }
    val arabic = when (item) {
        is TranscriptItem.Spoken -> item.line.arabic
        is TranscriptItem.MyReply -> item.option.arabic
    }
    val speaker = (item as? TranscriptItem.Spoken)?.let { story.characters[it.line.speakerId] }
    val quality = (item as? TranscriptItem.MyReply)?.option?.quality

    // In the RTL layout, the learner's replies sit on the start (right) side, others on the end.
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!mine) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(speaker?.emoji ?: "🙂", fontSize = 30.sp)
                Text(speaker?.nameAr.orEmpty(), style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), color = colors.textSecondary)
            }
            Spacer(Modifier.width(6.dp))
        }
        Column(
            Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    when {
                        !mine -> colors.cardBackground
                        quality == ReplyQuality.BAD -> colors.wrongBackground
                        else -> colors.correctBackground
                    },
                )
                .clickable { showArabic = !showArabic }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EnglishText(english, style = EnglishTextStyles.chip, color = colors.textPrimary, modifier = Modifier.weight(1f, fill = false))
                if (!mine) {
                    Spacer(Modifier.width(8.dp))
                    MiniSpeaker(english)
                }
            }
            AnimatedVisibility(showArabic) {
                ArabicText(arabic, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun TypingIndicator(onTap: () -> Unit) {
    var dots by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(350)
            dots = dots % 3 + 1
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(
            "•".repeat(dots) + "  " + stringResource(R.string.story_tap_continue),
            style = MaterialTheme.typography.labelMedium,
            color = RihlaTheme.colors.textSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onTap)
                .padding(8.dp),
        )
    }
}

@Composable
private fun ChoicePanel(play: StoryState, onChoose: (Int) -> Unit) {
    val step = play.currentStep as? StoryStep.Choice ?: return
    val colors = RihlaTheme.colors
    val context = LocalContext.current
    val recognizer = LocalSpeechRecognizer.current
    val scope = rememberCoroutineScope()
    var listening by remember { mutableStateOf(false) }
    var noMatch by remember(play.stepIndex, play.transcript.size) { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }

    fun listen() {
        val manager = recognizer ?: return
        job?.cancel()
        listening = true
        noMatch = false
        job = scope.launch {
            manager.listen().collect { event ->
                when (event) {
                    is RecognitionEvent.Results -> {
                        listening = false
                        val best = step.options.indices.maxByOrNull { i ->
                            AnswerMatcher.pronunciationScore(event.candidates, listOf(step.options[i].english))
                        }
                        val score = best?.let { AnswerMatcher.pronunciationScore(event.candidates, listOf(step.options[it].english)) } ?: 0.0
                        if (best != null && score >= 0.6) onChoose(best) else noMatch = true
                    }
                    is RecognitionEvent.Error -> { listening = false; noMatch = true }
                    else -> Unit
                }
            }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) listen() }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(colors.cardBackground)
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        play.lastFeedback?.let { FeedbackStrip(it) }
        Text(stringResource(R.string.story_your_turn) + "  " + step.promptAr, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        step.options.forEachIndexed { i, option ->
            ChoiceCard(number = i + 1, selected = false, enabled = !listening, onClick = { onChoose(i) }) {
                Column {
                    EnglishText(option.english, style = EnglishTextStyles.chip, color = colors.textPrimary)
                    Text(option.arabic, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }
        }
        if (recognizer?.isAvailable() == true) {
            TextButton(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (granted) listen() else permission.launch(Manifest.permission.RECORD_AUDIO)
                },
                enabled = !listening,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (listening) stringResource(R.string.speak_listening) else stringResource(R.string.story_speak_reply),
                    style = MaterialTheme.typography.labelLarge,
                    color = Oasis,
                )
            }
        }
        if (noMatch) Text(stringResource(R.string.story_no_match), style = MaterialTheme.typography.bodyMedium, color = colors.wrongText)
    }
}

@Composable
private fun FeedbackStrip(option: StoryOption) {
    val (color, label) = when (option.quality) {
        ReplyQuality.GOOD -> Oasis to R.string.story_good
        ReplyQuality.OK -> Saffron to R.string.story_ok
        ReplyQuality.BAD -> Coral to R.string.story_bad
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(label), style = MaterialTheme.typography.titleMedium, color = color)
        ArabicText(option.feedbackAr, style = MaterialTheme.typography.bodyMedium, color = RihlaTheme.colors.textPrimary)
    }
}

@Composable
private fun StoryRewardPanel(play: StoryState, reward: StoryReward, onReplay: () -> Unit, onDone: () -> Unit) {
    val starScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) { starScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
    AnimatedVisibility(true, enter = slideInVertically { it } + fadeIn()) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(Night, Color(0xFF3B3F7A))))
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.story_finished), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Row(Modifier.graphicsLayer { scaleX = starScale.value; scaleY = starScale.value }) {
                repeat(3) { i -> Text("★", fontSize = 48.sp, color = if (i < reward.stars) Saffron else Color.White.copy(alpha = 0.25f)) }
            }
            Text(play.story.endingAr, style = MaterialTheme.typography.bodyLarge, color = Color.White, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.story_xp, reward.xp), style = MaterialTheme.typography.titleLarge, color = Saffron)
                Text("💎 +${reward.gems}", style = MaterialTheme.typography.titleLarge, color = Color.White)
                if (reward.streakExtended) Text("🔥 +1", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            reward.badges.forEach { badge ->
                Text("${badge.emoji} ${badge.titleAr}", style = MaterialTheme.typography.titleMedium, color = Saffron)
            }
            RihlaButton(text = stringResource(R.string.continue_label), onClick = onDone)
            Box(Modifier.size(4.dp))
            RihlaOutlinedButton(text = stringResource(R.string.story_replay), onClick = onReplay)
        }
    }
}
