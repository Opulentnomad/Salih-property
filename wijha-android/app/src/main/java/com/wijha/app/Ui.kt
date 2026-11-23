@file:OptIn(ExperimentalLayoutApi::class)

package com.wijha.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// ---- ألوان مستخرجة من التصميم المرجعي ----
val Bg = Color(0xFFFBF6F8)
val Ink = Color(0xFF26272D)
val Mute = Color(0xFF6F6B75)
val Blue = Color(0xFF2F5FD0)
val Rose = Color(0xFFE0457B)
val Heart = Color(0xFFE5304A)
val Bubbles = listOf(
    Color(0xFFE9E1FB) to Color(0xFF3B2F78),
    Color(0xFFFBE8CC) to Color(0xFF7A4A12),
    Color(0xFFD9EBFB) to Color(0xFF1F4A78),
)

val LocalLang = compositionLocalOf { "ar" }

@Composable
fun tr(ar: String, en: String): String = if (LocalLang.current == "ar") ar else en

@Composable
fun WijhaTheme(lang: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalLang provides lang,
        LocalLayoutDirection provides if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Ink, onPrimary = Color.White, secondary = Blue,
                background = Bg, surface = Bg, onBackground = Ink, onSurface = Ink,
            ),
            content = content,
        )
    }
}

// ---- مكونات صغيرة ----

@Composable
fun Pill(text: String, selected: Boolean = false, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier
            .shadow(3.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(if (selected) Ink else Color.White)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text,
            color = if (selected) Color.White else Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun CircleBtn(icon: ImageVector, desc: String, size: Dp = 40.dp, tint: Color = Ink, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun PrimaryButton(text: String, icon: ImageVector? = null, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(if (enabled) Ink else Ink.copy(alpha = 0.35f))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
}

// ---- الصور ----

@Composable
fun rememberHero(d: Destination): State<String?> =
    produceState<String?>(initialValue = null, d.id) { value = ImageRepo.hero(d) }

@Composable
fun rememberGallery(d: Destination): State<List<String>> =
    produceState(initialValue = emptyList<String>(), d.id) { value = ImageRepo.gallery(d) }

@Composable
fun GradientBox(d: Destination, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit = {}) {
    Box(
        modifier.background(Brush.verticalGradient(listOf(Color(d.c1), Color(d.c2)))),
        content = content,
    )
}

/** صورة الوجهة من الإنترنت، وخلفها تدرج لوني يظهر أثناء التحميل أو دون اتصال. */
@Composable
fun DestPhoto(d: Destination, modifier: Modifier = Modifier, url: String? = null) {
    val hero by rememberHero(d)
    GradientBox(d, modifier) {
        val src = url ?: hero
        if (src != null) {
            AsyncImage(
                model = src,
                contentDescription = d.ar,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private val RATIOS = listOf(0.78f, 1.25f, 1.0f, 0.9f, 1.15f, 0.85f)

@Composable
fun PhotoGrid(
    urls: List<String>,
    favs: Set<String>,
    onFav: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomPad: Dp = 100.dp,
    header: LazyStaggeredGridScope.() -> Unit = {},
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = bottomPad),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
    ) {
        header()
        items(urls.size) { i ->
            val url = urls[i]
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(RATIOS[i % RATIOS.size])
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE8E2EA)),
            ) {
                AsyncImage(
                    model = url, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                val fav = url in favs
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (fav) 1f else 0.8f))
                        .clickable { onFav(url) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (fav) Heart else Ink,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}
