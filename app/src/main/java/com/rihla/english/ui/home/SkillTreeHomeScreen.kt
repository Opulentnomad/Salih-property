package com.rihla.english.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.engine.NodeState
import com.rihla.english.core.model.NodeType
import com.rihla.english.core.model.ShopItem
import com.rihla.english.core.model.UserStats
import com.rihla.english.ui.asText
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.components.TopGamificationBar
import com.rihla.english.ui.gamification.ChestRewardUi
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.gamification.HomeUiState
import com.rihla.english.ui.gamification.NodeUi
import com.rihla.english.ui.gamification.TimerState
import com.rihla.english.ui.gamification.UnitUi
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.BlueDark
import com.rihla.english.ui.theme.EnglishTextStyles
import com.rihla.english.ui.theme.Gold
import com.rihla.english.ui.theme.Green
import com.rihla.english.ui.theme.Orange
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.util.formatCountdown
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private val NodeRowHeight = 104.dp
private val NodeSize = 76.dp
private val PathAmplitude = 72.dp

/** Horizontal offset of the n-th node on the snake path (a sine wave). */
private fun pathOffset(index: Int): Dp = PathAmplitude * sin(index * PI / 4).toFloat()

private enum class StatDialog { STREAK, GEMS, HEARTS }

/** Duolingo-style winding path of lessons and treasure chests, grouped by unit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillTreeHomeScreen(
    viewModel: GamificationViewModel,
    onStartLesson: (String) -> Unit,
    onPractice: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val chest by viewModel.chestReward.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var selectedNodeId by rememberSaveable { mutableStateOf<String?>(null) }
    var statDialog by remember { mutableStateOf<StatDialog?>(null) }
    var pendingMessage by remember { mutableStateOf<com.rihla.english.ui.UiMessage?>(null) }
    val lockedText = stringResource(R.string.lesson_locked)

    LaunchedEffect(viewModel) { viewModel.messages.collect { pendingMessage = it } }
    val messageText = pendingMessage?.asText()
    LaunchedEffect(messageText) {
        if (messageText != null) {
            snackbar.showSnackbar(messageText)
            pendingMessage = null
        }
    }

    // Flatten units into list rows so we can scroll to the current lesson.
    val rows = remember(state.units) { buildRows(state.units) }
    Scaffold(
        topBar = {
            TopGamificationBar(
                stats = state.stats,
                doubleXpRemainingMillis = timers.doubleXpRemainingMillis,
                onStreakClick = { statDialog = StatDialog.STREAK },
                onGemsClick = { statDialog = StatDialog.GEMS },
                onHeartsClick = { statDialog = StatDialog.HEARTS },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPractice,
                containerColor = Blue,
                contentColor = Color.White,
                text = {
                    Text(
                        if (state.mistakeCount > 0) stringResource(R.string.practice_mistakes_count, state.mistakeCount)
                        else stringResource(R.string.practice_mistakes),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                icon = { Text("💪", fontSize = 20.sp) },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green)
            }
            return@Scaffold
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item(key = "hero") { HeroCard(state, Modifier.padding(top = 12.dp)) }
            if (state.season != null) {
                item(key = "season") { SeasonBanner(state, Modifier.padding(top = 14.dp)) }
            }
            item(key = "plan") { DailyPlanCard(state, onClaim = viewModel::claimDailyBonus, modifier = Modifier.padding(top = 14.dp)) }
            val next = state.units.flatMap { u -> u.nodes.map { u to it } }.firstOrNull { it.second.node.id == state.currentLessonId }
            if (next != null) {
                item(key = "continue") {
                    ContinueCard(
                        node = next.second.node,
                        unitColor = Color(next.first.unit.colorHex),
                        onStart = { onStartLesson(next.second.node.id) },
                        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
                    )
                }
            }
            itemsIndexed(rows, key = { _, row -> row.key }) { _, row ->
                when (row) {
                    is HomeRow.Header -> UnitHeader(row.unit)
                    is HomeRow.Node -> PathNodeRow(
                        row = row,
                        onClick = {
                            when {
                                row.node.node.type == NodeType.CHEST -> viewModel.openChest(row.node.node.id)
                                row.node.state == NodeState.LOCKED -> scope.launch {
                                    snackbar.showSnackbar(lockedText)
                                }
                                else -> selectedNodeId = row.node.node.id
                            }
                        },
                    )
                }
            }
            if (state.courseCompleted) {
                item(key = "done") {
                    Text(
                        stringResource(R.string.course_complete),
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    )
                }
            }
        }
    }

    val selected = state.units.flatMap { it.nodes }.firstOrNull { it.node.id == selectedNodeId }
    if (selected != null) {
        ModalBottomSheet(onDismissRequest = { selectedNodeId = null }) {
            LessonStartSheet(
                node = selected,
                onStart = {
                    selectedNodeId = null
                    onStartLesson(selected.node.id)
                },
            )
        }
    }

    chest?.let { reward -> ChestRewardDialog(reward, onDismiss = viewModel::dismissChest) }

    statDialog?.let { dialog ->
        StatInfoDialog(
            dialog = dialog,
            stats = state.stats,
            timers = timers,
            onDismiss = { statDialog = null },
            onPractice = { statDialog = null; onPractice() },
            onRefill = { statDialog = null; viewModel.purchase(ShopItem.HEART_REFILL) },
        )
    }
}

// --------------------------------------------------------------------------- rows

private sealed interface HomeRow {
    val key: String

    data class Header(val unit: UnitUi) : HomeRow {
        override val key get() = "h_${unit.unit.id}"
    }

    data class Node(
        val node: NodeUi,
        val color: Color,
        val offset: Dp,
        val nextOffset: Dp?,
        val nextState: NodeState?,
    ) : HomeRow {
        override val key get() = node.node.id
    }
}

private fun buildRows(units: List<UnitUi>): List<HomeRow> = buildList {
    units.forEach { unit ->
        add(HomeRow.Header(unit))
        val color = Color(unit.unit.colorHex)
        unit.nodes.forEachIndexed { i, node ->
            val next = unit.nodes.getOrNull(i + 1)
            add(
                HomeRow.Node(
                    node = node,
                    color = color,
                    offset = pathOffset(i),
                    nextOffset = next?.let { pathOffset(i + 1) },
                    nextState = next?.state,
                ),
            )
        }
    }
}

// --------------------------------------------------------------------------- pieces

/** Each unit is a "destination" on the journey, with its own landmark emoji. */
private val unitLandmarks = mapOf("u1" to "🔤", "u2" to "👋", "u3" to "🧱", "u4" to "☕", "u5" to "🪤", "u6" to "🌍")

