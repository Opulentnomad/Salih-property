package com.rihla.english.core.model

enum class PatternCategory { GRAMMAR, SOUND }

/**
 * Typical mistakes Arabic speakers make in English, grouped as grammar "traps" (transfer from
 * Arabic structure) and sounds (phonemes Arabic lacks or merges). Exercises are tagged with a
 * pattern so the app can show a personal "mistake profile" and build targeted practice.
 */
enum class ErrorPattern(
    val category: PatternCategory,
    val emoji: String,
    val titleAr: String,
    val explanationAr: String,
    val exampleWrong: String,
    val exampleRight: String,
) {
    // ------------------------------------------------------------------ grammar traps
    MISSING_BE(
        PatternCategory.GRAMMAR, "🧱", "نسيان am / is / are",
        "العربية تقول «هو سعيد» بلا فعل، لكن الإنجليزية تحتاج am / is / are دائمًا.",
        "He happy.", "He is happy.",
    ),
    VERB_AS_ADJECTIVE(
        PatternCategory.GRAMMAR, "🙅", "am مع الأفعال",
        "لا نضع am / is / are قبل الفعل: نقول I agree وليس I am agree.",
        "I am agree.", "I agree.",
    ),
    THIRD_PERSON_S(
        PatternCategory.GRAMMAR, "🐍", "حرف s مع he / she / it",
        "في المضارع نضيف s للفعل مع he / she / it، ونستخدم doesn't بدل don't.",
        "She like tea.", "She likes tea.",
    ),
    ADJECTIVE_ORDER(
        PatternCategory.GRAMMAR, "🔀", "ترتيب الصفة",
        "الصفة تأتي قبل الاسم في الإنجليزية ولا تُجمع: red cars وليس cars reds.",
        "a car red", "a red car",
    ),
    ARTICLES(
        PatternCategory.GRAMMAR, "🅰️", "a / an / the",
        "نضع a أو an قبل الاسم المفرد غير المحدد، ولا نضع the قبل الأشياء العامة.",
        "I am student.", "I am a student.",
    ),
    PREPOSITIONS(
        PatternCategory.GRAMMAR, "📍", "حروف الجر",
        "حروف الجر لا تُترجم حرفيًا: married to، on Monday، at 5 o'clock.",
        "married with him", "married to him",
    ),
    WRONG_VERB(
        PatternCategory.GRAMMAR, "💡", "الترجمة الحرفية للأفعال",
        "بعض الأفعال لا تُترجم حرفيًا: turn on the light وليس open the light.",
        "Open the light.", "Turn on the light.",
    ),

    // ------------------------------------------------------------------ sounds
    SOUND_P_B(
        PatternCategory.SOUND, "🅿️", "صوت P و B",
        "P = باء مع نفخة هواء وبلا اهتزاز الحنجرة. B = الباء العربية.",
        "bark", "park",
    ),
    SOUND_V_F(
        PatternCategory.SOUND, "✌️", "صوت V و F",
        "V = فاء مع اهتزاز الحنجرة (ڤ). F = الفاء العربية.",
        "fan", "van",
    ),
    SOUND_TH(
        PatternCategory.SOUND, "👅", "صوت th",
        "أخرج طرف لسانك بين أسنانك: think = ث، this = ذ. لا تقل (س) أو (ز).",
        "sink", "think",
    ),
    SOUND_SH_CH(
        PatternCategory.SOUND, "🤫", "صوت sh و ch",
        "sh = ش، ch = تش. انتبه للفرق بين ship و chip.",
        "ship", "chip",
    ),
    SOUND_I_EE(
        PatternCategory.SOUND, "🐑", "i القصيرة و ee الطويلة",
        "ship (كسرة قصيرة) و sheep (ياء طويلة) كلمتان مختلفتان تمامًا.",
        "sheep", "ship",
    ),
    SOUND_E_I(
        PatternCategory.SOUND, "🖊️", "e و i",
        "pen (فتحة ممالة) و pin (كسرة). كثير من العرب ينطقهما بنفس الطريقة.",
        "pin", "pen",
    ),
    ;

    companion object {
        val grammar: List<ErrorPattern> get() = entries.filter { it.category == PatternCategory.GRAMMAR }
        val sounds: List<ErrorPattern> get() = entries.filter { it.category == PatternCategory.SOUND }
    }
}
