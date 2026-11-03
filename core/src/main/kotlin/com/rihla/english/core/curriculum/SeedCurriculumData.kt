package com.rihla.english.core.curriculum

import com.rihla.english.core.model.CourseUnit
import com.rihla.english.core.model.ErrorPattern
import com.rihla.english.core.model.MatchMode

/**
 * Pre-populated A0/A1 English course for native Arabic speakers.
 *
 * Pedagogical notes
 *  - All instructions/explanations are Arabic; all English is rendered LTR by the UI.
 *  - Unit 1 targets sounds Arabic lacks: /p/ vs /b/, /v/ vs /f/, digraphs (sh, ch, th), silent letters.
 *  - Unit 3 uses Grammar Bridges to show that Arabic nominal sentences ("أنا طالب") need the
 *    English copula and article ("I am a student").
 *
 * Bump [VERSION] whenever content changes: the app re-seeds Room while keeping progress.
 */
object SeedCurriculumData {

    const val VERSION = 2

    val units: List<CourseUnit> by lazy { listOf(unit1(), unit2(), unit3(), unit4(), unit5(), unit6()) }

    // =====================================================================================
    // UNIT 1 — Alphabet & Essential Phonics
    // =====================================================================================
    private fun unit1() = courseUnit(
        id = "u1", order = 0,
        titleAr = "الوحدة ١: الحروف والأصوات الأساسية",
        titleEn = "Alphabet & Essential Phonics",
        descriptionAr = "تعرّف على الحروف الكبيرة والصغيرة، وأتقن الأصوات غير الموجودة في العربية مثل P و V.",
        color = 0xFF58CC02,
    ) {
        lesson(
            "الحروف A إلى F", "Letters A–F", "🔤",
            intro = "الإنجليزية تُكتب من اليسار إلى اليمين، ولكل حرف شكلان: كبير (Capital) وصغير (small). لا توجد حروف متصلة كما في العربية!",
        ) {
            card("Apple", "تفاحة", "🍎", "آبِل", "/ˈæp.əl/", "حرف A هنا يُنطق مثل فتحة ممدودة قليلًا (أَ).", letters = "A a")
            card("Ball", "كرة", "⚽", "بُول", "/bɔːl/", "B مثل الباء العربية تمامًا.", letters = "B b")
            card("Cat", "قطة", "🐱", "كات", "/kæt/", "C تُنطق غالبًا (ك)، وأحيانًا (س) كما في city.", letters = "C c")
            card("Dog", "كلب", "🐶", "دُوغ", "/dɒɡ/", "D مثل الدال العربية.", letters = "D d")
            card("Egg", "بيضة", "🥚", "إِغ", "/eɡ/", "E القصيرة تُنطق مثل كسرة مفتوحة (إِ).", letters = "E e")
            card("Fish", "سمكة", "🐟", "فِش", "/fɪʃ/", "F مثل الفاء العربية تمامًا.", letters = "F f")
            match("A" to "a", "B" to "b", "D" to "d", "E" to "e", mode = MatchMode.UPPER_TO_LOWER)
            choice(
                "ما الحرف الصغير المقابل للحرف الكبير؟", prompt = "D",
                options = listOf("d", "b", "p", "q"), answer = "d", english = true,
                why = "انتبه: d الصغير بطنه على اليسار وعصاه على اليمين، أما b فعصاه على اليسار.",
            )
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "Fish",
                options = listOf("سمكة", "قطة", "كرة", "بيضة"), answer = "سمكة", english = false,
                why = "Fish تعني سمكة 🐟. تبدأ بحرف F الذي يُنطق مثل الفاء.", audio = "Fish",
            )
            hear("Cat", listOf("Cat", "Dog", "Egg", "Ball"), "قطة", "سمعت كلمة Cat وتعني قطة. لاحظ أن C هنا تُنطق (ك).")
            match("Apple" to "تفاحة", "Ball" to "كرة", "Dog" to "كلب", "Egg" to "بيضة")
            say("Apple", "تفاحة", "آبِل", "افتح فمك جيدًا عند الصوت الأول (آ) ثم قل (بِل).")
        }

        lesson("الحروف G إلى M", "Letters G–M", "🔡") {
            card("Girl", "بنت", "👧", "غِيرل", "/ɡɜːl/", "G هنا تُنطق مثل الجيم المصرية (غ خفيفة).", letters = "G g")
            card("Hat", "قبعة", "🎩", "هات", "/hæt/", "H مثل الهاء العربية لكن أخف.", letters = "H h")
            card("Ice", "ثلج", "🧊", "آيس", "/aɪs/", "I هنا تُنطق (آي) مثل اسم الحرف.", letters = "I i")
            card("Juice", "عصير", "🧃", "جُوس", "/dʒuːs/", "J مثل الجيم العربية الفصحى.", letters = "J j")
            card("Key", "مفتاح", "🔑", "كِي", "/kiː/", "K مثل الكاف العربية.", letters = "K k")
            card("Lemon", "ليمونة", "🍋", "لِيمُن", "/ˈlem.ən/", letters = "L l")
            card("Moon", "قمر", "🌙", "مُون", "/muːn/", "oo تُنطق واوًا طويلة (و).", letters = "M m")
            match("G" to "g", "H" to "h", "J" to "j", "L" to "l", mode = MatchMode.UPPER_TO_LOWER)
            choice(
                "ما الحرف الكبير المقابل للحرف الصغير؟", prompt = "m",
                options = listOf("M", "N", "W", "H"), answer = "M", english = true,
                why = "m الصغير له قوسان، والكبير M له قمتان. لا تخلط بينه وبين W المقلوب.",
            )
            choice(
                "اختر الكلمة الإنجليزية التي تعني «مفتاح» 🔑",
                options = listOf("Key", "Girl", "Ice", "Hat"), answer = "Key", english = true,
                why = "Key = مفتاح. تُنطق (كِي) والحرف y في آخرها لا يُنطق منفصلًا.",
            )
            hear("Moon", listOf("Moon", "Lemon", "Key", "Ice"), "قمر", "Moon تعني قمر 🌙، و oo فيها واو طويلة.")
            match("Key" to "مفتاح", "Moon" to "قمر", "Juice" to "عصير", "Hat" to "قبعة")
            say("Key", "مفتاح", "كِي", "قلها بسرعة وبصوت كسرة طويلة: كِيي.")
        }

