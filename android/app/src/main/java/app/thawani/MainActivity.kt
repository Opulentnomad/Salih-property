package app.thawani

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = AppState(Prefs(this))
        setContent { ThawaniApp(app, onExit = { finish() }) }
    }
}

@Composable
fun ThawaniApp(app: AppState, onExit: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val speaker = remember { Speaker(ctx) }
    val cpr = remember { Cpr(scope, ctx) }
    DisposableEffect(Unit) { onDispose { speaker.shutdown(); cpr.release() } }
    val d = LocalDensity.current

    val voice = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            val t = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!t.isNullOrBlank()) {
                app.query = t; app.cat = "all"; app.place = "all"; app.season = ""
                val l = app.filtered()
                if (l.size == 1) app.go(Dest.CardD(l[0].id)) else if (app.current != Dest.Library) app.go(Dest.Library)
            }
        }
    }
    val startVoice = {
        try {
            voice.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA"))
        } catch (e: Exception) { ctx.toast("البحث الصوتي غير متاح على هذا الجهاز") }
    }
    val openCard = { id: String -> app.emergency = false; app.go(Dest.CardD(id)) }

    CompositionLocalProvider(
        LocalDensity provides Density(d.density, d.fontScale * app.scale),
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        ThawaniTheme(app.theme) {
            val th = LocalTh.current
            BackHandler(enabled = app.emergency || app.stack.size > 1) { if (app.emergency) app.emergency = false else app.back() }
            if (app.showWelcome) Welcome(app) else {
                Scaffold(
                    containerColor = th.bg, contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = { TopBar(app) },
                    bottomBar = { BottomBar(app) }
                ) { pad ->
                    Box(Modifier.fillMaxSize().padding(pad).background(th.bg)) {
                        when (val dest = app.current) {
                            Dest.Home -> HomeScreen(app) { openCard(it) }
                            Dest.Library -> LibraryScreen(app, { openCard(it) }, startVoice)
                            is Dest.CardD -> CardScreen(app, dest.id, dest.mode, speaker)
                            Dest.CprD -> CprScreen(app, cpr)
                            Dest.Kit -> KitScreen(app)
                            Dest.More -> MoreScreen(app)
                            Dest.FaqD -> FaqScreen(app)
                            Dest.NumsD -> NumsScreen(app)
                            Dest.Plan -> PlanScreen(app)
                            Dest.RefsD -> RefsScreen()
                            Dest.About -> AboutScreen()
                        }
                    }
                }
                if (app.emergency) EmergencyDialog(app, onOpen = { app.emergency = false; app.go(Dest.CardD(it, "guide")) }, onClose = { app.emergency = false })
            }
        }
    }
}

@Composable
private fun TopBar(app: AppState) {
    val th = LocalTh.current
    Row(Modifier.fillMaxWidth().background(th.bg).statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (app.stack.size > 1) Box(Modifier.size(44.dp).clip(CircleShape).background(th.card).border(1.dp, th.line, CircleShape).clickable { app.back() }, contentAlignment = Alignment.Center) { Text("→", color = th.ink, fontSize = 20.sp) }
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(th.acc), contentAlignment = Alignment.Center) { Text("ث", color = th.onAcc, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        Text("ثواني.", color = th.ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        listOf("أ−" to -1, "أ+" to 1).forEach { (t, dlt) ->
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(th.card).border(1.dp, th.line, RoundedCornerShape(12.dp)).clickable {
                val scales = listOf(1f, 1.12f, 1.25f, 1.4f); val i = scales.indexOf(app.scale).let { if (it < 0) 0 else it }
                app.updScale(scales[(i + dlt).coerceIn(0, scales.lastIndex)])
            }, contentAlignment = Alignment.Center) { Text(t, color = th.ink, fontWeight = FontWeight.Bold) }
        }
        Box(Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp)).background(Pal.red).clickable { app.emergency = true }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text("طوارئ", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomBar(app: AppState) {
    val th = LocalTh.current
    val cur = app.current
    val tabs = buildList {
        add(Triple("الرئيسية", "⌂", Dest.Home))
        add(Triple("البطاقات", "▤", Dest.Library))
        if (!app.simple) add(Triple("إنعاش", "♥", Dest.CprD))
        if (!app.simple) add(Triple("التجهيز", "☑", Dest.Kit))
        add(Triple("المزيد", "⋯", Dest.More))
    }
    Row(Modifier.fillMaxWidth().background(th.card).border(1.dp, th.line).navigationBarsPadding().padding(4.dp), horizontalArrangement = Arrangement.SpaceAround) {
        tabs.forEach { (label, glyph, dest) ->
            val sel = cur == dest || (dest == Dest.Library && cur is Dest.CardD) || (dest == Dest.More && (cur == Dest.FaqD || cur == Dest.NumsD || cur == Dest.Plan || cur == Dest.RefsD || cur == Dest.About))
            val isCpr = dest == Dest.CprD
            Column(Modifier.weight(1f).heightIn(min = 56.dp).clip(RoundedCornerShape(14.dp)).clickable { app.tab(dest) }.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (isCpr) Box(Modifier.size(36.dp).clip(CircleShape).background(Pal.red), contentAlignment = Alignment.Center) { Text(glyph, color = Color.White, fontSize = 18.sp) }
                else Text(glyph, color = if (sel) th.acc else th.ink2, fontSize = 22.sp)
                Text(label, color = if (sel) th.acc else th.ink2, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun Welcome(app: AppState) {
    val th = LocalTh.current
    Column(Modifier.fillMaxSize().background(th.bg).statusBarsPadding().navigationBarsPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.height(40.dp))
        Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(th.acc), contentAlignment = Alignment.Center) { Text("ث", color = th.onAcc, fontSize = 60.sp, fontWeight = FontWeight.Bold) }
        Text("مرحباً بك في ثواني", color = th.ink, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("كل ثانية تصنع فارقاً", color = th.ink2, fontSize = 18.sp)
        Box(Modifier.height(8.dp))
        Text("من سيستخدم ثواني؟ نضبط لك الخط والعرض والصوت. تقدر تغيّره لاحقاً من «خطتي».", color = th.ink2, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        listOf(Triple("child", "طفل أو مراهق", "بطاقات مناسبة لعمرك واختبار قصير"), Triple("adult", "بالغ", "كل الأقسام"), Triple("senior", "كبير سن", "خط أكبر وعرض مبسّط وقراءة بالصوت")).forEach { (k, t, s) ->
            ThCard(Modifier.fillMaxWidth().clickable { app.applyProfile(k) }) {
                Column(Modifier.padding(16.dp)) { Text(t, color = th.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(s, color = th.ink2, fontSize = 14.sp) }
            }
        }
        Btn("تخطي", { app.applyProfile("adult") }, kind = "ghost", modifier = Modifier.fillMaxWidth())
    }
}
