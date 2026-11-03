package com.rihla.english.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.rihla.english.R
import com.rihla.english.RihlaApplication
import com.rihla.english.core.engine.GamificationEngine
import com.rihla.english.core.engine.GamificationEvent
import com.rihla.english.core.engine.LeagueEngine
import com.rihla.english.core.engine.NodeState
import com.rihla.english.core.engine.PurchaseResult
import com.rihla.english.core.engine.SkillPath
import com.rihla.english.core.model.Badge
import com.rihla.english.core.model.CourseUnit
import com.rihla.english.core.model.LeagueEntry
import com.rihla.english.core.model.LeagueOutcome
import com.rihla.english.core.model.NodeType
import com.rihla.english.core.model.PathNode
import com.rihla.english.core.model.ShopItem
import com.rihla.english.core.model.UserStats
import com.rihla.english.core.repository.CurriculumRepository
import com.rihla.english.core.repository.UserStatsRepository
import com.rihla.english.ui.UiMessage
import com.rihla.english.util.AppClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class NodeUi(val node: PathNode, val state: NodeState, val isCurrent: Boolean)
data class UnitUi(val unit: CourseUnit, val nodes: List<NodeUi>)
data class ChestRewardUi(val gems: Int, val bonusFreeze: Boolean)
data class BadgeUi(val badge: Badge, val unlocked: Boolean)

data class HomeUiState(
    val isLoading: Boolean = true,
    val stats: UserStats = UserStats(),
    val units: List<UnitUi> = emptyList(),
    val currentLessonId: String? = null,
    val courseCompleted: Boolean = false,
    val wordsLearned: Int = 0,
    val mistakeCount: Int = 0,
    val badges: List<BadgeUi> = Badge.entries.map { BadgeUi(it, false) },
    val leagueEntries: List<LeagueEntry> = emptyList(),
    val leagueRank: Int = 0,
    val leagueDaysLeft: Int = 7,
)

/** Second-by-second countdowns, kept separate so only the widgets that show them recompose. */
data class TimerState(val nextHeartMillis: Long? = null, val doubleXpRemainingMillis: Long? = null)

/**
 * Activity-scoped ViewModel for everything outside a lesson: the skill tree, top bar stats,
 * daily streak/freeze processing, heart regeneration, chests, shop, leagues and badges.
 */
