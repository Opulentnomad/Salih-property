package com.rihla.english.ui.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rihla.english.R
import com.rihla.english.core.engine.LeagueEngine
import com.rihla.english.core.model.LeagueEntry
import com.rihla.english.core.model.LeagueTier
import com.rihla.english.core.model.ShopItem
import com.rihla.english.core.model.UserStats
import com.rihla.english.ui.UiMessage
import com.rihla.english.ui.asText
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.components.TopGamificationBar
import com.rihla.english.ui.gamification.BadgeUi
import com.rihla.english.ui.gamification.GamificationViewModel
import com.rihla.english.ui.gamification.HomeUiState
import com.rihla.english.ui.gamification.TimerState
import com.rihla.english.ui.theme.Blue
import com.rihla.english.ui.theme.BlueDark
import com.rihla.english.ui.theme.Gold
import com.rihla.english.ui.theme.Green
import com.rihla.english.ui.theme.Orange
import com.rihla.english.ui.theme.Purple
import com.rihla.english.ui.theme.Red
import com.rihla.english.ui.theme.RihlaTheme
import com.rihla.english.util.formatCountdown

/** Gems shop, achievement badges + stats, and the weekly league leaderboard. */
@Composable
fun ShopAndAchievementsScreen(viewModel: GamificationViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf<UiMessage?>(null) }

    LaunchedEffect(viewModel) { viewModel.messages.collect { pending = it } }
    val text = pending?.asText()
    LaunchedEffect(text) {
        if (text != null) {
            snackbar.showSnackbar(text)
            pending = null
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopGamificationBar(
                    stats = state.stats,
                    doubleXpRemainingMillis = timers.doubleXpRemainingMillis,
                    onStreakClick = { tab = 1 },
                    onGemsClick = { tab = 0 },
                    onHeartsClick = { tab = 0 },
                )
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = Blue,
                ) {
                    listOf(R.string.tab_shop, R.string.tab_badges, R.string.tab_league).forEachIndexed { i, res ->
                        Tab(
                            selected = tab == i,
                            onClick = { tab = i },
                            text = { Text(stringResource(res), style = MaterialTheme.typography.labelLarge) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> ShopTab(state.stats, timers, onBuy = viewModel::purchase)
                1 -> BadgesTab(state)
                else -> LeagueTab(state)
            }
        }
    }
}

// ------------------------------------------------------------------------------- Shop

@Composable
private fun ShopTab(stats: UserStats, timers: TimerState, onBuy: (ShopItem) -> Unit) {
    val colors = RihlaTheme.colors
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Blue.copy(alpha = 0.12f))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("💎", fontSize = 40.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.shop_balance), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    Text("${stats.gems}", style = MaterialTheme.typography.headlineLarge, color = Blue)
                }
            }
        }
        itemsIndexed(ShopItem.entries) { _, item ->
            val status = when (item) {
                ShopItem.STREAK_FREEZE -> stringResource(R.string.owned_freezes, stats.streakFreezes, UserStats.MAX_STREAK_FREEZES)
                ShopItem.HEART_REFILL -> stringResource(R.string.owned_hearts, stats.hearts, UserStats.MAX_HEARTS)
                ShopItem.DOUBLE_XP_BOOST -> timers.doubleXpRemainingMillis?.let {
                    stringResource(R.string.boost_remaining, formatCountdown(it))
                }
            }
            ShopItemCard(item, status, affordable = stats.gems >= item.priceGems, onBuy = { onBuy(item) })
        }
    }
}

