package app.thawani

import android.content.Context
import android.content.SharedPreferences

/** كل البيانات تُحفظ على الجهاز فقط. */
class Prefs(ctx: Context) {
    private val sp: SharedPreferences = ctx.getSharedPreferences("thawani", Context.MODE_PRIVATE)

    var profile: String
        get() = sp.getString("profile", "") ?: ""
        set(v) = sp.edit().putString("profile", v).apply()
    var scale: Float
        get() = sp.getFloat("scale", 1f)
        set(v) = sp.edit().putFloat("scale", v).apply()
    var theme: String // auto | light | dark
        get() = sp.getString("theme", "auto") ?: "auto"
        set(v) = sp.edit().putString("theme", v).apply()
    var rate: Float
        get() = sp.getFloat("rate", 0.95f)
        set(v) = sp.edit().putFloat("rate", v).apply()
    var autoread: Boolean
        get() = sp.getBoolean("autoread", false)
        set(v) = sp.edit().putBoolean("autoread", v).apply()
    var simple: Boolean
        get() = sp.getBoolean("simple", false)
        set(v) = sp.edit().putBoolean("simple", v).apply()
    var stars: Int
        get() = sp.getInt("stars", 0)
        set(v) = sp.edit().putInt("stars", v).apply()

    fun family(key: String, def: String): String = (sp.getString("fam_$key", "") ?: "").trim().ifEmpty { def }
    fun familyRaw(key: String): String = sp.getString("fam_$key", "") ?: ""
    fun setFamily(key: String, v: String) = sp.edit().putString("fam_$key", v).apply()

    fun kitChecked(id: String): Set<Int> =
        (sp.getString("kit_$id", "") ?: "").split(",").mapNotNull { it.toIntOrNull() }.toSet()
    fun setKit(id: String, v: Set<Int>) = sp.edit().putString("kit_$id", v.joinToString(",")).apply()
}