class GamificationViewModel(
    private val curriculum: CurriculumRepository,
    private val statsRepository: UserStatsRepository,
    private val engine: GamificationEngine,
    private val clock: AppClock,
) : ViewModel() {

    private val messageChannel = Channel<UiMessage>(Channel.BUFFERED)
    val messages: Flow<UiMessage> = messageChannel.receiveAsFlow()

    private val _chestReward = MutableStateFlow<ChestRewardUi?>(null)
    val chestReward: StateFlow<ChestRewardUi?> = _chestReward.asStateFlow()

    private val _timers = MutableStateFlow(TimerState())
    val timers: StateFlow<TimerState> = _timers.asStateFlow()

    private var tickerJob: Job? = null
    private var lastRefreshDay = Long.MIN_VALUE

    val uiState: StateFlow<HomeUiState> = combine(
        statsRepository.stats,
        curriculum.observeUnits(),
        curriculum.observeCompletedNodeIds(),
        curriculum.observeWordsLearned(),
        curriculum.observeMistakeCount(),
    ) { stats, units, completed, words, mistakes ->
        buildState(stats, units, completed, words, mistakes)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        viewModelScope.launch {
            curriculum.ensureSeeded()
            refreshDay()
        }
    }

    private fun buildState(
        stats: UserStats,
        units: List<CourseUnit>,
        completed: Set<String>,
        words: Int,
        mistakes: Int,
    ): HomeUiState {
        val states = SkillPath.nodeStates(units, completed)
        val current = SkillPath.currentLessonId(units, states)
        val today = clock.todayEpochDay()
        val weekStart = if (stats.weekStartEpochDay == UserStats.NO_DAY) LeagueEngine.weekStart(today) else stats.weekStartEpochDay
        val entries = LeagueEngine.standings(
            tier = stats.league,
            weekStartEpochDay = weekStart,
            userWeeklyXp = stats.weeklyXp,
            weekProgress = LeagueEngine.weekProgress(today, clock.fractionOfDay()),
        )
        val earned = stats.unlockedBadges
        return HomeUiState(
            isLoading = units.isEmpty(),
            stats = stats,
            units = units.map { unit ->
                UnitUi(unit, unit.nodes.map { NodeUi(it, states[it.id] ?: NodeState.LOCKED, it.id == current) })
            },
            currentLessonId = current,
            courseCompleted = units.isNotEmpty() && current == null,
            wordsLearned = words,
            mistakeCount = mistakes,
            badges = Badge.entries.map { BadgeUi(it, it.name in earned) },
            leagueEntries = entries,
            leagueRank = LeagueEngine.rankOf(entries),
            leagueDaysLeft = (7 - (today - weekStart)).toInt().coerceIn(1, 7),
        )
    }

    // ------------------------------------------------------------------ lifecycle

    fun onForeground() {
        viewModelScope.launch { refreshDay() }
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (isActive) {
                tick()
                delay(1_000)
            }
        }
    }

    fun onBackground() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private suspend fun tick() {
        val now = clock.nowMillis()
        val stats = statsRepository.stats.first()
        if (clock.todayEpochDay() != lastRefreshDay) refreshDay()
        if (engine.regenerateHearts(stats, now).events.isNotEmpty()) {
            statsRepository.update { engine.regenerateHearts(it, now).stats }
        }
        _timers.value = TimerState(
            nextHeartMillis = engine.millisUntilNextHeart(stats, now),
            doubleXpRemainingMillis = (stats.doubleXpUntilMillis - now).takeIf { it > 0 },
        )
    }

    /** Daily rollover: resets daily XP, applies Streak Freezes, regenerates hearts, resolves leagues. */
    private suspend fun refreshDay() {
        val today = clock.todayEpochDay()
        lastRefreshDay = today
        val events = statsRepository.updateAndGet { stats ->
            val outcome = engine.refresh(stats, today, clock.nowMillis())
            outcome.stats to outcome.events
        }
        events.mapNotNull(::messageFor).forEach { messageChannel.send(it) }
    }

    private fun messageFor(event: GamificationEvent): UiMessage? = when (event) {
        is GamificationEvent.StreakFreezeUsed -> UiMessage.Res(R.string.event_freeze_used)
        is GamificationEvent.StreakLost -> UiMessage.Res(R.string.event_streak_lost, listOf(event.previousStreak))
        is GamificationEvent.BadgeUnlocked -> UiMessage.Res(R.string.event_badge, listOf(event.badge.titleAr))
        is GamificationEvent.LeagueResolved -> when (event.outcome) {
            LeagueOutcome.PROMOTED -> UiMessage.Res(R.string.league_promoted, listOf(event.newTier.titleAr))
            LeagueOutcome.DEMOTED -> UiMessage.Res(R.string.league_demoted, listOf(event.newTier.titleAr))
            LeagueOutcome.STAYED -> UiMessage.Res(R.string.league_stayed, listOf(event.newTier.titleAr))
        }
        else -> null
    }

    // ------------------------------------------------------------------ chests & shop

    fun openChest(nodeId: String) {
        val node = uiState.value.units.flatMap { it.nodes }.firstOrNull { it.node.id == nodeId } ?: return
        if (node.node.type != NodeType.CHEST) return
        when (node.state) {
            NodeState.LOCKED -> { messageChannel.trySend(UiMessage.Res(R.string.chest_locked)); return }
            NodeState.COMPLETED -> { messageChannel.trySend(UiMessage.Res(R.string.chest_opened_already)); return }
            NodeState.AVAILABLE -> Unit
        }
        viewModelScope.launch {
            curriculum.markNodeCompleted(nodeId, accuracyPercent = 100, nowMillis = clock.nowMillis())
            val events = statsRepository.updateAndGet { stats ->
                val outcome = engine.openChest(stats, nodeId)
                outcome.stats to outcome.events
            }
            events.filterIsInstance<GamificationEvent.ChestOpened>().firstOrNull()?.let {
                _chestReward.value = ChestRewardUi(it.gems, it.bonusFreeze)
            }
            events.filterIsInstance<GamificationEvent.BadgeUnlocked>().mapNotNull(::messageFor).forEach { messageChannel.send(it) }
        }
    }

    fun dismissChest() {
        _chestReward.value = null
    }

    fun purchase(item: ShopItem) {
        viewModelScope.launch {
            val result = statsRepository.updateAndGet { stats ->
                when (val r = engine.purchase(stats, item, clock.nowMillis())) {
                    is PurchaseResult.Success -> r.outcome.stats to r
                    else -> stats to r
                }
            }
            val message = when (result) {
                is PurchaseResult.Success -> UiMessage.Res(R.string.purchase_success)
                is PurchaseResult.NotEnoughGems -> UiMessage.Res(R.string.not_enough_gems, listOf(result.missing))
                is PurchaseResult.NotNeeded -> UiMessage.Raw(result.reasonAr)
            }
            messageChannel.send(message)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as RihlaApplication).container
                GamificationViewModel(
                    curriculum = container.curriculumRepository,
                    statsRepository = container.userStatsRepository,
                    engine = container.gamificationEngine,
                    clock = container.clock,
                )
            }
        }
    }
}
