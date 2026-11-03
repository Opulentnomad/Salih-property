package com.rihla.english.ui.lesson

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.engine.UserAnswer
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.FlashcardAudio
import com.rihla.english.core.model.ListeningChallenge
import com.rihla.english.core.model.MatchPairs
import com.rihla.english.core.model.MultipleChoice
import com.rihla.english.core.model.ShopItem
import com.rihla.english.core.model.SpeakingChallenge
import com.rihla.english.core.model.WordBankSentence
import com.rihla.english.ui.asText
import com.rihla.english.ui.components.ArabicText
import com.rihla.english.ui.components.GrammarBridgeCard
import com.rihla.english.ui.components.LocalSpeechPlayer
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.components.RihlaOutlinedButton
import com.rihla.english.ui.lesson.exercises.FlashcardView
import com.rihla.english.ui.lesson.exercises.ListeningView
import com.rihla.english.ui.lesson.exercises.MatchPairsView
import com.rihla.english.ui.lesson.exercises.MultipleChoiceView
import com.rihla.english.ui.lesson.exercises.SpeakingView
import com.rihla.english.ui.lesson.exercises.WordBankView
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.BlueDark
import com.rihla.english.ui.theme.Green
import com.rihla.english.ui.theme.Orange
import com.rihla.english.ui.theme.Purple
import com.rihla.english.ui.theme.Red
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.ui.victory.LessonVictoryScreen
import com.rihla.english.util.formatCountdown

/** The lesson "arena": progress bar, polymorphic exercise view, check button and feedback sheet. */
@Composable
fun LessonArenaScreen(
    viewModel: LessonViewModel,
    onExit: () -> Unit,
    onOpenPractice: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmQuit by remember { mutableStateOf(false) }
    val inProgress = state.phase == LessonPhase.PLAYING || state.phase == LessonPhase.INTRO

    BackHandler(enabled = inProgress) { confirmQuit = true }

    val messageText = state.message?.asText()
    LaunchedEffect(messageText) {
        if (messageText != null) {
            snackbar.showSnackbar(messageText)
            viewModel.messageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // insets are applied per section
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.phase) {
                LessonPhase.LOADING, LessonPhase.SAVING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
                LessonPhase.ERROR -> CenteredMessage(stringResource(R.string.lesson_error), onExit)
                LessonPhase.INTRO -> LessonIntro(state, onStart = viewModel::startFromIntro, onClose = { confirmQuit = true })
                LessonPhase.PLAYING -> ArenaContent(
                    state = state,
                    onClose = { confirmQuit = true },
                    onSubmit = viewModel::submit,
                    onContinue = viewModel::continueAfterFeedback,
                    onSkipSpeaking = viewModel::skipSpeaking,
                )
                LessonPhase.OUT_OF_HEARTS -> OutOfHeartsContent(
                    state = state,
                    onRefill = viewModel::refillHeartsWithGems,
                    onPractice = onOpenPractice,
                    onResume = viewModel::resumeWithRegeneratedHearts,
                    onQuit = onExit,
                )
                LessonPhase.FINISHED -> state.victory?.let { LessonVictoryScreen(it, onContinue = onExit) }
            }
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text(stringResource(R.string.quit_title)) },
            text = { Text(stringResource(R.string.quit_body)) },
            confirmButton = {
                TextButton(onClick = { confirmQuit = false; onExit() }) {
                    Text(stringResource(R.string.quit_confirm), color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmQuit = false }) { Text(stringResource(R.string.quit_cancel), color = Blue) }
            },
        )
    }
}

