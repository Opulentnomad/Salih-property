package com.rihla.english.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.LearningInsights
import com.rihla.english.core.model.PatternStat
import com.rihla.english.core.model.SessionKind
import com.rihla.english.core.model.StoryProgress
import com.rihla.english.core.model.UserProfile
import com.rihla.english.core.repository.InsightsRepository
import com.rihla.english.core.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.learningDataStore: DataStore<Preferences> by preferencesDataStore(name = "learning")

/**
 * Learning analytics (mistake profile, daily activity, story stars) and the onboarding profile,
 * stored as small JSON documents in a Preferences DataStore. Updates are atomic transactions.
 */
class LearningDataStore(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : InsightsRepository, ProfileRepository {

    private val insightsKey = stringPreferencesKey("insights_json")
    private val profileKey = stringPreferencesKey("profile_json")

    override val insights: Flow<LearningInsights> = dataStore.data
        .map { it.decode(insightsKey) ?: LearningInsights() }
        .distinctUntilChanged()

    override val profile: Flow<UserProfile> = dataStore.data
        .map { it.decode(profileKey) ?: UserProfile() }
        .distinctUntilChanged()

    override suspend fun update(transform: (UserProfile) -> UserProfile) {
        dataStore.edit { prefs ->
            val current = prefs.decode(profileKey) ?: UserProfile()
            prefs[profileKey] = json.encodeToString(UserProfile.serializer(), transform(current))
        }
    }

    override suspend fun recordSession(
        todayEpochDay: Long,
        xp: Int,
        kind: SessionKind,
        patternResults: List<Pair<ErrorPattern, Boolean>>,
    ) = editInsights(todayEpochDay) { current ->
        val day = current.day(todayEpochDay)
        val updatedDay = when (kind) {
            SessionKind.LESSON -> day.copy(xp = day.xp + xp, lessons = day.lessons + 1)
            SessionKind.PRACTICE, SessionKind.PATTERN -> day.copy(xp = day.xp + xp, practices = day.practices + 1)
            SessionKind.SOUND -> day.copy(xp = day.xp + xp, soundDrills = day.soundDrills + 1)
        }
        val patterns = current.patterns.toMutableMap()
        patternResults.forEach { (pattern, firstTryCorrect) ->
            val stat = patterns[pattern.name] ?: PatternStat()
            patterns[pattern.name] = stat.copy(
                attempts = stat.attempts + 1,
                misses = stat.misses + if (firstTryCorrect) 0 else 1,
                lastPracticedDay = todayEpochDay,
            )
        }
        current.copy(days = current.days + (todayEpochDay to updatedDay), patterns = patterns)
    }

    override suspend fun recordStory(todayEpochDay: Long, storyId: String, stars: Int, xp: Int) =
        editInsights(todayEpochDay) { current ->
            val day = current.day(todayEpochDay)
            val progress = current.stories[storyId] ?: StoryProgress()
            current.copy(
                days = current.days + (todayEpochDay to day.copy(xp = day.xp + xp, stories = day.stories + 1)),
                stories = current.stories + (storyId to StoryProgress(maxOf(progress.bestStars, stars), progress.timesCompleted + 1)),
            )
        }

    private suspend fun editInsights(todayEpochDay: Long, transform: (LearningInsights) -> LearningInsights) {
        dataStore.edit { prefs ->
            val updated = transform(prefs.decode(insightsKey) ?: LearningInsights())
            val oldest = todayEpochDay - LearningInsights.DAYS_TO_KEEP
            val pruned = updated.copy(days = updated.days.filterKeys { it >= oldest })
            prefs[insightsKey] = json.encodeToString(LearningInsights.serializer(), pruned)
        }
    }

    private inline fun <reified T> Preferences.decode(key: Preferences.Key<String>): T? =
        this[key]?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() }
}

