@file:OptIn(ExperimentalFoundationApi::class)

package com.wijha.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.wijha.app.AppViewModel
import com.wijha.app.data.PhotoItem
import com.wijha.app.ui.Mute

data class LightboxState(val items: List<PhotoItem>, val index: Int)

/** عارض صور بملء الشاشة مع تمرير جانبي، ويعرض التعليق واسم المصوّر. */
@Composable
fun Lightbox(vm: AppViewModel, state: LightboxState, onDismiss: () -> Unit) {
    val data by vm.state.collectAsState()
    val pager = rememberPagerState(initialPage = state.index.coerceIn(0, maxOf(0, state.items.size - 1)), pageCount = { state.items.size })
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().background(Color(0xFA080C1A))) {
            HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
                AsyncImage(vm.photoFile(state.items[page].id), state.items[page].caption.ifBlank { null }, Modifier.fillMaxSize().padding(vertical = 80.dp), contentScale = ContentScale.Fit)
            }
            IconButton(onClick = onDismiss, Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp)) {
                Icon(Icons.Filled.Close, "إغلاق", tint = Color.White)
            }
            val cur = state.items.getOrNull(pager.currentPage)
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (cur != null && cur.caption.isNotBlank()) Text(cur.caption, color = Color.White, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                data.credits[cur?.id]?.let { Text(it, color = Mute, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
                Text("${pager.currentPage + 1} / ${state.items.size}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
