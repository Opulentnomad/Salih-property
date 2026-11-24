package app.thawani

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

/** وضع الطوارئ: شاشة واحدة بزر 997 كبير وخيارات سريعة. */
@Composable
fun EmergencyDialog(app: AppState, onOpen: (String) -> Unit, onClose: () -> Unit) {
    val th = LocalTh.current
    val ctx = LocalContext.current
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) shareLoc(ctx) else ctx.toast("لم يُمنح إذن الموقع") }
    val items = listOf("p3" to "لا يتنفس أو لا يستجيب", "p2" to "ينزف بشدة", "p1" to "يختنق", "p4" to "حساسية شديدة", "p20" to "رائحة غاز", "p21" to "حريق")
    val loc = app.prefs.familyRaw("loc").ifEmpty { "اذكر المدينة والحي واسم الشارع وأقرب معلم" }
    val script = listOf("«السلام عليكم، أحتاج إسعافاً.»", "العنوان: $loc.", "ماذا حدث؟ مثال: «شخص لا يتنفس» أو «نزيف شديد» أو «حريق في المطبخ».",
        "كم عدد المصابين وكم عمرهم تقريباً؟", "هل المصاب واعٍ؟ هل يتنفس؟", "اسمي … ورقم جوالي … وابقَ على الخط ولا تغلقه حتى يطلب منك.")
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(th.bg).statusBarsPadding().navigationBarsPadding()) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("وضع الطوارئ", color = th.acc, fontWeight = FontWeight.SemiBold); Text("ماذا يحدث الآن؟", color = th.ink, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
                    Btn("إغلاق", onClose, kind = "ghost")
                }
                Btn("اتصل بالإسعاف  997", { ctx.dial("997") }, kind = "red", modifier = Modifier.fillMaxWidth().height(88.dp))
                items.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (id, label) ->
                            Box(Modifier.weight(1f).heightIn(min = 76.dp).clip(RoundedCornerShape(16.dp)).background(th.card).border(2.dp, th.line, RoundedCornerShape(16.dp))
                                .clickable { onOpen(id) }.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = th.ink, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Btn("شارك موقعي", { if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) shareLoc(ctx) else perm.launch(Manifest.permission.ACCESS_FINE_LOCATION) })
                    Btn("911", { ctx.dial("911") }, kind = "ghost")
                    Btn("998", { ctx.dial("998") }, kind = "ghost")
                }
                ThCard(Modifier.fillMaxWidth(), color = th.soft) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("ماذا أقول عند الاتصال؟", color = th.ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        script.forEachIndexed { i, s -> Text("${i + 1}. $s", color = th.ink, fontSize = 16.sp, lineHeight = 24.sp) }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
