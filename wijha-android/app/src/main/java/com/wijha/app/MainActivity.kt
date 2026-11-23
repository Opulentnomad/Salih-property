package com.wijha.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class AppVm(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)
    var state by mutableStateOf(store.load())
        private set

    fun update(f: (UserState) -> UserState) {
        state = f(state)
        store.save(state)
    }

    /** إعجاب بوجهة: يرفع وزن وسومها (تعلّم مباشر على الجهاز). */
    fun toggleLike(d: Destination) = update { s ->
        val liked = d.id in s.liked
        val w = s.weights.toMutableMap()
        if (!liked) d.tags.forEach { (t, v) -> w[t] = ((w[t] ?: 0f) + 0.12f * v).coerceAtMost(1f) }
        s.copy(liked = if (liked) s.liked - d.id else s.liked + d.id, weights = w)
    }

    /** «ليس لي»: يخفي الوجهة ويخفّض وزن وسومها. */
    fun dismiss(d: Destination) = update { s ->
        val w = s.weights.toMutableMap()
        d.tags.forEach { (t, v) -> w[t] = ((w[t] ?: 0f) - 0.08f * v).coerceAtLeast(0f) }
        s.copy(hidden = s.hidden + d.id, weights = w)
    }

    fun toggleFavPhoto(url: String) = update { s ->
        s.copy(favPhotos = if (url in s.favPhotos) s.favPhotos - url else s.favPhotos + url)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppVm = viewModel()
            WijhaTheme(vm.state.lang) { AppRoot(vm) }
        }
    }
}