@Composable
private fun UnitHeader(unit: UnitUi) {
    val color = Color(unit.unit.colorHex)
    val done = unit.nodes.count { it.state == NodeState.COMPLETED }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(color, lerp(color, Color.Black, 0.35f))))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(unit.unit.titleAr, style = MaterialTheme.typography.titleLarge, color = Color.White)
            EnglishText(unit.unit.titleEn, style = EnglishTextStyles.chip, color = Color.White.copy(alpha = 0.9f))
            Text(unit.unit.descriptionAr, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f))
            LinearProgressIndicator(
                progress = { done.toFloat() / unit.nodes.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Gold,
                trackColor = Color.White.copy(alpha = 0.25f),
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) { Text(unitLandmarks[unit.unit.id] ?: "📍", fontSize = 32.sp) }
    }
}

/**
 * One node on the path. Each row also draws the dashed connector down to the next node, using
 * absolute (non-mirrored) coordinates so the curve matches the nodes in RTL too.
 */
@Composable
private fun PathNodeRow(row: HomeRow.Node, onClick: () -> Unit) {
    val colors = RihlaTheme.colors
    val connectorDone = colors.border
    Box(
        Modifier
            .fillMaxWidth()
            .height(NodeRowHeight)
            .drawBehind {
                val next = row.nextOffset ?: return@drawBehind
                val start = Offset(size.width / 2 + row.offset.toPx(), size.height / 2)
                val end = Offset(size.width / 2 + next.toPx(), size.height * 1.5f)
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(start.x, start.y + size.height * 0.5f, end.x, end.y - size.height * 0.5f, end.x, end.y)
                }
                val active = row.node.state == NodeState.COMPLETED && row.nextState != NodeState.LOCKED
                drawPath(
                    path = path,
                    color = if (active) row.color.copy(alpha = 0.6f) else connectorDone,
                    style = Stroke(
                        width = 10.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 26.dp.toPx())),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.absoluteOffset(x = row.offset), contentAlignment = Alignment.Center) {
            when (row.node.node.type) {
                NodeType.LESSON -> LessonNodeButton(row.node, row.color, onClick)
                NodeType.CHEST -> ChestNodeButton(row.node, onClick)
            }
        }
    }
}

