package com.rihla.english.core.sound

import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.Exercise
import com.rihla.english.core.model.ListeningChallenge
import com.rihla.english.core.model.ListeningMode
import com.rihla.english.core.model.MatchMode
import com.rihla.english.core.model.MatchPair
import com.rihla.english.core.model.MatchPairs
import com.rihla.english.core.model.SpeakingChallenge
import kotlin.random.Random

/** Two words that differ only in the target sound. [target] uses the sound being trained. */
data class MinimalPair(
    val target: String,
    val targetAr: String,
    val other: String,
    val otherAr: String,
    val targetPhonetic: String,
)

data class SoundDrill(
    val pattern: ErrorPattern,
    /** Mouth-position coaching in Arabic, shown before the drill. */
    val mouthTipAr: String,
    val pairs: List<MinimalPair>,
)

/**
 * "مختبر النطق" — per-sound drills built from minimal pairs. Every speaking item knows its
 * minimal-pair partner, so the grader can say "I heard *bear*, not *pear*".
 */
object SoundLab {

    val drills: Map<ErrorPattern, SoundDrill> by lazy {
        listOf(
            SoundDrill(
                ErrorPattern.SOUND_P_B,
                "P: أغلق شفتيك ثم افتحهما بنفخة هواء قوية، والحنجرة لا تهتز. ضع ورقة أمام فمك: يجب أن تتحرك مع P.",
                listOf(
                    MinimalPair("pear", "كمثرى", "bear", "دب", "پِير"),
                    MinimalPair("pin", "دبوس", "bin", "سلة مهملات", "پِن"),
                    MinimalPair("park", "حديقة", "bark", "نباح", "پارك"),
                    MinimalPair("pack", "يحزم", "back", "ظهر", "پاك"),
                    MinimalPair("pill", "حبة دواء", "bill", "فاتورة", "پِل"),
                    MinimalPair("pay", "يدفع", "bay", "خليج", "پَي"),
                ),
            ),
            SoundDrill(
                ErrorPattern.SOUND_V_F,
                "V: ضع أسنانك العليا على شفتك السفلى وانطق الفاء مع اهتزاز الحنجرة. ضع يدك على حلقك لتشعر بالاهتزاز.",
                listOf(
                    MinimalPair("van", "شاحنة صغيرة", "fan", "مروحة", "ڤان"),
                    MinimalPair("vine", "كرمة", "fine", "بخير", "ڤاين"),
                    MinimalPair("very", "جدًا", "ferry", "عبّارة", "ڤيري"),
                    MinimalPair("view", "منظر", "few", "قليل", "ڤيو"),
                    MinimalPair("vast", "واسع", "fast", "سريع", "ڤاست"),
                    MinimalPair("vest", "سترة", "fest", "مهرجان", "ڤِست"),
                ),
            ),
            SoundDrill(
                ErrorPattern.SOUND_TH,
                "th: أخرج طرف لسانك قليلًا بين أسنانك وانفخ: think = ث. لا تقل (س) أو (ت).",
                listOf(
                    MinimalPair("think", "يفكر", "sink", "حوض", "ثِنك"),
                    MinimalPair("three", "ثلاثة", "tree", "شجرة", "ثري"),
                    MinimalPair("thank", "يشكر", "tank", "خزان", "ثانك"),
                    MinimalPair("path", "طريق", "pass", "يمرّ", "پاث"),
                    MinimalPair("thin", "نحيف", "tin", "علبة صفيح", "ثِن"),
                    MinimalPair("mouth", "فم", "mouse", "فأر", "ماوث"),
                ),
            ),
            SoundDrill(
                ErrorPattern.SOUND_SH_CH,
                "sh = ش (هواء مستمر). ch = تش (يبدأ بانغلاق مثل التاء ثم ش). جرّب: ship ثم chip.",
                listOf(
                    MinimalPair("chip", "رقاقة", "ship", "سفينة", "تشِب"),
                    MinimalPair("cheap", "رخيص", "sheep", "خروف", "تشيپ"),
                    MinimalPair("chair", "كرسي", "share", "يشارك", "تشير"),
                    MinimalPair("watch", "ساعة يد", "wash", "يغسل", "ووتش"),
                    MinimalPair("chop", "يقطّع", "shop", "متجر", "تشوپ"),
                    MinimalPair("catch", "يمسك", "cash", "نقد", "كاتش"),
                ),
            ),
            SoundDrill(
                ErrorPattern.SOUND_I_EE,
                "i القصيرة: كسرة سريعة ومرتخية (ship). ee الطويلة: ياء ممدودة مع ابتسامة (sheep).",
                listOf(
                    MinimalPair("ship", "سفينة", "sheep", "خروف", "شِب"),
                    MinimalPair("live", "يعيش", "leave", "يغادر", "لِڤ"),
                    MinimalPair("sit", "يجلس", "seat", "مقعد", "سِت"),
                    MinimalPair("fill", "يملأ", "feel", "يشعر", "فِل"),
                    MinimalPair("hit", "يضرب", "heat", "حرارة", "هِت"),
                    MinimalPair("bit", "قطعة صغيرة", "beat", "يهزم", "بِت"),
                ),
            ),
            SoundDrill(
                ErrorPattern.SOUND_E_I,
                "e: افتح فمك أكثر قليلًا كأنك تقول (إِ) ممالة (pen). i: كسرة قصيرة وفم أضيق (pin).",
                listOf(
                    MinimalPair("pen", "قلم", "pin", "دبوس", "پِن"),
                    MinimalPair("ten", "عشرة", "tin", "علبة صفيح", "تِن"),
                    MinimalPair("bed", "سرير", "bid", "مزايدة", "بِد"),
                    MinimalPair("set", "يضع", "sit", "يجلس", "سِت"),
                    MinimalPair("well", "جيدًا", "will", "سوف", "وِل"),
                    MinimalPair("met", "قابل", "mitt", "قفاز", "مِت"),
                ),
            ),
        ).associateBy { it.pattern }
    }

