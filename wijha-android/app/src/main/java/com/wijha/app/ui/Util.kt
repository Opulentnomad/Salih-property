package com.wijha.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/** الوقت الحالي، يتجدد كل نصف دقيقة لتحديث العدّاد والحالات. */
@Composable
fun rememberNow(): State<LocalDateTime> = produceState(LocalDateTime.now()) {
    while (true) {
        delay(30_000)
        value = LocalDateTime.now()
    }
}

fun openMaps(ctx: Context, query: String) {
    val q = Uri.encode(query)
    val tries = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$q")),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$q")),
    )
    for (i in tries) {
        try {
            ctx.startActivity(i)
            return
        } catch (e: Exception) {
            // جرّب الخيار التالي
        }
    }
}

fun shareText(ctx: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(send, null))
}
