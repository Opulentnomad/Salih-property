package com.rihla.english.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rihla.english.core.journey.JourneyProgress
import com.rihla.english.core.journey.JourneyRepository
import com.rihla.english.core.journey.ProfileRepository
import com.rihla.english.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

val Context.journeyDataStore: DataStore<Preferences> by preferencesDataStore(name = "journey")

/**
 * Offline-first persistence: the learner's journey progress (scenes, notebook with SM-2
 * schedule, missions, daily log) and profile, as JSON documents in DataStore. Every update is
 * an atomic read-modify-write transaction.
 */
class JourneyStore(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : JourneyRepository, ProfileRepository {

    private val progressKey = stringPreferencesKey("progress_json")
    private val profileKey = stringPreferencesKey("profile_json")

    override val progress: Flow<JourneyProgress> =
        dataStore.data.map { it.read(progressKey, JourneyProgress.serializer()) ?: JourneyProgress() }.distinctUntilChanged()

    override val profile: Flow<UserProfile> =
        dataStore.data.map { it.read(profileKey, UserProfile.serializer()) ?: UserProfile() }.distinctUntilChanged()

    override suspend fun update(transform: (JourneyProgress) -> JourneyProgress) {
        dataStore.edit { prefs ->
            val current = prefs.read(progressKey, JourneyProgress.serializer()) ?: JourneyProgress()
            prefs[progressKey] = json.encodeToString(JourneyProgress.serializer(), transform(current))
        }
    }

    override suspend fun updateProfile(transform: (UserProfile) -> UserProfile) {
        dataStore.edit { prefs ->
            val current = prefs.read(profileKey, UserProfile.serializer()) ?: UserProfile()
            prefs[profileKey] = json.encodeToString(UserProfile.serializer(), transform(current))
        }
    }

    private fun <T> Preferences.read(key: Preferences.Key<String>, serializer: KSerializer<T>): T? =
        this[key]?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }
}