    /**
     * Builds a drill session: minimal-pair listening, an audio match grid and speaking items that
     * detect the confusable word. A new random mix each time, so the drill stays fresh.
     */
    fun buildSession(pattern: ErrorPattern, random: Random = Random.Default): List<Exercise> {
        val drill = drills[pattern] ?: return emptyList()
        val pairs = drill.pairs.shuffled(random)
        val prefix = "sound_${pattern.name.lowercase()}"
        val exercises = mutableListOf<Exercise>()

        pairs.take(4).forEachIndexed { i, p ->
            val audio = if (random.nextBoolean()) p.target else p.other
            val options = listOf(p.target, p.other).shuffled(random)
            exercises += ListeningChallenge(
                id = "${prefix}_listen_${p.target}",
                audioText = audio,
                mode = ListeningMode.SELECT,
                options = options,
                correctIndex = options.indexOf(audio),
                translationAr = if (audio == p.target) p.targetAr else p.otherAr,
                explanationAr = "سمعت «$audio». ${drill.pattern.explanationAr}",
                pattern = pattern,
            )
            if (i == 1) {
                val four = pairs.take(4)
                exercises += MatchPairs(
                    id = "${prefix}_match",
                    pairs = four.map { MatchPair(it.target, it.targetAr) },
                    mode = MatchMode.AUDIO_TO_ARABIC,
                    pattern = pattern,
                )
            }
        }
        pairs.take(4).forEach { p ->
            exercises += SpeakingChallenge(
                id = "${prefix}_say_${p.target}",
                targetText = p.target,
                arabic = p.targetAr,
                arabicPhonetic = p.targetPhonetic,
                tipAr = drill.mouthTipAr,
                confusables = listOf(p.other),
                pattern = pattern,
            )
        }
        return exercises
    }
}
