package app.thawani

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.drawToBitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class SmokeTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private fun shot(name: String) {
        rule.waitForIdle()
        val bmp = rule.activity.window.decorView.drawToBitmap()
        val dir = File("/tmp/thawani-shots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SHOT $name ${bmp.width}x${bmp.height}")
    }

    @Test fun data_is_complete() {
        assertEquals(47, Data.cards.size)
        assertEquals(47, Data.cards.map { it.id }.toSet().size)
        assertEquals(20, (listOf("p3", "p1", "p2", "p14").sumOf { id -> (0..4).count { Data.illus(id, it) != null } }))
        assertEquals(6, Data.kits.size); assertEquals(15, Data.faq.size); assertEquals(8, Data.en.size)
        // every SVG must be parseable by AndroidSVG
        for (id in listOf("p3", "p1", "p2", "p14")) for (i in 0..4) assert(svgToBitmap(Data.illus(id, i)!!, 220, 150) != null) { "svg $id/$i failed" }
    }

    @Test fun screens_render() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app = AppState(Prefs(ctx)).apply { applyProfile("adult") }
        rule.setContent { ThawaniApp(app, onExit = {}) }
        shot("01_home")
        app.tab(Dest.Library); shot("02_library")
        app.go(Dest.CardD("p3")); shot("03_card_overview")
        app.stack.removeAt(app.stack.lastIndex); app.go(Dest.CardD("p3", "guide")); shot("04_card_guide")
        app.stack.removeAt(app.stack.lastIndex); app.go(Dest.CardD("p1")); shot("05_card_choke")
        app.tab(Dest.CprD); shot("06_cpr")
        app.tab(Dest.Kit); shot("07_kit")
        app.tab(Dest.More); app.go(Dest.FaqD); shot("08_faq")
        app.tab(Dest.More); app.go(Dest.Plan); shot("09_plan")
        app.emergency = true; shot("10_emergency"); app.emergency = false
        app.applyProfile("senior"); app.tab(Dest.Home); shot("11_home_senior")
        app.updTheme("dark"); shot("12_home_dark")
    }
}