@Composable
private fun LessonNodeButton(node: NodeUi, unitColor: Color, onClick: () -> Unit) {
    val colors = RihlaTheme.colors
    val pulse = rememberInfiniteTransition(label = "pulse")
    val ring by pulse.animateFloat(1f, 1.18f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "ring")
    val bob by pulse.animateFloat(0f, -6f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "bob")
    val (face, shadow) = when (node.state) {
        NodeState.LOCKED -> colors.locked to colors.lockedShadow
        NodeState.AVAILABLE, NodeState.COMPLETED -> unitColor to lerp(unitColor, Color.Black, 0.22f)
    }

    Box(contentAlignment = Alignment.Center) {
        if (node.isCurrent) {
            Box(
                Modifier
                    .size(NodeSize + 14.dp)
                    .graphicsLayer { scaleX = ring; scaleY = ring; alpha = 0.3f }
                    .clip(CircleShape)
                    .background(unitColor),
            )
        }
        PressableSurface(
            onClick = onClick,
            modifier = Modifier.width(NodeSize),
            color = face,
            shadowColor = shadow,
            shape = CircleShape,
            depth = 6.dp,
        ) {
            Box(Modifier.size(NodeSize - 6.dp), contentAlignment = Alignment.Center) {
                Text(
                    when (node.state) {
                        NodeState.LOCKED -> "🔒"
                        else -> node.node.iconEmoji
                    },
                    fontSize = 30.sp,
                )
            }
        }
        if (node.state == NodeState.COMPLETED) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Gold),
                contentAlignment = Alignment.Center,
            ) { Text("✓", color = Color.White, fontSize = 15.sp) }
        }
        if (node.isCurrent) {
            Text(
                stringResource(R.string.start),
                style = MaterialTheme.typography.labelLarge,
                color = unitColor,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .absoluteOffset(y = (-34).dp)
                    .graphicsLayer { translationY = bob }
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(2.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ChestNodeButton(node: NodeUi, onClick: () -> Unit) {
    val bounce = rememberInfiniteTransition(label = "chest")
    val dy by bounce.animateFloat(0f, -10f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "dy")
    val tilt by bounce.animateFloat(-6f, 6f, infiniteRepeatable(tween(250), RepeatMode.Reverse), label = "tilt")
    val ready = node.state == NodeState.AVAILABLE
    PressableSurface(
        onClick = onClick,
        modifier = Modifier.width(72.dp),
        color = Color.Transparent,
        shadowColor = Color.Transparent,
        depth = 0.dp,
    ) {
        Text(
            when (node.state) {
                NodeState.COMPLETED -> "📭"
                else -> "🎁"
            },
            fontSize = 52.sp,
            modifier = Modifier.graphicsLayer {
                if (ready) {
                    translationY = dy
                    rotationZ = tilt
                }
                alpha = if (node.state == NodeState.LOCKED) 0.4f else 1f
            },
        )
    }
}

@Composable
private fun LessonStartSheet(node: NodeUi, onStart: () -> Unit) {
    val colors = RihlaTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(node.node.iconEmoji, fontSize = 56.sp)
        Text(node.node.titleAr, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary, textAlign = TextAlign.Center)
        EnglishText(node.node.titleEn, style = EnglishTextStyles.medium, color = colors.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        val completed = node.state == NodeState.COMPLETED
        RihlaButton(
            text = if (completed) stringResource(R.string.review_lesson)
            else stringResource(R.string.start_lesson, GamificationEngine.LESSON_COMPLETION_BONUS),
            onClick = onStart,
            color = if (completed) Gold else Green,
            shadowColor = if (completed) com.rihla.english.ui.theme.GoldDark else com.rihla.english.ui.theme.GreenDark,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ChestRewardDialog(reward: ChestRewardUi, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chest_reward_title), style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                com.rihla.english.ui.victory.TreasureChestReveal(gems = reward.gems, autoOpenMillis = 600)
                if (reward.bonusFreeze) {
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.chest_reward_freeze), style = MaterialTheme.typography.titleMedium, color = Blue)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.awesome), color = Green, style = MaterialTheme.typography.labelLarge) }
        },
    )
}

@Composable
private fun StatInfoDialog(
    dialog: StatDialog,
    stats: UserStats,
    timers: TimerState,
    onDismiss: () -> Unit,
    onPractice: () -> Unit,
    onRefill: () -> Unit,
) {
    val title: String
    val body: String
    when (dialog) {
        StatDialog.STREAK -> {
            title = stringResource(R.string.streak_title)
            body = buildString {
                append(stringResource(R.string.streak_body, stats.dailyGoalXp, stats.bestStreak))
                append("\n\n")
                append(
                    if (stats.dailyGoalReached) stringResource(R.string.streak_today_done)
                    else stringResource(R.string.streak_today_progress, stats.dailyXp, stats.dailyGoalXp),
                )
                append("\n")
                append(stringResource(R.string.streak_freezes, stats.streakFreezes))
            }
        }
        StatDialog.GEMS -> {
            title = stringResource(R.string.gems_title)
            body = stringResource(R.string.gems_body, stats.gems)
        }
        StatDialog.HEARTS -> {
            title = stringResource(R.string.hearts_title)
            body = if (stats.hearts >= UserStats.MAX_HEARTS) stringResource(R.string.hearts_full)
            else stringResource(
                R.string.hearts_body,
                stats.hearts,
                UserStats.MAX_HEARTS,
                formatCountdown(timers.nextHeartMillis ?: GamificationEngine.HEART_REGEN_MILLIS),
            )
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            if (dialog == StatDialog.HEARTS && stats.hearts < UserStats.MAX_HEARTS) {
                TextButton(onClick = onRefill) {
                    Text(stringResource(R.string.hearts_refill, ShopItem.HEART_REFILL.priceGems), color = Blue)
                }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = Blue) }
            }
        },
        dismissButton = {
            if (dialog == StatDialog.HEARTS && stats.hearts < UserStats.MAX_HEARTS) {
                TextButton(onClick = onPractice) { Text(stringResource(R.string.hearts_practice), color = Green) }
            }
        },
    )
}
