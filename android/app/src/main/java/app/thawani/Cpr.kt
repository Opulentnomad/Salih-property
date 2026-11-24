package app.thawani

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** إيقاع الإنعاش 110 نبضة/دقيقة + مؤقت العاصبة. يعمل في الخلفية أثناء التنقل بين الشاشات. */
class Cpr(private val scope: CoroutineScope, private val ctx: Context) {
    var running by mutableStateOf(false)
    var count by mutableIntStateOf(0)
    var elapsed by mutableLongStateOf(0L)
    var beat by mutableStateOf(false)
    var alert by mutableStateOf<String?>(null)
    private var job: Job? = null
    private var startMs = 0L
    private var acc = 0L
    private var lastSwap = 0L
    private val tone: ToneGenerator? = try { ToneGenerator(AudioManager.STREAM_MUSIC, 90) } catch (_: Exception) { null }

    fun toggle() = if (running) pause() else start()

    private fun start() {
        running = true
        startMs = SystemClock.elapsedRealtime()
        job = scope.launch {
            while (isActive) {
                count++
                tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                ctx.vibrateShort()
                beat = true
                elapsed = (acc + SystemClock.elapsedRealtime() - startMs) / 1000
                if (count % 30 == 0) alert = "30 ضغطة. إن كنت مدرباً أعطِ نفسين إنقاذيين، وإلا واصل الضغط دون توقف."
                if (elapsed > 0 && elapsed % 120 == 0L && lastSwap != elapsed) { lastSwap = elapsed; alert = "مضت دقيقتان. بدّل المسعف إن وُجد من يساعدك." }
                delay(100); beat = false
                delay(445)
            }
        }
    }

    private fun pause() {
        acc += SystemClock.elapsedRealtime() - startMs
        running = false; job?.cancel(); beat = false
    }

    fun reset() { running = false; job?.cancel(); count = 0; acc = 0; elapsed = 0; lastSwap = 0; alert = null; beat = false }
    fun release() { job?.cancel(); tone?.release() }

    /* مؤقت العاصبة */
    var tRunning by mutableStateOf(false)
    var tSeconds by mutableLongStateOf(0L)
    var tSince by mutableStateOf("")
    private var tJob: Job? = null
    private var tStart = 0L
    private var tAcc = 0L

    fun tToggle() {
        if (tRunning) { tAcc += SystemClock.elapsedRealtime() - tStart; tRunning = false; tJob?.cancel(); return }
        if (tAcc == 0L) tSince = "وقت التركيب: " + java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale("ar")).format(java.util.Date())
        tRunning = true; tStart = SystemClock.elapsedRealtime()
        tJob = scope.launch { while (isActive) { tSeconds = (tAcc + SystemClock.elapsedRealtime() - tStart) / 1000; delay(250) } }
    }
    fun tReset() { tRunning = false; tJob?.cancel(); tAcc = 0; tSeconds = 0; tSince = "" }
}
