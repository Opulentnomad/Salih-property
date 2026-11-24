package com.wijha.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wijha.app.data.AppData
import com.wijha.app.data.CommonsPhoto
import com.wijha.app.data.Flight
import com.wijha.app.data.Net
import com.wijha.app.data.PhotoItem
import com.wijha.app.data.Post
import com.wijha.app.data.Store
import com.wijha.app.data.sampleFlights
import com.wijha.app.data.samplePost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Executors

class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val store = Store(app)
    private val saver = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val _state = MutableStateFlow(store.load())
    val state: StateFlow<AppData> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (!_state.value.seeded) seed()
            Reminders.rescheduleAll(app, _state.value)
        }
    }

    fun photoFile(id: String): File = store.photoFile(id)

    private fun commit(f: (AppData) -> AppData) {
        _state.update(f)
        val snap = _state.value
        viewModelScope.launch(saver) { store.save(snap) }
    }

    private fun seed() {
        val ids = (0..5).map { "mdemo$it" }
        ids.forEachIndexed { i, id -> store.writeSampleImage(id, i) }
        commit {
            it.copy(
                flights = sampleFlights(LocalDate.now()), posts = listOf(samplePost(ids)),
                credits = ids.associateWith { "صورة تجريبية مرسومة" }, sampleFlights = true, seeded = true,
            )
        }
    }

    // ---- الرحلات ----
    fun saveFlight(f: Flight) {
        commit { d ->
            val exists = d.flights.any { it.id == f.id }
            d.copy(flights = if (exists) d.flights.map { if (it.id == f.id) f else it } else d.flights + f, sampleFlights = false)
        }
        Reminders.schedule(app, f)
    }

    fun deleteFlight(f: Flight) {
        commit { d -> d.copy(flights = d.flights.filter { it.id != f.id }) }
        Reminders.cancel(app, f)
    }

    fun clearFlights() {
        _state.value.flights.forEach { Reminders.cancel(app, it) }
        commit { it.copy(flights = emptyList(), sampleFlights = false) }
    }

    fun restoreSampleFlights() {
        val fl = sampleFlights(LocalDate.now())
        commit { it.copy(flights = fl, sampleFlights = true) }
        fl.forEach { Reminders.schedule(app, it) }
    }

    fun setName(n: String) = commit { it.copy(name = n) }
    fun setHome(code: String) = commit { it.copy(home = code) }

    // ---- المدونة ----
    fun savePost(p: Post) {
        commit { d ->
            val exists = d.posts.any { it.id == p.id }
            d.copy(posts = if (exists) d.posts.map { if (it.id == p.id) p else it } else d.posts + p)
        }
        gc()
    }

    fun deletePost(id: String) {
        commit { d -> d.copy(posts = d.posts.filter { it.id != id }) }
        gc()
    }

    private fun gc() {
        val snap = _state.value
        viewModelScope.launch(saver) { store.gc(snap) }
    }

    suspend fun importPhotos(uris: List<Uri>): List<PhotoItem> = withContext(Dispatchers.IO) {
        uris.mapNotNull { store.importUri(it) }.map { PhotoItem(it) }
    }

    suspend fun importRemote(photos: List<CommonsPhoto>): List<PhotoItem> = withContext(Dispatchers.IO) {
        val out = ArrayList<PhotoItem>()
        for (p in photos) {
            val bytes = Net.download(p.url) ?: continue
            val id = store.saveBytes(bytes)
            _state.update { it.copy(credits = it.credits + (id to p.credit)) }
            out += PhotoItem(id)
        }
        val snap = _state.value
        launchSave(snap)
        out
    }

    private fun launchSave(snap: AppData) {
        viewModelScope.launch(saver) { store.save(snap) }
    }
}
