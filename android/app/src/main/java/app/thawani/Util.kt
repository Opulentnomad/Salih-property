package app.thawani

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.caverock.androidsvg.SVG
import java.util.Locale

fun Context.toast(t: String) = Toast.makeText(this, t, Toast.LENGTH_LONG).show()

fun Context.dial(num: String) {
    try { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (e: Exception) { toast("تعذر فتح الاتصال. الرقم: $num") }
}

fun Context.openUrl(url: String) {
    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (e: Exception) { toast("تعذر فتح الرابط") }
}

fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    startActivity(Intent.createChooser(send, "مشاركة").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun Context.vibrateShort() {
    try {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
    } catch (_: Exception) {}
}

@SuppressLint("MissingPermission")
fun Context.lastLocation(): Location? {
    return try {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    } catch (_: Exception) { null }
}

fun svgToBitmap(svg: String, w: Int, h: Int): ImageBitmap? = try {
    val s = SVG.getFromString(svg)
    s.setDocumentWidth(w.toFloat()); s.setDocumentHeight(h.toFloat())
    val pic = s.renderToPicture()
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(bmp).drawPicture(pic)
    bmp.asImageBitmap()
} catch (_: Exception) { null }

/** قراءة صوتية بالعربية */
class Speaker(ctx: Context) {
    private var tts: TextToSpeech? = null
    var ready by mutableStateOf(false)
    var arabicOk by mutableStateOf(false)
    var speaking by mutableStateOf(false)

    init {
        tts = TextToSpeech(ctx.applicationContext) { st ->
            if (st == TextToSpeech.SUCCESS) {
                val r = tts?.setLanguage(Locale("ar"))
                arabicOk = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { speaking = true }
                    override fun onDone(id: String?) { speaking = false }
                    @Deprecated("deprecated") override fun onError(id: String?) { speaking = false }
                })
                ready = true
            }
        }
    }
    fun speak(text: String, rate: Float) {
        val t = tts ?: return
        t.setSpeechRate(rate); t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "th"); speaking = true
    }
    fun stop() { tts?.stop(); speaking = false }
    fun shutdown() { tts?.stop(); tts?.shutdown() }
}