        lesson("الحروف N إلى T", "Letters N–T", "🔠") {
            card("Nose", "أنف", "👃", "نُوز", "/noʊz/", "s بين حرفين متحركين تُنطق أحيانًا (ز).", letters = "N n")
            card("Orange", "برتقالة", "🍊", "أُورِنج", "/ˈɒr.ɪndʒ/", letters = "O o")
            card(
                "Pen", "قلم", "🖊️", "پِن", "/pen/",
                "صوت P غير موجود في العربية! انطق الباء مع نفخة هواء قوية ومن دون اهتزاز الحنجرة.", letters = "P p",
            )
            card("Queen", "ملكة", "👑", "كْوِين", "/kwiːn/", "Q تأتي دائمًا تقريبًا مع u وتُنطقان معًا (كو).", letters = "Q q")
            card("Rain", "مطر", "🌧️", "رِين", "/reɪn/", "R الإنجليزية لا تُكرَّر مثل الراء العربية؛ اللسان لا يلمس سقف الفم.", letters = "R r")
            card("Sun", "شمس", "☀️", "سَن", "/sʌn/", letters = "S s")
            card("Tea", "شاي", "🍵", "تِي", "/tiː/", "T أخف من التاء العربية، مع نفخة هواء بسيطة.", letters = "T t")
            match("N" to "n", "Q" to "q", "R" to "r", "T" to "t", mode = MatchMode.UPPER_TO_LOWER)
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "Sun",
                options = listOf("شمس", "قمر", "مطر", "شاي"), answer = "شمس", english = false,
                why = "Sun = شمس ☀️. أما القمر فهو Moon.", audio = "Sun",
            )
            hear("Queen", listOf("Queen", "Green", "Rain", "Pen"), "ملكة", "Queen تبدأ بصوت (كو). qu تُنطق (كو) دائمًا تقريبًا.")
            match("Nose" to "أنف", "Orange" to "برتقالة", "Rain" to "مطر", "Tea" to "شاي")
            say("Sun", "شمس", "سَن", "u القصيرة هنا تُنطق فتحة قصيرة: سَن.")
        }

        lesson("الحروف U إلى Z", "Letters U–Z", "🅰️") {
            card("Umbrella", "مظلة", "☂️", "أَمبرِيلا", "/ʌmˈbrel.ə/", letters = "U u")
            card(
                "Van", "شاحنة صغيرة", "🚐", "ڤان", "/væn/",
                "V غير موجود في العربية: ضع أسنانك العليا على شفتك السفلى وانطق الفاء مع اهتزاز الحنجرة.", letters = "V v",
            )
            card("Water", "ماء", "💧", "ووتَر", "/ˈwɔː.tər/", "W: اضمم شفتيك كأنك تقول (و).", letters = "W w")
            card("Box", "صندوق", "📦", "بُوكس", "/bɒks/", "X في آخر الكلمة تُنطق (كس).", letters = "X x")
            card("Yellow", "أصفر", "🟡", "يِيلو", "/ˈjel.oʊ/", "Y في أول الكلمة مثل الياء العربية.", letters = "Y y")
            card("Zoo", "حديقة حيوانات", "🦓", "زُو", "/zuː/", letters = "Z z")
            match("U" to "u", "W" to "w", "Y" to "y", "Z" to "z", mode = MatchMode.UPPER_TO_LOWER)
            choice(
                "كم عدد حروف الأبجدية الإنجليزية؟",
                options = listOf("٢٦", "٢٨", "٢٤", "٣٠"), answer = "٢٦", english = false,
                why = "الأبجدية الإنجليزية ٢٦ حرفًا، بينما الأبجدية العربية ٢٨ حرفًا.",
            )
            hear("Water", listOf("Water", "Yellow", "Zoo", "Box"), "ماء", "Water تعني ماء 💧، وتبدأ بصوت (و).")
            match("Umbrella" to "مظلة", "Water" to "ماء", "Yellow" to "أصفر", "Zoo" to "حديقة حيوانات")
            say("Zoo", "حديقة حيوانات", "زُو", "Z مثل الزاي العربية تمامًا، ثم واو طويلة.")
        }

        chest()

        lesson(
            "تحدي P و B", "P vs B", "🅿️",
            pattern = ErrorPattern.SOUND_P_B,
            intro = "العربية فيها (ب) فقط، أما الإنجليزية ففيها B و P.\nصوت P = باء مع نفخة هواء قوية. جرّب: ضع ورقة أمام فمك؛ مع P تتحرك الورقة، ومع B لا تتحرك!",
        ) {
            card("Pear", "كمثرى", "🍐", "پِير", "/peər/", "P: أغلق شفتيك ثم افتحهما بنفخة هواء قوية.")
            card("Bear", "دب", "🐻", "بِير", "/beər/", "B: مثل الباء العربية، والحنجرة تهتز.")
            card("Pin", "دبوس", "📌", "پِن", "/pɪn/", "P بنفخة هواء — بلا اهتزاز.")
            card("Bin", "سلة مهملات", "🗑️", "بِن", "/bɪn/", "B مع اهتزاز الحنجرة.")
            hear("Pear", listOf("Pear", "Bear"), "كمثرى", "Pear تبدأ بـ P (نفخة هواء) وتعني كمثرى، أما Bear فتبدأ بـ B وتعني دب.")
            hear("Bin", listOf("Pin", "Bin"), "سلة مهملات", "Bin = سلة مهملات (بـ B بلا نفخة)، و Pin = دبوس (بـ P مع نفخة).")
            hear("Bear", listOf("Bear", "Pear"), "دب", "في Bear لا توجد نفخة هواء، والحنجرة تهتز: إنها B.")
            choice(
                "أي كلمة تعني «دبوس» 📌؟",
                options = listOf("Pin", "Bin", "Pen", "Bear"), answer = "Pin", english = true,
                why = "Pin = دبوس. لا تخلط بينها وبين Bin (سلة مهملات) — الفرق فقط في P و B!",
            )
            match("Pear" to "كمثرى", "Bear" to "دب", "Pin" to "دبوس", "Bin" to "سلة مهملات", mode = MatchMode.AUDIO_TO_ARABIC)
            hear("Pin", listOf("Pin", "Bin"), "دبوس", "استمع لنفخة الهواء في بداية Pin.")
            say("Pear", "كمثرى", "پِير", "انفخ الهواء مع P: ضع يدك أمام فمك لتشعر بالنفخة.", confusables = listOf("bear"))
            say("Bear", "دب", "بِير", "B بلا نفخة هواء، مثل الباء العربية.", confusables = listOf("pear"))
        }

        lesson(
            "تحدي F و V", "F vs V", "✌️",
            pattern = ErrorPattern.SOUND_V_F,
            intro = "صوت F = الفاء العربية تمامًا.\nأما V فله وضع الفم نفسه (الأسنان العليا على الشفة السفلى) لكن مع اهتزاز الحنجرة. ضع يدك على حلقك: مع V ستشعر بالاهتزاز!",
        ) {
            card("Fan", "مروحة", "🌀", "فان", "/fæn/", "F = ف بلا اهتزاز.")
            card("Van", "شاحنة صغيرة", "🚐", "ڤان", "/væn/", "V = ف مع اهتزاز الحنجرة (ڤ).")
            card("Fine", "بخير", "👍", "فاين", "/faɪn/")
            card("Vine", "كرمة العنب", "🍇", "ڤاين", "/vaɪn/")
            card("Very", "جدًا", "✨", "ڤيري", "/ˈver.i/", "لا تقل (فيري)! الحرف الأول V يهتز.")
            card("Five", "خمسة", "5️⃣", "فايڤ", "/faɪv/", "تبدأ بـ F وتنتهي بـ V: فايڤ.")
            hear("Van", listOf("Fan", "Van"), "شاحنة صغيرة", "Van تبدأ بـ V المهتزة وتعني شاحنة صغيرة، و Fan تعني مروحة.")
            hear("Fine", listOf("Fine", "Vine"), "بخير", "Fine تبدأ بـ F (فاء عادية) وتعني بخير.")
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "Very",
                options = listOf("جدًا", "بخير", "خمسة", "مروحة"), answer = "جدًا", english = false,
                why = "Very = جدًا. مثال: Very good = جيد جدًا.", audio = "Very",
            )
            match("Fan" to "مروحة", "Van" to "شاحنة صغيرة", "Fine" to "بخير", "Five" to "خمسة", mode = MatchMode.AUDIO_TO_ARABIC)
            hearBuild("Very fine", listOf("fan", "vine", "five"), "بخير جدًا", "Very تبدأ بـ V و fine تبدأ بـ F — لاحظ الفرق.")
            say("Very", "جدًا", "ڤيري", "عضّ شفتك السفلى بلطف وأخرج صوتًا مهتزًا: ڤ ڤ ڤيري.", confusables = listOf("ferry"))
            say("Five", "خمسة", "فايڤ", "ابدأ بفاء عادية واختم بـ V مهتزة.")
        }

        lesson(
            "sh و ch و th والحروف الصامتة", "Digraphs & Silent Letters", "🤫",
            pattern = ErrorPattern.SOUND_TH,
            intro = "بعض الحروف تجتمع لتصنع صوتًا واحدًا جديدًا:\nsh = ش   •   ch = تش   •   th = ث أو ذ\nوبعض الحروف تُكتب ولا تُنطق، مثل k في knife!",
        ) {
            card("Ship", "سفينة", "🚢", "شِب", "/ʃɪp/", "sh = ش")
            card("Chair", "كرسي", "🪑", "تشِير", "/tʃeər/", "ch = تش")
            card("Three", "ثلاثة", "3️⃣", "ثْرِي", "/θriː/", "th هنا = ث: أخرج طرف لسانك قليلًا بين أسنانك.")
            card("This", "هذا", "👉", "ذِس", "/ðɪs/", "th هنا = ذ مع اهتزاز.")
            card("Knife", "سكين", "🔪", "نايف", "/naɪf/", "حرف k صامت! لا تنطقه.")
            card("Write", "يكتب", "✍️", "رايت", "/raɪt/", "حرف w صامت في write.")
            card("Hour", "ساعة (٦٠ دقيقة)", "⏰", "آوَر", "/aʊər/", "حرف h صامت في hour.")
            choice(
                "ما الحرف الصامت (الذي لا يُنطق) في كلمة Knife؟", prompt = "Knife",
                options = listOf("k", "n", "i", "f"), answer = "k", english = true,
                why = "في Knife حرف k صامت، فتُنطق (نايف). وكذلك know و knee.",
            )
            choice(
                "الحرفان ch معًا يُنطقان مثل:", prompt = "ch",
                options = listOf("تش", "ش", "ك", "خ"), answer = "تش", english = false,
                why = "ch = تش كما في Chair (تشير). أما sh فهي (ش).",
            )
            hear("Ship", listOf("Ship", "Chip"), "سفينة", "Ship تبدأ بـ sh (ش)، أما Chip فتبدأ بـ ch (تش).")
            hear("Three", listOf("Three", "Tree"), "ثلاثة", "Three = ثْري (ثلاثة) و Tree = تري (شجرة). أخرج لسانك مع th!")
            match("Ship" to "سفينة", "Chair" to "كرسي", "Knife" to "سكين", "Hour" to "ساعة (٦٠ دقيقة)")
            say("Three", "ثلاثة", "ثْرِي", "ضع طرف لسانك بين أسنانك وانطق (ث) ثم (ري).", confusables = listOf("tree"))
            say("Chair", "كرسي", "تشِير", "ابدأ بـ (ت) ثم (ش) بسرعة معًا: تش.")
        }

        chest("كنز نهاية الوحدة")
    }

    // =====================================================================================
    // UNIT 2 — First Words & Greetings
    // =====================================================================================
    private fun unit2() = courseUnit(
        id = "u2", order = 1,
        titleAr = "الوحدة ٢: كلماتك الأولى والتحيات",
        titleEn = "First Words & Greetings",
        descriptionAr = "قل مرحبًا، عرّف بنفسك، وتعلّم الضمائر والأرقام والألوان.",
        color = 0xFF1CB0F6,
    ) {
        lesson(
            "مرحبًا ووداعًا", "Hello & Goodbye", "👋",
            intro = "التحية في الإنجليزية بسيطة: Hello للجميع، و Hi بين الأصدقاء، و Goodbye عند المغادرة.",
        ) {
            card("Hello", "مرحبًا", "👋", "هَلُو", "/həˈloʊ/")
            card("Hi", "أهلًا", "🙋", "هاي", "/haɪ/", "تحية غير رسمية بين الأصدقاء.")
            card("Goodbye", "مع السلامة", "🫡", "غُودباي", "/ɡʊdˈbaɪ/")
            card("Good morning", "صباح الخير", "🌅", "غُود مُورنِنغ", "/ɡʊd ˈmɔː.nɪŋ/", "ng في آخر الكلمة تُنطق نونًا أنفية خفيفة.")
            card("Thank you", "شكرًا لك", "🙏", "ثانك يُو", "/ˈθæŋk juː/", "th = ث: أخرج طرف لسانك.")
            card("Please", "من فضلك", "🥺", "پْلِيز", "/pliːz/", "P مع نفخة هواء!")
            choice(
                "كيف تقول «صباح الخير»؟",
                options = listOf("Good morning", "Goodbye", "Good night", "Hello"), answer = "Good morning", english = true,
                why = "Good morning = صباح الخير. أما Good night فتعني تصبح على خير وتُقال قبل النوم.",
            )
            bank("شكرًا لك", "Thank you", listOf("please", "hello", "good"), "Thank you = شكرًا لك. ويمكن أيضًا قول Thanks.", alternatives = listOf("Thanks"))
            match("Hello" to "مرحبًا", "Goodbye" to "مع السلامة", "Please" to "من فضلك", "Thank you" to "شكرًا لك")
            hear("Goodbye", listOf("Goodbye", "Good morning", "Hello", "Thank you"), "مع السلامة", "Goodbye تعني مع السلامة.")
            bank("مرحبًا، صباح الخير", "Hello good morning", listOf("goodbye", "night", "please"), "Hello = مرحبًا و good morning = صباح الخير.", alternatives = listOf("Hi good morning"))
            hearBuild("Good morning", listOf("night", "hello", "bye"), "صباح الخير", "Good + morning = صباح الخير.")
            say("Hello", "مرحبًا", "هَلُو", "الهاء خفيفة، والتركيز على المقطع الثاني: هَـ لُو.")
            say("Thank you", "شكرًا لك", "ثانك يُو", "ابدأ بـ (ث) وليس (س) أو (ت).", variants = listOf("Thanks", "Thank you very much"))
        }

        lesson(
            "عرّف بنفسك", "Introduce Yourself", "🙋",
            intro = "My name is … = اسمي …\nلاحظ أن الإنجليزية تحتاج كلمة is في الوسط، بينما العربية تقول «اسمي سارة» فقط.",
            grammarBridge = bridge(
                listOf("اسمي", "سارة"),
                "My" to 0, "name" to 0, "is" to null, "Sara" to 1,
                explanation = "«اسمي» = My name (كلمتان!) ثم نضيف is التي لا تُكتب في العربية.",
            ),
        ) {
            card("My name is", "اسمي", "📛", "ماي نِيم إز", "/maɪ neɪm ɪz/")
            card("What is your name?", "ما اسمك؟", "❓", "وَت إز يُور نِيم؟", "/wɒt ɪz jɔːr neɪm/")
            card("Nice to meet you", "سعيد بلقائك", "🤝", "نايس تُو مِيت يُو", "/naɪs tə miːt juː/")
            card("I am from", "أنا من", "🌍", "آي آم فرُوم", "/aɪ æm frɒm/")
            card("Yes", "نعم", "✅", "يِس", "/jes/")
            card("No", "لا", "❌", "نُو", "/noʊ/")
            bank(
                "اسمي سارة", "My name is Sara", listOf("am", "your", "I"),
                "في الإنجليزية نقول My name IS Sara — كلمة is ضرورية ولا مقابل لها في «اسمي سارة».",
                grammar = bridge(listOf("اسمي", "سارة"), "My" to 0, "name" to 0, "is" to null, "Sara" to 1, explanation = "is كلمة مخفية في العربية لكنها إلزامية في الإنجليزية."),
            )
            bank("ما اسمك؟", "What is your name", listOf("my", "are", "you"), "your = ـك (للمخاطب) و my = ـي (للمتكلم).", alternatives = listOf("What's your name"))
            bank(
                "أنا من مصر", "I am from Egypt", listOf("is", "my", "to"),
                "«أنا من مصر» تحتاج am بعد I: I am from Egypt.",
                alternatives = listOf("I'm from Egypt"),
                grammar = bridge(listOf("أنا", "من", "مصر"), "I" to 0, "am" to null, "from" to 1, "Egypt" to 2, explanation = "am هي الفعل المخفي بعد «أنا»."),
            )
            choice(
                "ماذا تقول عندما تقابل شخصًا لأول مرة؟",
                options = listOf("Nice to meet you", "Goodbye", "Good night", "No"), answer = "Nice to meet you", english = true,
                why = "Nice to meet you = سعيد بلقائك، وتُقال عند التعارف.",
            )
            match("Yes" to "نعم", "No" to "لا", "Nice to meet you" to "سعيد بلقائك", "My name is" to "اسمي")
            hearBuild("Nice to meet you", listOf("my", "name", "is"), "سعيد بلقائك", "Nice to meet you — أربع كلمات تُقال كأنها كلمة واحدة.")
            say("Nice to meet you", "سعيد بلقائك", "نايس تُو مِيت يُو", "صِل الكلمات ببعضها بسلاسة.")
            say("My name is Ali", "اسمي علي", "ماي نِيم إز علي", "لا تنسَ is في الوسط!", variants = listOf("My name's Ali"))
        }

        lesson(
            "الضمائر", "Pronouns", "👥",
            intro = "الضمائر الإنجليزية أبسط من العربية: You تعني أنتَ وأنتِ وأنتم وأنتن معًا، و They لكل أنواع الجمع.\nتحذير: He = هو، و She = هي!",
        ) {
            card("I", "أنا", "🙋", "آي", "/aɪ/", "I تُكتب دائمًا بحرف كبير، حتى في وسط الجملة.")
            card("You", "أنتَ / أنتِ / أنتم", "👉", "يُو", "/juː/", "You واحدة للمذكر والمؤنث والجمع.")
            card("He", "هو", "👨", "هِي", "/hiː/", "انتبه: He تعني «هو» رغم أن نطقها يشبه «هي»!")
            card("She", "هي", "👩", "شِي", "/ʃiː/", "She = هي (للمؤنث). sh = ش.")
            card("We", "نحن", "👨‍👩‍👧", "وِي", "/wiː/")
            card("They", "هم / هنّ", "👥", "ذَي", "/ðeɪ/", "th هنا = ذ.")
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "He",
                options = listOf("هو", "هي", "نحن", "أنا"), answer = "هو", english = false,
                why = "He = هو. أما «هي» فهي She. نطق He يشبه «هي» العربية — لا تقع في هذا الفخ!",
                audio = "He",
            )
            choice(
                "اختر الضمير الإنجليزي المناسب لـ «هي»",
                options = listOf("She", "He", "We", "They"), answer = "She", english = true,
                why = "هي = She. تذكّر: She تبدأ بـ sh (ش) مثل «شي».",
            )
            match("I" to "أنا", "We" to "نحن", "They" to "هم / هنّ", "She" to "هي")
            hear("She", listOf("She", "He", "We", "They"), "هي", "She تبدأ بصوت (ش). He تبدأ بصوت (هـ).")
            choice(
                "كيف تقول «أنتِ» (للمؤنث)؟",
                options = listOf("You", "She", "I", "He"), answer = "You", english = true,
                why = "You تُستخدم للمخاطَب مذكرًا كان أو مؤنثًا، مفردًا أو جمعًا.",
            )
            hearBuild("You and I", listOf("he", "she", "we"), "أنت وأنا", "and = و. في الإنجليزية نقول عادة You and I (المتكلم أخيرًا).")
            say("They", "هم", "ذَي", "ضع لسانك بين أسنانك لصوت (ذ).")
            say("She", "هي", "شِي", "sh = ش ثم كسرة طويلة.")
        }

        chest()

        lesson(
            "الأرقام ١–١٠", "Numbers 1–10", "🔢",
            intro = "الأرقام 1 2 3 هي نفسها الأرقام ١ ٢ ٣ لكن بشكل مختلف. وتُكتب الأعداد الإنجليزية من اليسار إلى اليمين.",
        ) {
            card("One", "واحد", "1️⃣", "وَن", "/wʌn/", "تُكتب One لكن تُنطق (وَن)!")
            card("Two", "اثنان", "2️⃣", "تُو", "/tuː/", "حرف w صامت في two.")
            card("Four", "أربعة", "4️⃣", "فُور", "/fɔːr/")
            card("Six", "ستة", "6️⃣", "سِكس", "/sɪks/")
            card("Seven", "سبعة", "7️⃣", "سِڤِن", "/ˈsev.ən/", "v مهتزة: سِڤِن وليس سِفِن.")
            card("Eight", "ثمانية", "8️⃣", "إيت", "/eɪt/", "gh صامتة في eight.")
            card("Nine", "تسعة", "9️⃣", "ناين", "/naɪn/")
            card("Ten", "عشرة", "🔟", "تِن", "/ten/")
            match("One" to "١", "Two" to "٢", "Four" to "٤", "Six" to "٦")
            match("Seven" to "٧", "Eight" to "٨", "Nine" to "٩", "Ten" to "١٠")
            choice(
                "ما الرقم؟", prompt = "Eight",
                options = listOf("٨", "٣", "٦", "١٠"), answer = "٨", english = false,
                why = "Eight = ٨ (8). تُنطق (إيت) لأن gh صامتة.", audio = "Eight",
            )
            hear("Seven", listOf("Seven", "Six", "Ten", "Nine"), "سبعة", "Seven = ٧. لاحظ صوت v المهتز في الوسط.")
            hearBuild("Three two one", listOf("four", "ten", "nine"), "ثلاثة، اثنان، واحد", "عدّ تنازلي: Three, two, one!")
            say("Ten", "عشرة", "تِن", "T خفيفة بنفخة هواء.")
            say("Eight", "ثمانية", "إيت", "تجاهل gh تمامًا.", variants = listOf("8"))
        }

        lesson(
            "الألوان", "Colors", "🎨",
            pattern = ErrorPattern.ADJECTIVE_ORDER,
            intro = "في الإنجليزية الصفة تأتي قبل الاسم: «سيارة حمراء» = a red car (اللون أولًا!).",
            grammarBridge = bridge(
                listOf("سيارة", "حمراء"),
                "a" to null, "red" to 1, "car" to 0,
                explanation = "الترتيب معكوس: الصفة red قبل الاسم car، ونضيف a قبل الاسم المفرد.",
            ),
        ) {
            card("Red", "أحمر", "🔴", "رِد", "/red/")
            card("Blue", "أزرق", "🔵", "بْلُو", "/bluː/")
            card("Green", "أخضر", "🟢", "غْرِين", "/ɡriːn/")
            card("Black", "أسود", "⚫", "بْلاك", "/blæk/")
            card("White", "أبيض", "⚪", "وايت", "/waɪt/", "h شبه صامتة في white.")
            card("Car", "سيارة", "🚗", "كار", "/kɑːr/")
            bank(
                "سيارة حمراء", "a red car", listOf("the", "blue", "is"),
                "الصفة (red) تأتي قبل الاسم (car) في الإنجليزية، عكس العربية.",
                alternatives = listOf("red car"),
                grammar = bridge(listOf("سيارة", "حمراء"), "a" to null, "red" to 1, "car" to 0, explanation = "red ← حمراء و car ← سيارة: الترتيب ينقلب!"),
            )
            bank("قلم أزرق", "a blue pen", listOf("red", "the", "is"), "اللون blue قبل الاسم pen.", alternatives = listOf("blue pen"))
            match("Red" to "أحمر", "Blue" to "أزرق", "Green" to "أخضر", "Black" to "أسود")
            choice(
                "ما لون الشمس ☀️ بالإنجليزية؟",
                options = listOf("Yellow", "Blue", "Black", "Green"), answer = "Yellow", english = true,
                why = "Yellow = أصفر 🟡. تعلّمناها في الوحدة الأولى.",
            )
            hear("Green", listOf("Green", "Queen", "Red", "Blue"), "أخضر", "Green تبدأ بـ (غر)، أما Queen فتبدأ بـ (كو).")
            say("Blue", "أزرق", "بْلُو", "B ثم L مباشرة بدون حركة بينهما: بْلُو.")
            say("White", "أبيض", "وايت", "اضمم شفتيك للـ W في البداية.")
        }

        chest("كنز نهاية الوحدة")
    }

    // =====================================================================================
    // UNIT 3 — Building Your First Sentences (am / is / are)
    // =====================================================================================
    private fun unit3() = courseUnit(
        id = "u3", order = 2,
        titleAr = "الوحدة ٣: بناء جملك الأولى",
        titleEn = "Building Your First Sentences",
        descriptionAr = "اكتشف الفعل المخفي am / is / are الذي لا يظهر في الجملة الاسمية العربية.",
        color = 0xFFCE82FF,
    ) {
        lesson(
            "أنا طالب: الفعل am", "I am a student", "🧱",
            pattern = ErrorPattern.MISSING_BE,
            intro = "في العربية نقول «أنا طالب» بلا فعل. في الإنجليزية لا بد من فعل الكون am بعد I، ومن الأداة a قبل الاسم المفرد.",
            grammarBridge = bridge(
                listOf("أنا", "طالب"),
                "I" to 0, "am" to null, "a" to null, "student" to 1,
                explanation = "كلمتان عربيتان = أربع كلمات إنجليزية! am و a لا مقابل لهما في العربية.",
            ),
        ) {
            card("Student", "طالب", "🧑‍🎓", "ستيُودِنت", "/ˈstjuː.dənt/")
            card("Teacher", "معلّم", "👩‍🏫", "تِيتشَر", "/ˈtiː.tʃər/", "ch = تش")
            card("Doctor", "طبيب", "🩺", "دُكتَر", "/ˈdɒk.tər/")
            card("Happy", "سعيد", "😊", "هاپي", "/ˈhæp.i/", "pp = P بنفخة هواء.")
            card("Tired", "متعب", "😴", "تايرد", "/taɪərd/")
            card("I am", "أنا (أكون)", "🙋", "آي آم", "/aɪ æm/", "تُختصر في الكلام: I'm = آيم.")
            bank(
                "أنا طالب", "I am a student", listOf("is", "are", "the"),
                "«أنا طالب» = I am a student. لا تحذف am ولا a!",
                alternatives = listOf("I'm a student"),
                grammar = bridge(listOf("أنا", "طالب"), "I" to 0, "am" to null, "a" to null, "student" to 1, explanation = "am و a مخفيتان في العربية."),
            )
            bank("أنا معلّم", "I am a teacher", listOf("is", "an", "you"), "I am a teacher. مع I نستخدم am دائمًا.", alternatives = listOf("I'm a teacher"))
            bank(
                "أنا سعيد", "I am happy", listOf("a", "is", "he"),
                "مع الصفات (happy) لا نضع a: I am happy.",
                alternatives = listOf("I'm happy"),
                grammar = bridge(listOf("أنا", "سعيد"), "I" to 0, "am" to null, "happy" to 1, explanation = "مع الصفة: am فقط، بدون a."),
            )
            choice(
                "أكمل الجملة:", prompt = "I ___ a doctor.",
                options = listOf("am", "is", "are", "be"), answer = "am", english = true,
                why = "مع I نستخدم دائمًا am: I am a doctor.",
            )
            choice(
                "اختر الجملة الصحيحة",
                options = listOf("I am tired.", "I tired.", "I is tired.", "I are tired."), answer = "I am tired.", english = true,
                why = "لا تحذف am! الجملة الإنجليزية تحتاج فعلًا دائمًا: I am tired.",
            )
            hearBuild("I am a doctor", listOf("is", "teacher", "are"), "أنا طبيب", "I am a doctor = أنا طبيب.")
            match("Student" to "طالب", "Teacher" to "معلّم", "Doctor" to "طبيب", "Tired" to "متعب")
            say("I am a student", "أنا طالب", "آي آم أ ستيُودِنت", "قلها ببطء أولًا ثم بسرعة.", variants = listOf("I'm a student"))
        }

        lesson(
            "is و are", "He is / They are", "🔗",
            pattern = ErrorPattern.MISSING_BE,
            intro = "am مع I فقط\nis مع المفرد: he, she, it\nare مع you, we, they",
            grammarBridge = bridge(
                listOf("هي", "طبيبة"),
                "She" to 0, "is" to null, "a" to null, "doctor" to 1,
                explanation = "مع She نستخدم is. ولاحظ أن doctor لا تتغير للمؤنث!",
            ),
        ) {
            card("He is", "هو (يكون)", "👨", "هِي إز", "/hiː ɪz/", "تُختصر: He's.")
            card("She is", "هي (تكون)", "👩", "شِي إز", "/ʃiː ɪz/", "تُختصر: She's.")
            card("It is", "هو / هي (لغير العاقل)", "🐱", "إت إز", "/ɪt ɪz/", "It للأشياء والحيوانات.")
            card("You are", "أنت / أنتم", "👉", "يُو آر", "/juː ɑːr/")
            card("We are", "نحن", "👨‍👩‍👧", "وِي آر", "/wiː ɑːr/")
            card("They are", "هم", "👥", "ذَي آر", "/ðeɪ ɑːr/")
            choice(
                "أكمل الجملة:", prompt = "He ___ happy.",
                options = listOf("is", "am", "are", "be"), answer = "is", english = true,
                why = "He (مفرد غائب) يأخذ is: He is happy.",
            )
            choice(
                "أكمل الجملة:", prompt = "They ___ students.",
                options = listOf("are", "is", "am", "be"), answer = "are", english = true,
                why = "They (جمع) يأخذ are: They are students. ولاحظ s الجمع في students.",
            )
            choice(
                "أكمل الجملة:", prompt = "You ___ a teacher.",
                options = listOf("are", "is", "am", "be"), answer = "are", english = true,
                why = "مع You نستخدم are دائمًا، حتى لو كان المخاطَب شخصًا واحدًا.",
            )
            bank(
                "هي طبيبة", "She is a doctor", listOf("are", "he", "am"),
                "She is a doctor. المهن في الإنجليزية لا تتغير للمؤنث.",
                alternatives = listOf("She's a doctor"),
                grammar = bridge(listOf("هي", "طبيبة"), "She" to 0, "is" to null, "a" to null, "doctor" to 1, explanation = "is و a مخفيتان في العربية."),
            )
            bank("نحن سعداء", "We are happy", listOf("is", "am", "a"), "We تأخذ are، والصفة happy لا تُجمع في الإنجليزية.", alternatives = listOf("We're happy"))
            bank("إنها قطة", "It is a cat", listOf("she", "are", "an"), "للحيوانات والأشياء نستخدم It is.", alternatives = listOf("It's a cat"))
            hearBuild("They are tired", listOf("is", "we", "am"), "هم متعبون", "They are tired = هم متعبون.")
            match("He is" to "هو (يكون)", "We are" to "نحن", "They are" to "هم", "It is" to "هو / هي (لغير العاقل)")
            say("She is happy", "هي سعيدة", "شِي إز هاپي", "ركّز على صوت sh في She.", variants = listOf("She's happy"))
        }

        lesson(
            "النفي: not", "Negatives with not", "🚫",
            pattern = ErrorPattern.MISSING_BE,
            intro = "للنفي نضع not بعد am / is / are مباشرة:\nI am not tired = أنا لستُ متعبًا.",
            grammarBridge = bridge(
                listOf("أنا", "لستُ", "متعبًا"),
                "I" to 0, "am" to 1, "not" to 1, "tired" to 2,
                explanation = "«لستُ» = am not (كلمتان). not تأتي دائمًا بعد am / is / are.",
            ),
        ) {
            card("Not", "ليس / لا", "🚫", "نُت", "/nɒt/")
            card("Sad", "حزين", "😢", "ساد", "/sæd/")
            card("Hungry", "جائع", "🍽️", "هَنغري", "/ˈhʌŋ.ɡri/")
            card("Busy", "مشغول", "📅", "بِيزي", "/ˈbɪz.i/", "u هنا تُنطق (ي) و s تُنطق (ز).")
            card("I am not", "أنا لستُ", "🙅", "آي آم نُت", "/aɪ æm nɒt/", "تُختصر: I'm not.")
            bank(
                "أنا لستُ جائعًا", "I am not hungry", listOf("is", "no", "a"),
                "نستخدم not (وليس no) لنفي الجملة: I am not hungry.",
                alternatives = listOf("I'm not hungry"),
            )
            bank("هو ليس حزينًا", "He is not sad", listOf("am", "no", "she"), "He is not sad. ويمكن اختصارها: He isn't sad.", alternatives = listOf("He isn't sad", "He's not sad"))
            bank("هم ليسوا مشغولين", "They are not busy", listOf("is", "no", "we"), "They are not busy. not بعد are مباشرة.", alternatives = listOf("They aren't busy", "They're not busy"))
            choice(
                "اختر النفي الصحيح للجملة:", prompt = "She is busy.",
                options = listOf("She is not busy.", "She not is busy.", "She no busy.", "She are not busy."), answer = "She is not busy.", english = true,
                why = "not تأتي بعد is مباشرة: She is not busy.",
            )
            choice(
                "ماذا تعني الجملة؟", prompt = "We are not tired.",
                options = listOf("نحن لسنا متعبين", "نحن متعبون", "هم ليسوا متعبين", "أنا لستُ متعبًا"), answer = "نحن لسنا متعبين", english = false,
                why = "We = نحن، و are not = لسنا. إذن: نحن لسنا متعبين.", audio = "We are not tired.",
            )
            hear(
                "He is not hungry",
                listOf("He is not hungry", "He is hungry", "She is not hungry", "He is not happy"),
                "هو ليس جائعًا", "انتبه لكلمة not ولصوت He (هـ) وليس She (ش).",
            )
            match("Sad" to "حزين", "Hungry" to "جائع", "Busy" to "مشغول", "Not" to "ليس / لا")
            say("I am not busy", "أنا لستُ مشغولًا", "آي آم نُت بِيزي", "يمكنك قولها مختصرة: I'm not busy.", variants = listOf("I'm not busy"))
        }

        chest()

        lesson(
            "أشياء يومية: This is", "This is a book", "📚",
            pattern = ErrorPattern.ARTICLES,
            intro = "This is = هذا / هذه (للقريب).\nمرة أخرى is مطلوبة: «هذا كتاب» = This is a book.\nونستخدم an بدل a قبل الصوت المتحرك: an apple.",
            grammarBridge = bridge(
                listOf("هذا", "كتاب"),
                "This" to 0, "is" to null, "a" to null, "book" to 1,
                explanation = "This لا تتغير للمذكر والمؤنث: هذا / هذه = This.",
            ),
        ) {
            card("Book", "كتاب", "📖", "بُك", "/bʊk/")
            card("Phone", "هاتف", "📱", "فُون", "/foʊn/", "ph تُنطق (ف)!")
            card("Door", "باب", "🚪", "دُور", "/dɔːr/")
            card("House", "بيت", "🏠", "هاوس", "/haʊs/")
            card("Bag", "حقيبة", "👜", "باغ", "/bæɡ/")
            card("Cup", "كوب", "☕", "كَپ", "/kʌp/", "تنتهي بـ P: انفخ الهواء في الآخر.")
            bank(
                "هذا كتاب", "This is a book", listOf("are", "the", "am"),
                "This is a book. لا تنسَ is و a!",
                grammar = bridge(listOf("هذا", "كتاب"), "This" to 0, "is" to null, "a" to null, "book" to 1, explanation = "is و a لا تظهران في العربية."),
            )
            bank("هذا ليس هاتفًا", "This is not a phone", listOf("are", "no", "am"), "النفي: This is not a phone.", alternatives = listOf("This isn't a phone"))
            bank("هذه حقيبة حمراء", "This is a red bag", listOf("are", "blue", "the"), "This is a red bag. الصفة red قبل الاسم bag.")
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "Phone",
                options = listOf("هاتف", "باب", "كتاب", "كوب"), answer = "هاتف", english = false,
                why = "Phone = هاتف 📱. ph تُنطق (ف).", audio = "Phone",
            )
            choice(
                "أكمل الجملة:", prompt = "This is ___ apple.",
                options = listOf("an", "a", "the", "is"), answer = "an", english = true,
                why = "نستخدم an قبل الكلمة التي تبدأ بصوت متحرك (a, e, i, o, u): an apple, an egg.",
            )
            hear(
                "This is a cup", listOf("This is a cup", "This is a cat", "This is a car", "This is a book"),
                "هذا كوب", "انتبه للفرق بين cup (كوب) و cat (قطة) و car (سيارة).",
            )
            match("Book" to "كتاب", "Door" to "باب", "House" to "بيت", "Bag" to "حقيبة")
            say("This is a book", "هذا كتاب", "ذِس إز أ بُك", "th في This = ذ.")
        }

        chest("كنز نهاية الوحدة")
    }

    // =====================================================================================
    // UNIT 4 — Daily Survival English
    // =====================================================================================
    private fun unit4() = courseUnit(
        id = "u4", order = 3,
        titleAr = "الوحدة ٤: الإنجليزية للحياة اليومية",
        titleEn = "Daily Survival English",
        descriptionAr = "تحدّث عن عائلتك، اطلب طعامك في المقهى، واستخدم أهم الأفعال اليومية.",
        color = 0xFFFF9600,
    ) {
        lesson(
            "العائلة", "Family", "👨‍👩‍👧‍👦",
            intro = "my = ـي (ضمير الملكية): my mother = أمّي.\nفي العربية الضمير يتصل بآخر الكلمة، أما في الإنجليزية فيأتي my قبلها.",
            grammarBridge = bridge(
                listOf("هذه", "أمّي"),
                "This" to 0, "is" to null, "my" to 1, "mother" to 1,
                explanation = "«أمّي» كلمة واحدة = my mother (كلمتان)، و is مخفية كالعادة.",
            ),
        ) {
            card("Mother", "أم", "👩", "مَذَر", "/ˈmʌð.ər/", "th = ذ.")
            card("Father", "أب", "👨", "فاذَر", "/ˈfɑː.ðər/")
            card("Brother", "أخ", "👦", "بْرَذَر", "/ˈbrʌð.ər/")
            card("Sister", "أخت", "👧", "سِستَر", "/ˈsɪs.tər/")
            card("Family", "عائلة", "👨‍👩‍👧‍👦", "فامِلي", "/ˈfæm.əl.i/")
            card("My", "ـي (ملكي)", "🫵", "ماي", "/maɪ/")
            bank(
                "هذه أمّي", "This is my mother", listOf("your", "a", "are"),
                "This is my mother. my قبل الاسم، و is ضرورية.",
                grammar = bridge(listOf("هذه", "أمّي"), "This" to 0, "is" to null, "my" to 1, "mother" to 1, explanation = "أمّي = my mother."),
            )
            bank("أخي طالب", "My brother is a student", listOf("am", "sister", "are"), "My brother is a student. is بعد الفاعل المفرد.", alternatives = listOf("My brother's a student"))
            bank("أختي سعيدة", "My sister is happy", listOf("are", "brother", "a"), "My sister is happy. لا نضع a قبل الصفة.")
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "Father",
                options = listOf("أب", "أم", "أخ", "أخت"), answer = "أب", english = false,
                why = "Father = أب، و Mother = أم.", audio = "Father",
            )
            match("Mother" to "أم", "Father" to "أب", "Brother" to "أخ", "Sister" to "أخت")
            hearBuild("This is my family", listOf("are", "your", "a"), "هذه عائلتي", "This is my family = هذه عائلتي.")
            say("My family", "عائلتي", "ماي فامِلي", "الضغط على المقطع الأول: فا-مِلي.")
            say("Mother", "أم", "مَذَر", "th = ذ: لسانك بين أسنانك.")
        }

        lesson(
            "الطعام والشراب", "Food & Drinks", "🍞",
            pattern = ErrorPattern.VERB_AS_ADJECTIVE,
            intro = "مع الأفعال مثل like (يحب) لا نستخدم am:\nI like tea ✅    I am like tea ❌",
            grammarBridge = bridge(
                listOf("أنا", "أحب", "الشاي"),
                "I" to 0, "like" to 1, "tea" to 2,
                explanation = "هنا يوجد فعل (أحب = like)، لذلك لا نحتاج am. ولا نضع the قبل الأطعمة بشكل عام.",
            ),
        ) {
            card("Bread", "خبز", "🍞", "بْرِد", "/bred/")
            card("Coffee", "قهوة", "☕", "كُوفي", "/ˈkɒf.i/", "ff = ف واحدة.")
            card("Rice", "أرز", "🍚", "رايس", "/raɪs/")
            card("Milk", "حليب", "🥛", "مِلك", "/mɪlk/")
            card("Chicken", "دجاج", "🍗", "تشِكِن", "/ˈtʃɪk.ɪn/", "ch = تش.")
            card("I like", "أنا أحب", "👍", "آي لايك", "/aɪ laɪk/")
            bank(
                "أنا أحب القهوة", "I like coffee", listOf("am", "a", "the"),
                "مع الفعل like لا نستخدم am: I like coffee (وليس I am like coffee).",
                grammar = bridge(listOf("أنا", "أحب", "القهوة"), "I" to 0, "like" to 1, "coffee" to 2, explanation = "لا يوجد am لأن الجملة فيها فعل."),
            )
            bank("أنا أحب الأرز والدجاج", "I like rice and chicken", listOf("is", "milk", "am"), "and = و. I like rice and chicken.")
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "Milk",
                options = listOf("حليب", "ماء", "خبز", "شاي"), answer = "حليب", english = false,
                why = "Milk = حليب 🥛.", audio = "Milk",
            )
            choice(
                "اختر الجملة الصحيحة",
                options = listOf("I like tea.", "I am like tea.", "I likes tea.", "Like I tea."), answer = "I like tea.", english = true,
                why = "مع I نقول like بدون s وبدون am: I like tea.",
            )
            match("Bread" to "خبز", "Coffee" to "قهوة", "Rice" to "أرز", "Chicken" to "دجاج")
            hear("Milk", listOf("Milk", "Rice", "Bread", "Coffee"), "حليب", "Milk = حليب. اللام فيها خفيفة.")
            say("Chicken", "دجاج", "تشِكِن", "ابدأ بـ (تش) بوضوح.")
            say("I like tea", "أنا أحب الشاي", "آي لايك تِي", "لا تضف am!")
        }

        lesson(
            "في المقهى", "At the Café", "☕",
            intro = "عند الطلب كن مهذبًا:\nplease = من فضلك\nCan I have …? = هل يمكنني الحصول على …؟\nHow much is it? = بكم هذا؟",
        ) {
            card("Can I have", "هل يمكنني الحصول على", "🙋", "كان آي هاڤ", "/kæn aɪ hæv/", "v في have مهتزة.")
            card("How much is it?", "بكم هذا؟", "💰", "هاو مَتش إز إت؟", "/haʊ mʌtʃ ɪz ɪt/")
            card("A cup of tea", "كوب شاي", "🍵", "أ كَپ أُڤ تِي", "/ə kʌp əv tiː/", "of تُنطق (أُڤ).")
            card("Here you are", "تفضّل", "🤲", "هِير يُو آر", "/hɪər juː ɑːr/")
            card("Sugar", "سكر", "🍬", "شُوغَر", "/ˈʃʊɡ.ər/", "s هنا تُنطق (ش)!")
            bank(
                "هل يمكنني الحصول على قهوة من فضلك؟", "Can I have a coffee please", listOf("am", "is", "much"),
                "Can I have a coffee, please? — أسلوب مهذب للطلب.",
                alternatives = listOf("Can I have coffee please"),
            )
            bank("بكم هذا؟", "How much is it", listOf("are", "many", "this"), "How much is it? = بكم هذا؟ (للسعر).", alternatives = listOf("How much is this"))
            bank("كوب شاي من فضلك", "A cup of tea please", listOf("milk", "an", "is"), "A cup of tea, please. of = من.")
            choice(
                "النادل يقول: Here you are. ماذا يعني؟", prompt = "Here you are.",
                options = listOf("تفضّل", "أين أنت؟", "مرحبًا", "بكم هذا؟"), answer = "تفضّل", english = false,
                why = "Here you are = تفضّل، وتُقال عند تقديم شيء لشخص.", audio = "Here you are.",
            )
            choice(
                "كيف تطلب الشاي بأدب؟",
                options = listOf("Can I have tea, please?", "Give tea!", "I am tea.", "Tea is me."), answer = "Can I have tea, please?", english = true,
                why = "Can I have …, please? هي الطريقة المهذبة للطلب.",
            )
            hearBuild("Can I have water please", listOf("is", "milk", "am"), "هل يمكنني الحصول على ماء من فضلك؟", "Can I have water, please?")
            match("Sugar" to "سكر", "Please" to "من فضلك", "Here you are" to "تفضّل", "How much is it?" to "بكم هذا؟")
            say("How much is it?", "بكم هذا؟", "هاو مَتش إز إت؟", "ch في much = تش.", variants = listOf("How much is this"))
            say("Thank you very much", "شكرًا جزيلًا", "ثانك يُو ڤيري مَتش", "تذكّر: V مهتزة في very.", variants = listOf("Thanks very much"))
        }

        chest()

        lesson(
            "أفعال شائعة", "Common Verbs", "🏃",
            pattern = ErrorPattern.VERB_AS_ADJECTIVE,
            intro = "الفعل في المضارع البسيط: I eat = أنا آكل.\nلا نضع am قبل الفعل! am / is / are تأتي مع الأسماء والصفات فقط.",
            grammarBridge = bridge(
                listOf("أنا", "آكل", "خبزًا"),
                "I" to 0, "eat" to 1, "bread" to 2,
                explanation = "لاحظ: لا يوجد am هنا! الترتيب مطابق للعربية تمامًا: فاعل + فعل + مفعول.",
            ),
        ) {
            card("Eat", "يأكل", "🍽️", "إيت", "/iːt/")
            card("Drink", "يشرب", "🥤", "دْرِنك", "/drɪŋk/")
            card("Go", "يذهب", "🚶", "غُو", "/ɡoʊ/")
            card("Read", "يقرأ", "📖", "رِيد", "/riːd/")
            card("Sleep", "ينام", "😴", "سْلِيپ", "/sliːp/", "تنتهي بـ P: نفخة هواء خفيفة.")
            card("Work", "يعمل / العمل", "💼", "وُرك", "/wɜːk/")
            bank(
                "أنا آكل الخبز", "I eat bread", listOf("am", "drink", "a"),
                "I eat bread. لا نضع am قبل الفعل eat.",
                grammar = bridge(listOf("أنا", "آكل", "الخبز"), "I" to 0, "eat" to 1, "bread" to 2, explanation = "فاعل + فعل + مفعول، بدون am."),
            )
            bank("نحن نشرب الماء", "We drink water", listOf("are", "eat", "is"), "We drink water. لا are مع الفعل!")
            bank("هم يذهبون إلى العمل", "They go to work", listOf("are", "is", "sleep"), "to = إلى. They go to work.")
            choice(
                "اختر الجملة الصحيحة",
                options = listOf("I read a book.", "I am read a book.", "I reading book.", "Read I a book."), answer = "I read a book.", english = true,
                why = "فاعل + فعل + مفعول: I read a book. بدون am.",
            )
            choice(
                "ماذا تعني هذه الكلمة؟", prompt = "Sleep",
                options = listOf("ينام", "يأكل", "يذهب", "يشرب"), answer = "ينام", english = false,
                why = "Sleep = ينام 😴.", audio = "Sleep",
            )
            match("Eat" to "يأكل", "Drink" to "يشرب", "Go" to "يذهب", "Sleep" to "ينام")
            hearBuild("I drink coffee", listOf("eat", "am", "tea"), "أنا أشرب القهوة", "I drink coffee = أنا أشرب القهوة.")
            say("I read a book", "أنا أقرأ كتابًا", "آي رِيد أ بُك", "ea في read = ي طويلة.")
            say("I go to work", "أنا أذهب إلى العمل", "آي غُو تُو وُرك", "صِل الكلمات: آي غُوتُو وُرك.")
        }

        chest("الكنز الأكبر 🏆")
    }

    // =====================================================================================
    // UNIT 5 — Arab Traps: the mistakes that come from thinking in Arabic
    // =====================================================================================
    private fun unit5() = courseUnit(
        id = "u5", order = 4,
        titleAr = "الوحدة ٥: فخاخ العربي",
        titleEn = "Arab Traps",
        descriptionAr = "أخطاء يقع فيها أغلب العرب لأننا نترجم من العربية. اكتشفها وصحّحها قبل أن تصبح عادة!",
        color = 0xFFFF6B5A,
    ) {
        lesson(
            "لا تنسَ am / is / are", "Don't drop the verb to be", "🧱",
            intro = "الخطأ رقم ١ عند العرب: «He late» بدل «He is late».\nفي الإنجليزية كل جملة تحتاج فعلًا. إذا لم يوجد فعل، أضف am / is / are.",
            pattern = ErrorPattern.MISSING_BE,
        ) {
            card("Ready", "جاهز", "✅", "رِيدي", "/ˈred.i/")
            card("Late", "متأخر", "⏰", "لِيت", "/leɪt/")
            card("Here", "هنا", "📍", "هِير", "/hɪər/")
            card("Open", "مفتوح", "🔓", "أوپن", "/ˈoʊ.pən/")
            fix("He late today", "He", listOf("He is", "He are", "His"), "He is", "هو متأخر اليوم", "نحتاج is بعد He: He is late today.")
            fix("The shop open now", "shop", listOf("shop is", "shop are", "shops"), "shop is", "المحل مفتوح الآن", "المحل مفرد، فنقول The shop is open.")
            fix("We ready", "We", listOf("We are", "We is", "We am"), "We are", "نحن جاهزون", "مع We نستخدم are: We are ready.")
            fix("I here", "I", listOf("I am", "I is", "I are"), "I am", "أنا هنا", "مع I نستخدم am دائمًا: I am here.")
            bank("الباب مفتوح", "The door is open", listOf("are", "a", "am"), "الباب مفرد: The door is open.")
            choice(
                "اختر الجملة الصحيحة",
                options = listOf("My father is a doctor.", "My father doctor.", "My father are doctor.", "My father a doctor."),
                answer = "My father is a doctor.", english = true,
                why = "«أبي طبيب» = My father is a doctor. نحتاج is و a.",
            )
            match("Ready" to "جاهز", "Late" to "متأخر", "Here" to "هنا", "Open" to "مفتوح")
            hearBuild("The coffee is ready", listOf("are", "a", "am"), "القهوة جاهزة", "The coffee is ready — لا تنسَ is.")
            say("I am ready", "أنا جاهز", "آي آم رِيدي", "يمكنك قولها مختصرة: I'm ready.", variants = listOf("I'm ready"))
        }

        lesson(
            "I agree وليس I am agree", "No 'am' before verbs", "🙅",
            intro = "العكس أيضًا خطأ شائع: نضع am قبل الفعل!\nI agree ✅   I am agree ❌\nI know ✅   I am know ❌\nam / is / are لا تأتي قبل الأفعال في المضارع البسيط.",
            pattern = ErrorPattern.VERB_AS_ADJECTIVE,
        ) {
            card("Agree", "يوافق", "🤝", "أَغْرِي", "/əˈɡriː/", "agree فعل وليست صفة: I agree.")
            card("Know", "يعرف", "🧠", "نُو", "/noʊ/", "k صامتة. والنفي: I don't know.")
            card("Want", "يريد", "🙋", "وُنْت", "/wɒnt/")
            card("Need", "يحتاج", "🆘", "نِيد", "/niːd/")
            fix("I am agree with you", "am", listOf("", "is", "are"), "", "أنا أوافقك", "agree فعل، فلا نضع قبله am: I agree with you.")
            fix("I am want coffee", "am", listOf("", "is", "do"), "", "أريد قهوة", "want فعل: I want coffee.")
            fix("We are need help", "are", listOf("", "is", "am"), "", "نحتاج مساعدة", "need فعل: We need help.")
            fix("I am not know", "am", listOf("do", "is", ""), "do", "لا أعرف", "نفي الفعل يكون بـ do not: I do not know (I don't know).")
            bank("أنا أوافقك", "I agree with you", listOf("am", "is", "to"), "I agree with you — بدون am.")
            choice(
                "اختر الجملة الصحيحة",
                options = listOf("I know the answer.", "I am know the answer.", "I knowing the answer.", "I am knows the answer."),
                answer = "I know the answer.", english = true,
                why = "know فعل: I know the answer.",
            )
            match("Agree" to "يوافق", "Know" to "يعرف", "Want" to "يريد", "Need" to "يحتاج")
            hear("I need help", listOf("I need help", "I am need help", "I needs help"), "أحتاج مساعدة", "I need help — لا am ولا s مع I.")
            say("I agree with you", "أنا أوافقك", "آي أَغْرِي وِذ يُو", "لا تضف am!")
        }

        lesson(
            "حرف s السحري", "He works, she likes", "🐍",
            intro = "في المضارع، إذا كان الفاعل he / she / it أو اسمًا مفردًا، نضيف s للفعل:\nI work → He works\nI like → She likes\nوالنفي: He doesn't (وليس He don't).",
            pattern = ErrorPattern.THIRD_PERSON_S,
        ) {
            card("Works", "يعمل (هو / هي)", "💼", "وُركس", "/wɜːks/", "work + s مع he / she.")
            card("Lives", "يسكن", "🏠", "لِڤز", "/lɪvz/")
            card("Goes", "يذهب (هو / هي)", "🚶", "غُوز", "/ɡoʊz/", "go تصبح goes.")
            card("Doesn't", "لا (للمفرد الغائب)", "🙅", "دَزِنْت", "/ˈdʌz.ənt/", "He doesn't = هو لا.")
            fix("She like tea", "like", listOf("likes", "liking", "is like"), "likes", "هي تحب الشاي", "مع She نضيف s: She likes tea.")
            fix("My brother work in Riyadh", "work", listOf("works", "working", "is work"), "works", "أخي يعمل في الرياض", "My brother = he، فنقول works.")
            fix("He don't know", "don't", listOf("doesn't", "not", "isn't"), "doesn't", "هو لا يعرف", "مع He نستخدم doesn't.")
            fix("Sara live in Jeddah", "live", listOf("lives", "living", "is live"), "lives", "سارة تسكن في جدة", "Sara = she، فنقول lives.")
            bank("أبي يعمل في البنك", "My father works in a bank", listOf("work", "is", "at"), "My father works — مع الأب (هو) نضيف s.", alternatives = listOf("My father works in the bank"))
            choice(
                "أكمل الجملة:", prompt = "She ___ to school every day.",
                options = listOf("goes", "go", "is go", "going"), answer = "goes", english = true,
                why = "She + goes. الفعل go يصبح goes مع he / she.",
            )
            match("Works" to "يعمل (هو / هي)", "Lives" to "يسكن", "Goes" to "يذهب (هو / هي)", "Doesn't" to "لا (للمفرد الغائب)")
            hear(
                "He doesn't like coffee",
                listOf("He doesn't like coffee", "He don't like coffee", "He likes coffee", "She doesn't like coffee"),
                "هو لا يحب القهوة", "استمع جيدًا لـ doesn't مع He.",
            )
            say("She works in a hospital", "هي تعمل في مستشفى", "شِي وُركس إن أ هوسبيتال", "لا تنسَ s في works!")
        }

        chest()

        lesson(
            "turn on وليس open", "Verbs you can't translate", "💡",
            intro = "بعض الأفعال لا تُترجم حرفيًا من العربية:\nافتح النور ← Turn on the light (وليس open)\nسكّر التلفزيون ← Turn off the TV\nخذ صورة ← Take a picture\nسوّيت غلطة ← I made a mistake",
            pattern = ErrorPattern.WRONG_VERB,
        ) {
            card("Turn on", "يشغّل", "💡", "تِيرن أون", "/tɜːn ɒn/", "للأجهزة والأنوار: turn on.")
            card("Turn off", "يطفئ", "🔌", "تِيرن أوف", "/tɜːn ɒf/")
            card("Take a picture", "يلتقط صورة", "📸", "تيك أ پِكتشر", "/teɪk ə ˈpɪk.tʃər/")
            card("Make a mistake", "يرتكب خطأ", "❌", "ميك أ مِستيك", "/meɪk ə mɪˈsteɪk/")
            fix("Please open the light", "open", listOf("turn on", "make", "do"), "turn on", "شغّل النور من فضلك", "مع الأنوار والأجهزة نقول turn on.")
            fix("Close the TV please", "Close", listOf("Turn off", "Shut", "Stop"), "Turn off", "أطفئ التلفزيون من فضلك", "مع الأجهزة نقول turn off.")
            fix("Can I catch a picture", "catch", listOf("take", "make", "do"), "take", "هل يمكنني التقاط صورة؟", "نقول take a picture.")
            fix("I did a mistake", "did", listOf("made", "took", "said"), "made", "ارتكبت خطأً", "نقول make a mistake → I made a mistake.")
            bank("شغّل المكيّف من فضلك", "Turn on the AC please", listOf("open", "close", "is"), "Turn on the AC — وليس open.", alternatives = listOf("Please turn on the AC"))
            choice(
                "كيف تقول «أطفئ النور»؟",
                options = listOf("Turn off the light.", "Close the light.", "Kill the light.", "Open the light."),
                answer = "Turn off the light.", english = true,
                why = "للأنوار: turn on (شغّل) و turn off (أطفئ).",
            )
            match("Turn on" to "يشغّل", "Turn off" to "يطفئ", "Take a picture" to "يلتقط صورة", "Make a mistake" to "يرتكب خطأ")
            hearBuild("Turn off the TV please", listOf("close", "open", "is"), "أطفئ التلفزيون من فضلك", "Turn off the TV.")
            say("Turn off the light", "أطفئ النور", "تِيرن أوف ذَ لايت", "th في the = ذ.")
        }

        lesson(
            "حروف الجر", "on Monday, at 5, married to", "📍",
            intro = "حروف الجر لا تُترجم حرفيًا:\non + الأيام: on Monday\nat + الساعة: at 5 o'clock\nin + المدن والشهور: in Riyadh, in May\nمتزوج من ← married to (وليس with)",
            pattern = ErrorPattern.PREPOSITIONS,
        ) {
            card("On Monday", "يوم الاثنين", "📅", "أون مَندي", "/ɒn ˈmʌn.deɪ/", "مع الأيام نستخدم on.")
            card("At 5 o'clock", "الساعة الخامسة", "🕔", "آت فايڤ أُكلوك", "/æt faɪv əˈklɒk/", "مع الساعة نستخدم at.")
            card("In Riyadh", "في الرياض", "🏙️", "إن رِياض", "/ɪn rɪˈjɑːd/", "مع المدن نستخدم in.")
            card("Married to", "متزوج من", "💍", "ماريد تُو", "/ˈmær.id tuː/", "married to وليس married with.")
            fix("See you in Monday", "in", listOf("on", "at", "from"), "on", "أراك يوم الاثنين", "مع أيام الأسبوع نستخدم on.")
            fix("The meeting is in 5 o'clock", "in", listOf("at", "on", "from"), "at", "الاجتماع الساعة الخامسة", "مع الساعة نستخدم at.")
            fix("She is married with a doctor", "with", listOf("to", "from", "on"), "to", "هي متزوجة من طبيب", "نقول married to.")
            fix("I live at Riyadh", "at", listOf("in", "on", "to"), "in", "أسكن في الرياض", "مع المدن نستخدم in.")
            bank("أراك يوم الأحد", "See you on Sunday", listOf("in", "at", "the"), "on + اليوم: See you on Sunday.")
            choice(
                "أكمل الجملة:", prompt = "The class starts ___ 8 o'clock.",
                options = listOf("at", "in", "on", "from"), answer = "at", english = true,
                why = "مع الساعة نستخدم at.",
            )
            match("On Monday" to "يوم الاثنين", "At 5 o'clock" to "الساعة الخامسة", "In Riyadh" to "في الرياض", "Married to" to "متزوج من")
            hear("I work in Riyadh", listOf("I work in Riyadh", "I work at Riyadh", "I walk in Riyadh"), "أعمل في الرياض", "in + المدينة. وانتبه: work (يعمل) و walk (يمشي).")
            say("See you on Monday", "أراك يوم الاثنين", "سي يُو أون مَندي", "on مع الأيام.")
        }

        chest("كنز صياد الأخطاء")
    }

    // =====================================================================================
    // UNIT 6 — Situations from your life (Gulf)
    // =====================================================================================
    private fun unit6() = courseUnit(
        id = "u6", order = 5,
        titleAr = "الوحدة ٦: مواقف من حياتك",
        titleEn = "Real-life situations",
        descriptionAr = "المطار، السائق، العيادة، والعمل: الإنجليزية التي تحتاجها فعلًا في يومك.",
        color = 0xFF0EA5A4,
    ) {
        lesson("في المطار", "At the airport", "✈️", intro = "كلمات ستسمعها في كل رحلة. بعد هذا الدرس جرّب موقف «في المطار» في تبويب المواقف 💬") {
            card("Passport", "جواز سفر", "🛂", "پاسپورت", "/ˈpɑːs.pɔːt/")
            card("Boarding pass", "بطاقة صعود الطائرة", "🎫", "بوردنغ پاس", "/ˈbɔː.dɪŋ pɑːs/")
            card("Gate", "بوابة", "🚪", "غيت", "/ɡeɪt/")
            card("Flight", "رحلة جوية", "🛫", "فلايت", "/flaɪt/", "gh صامتة.")
            card("Luggage", "أمتعة", "🧳", "لَغِج", "/ˈlʌɡ.ɪdʒ/")
            choice(
                "الموظف يقول: Can I see your passport? ماذا يطلب؟", prompt = "Can I see your passport?",
                options = listOf("جواز سفرك", "تذكرتك", "حقيبتك", "اسمك"), answer = "جواز سفرك", english = false,
                why = "passport = جواز السفر.", audio = "Can I see your passport?",
            )
            bank("أنا مسافر إلى لندن", "I am flying to London", listOf("fly", "in", "is"), "I am flying to + المكان.", alternatives = listOf("I'm flying to London"))
            bank("مقعد بجانب النافذة من فضلك", "A window seat please", listOf("open", "the", "is"), "A window seat, please.")
            match("Passport" to "جواز سفر", "Boarding pass" to "بطاقة صعود الطائرة", "Gate" to "بوابة", "Luggage" to "أمتعة")
            hear("Your flight is at gate twelve", listOf("Your flight is at gate twelve", "Your flight is at gate twenty", "Your fight is at gate twelve"), "رحلتك عند البوابة ١٢", "twelve = ١٢ و twenty = ٢٠.")
            say("Here is my passport", "هذا جواز سفري", "هير إز ماي پاسپورت", "P في passport بنفخة هواء.")
        }

        lesson("مع السائق", "Giving directions", "🚗", intro = "وجّه السائق بثقة: يمين، يسار، مستقيم، وقف هنا.") {
            card("Turn right", "انعطف يمينًا", "➡️", "تِيرن رايت", "/tɜːn raɪt/")
            card("Turn left", "انعطف يسارًا", "⬅️", "تِيرن لِفت", "/tɜːn left/")
            card("Go straight", "امشِ مستقيمًا", "⬆️", "غو ستريت", "/ɡoʊ streɪt/", "gh صامتة في straight.")
            card("Stop here", "توقف هنا", "🛑", "ستوپ هير", "/stɒp hɪər/")
            card("Traffic light", "إشارة المرور", "🚦", "ترافِك لايت", "/ˈtræf.ɪk laɪt/")
            bank("توقف هنا من فضلك", "Stop here please", listOf("close", "is", "the"), "Stop here, please — وليس close.", alternatives = listOf("Please stop here"))
            bank("انعطف يسارًا عند الإشارة", "Turn left at the traffic light", listOf("in", "right", "go"), "at the traffic light = عند الإشارة.")
            choice(
                "السائق يسأل: Left or right? وأنت تريد اليمين.", prompt = "Left or right?",
                options = listOf("Right, please.", "Left, please.", "Straight, please.", "Stop, please."), answer = "Right, please.", english = true,
                why = "right = يمين و left = يسار.", audio = "Left or right?",
            )
            match("Turn right" to "انعطف يمينًا", "Turn left" to "انعطف يسارًا", "Go straight" to "امشِ مستقيمًا", "Stop here" to "توقف هنا")
            hearBuild("Go straight then turn right", listOf("left", "stop", "is"), "امشِ مستقيمًا ثم انعطف يمينًا", "then = ثم.")
            say("Turn right at the traffic light", "انعطف يمينًا عند الإشارة", "تِيرن رايت آت ذَ ترافِك لايت", "قلها ببطء أولًا.")
        }

        chest()

        lesson("في العيادة", "At the clinic", "🏥", intro = "صف ما تشعر به: I have a + الألم.\nلا تقل My head is pain، بل I have a headache.") {
            card("Headache", "صداع", "🤕", "هِدِيك", "/ˈhed.eɪk/", "ache = ألم، وتُنطق (إيك).")
            card("Fever", "حرارة", "🤒", "فِيڤَر", "/ˈfiː.vər/")
            card("Medicine", "دواء", "💊", "مِدِسِن", "/ˈmed.ɪ.sən/")
            card("Appointment", "موعد", "📅", "أپوينتمِنت", "/əˈpɔɪnt.mənt/")
            card("I feel sick", "أشعر بالمرض", "🤢", "آي فيل سِك", "/aɪ fiːl sɪk/")
            bank("عندي صداع", "I have a headache", listOf("am", "is", "my"), "I have a headache — وليس My head is pain.")
            bank("أحتاج موعدًا", "I need an appointment", listOf("a", "am", "is"), "an قبل appointment لأنها تبدأ بصوت متحرك.")
            choice(
                "كيف تقول «عندي حرارة»؟",
                options = listOf("I have a fever.", "I am fever.", "My fever is.", "I fever."), answer = "I have a fever.", english = true,
                why = "عندي = I have.",
            )
            match("Headache" to "صداع", "Fever" to "حرارة", "Medicine" to "دواء", "Appointment" to "موعد")
            hear("Take this medicine twice a day", listOf("Take this medicine twice a day", "Take this medicine three times a day", "Take this medicine once a day"), "خذ هذا الدواء مرتين يوميًا", "once = مرة، twice = مرتين، three times = ثلاث مرات.")
            say("I have a headache", "عندي صداع", "آي هاڤ أ هِدِيك", "v في have مهتزة.")
        }

        lesson("في العمل", "At work", "💼", intro = "عبارات الاجتماعات والإيميل التي ستحتاجها في عملك.") {
            card("Meeting", "اجتماع", "👥", "مِيتنغ", "/ˈmiː.tɪŋ/")
            card("Email", "بريد إلكتروني", "📧", "إيمِيل", "/ˈiː.meɪl/")
            card("Deadline", "موعد التسليم", "⏰", "دِدلاين", "/ˈded.laɪn/")
            card("Manager", "مدير", "👔", "مانِجَر", "/ˈmæn.ɪ.dʒər/")
            card("Colleague", "زميل عمل", "🤝", "كولِيغ", "/ˈkɒl.iːɡ/")
            bank("عندي اجتماع الساعة العاشرة", "I have a meeting at ten", listOf("in", "on", "am"), "at + الساعة.", alternatives = listOf("I have a meeting at 10"))
            bank("سأرسل لك إيميل", "I will send you an email", listOf("a", "am", "to"), "an email لأن email تبدأ بصوت متحرك.")
            choice(
                "كيف تبدأ إيميلًا رسميًا؟",
                options = listOf("Dear Mr. Smith,", "Hey man,", "Hello habibi,", "Yo Smith,"), answer = "Dear Mr. Smith,", english = true,
                why = "في الإيميل الرسمي نبدأ بـ Dear + اللقب + الاسم.",
            )
            match("Meeting" to "اجتماع", "Email" to "بريد إلكتروني", "Deadline" to "موعد التسليم", "Manager" to "مدير")
            hearBuild("The deadline is on Thursday", listOf("in", "at", "are"), "موعد التسليم يوم الخميس", "on + اليوم.")
            say("I will send the email today", "سأرسل الإيميل اليوم", "آي وِل سِند ذَ إيمِيل تودِي", "will = سوف.")
        }

        chest("الكنز الأكبر 🏆")
    }
}