@Composable
private fun ShopItemCard(item: ShopItem, status: String?, affordable: Boolean, onBuy: () -> Unit) {
    val colors = RihlaTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(item.emoji, fontSize = 44.sp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.titleAr, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(item.descriptionAr, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            status?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = Blue) }
        }
        Spacer(Modifier.width(10.dp))
        PressableSurface(
            onClick = onBuy,
            modifier = Modifier.width(92.dp),
            color = if (affordable) Blue else colors.locked,
            shadowColor = if (affordable) BlueDark else colors.lockedShadow,
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.buy), style = MaterialTheme.typography.labelMedium, color = Color.White)
                Text("💎 ${item.priceGems}", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

// ----------------------------------------------------------------------------- Badges

@Composable
private fun BadgesTab(state: HomeUiState) {
    val colors = RihlaTheme.colors
    val stats = state.stats
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(stringResource(R.string.stats_title), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("⚡", "${stats.totalXp}", stringResource(R.string.stat_total_xp), Gold, Modifier.weight(1f))
                    MiniStat("🔥", "${stats.bestStreak}", stringResource(R.string.stat_best_streak), Orange, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("📘", "${stats.lessonsCompleted}", stringResource(R.string.stat_lessons), Green, Modifier.weight(1f))
                    MiniStat("🔤", "${state.wordsLearned}", stringResource(R.string.stat_words), Purple, Modifier.weight(1f))
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                stringResource(R.string.badges_unlocked, state.badges.count { it.unlocked }, state.badges.size),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(state.badges, key = { it.badge.name }) { BadgeTile(it) }
    }
}

@Composable
private fun MiniStat(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    val colors = RihlaTheme.colors
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        }
    }
}

@Composable
private fun BadgeTile(item: BadgeUi) {
    val colors = RihlaTheme.colors
    Column(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (item.unlocked) Gold.copy(alpha = 0.15f) else colors.cardBackground)
            .border(2.dp, if (item.unlocked) Gold else colors.border, RoundedCornerShape(18.dp))
            .padding(10.dp)
            .alpha(if (item.unlocked) 1f else 0.45f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(if (item.unlocked) item.badge.emoji else "🔒", fontSize = 34.sp)
        Text(
            item.badge.titleAr,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
        )
        Text(
            item.badge.descriptionAr,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 3,
        )
    }
}

// ----------------------------------------------------------------------------- League

@Composable
private fun LeagueTab(state: HomeUiState) {
    val colors = RihlaTheme.colors
    val tier = state.stats.league
    val tierColor = Color(tier.colorHex)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(tierColor.copy(alpha = 0.18f))
                    .border(2.dp, tierColor, RoundedCornerShape(20.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LeagueTier.entries.forEach { t ->
                        Text(t.emoji, fontSize = if (t == tier) 40.sp else 26.sp, modifier = Modifier.alpha(if (t <= tier) 1f else 0.35f))
                    }
                }
                Text(tier.titleAr, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Text(stringResource(R.string.league_ends_in, state.leagueDaysLeft), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            Spacer(Modifier.height(8.dp))
        }
        itemsIndexed(state.leagueEntries) { index, entry ->
            val rank = index + 1
            if (rank == LeagueEngine.PROMOTION_ZONE + 1 && tier != LeagueTier.DIAMOND) {
                ZoneDivider(stringResource(R.string.league_promotion_zone), Green)
            }
            if (rank == LeagueEngine.GROUP_SIZE - LeagueEngine.DEMOTION_ZONE + 1 && tier != LeagueTier.BRONZE) {
                ZoneDivider(stringResource(R.string.league_demotion_zone), Red)
            }
            LeagueRow(rank, entry)
        }
    }
}

@Composable
private fun ZoneDivider(text: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(2.dp).background(color.copy(alpha = 0.5f)))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, modifier = Modifier.padding(horizontal = 8.dp))
        Box(Modifier.weight(1f).height(2.dp).background(color.copy(alpha = 0.5f)))
    }
}

@Composable
private fun LeagueRow(rank: Int, entry: LeagueEntry) {
    val colors = RihlaTheme.colors
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (entry.isCurrentUser) colors.selectedBackground else Color.Transparent)
            .border(
                BorderStroke(2.dp, if (entry.isCurrentUser) colors.selectedBorder else Color.Transparent),
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(36.dp), contentAlignment = Alignment.Center) {
            if (medal != null) Text(medal, fontSize = 22.sp)
            else Text("$rank", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.border),
            contentAlignment = Alignment.Center,
        ) { Text(entry.avatarEmoji, fontSize = 24.sp) }
        Spacer(Modifier.width(12.dp))
        Text(
            if (entry.isCurrentUser) stringResource(R.string.you) else entry.name,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text("${entry.weeklyXp} XP", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
    }
}