@Composable
private fun ArenaContent(
    state: LessonUiState,
    onClose: () -> Unit,
    onSubmit: (UserAnswer) -> Unit,
    onContinue: () -> Unit,
    onSkipSpeaking: () -> Unit,
) {
    val exercise = state.exercise ?: return
    val haptics = LocalHapticFeedback.current
    val player = LocalSpeechPlayer.current
    val locked = state.feedback != null
    var pending by remember(state.presentation) {
        mutableStateOf<UserAnswer?>(if (exercise is FlashcardAudio) UserAnswer.Acknowledged else null)
    }

    LaunchedEffect(state.feedback) {
        val fb = state.feedback ?: return@LaunchedEffect
        if (!fb.correct) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        // Reinforce the correct English sentence aloud after building it.
        if (fb.correct && exercise is WordBankSentence) player.speak(exercise.answerSentence)
    }

    Column(Modifier.fillMaxSize()) {
        LessonTopBar(state, onClose)
        AnimatedVisibility(state.isRetry) {
            Text(
                stringResource(R.string.retry_badge),
                style = MaterialTheme.typography.labelLarge,
                color = Orange,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            key(state.presentation) {
                ExerciseHost(
                    exercise = exercise,
                    presentation = state.presentation,
                    enabled = !locked,
                    onAnswerChanged = { pending = it },
                    onSubmitNow = onSubmit,
                    onSkipSpeaking = onSkipSpeaking,
                )
            }
        }

        BottomActionArea(
            exercise = exercise,
            feedback = state.feedback,
            canSubmit = pending != null,
            onCheck = { pending?.let(onSubmit) },
            onContinue = onContinue,
        )
    }
}

/** "Check" button, replaced by the sliding feedback sheet once an answer is graded. */
@Composable
private fun BottomActionArea(
    exercise: Exercise,
    feedback: AnswerFeedback?,
    canSubmit: Boolean,
    onCheck: () -> Unit,
    onContinue: () -> Unit,
) {
    // Keep the last feedback so the sheet can animate out after it is cleared.
    var shown by remember { mutableStateOf(feedback) }
    if (feedback != null) shown = feedback
    Box(Modifier.fillMaxWidth()) {
        val autoSubmits = exercise is MatchPairs || exercise is SpeakingChallenge
        if (feedback == null && !autoSubmits) {
            Column(Modifier.fillMaxWidth()) {
                HorizontalDivider(thickness = 2.dp, color = RihlaTheme.colors.border)
                RihlaButton(
                    text = stringResource(if (exercise is FlashcardAudio) R.string.continue_label else R.string.check),
                    onClick = onCheck,
                    enabled = canSubmit,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }
        }
        AnimatedVisibility(
            visible = feedback != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            shown?.let { FeedbackSheet(it, onContinue) }
        }
    }
}

/** Polymorphic dispatcher: one composable per exercise type. */
@Composable
private fun ExerciseHost(
    exercise: Exercise,
    presentation: Int,
    enabled: Boolean,
    onAnswerChanged: (UserAnswer?) -> Unit,
    onSubmitNow: (UserAnswer) -> Unit,
    onSkipSpeaking: () -> Unit,
) {
    when (exercise) {
        is FlashcardAudio -> FlashcardView(exercise)
        is MultipleChoice -> MultipleChoiceView(exercise, enabled, onAnswerChanged)
        is WordBankSentence -> WordBankView(exercise, presentation, enabled, onAnswerChanged)
        is MatchPairs -> MatchPairsView(exercise, presentation, enabled, onSubmitNow)
        is ListeningChallenge -> ListeningView(exercise, presentation, enabled, onAnswerChanged)
        is SpeakingChallenge -> SpeakingView(exercise, enabled, onSubmitNow, onSkipSpeaking)
    }
}

@Composable
private fun LessonTopBar(state: LessonUiState, onClose: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Text("✕", fontSize = 24.sp, color = RihlaTheme.colors.textSecondary)
        }
        AnimatedProgressBar(state.progress, Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        if (state.combo >= 3) {
            Text(stringResource(R.string.combo_badge, state.combo), style = MaterialTheme.typography.titleMedium, color = Orange)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            if (state.isPractice) "❤️ ∞" else "❤️ ${state.hearts}",
            style = MaterialTheme.typography.titleMedium,
            color = Red,
        )
    }
}

/** Rounded progress bar with a springy fill and a glossy highlight. Fills from the right in RTL. */
@Composable
fun AnimatedProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "progress",
    )
    val track = RihlaTheme.colors.border
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(modifier.height(18.dp)) {
        val radius = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(color = track, cornerRadius = radius)
        val fillWidth = size.width * animated
        if (fillWidth > 0f) {
            val left = if (rtl) size.width - fillWidth else 0f
            drawRoundRect(color = Green, topLeft = Offset(left, 0f), size = Size(fillWidth, size.height), cornerRadius = radius)
            val inset = size.height * 0.25f
            if (fillWidth > inset * 4) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(left + inset * 1.5f, inset * 0.8f),
                    size = Size(fillWidth - inset * 3, size.height * 0.22f),
                    cornerRadius = CornerRadius(inset, inset),
                )
            }
        }
    }
}

@Composable
private fun LessonIntro(state: LessonUiState, onStart: () -> Unit, onClose: () -> Unit) {
    val colors = RihlaTheme.colors
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.statusBarsPadding().padding(12.dp)) {
            IconButton(onClick = onClose) { Text("✕", fontSize = 24.sp, color = colors.textSecondary) }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                if (state.isPractice) stringResource(R.string.practice_title) else state.titleAr,
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
            Text(stringResource(R.string.intro_title), style = MaterialTheme.typography.titleLarge, color = Purple)
            val tip = if (state.isPractice) stringResource(R.string.practice_intro) else state.introTipAr
            tip?.let {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.selectedBackground)
                        .padding(16.dp),
                ) { ArabicText(it, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary) }
            }
            state.grammarBridge?.let { GrammarBridgeCard(it) }
        }
        RihlaButton(
            text = stringResource(R.string.intro_start),
            onClick = onStart,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(20.dp),
        )
    }
}

@Composable
private fun OutOfHeartsContent(
    state: LessonUiState,
    onRefill: () -> Unit,
    onPractice: () -> Unit,
    onResume: () -> Unit,
    onQuit: () -> Unit,
) {
    val colors = RihlaTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("💔", fontSize = 88.sp)
        Text(stringResource(R.string.out_of_hearts_title), style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)
        Text(
            stringResource(R.string.out_of_hearts_body),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        if (state.hearts > 0) {
            RihlaButton(text = stringResource(R.string.continue_label) + " (❤️ ${state.hearts})", onClick = onResume)
        }
        RihlaButton(
            text = stringResource(R.string.hearts_refill, ShopItem.HEART_REFILL.priceGems) + "  •  💎 ${state.gems}",
            onClick = onRefill,
            color = Blue,
            shadowColor = BlueDark,
        )
        RihlaOutlinedButton(text = "💪 " + stringResource(R.string.hearts_practice), onClick = onPractice)
        state.nextHeartMillis?.let {
            Text(
                stringResource(R.string.out_of_hearts_wait, formatCountdown(it)),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary,
            )
        }
        TextButton(onClick = onQuit) {
            Text(stringResource(R.string.out_of_hearts_quit), color = colors.textSecondary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CenteredMessage(text: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🐪", fontSize = 72.sp)
        Text(text, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        RihlaButton(text = stringResource(R.string.close), onClick = onBack)
    }
}
