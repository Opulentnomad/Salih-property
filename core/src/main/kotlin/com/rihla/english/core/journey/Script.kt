package com.rihla.english.core.journey

import com.rihla.english.core.model.ErrorPattern.ADJECTIVE_ORDER
import com.rihla.english.core.model.ErrorPattern.ARTICLES
import com.rihla.english.core.model.ErrorPattern.MISSING_BE
import com.rihla.english.core.model.ErrorPattern.PREPOSITIONS
import com.rihla.english.core.model.ErrorPattern.THIRD_PERSON_S
import com.rihla.english.core.model.ErrorPattern.VERB_AS_ADJECTIVE
import com.rihla.english.core.model.ErrorPattern.WRONG_VERB

/**
 * "رحلة" — the screenplay. A Saudi learner travels from Riyadh to London and builds a life
 * there, chapter by chapter. Every scene mixes comprehensible input (lines with highlighted
 * chunks), choosing natural replies (with Arabic-speaker trap coaching) and shadowing.
 */
object Script {

    val chapters: List<Chapter> by lazy {
        listOf(ch1(), ch2(), ch3(), ch4(), ch5(), ch6(), ch7(), ch8(), ch9(), ch10())
    }

    fun chapter(id: String): Chapter? = chapters.firstOrNull { it.id == id }
    fun scene(id: String): Pair<Chapter, Scene>? =
        chapters.firstNotNullOfOrNull { ch -> ch.scenes.firstOrNull { it.id == id }?.let { ch to it } }

    // ------------------------------------------------------------------------------ 1
    private fun ch1() = chapter(
        1, "RUH", "RIYADH · DEPARTED", "مطار الرياض", "Leaving Riyadh",
        "تسجيل الدخول والصعود للطائرة", SceneArt.DEPARTURES,
    ) {
        cast("agent" to "موظفة الطيران", "gate" to "موظف البوابة", "crew" to "مضيفة الطيران")
        scene("عند كاونتر الطيران", SceneArt.DEPARTURES) {
            narrate("مطار الملك خالد، الساعة الواحدة فجرًا. حقيبتك جاهزة، ورحلتك إلى لندن تبدأ الآن.")
            say("agent", "Good evening! Where are you flying today?", "مساء الخير! إلى أين تسافر اليوم؟", "Where are you flying" to "إلى أين تسافر")
            reply(
                "كيف ترد؟",
                natural("I'm flying to London.", "سأسافر إلى لندن.", "جملة كاملة وطبيعية: I'm flying to + المدينة."),
                ok("London.", "لندن.", "مفهومة، لكن الجملة الكاملة أجمل."),
                wrong("I am fly to London.", "أنا أطير إلى لندن.", "لا نضع am قبل الفعل fly. قل: I'm flying to London.", VERB_AS_ADJECTIVE),
            )
            say("agent", "Can I see your passport, please?", "هل يمكنني رؤية جوازك من فضلك؟", "Can I see your passport" to "هل يمكنني رؤية جوازك")
            reply(
                "تسلّمها الجواز.",
                natural("Sure, here you are.", "أكيد، تفضّلي.", "Here you are = تفضّل، تُقال عند تسليم شيء."),
                wrong("Take.", "خذي.", "تبدو أمرًا جافًا. الأنسب: Here you are."),
            )
            shadow("I'm flying to London.", "سأسافر إلى لندن", "آيم فلاينغ تو لَندَن", "الضغط على FLY و LON: آيم فلاينغ تو لَندَن.")
        }
        scene("عند بوابة الصعود", SceneArt.DEPARTURES) {
            narrate("البوابة ٢٤. الركاب يصطفّون، وموظف البوابة ينادي.")
            say("gate", "We are now boarding rows 20 to 35.", "نبدأ الآن صعود الصفوف من ٢٠ إلى ٣٥.", "now boarding" to "الصعود الآن")
            say("gate", "Boarding pass, please.", "بطاقة الصعود من فضلك.", "Boarding pass" to "بطاقة الصعود")
            reply(
                "مقعدك في الصف ٢٨.",
                natural("Here it is. Seat 28A.", "تفضّل. المقعد ٢٨A.", "واضحة ومهذبة."),
                ok("Yes, here.", "نعم، هنا.", "مفهومة."),
                wrong("I am in 28.", "أنا في ٢٨.", "تعني «أنا داخل ٢٨». قل: My seat is 28A.", PREPOSITIONS),
            )
            shadow("Here it is.", "تفضّل، هذه هي", "هير إت إز", "صِل الكلمات: هيرِتِز.")
        }
        scene("على متن الطائرة", SceneArt.PLANE) {
            narrate("ارتفعت الطائرة فوق الرياض. المضيفة تمرّ بعربة الطعام.")
            say("crew", "Would you like chicken or fish?", "هل تفضّل الدجاج أم السمك؟", "Would you like" to "هل تفضّل / هل تريد")
            reply(
                "تريد الدجاج.",
                natural("Chicken, please.", "دجاج من فضلك.", "قصيرة ومهذبة. please تفرق كثيرًا."),
                wrong("I want chicken.", "أريد دجاجًا.", "صحيحة نحويًا لكنها حادة قليلًا. الأفضل: Chicken, please.", null),
            )
            say("crew", "Anything to drink?", "شيء تشربه؟", "Anything to drink" to "شيء تشربه؟")
            reply(
                "تريد ماءً.",
                natural("Just water, thank you.", "ماء فقط، شكرًا.", "Just = فقط. طبيعية جدًا."),
                ok("Water.", "ماء.", "مفهومة، أضف please."),
            )
            shadow("Would you like chicken or fish?", "هل تفضّل الدجاج أم السمك؟", "وُد يو لايك تشِكِن أور فِش", "Would you تُنطق مدموجة: وُدْجو.")
        }
        mission(
            "عرّف بنفسك",
            "سجّل رسالة صوتية قصيرة لصديق تعرّف فيها بنفسك بالإنجليزية.",
            "Hi, I'm …", "I'm from Saudi Arabia.", "Nice to meet you!",
            stampCode = "HI", stampEn = "MISSION · HELLO",
        )
    }

    // ------------------------------------------------------------------------------ 2
    private fun ch2() = chapter(
        2, "LHR", "LONDON · ARRIVED", "وصلت لندن", "Arrival at Heathrow",
        "الجوازات والحقائب والتاكسي", SceneArt.ARRIVALS,
    ) {
        cast("officer" to "ضابط الجوازات", "staff" to "موظف الأمتعة", "driver" to "سائق التاكسي")
        scene("عند الجوازات", SceneArt.ARRIVALS) {
            narrate("هيثرو، الساعة السابعة صباحًا. طابور طويل، ثم يأتي دورك.")
            say("officer", "What's the purpose of your visit?", "ما سبب زيارتك؟", "purpose of your visit" to "سبب زيارتك")
            reply(
                "ردّك",
                natural("I'm here on holiday.", "أنا هنا في إجازة.", "هكذا يرد المتحدث الأصلي. on holiday = في إجازة."),
                wrong("I am tourism.", "أنا سياحة.", "tourism اسم، فالمعنى «أنا سياحة». قل: I'm here on holiday أو I'm a tourist.", ARTICLES),
                natural("I'm here to study.", "أنا هنا للدراسة.", "مناسبة إذا كنت مبتعثًا."),
            )
            say("officer", "How long are you staying?", "كم مدة إقامتك؟", "How long are you staying" to "كم مدة إقامتك")
            reply(
                "ستبقى أسبوعين.",
                natural("For two weeks.", "لمدة أسبوعين.", "for + مدة."),
                wrong("Since two weeks.", "منذ أسبوعين.", "since للماضي (منذ). للمدة نستخدم for.", PREPOSITIONS),
            )
            shadow("I'm here on holiday.", "أنا هنا في إجازة", "آيم هير أون هوليدَي", "الضغط على «هو» في HOLiday.")
        }
        scene("استلام الحقائب", SceneArt.ARRIVALS) {
            narrate("انتظرت عند السير نصف ساعة… حقيبتك لم تظهر.")
            say("staff", "Can I help you?", "هل أستطيع مساعدتك؟")
            reply(
                "حقيبتك ضاعت.",
                natural("My suitcase didn't arrive.", "حقيبتي لم تصل.", "didn't arrive = لم تصل. واضحة جدًا."),
                wrong("My bag is lost me.", "حقيبتي ضيّعتني.", "ترجمة حرفية. قل: My bag is lost أو My suitcase didn't arrive."),
            )
            say("staff", "Can you describe your bag?", "هل تستطيع وصف حقيبتك؟", "describe your bag" to "تصف حقيبتك")
            reply(
                "حقيبة سوداء كبيرة.",
                natural("It's a big black suitcase.", "إنها حقيبة سوداء كبيرة.", "الحجم ثم اللون ثم الاسم: big black suitcase."),
                wrong("It's a suitcase black big.", "حقيبة سوداء كبيرة.", "في الإنجليزية الصفات قبل الاسم: a big black suitcase.", ADJECTIVE_ORDER),
            )
            shadow("It's a big black suitcase.", "إنها حقيبة سوداء كبيرة", "إتس أ بِغ بلاك سوتكيس", "قلها بإيقاع منتظم: بِغ — بلاك — سوت.")
        }
        scene("تاكسي إلى الفندق", SceneArt.TAXI) {
            narrate("أخيرًا خارج المطار. سيارة أجرة سوداء تقف أمامك.")
            say("driver", "Where to, mate?", "إلى أين يا صديقي؟", "Where to" to "إلى أين؟")
            reply(
                "إلى فندق في وسط المدينة.",
                natural("To the Park Hotel, please.", "إلى فندق بارك من فضلك.", "To + المكان + please. مثالية."),
                ok("Park Hotel.", "فندق بارك.", "مفهومة."),
            )
            say("driver", "Traffic's bad today. It'll take about an hour.", "الزحمة سيئة اليوم. سيستغرق الأمر ساعة تقريبًا.", "It'll take about an hour" to "سيستغرق ساعة تقريبًا")
            reply(
                "لا مشكلة.",
                natural("No worries, take your time.", "لا مشكلة، خذ راحتك.", "No worries عبارة بريطانية ودودة."),
                wrong("No problem, I am not hurry.", "لا مشكلة، أنا لست عجلة.", "hurry فعل/اسم. قل: I'm not in a hurry.", MISSING_BE),
            )
            shadow("No worries, take your time.", "لا مشكلة، خذ راحتك", "نو ووريز، تيك يور تايم", "Worries تبدأ بشفاه مضمومة: وَ.")
        }
        mission(
            "اطلب قهوتك بالإنجليزية",
            "في أي مقهى اليوم، حتى لو كان الموظف يتكلم العربية.",
            "Can I have a latte, please?", "Could I get it to go?",
            stampCode = "CUP", stampEn = "MISSION · COFFEE",
        )
    }

    // ------------------------------------------------------------------------------ 3
    private fun ch3() = chapter(
        3, "INN", "HOTEL · CHECKED IN", "الفندق", "At the Hotel",
        "تسجيل الدخول ومشكلة في الغرفة", SceneArt.HOTEL,
    ) {
        cast("clerk" to "موظفة الاستقبال", "tech" to "عامل الصيانة")
        scene("عند الاستقبال", SceneArt.HOTEL) {
            narrate("ردهة الفندق دافئة، وموظفة الاستقبال تبتسم.")
            say("clerk", "Hi there! Do you have a reservation?", "أهلًا! هل لديك حجز؟", "Do you have a reservation" to "هل لديك حجز")
            reply(
                "عندك حجز باسم فيصل.",
                natural("Yes, it's under Faisal.", "نعم، باسم فيصل.", "under + الاسم = باسم."),
                wrong("Yes, I am reservation.", "نعم، أنا حجز.", "قل I have a reservation، وليس I am.", MISSING_BE),
            )
            say("clerk", "Here's your key card. You're in room 412.", "هذه بطاقة مفتاحك. غرفتك ٤١٢.", "key card" to "بطاقة المفتاح")
            reply(
                "تسأل عن الإفطار.",
                natural("What time is breakfast?", "متى موعد الإفطار؟", "What time is…? سؤال مفيد في كل مكان."),
                wrong("When the breakfast?", "متى الإفطار؟", "السؤال يحتاج فعلًا: What time is breakfast?", MISSING_BE),
            )
            shadow("What time is breakfast?", "متى موعد الإفطار؟", "وَت تايم إز بريكفَست", "الضغط على TIME و BREAK.")
        }
        scene("مشكلة في الغرفة", SceneArt.HOTEL) {
            narrate("الغرفة جميلة… لكن التكييف لا يعمل، والجو خانق.")
            say("clerk", "Reception, how can I help?", "الاستقبال، كيف أستطيع المساعدة؟")
            reply(
                "التكييف لا يعمل.",
                natural("The air conditioning isn't working.", "التكييف لا يعمل.", "isn't working = لا يعمل."),
                wrong("The AC is not walking.", "المكيّف لا يمشي.", "ترجمة حرفية لـ «ما يمشي». قل: isn't working.", WRONG_VERB),
            )
            say("clerk", "I'm so sorry. I'll send someone up right away.", "أعتذر جدًا. سأرسل أحدًا فورًا.", "right away" to "فورًا")
            say("tech", "Let me have a look… It should be fine now.", "دعني ألقي نظرة… يُفترض أنه يعمل الآن.", "have a look" to "ألقي نظرة")
            reply(
                "تشكره.",
                natural("Thanks so much, I really appreciate it.", "شكرًا جزيلًا، أقدّر ذلك.", "I appreciate it عبارة لبقة جدًا."),
                ok("Thank you.", "شكرًا.", "ممتاز وبسيط."),
            )
            shadow("The air conditioning isn't working.", "التكييف لا يعمل", "ذِ إير كَندِشَنِنغ إزنْت ووركِنغ", "isn't تُنطق «إزِنْت» بسرعة.")
        }
        scene("كلمة سر الواي فاي", SceneArt.HOTEL) {
            narrate("تريد أن تكلم أهلك… لكن الإنترنت لا يتصل.")
            reply(
                "تسأل الموظفة.",
                natural("Could I have the Wi-Fi password, please?", "هل يمكنني الحصول على كلمة سر الواي فاي؟", "Could I have…? أسلوب مهذب للطلب."),
                wrong("Give me the Wi-Fi.", "أعطني الواي فاي.", "تبدو أمرًا. الأنسب: Could I have the Wi-Fi password?"),
            )
            say("clerk", "Sure. It's on the back of your key card.", "أكيد. مكتوبة خلف بطاقة مفتاحك.", "on the back of" to "خلف / على ظهر")
            shadow("Could I have the Wi-Fi password, please?", "هل يمكنني الحصول على كلمة سر الواي فاي؟", "كُد آي هاڤ ذَ واي فاي پاسوِرد", "Could I تُدمج: كُدآي.")
        }
        mission(
            "اكتب خطة يومك",
            "اكتب ٣ جمل بالإنجليزية عن خطتك لليوم في ملاحظات جوالك.",
            "First, I'm going to …", "After that, I'll …", "In the evening, …",
            stampCode = "PLAN", stampEn = "MISSION · MY DAY",
        )
    }

    // ------------------------------------------------------------------------------ 4
    private fun ch4() = chapter(
        4, "CAF", "CAFÉ · FIRST SIP", "أول قهوة", "Your First Café",
        "الطلب والدفع ودردشة قصيرة", SceneArt.CAFE,
    ) {
        cast("barista" to "الباريستا", "local" to "رجل بريطاني")
        scene("الطلب", SceneArt.CAFE) {
            narrate("مقهى صغير في شارع ضيق. رائحة القهوة تملأ المكان.")
            say("barista", "Morning! What can I get you?", "صباح الخير! ماذا أحضر لك؟", "What can I get you" to "ماذا أحضر لك؟")
            reply(
                "تريد لاتيه.",
                natural("Can I have a latte, please?", "هل يمكنني الحصول على لاتيه؟", "Can I have…, please? مفتاح أي طلب."),
                wrong("I am want latte.", "أنا أريد لاتيه.", "لا am قبل want، ونحتاج a: I want a latte.", VERB_AS_ADJECTIVE),
            )
            say("barista", "Sure. To have here or to go?", "أكيد. هنا أم سفري؟", "to go" to "سفري")
            reply(
                "تريده سفري.",
                natural("To go, please.", "سفري من فضلك.", "to go = سفري."),
                wrong("Take away me.", "خذني بعيدًا.", "😄 تعني «خذني بعيدًا». قل: To go أو Takeaway, please."),
            )
            shadow("Can I have a latte, please?", "هل يمكنني الحصول على لاتيه؟", "كان آي هاڤ أ لاتيه پليز", "please بـ P منفوخة.")
        }
        scene("الدفع", SceneArt.CAFE) {
            narrate("القهوة جاهزة. حان وقت الدفع.")
            say("barista", "That's three pounds fifty.", "المبلغ ثلاثة جنيهات ونصف.", "That's three pounds fifty" to "المبلغ ٣.٥٠ جنيه")
            reply(
                "تسأل إن كانوا يقبلون البطاقة.",
                natural("Do you take cards?", "هل تقبلون البطاقات؟", "Do you take cards? سؤال يومي في بريطانيا."),
                wrong("You accept card?", "تقبل بطاقة؟", "السؤال يبدأ بـ Do: Do you take cards?", MISSING_BE),
            )
            say("barista", "Yep, just tap it here.", "نعم، فقط مرّرها هنا.", "tap it here" to "مرّرها هنا")
            shadow("Do you take cards?", "هل تقبلون البطاقات؟", "دو يو تيك كاردز", "cards تنتهي بصوت (ز).")
        }
        scene("دردشة عن الطقس", SceneArt.CAFE) {
            narrate("جلست قرب النافذة. رجل على الطاولة المجاورة يبتسم لك.")
            say("local", "Lovely weather today, isn't it?", "طقس جميل اليوم، أليس كذلك؟", "isn't it" to "أليس كذلك؟")
            reply(
                "الجو بارد عليك!",
                natural("It's a bit cold for me! I'm from Riyadh.", "باردة قليلًا عليّ! أنا من الرياض.", "رد ودود يفتح الحديث."),
                wrong("Yes, weather is cold very.", "نعم، الطقس بارد جدًا.", "very قبل الصفة: very cold، و the weather.", ADJECTIVE_ORDER),
            )
            say("local", "Riyadh! You must be freezing here then.", "الرياض! لا بد أنك تتجمد هنا إذن.", "You must be freezing" to "لا بد أنك تتجمد")
            reply(
                "تضحك وتوافق.",
                natural("Absolutely! I need a warmer jacket.", "تمامًا! أحتاج جاكيتًا أدفأ.", "warmer = أدفأ."),
                ok("Yes.", "نعم.", "صحيح، لكن الحديث سينتهي هنا."),
            )
            shadow("Lovely weather today, isn't it?", "طقس جميل اليوم، أليس كذلك؟", "لَڤلي وِذَر تودَي، إزِنْت إت", "isn't it بنبرة صاعدة في الآخر.")
        }
        mission(
            "اسأل عن سعر شيء",
            "اسأل بالإنجليزية عن سعر أي شيء في محل اليوم.",
            "How much is this?", "Do you take cards?",
            stampCode = "PAY", stampEn = "MISSION · PRICE",
        )
    }

    // ------------------------------------------------------------------------------ 5
    private fun ch5() = chapter(
        5, "TUBE", "UNDERGROUND", "المترو والمدينة", "The Tube",
        "التذاكر والخطوط وطلب الاتجاهات", SceneArt.TUBE,
    ) {
        cast("staff" to "موظف المحطة", "woman" to "سيدة في الشارع")
        scene("شراء تذكرة", SceneArt.TUBE) {
            narrate("محطة مترو تحت الأرض. خرائط ملونة في كل مكان.")
            reply(
                "تريد بطاقة مواصلات.",
                natural("Hi, how do I get an Oyster card?", "مرحبًا، كيف أحصل على بطاقة أويستر؟", "How do I…? = كيف أفعل كذا؟"),
                wrong("Hello, I need card metro.", "مرحبًا، أحتاج بطاقة مترو.", "الصفة قبل الاسم: a metro card.", ADJECTIVE_ORDER),
            )
            say("staff", "You can buy one from the machine, or just tap your bank card.", "تشتريها من الجهاز، أو فقط مرّر بطاقتك البنكية.", "just tap your bank card" to "فقط مرّر بطاقتك")
            shadow("How do I get an Oyster card?", "كيف أحصل على بطاقة أويستر؟", "هاو دو آي غِت أن أويستَر كارد", "an Oyster تُدمج: أنويستر.")
        }
        scene("أي خط؟", SceneArt.TUBE) {
            narrate("تريد الذهاب إلى المتحف البريطاني.")
            reply(
                "تسأل الموظف.",
                natural("Which line goes to the British Museum?", "أي خط يذهب إلى المتحف البريطاني؟", "Which line goes to…? سؤال المترو الذهبي."),
                wrong("Which line go to British Museum?", "أي خط يذهب للمتحف البريطاني؟", "الخط مفرد: goes. ومع المعالم: the British Museum.", THIRD_PERSON_S),
            )
            say("staff", "Take the Central line and get off at Holborn.", "اركب الخط المركزي وانزل في هولبورن.", "get off at" to "انزل في")
            say("staff", "It's about ten minutes.", "حوالي عشر دقائق.")
            shadow("Take the Central line and get off at Holborn.", "اركب الخط المركزي وانزل في هولبورن", "تيك ذَ سِنترَل لاين أند غِت أوف آت هوبِرن", "get off at تُدمج: غِتوفَت.")
        }
        scene("تهت في المدينة", SceneArt.TAXI) {
            narrate("خرجت من المحطة… والشوارع كلها متشابهة.")
            reply(
                "تسأل سيدة عن الطريق.",
                natural("Excuse me, how do I get to the museum?", "عفوًا، كيف أصل إلى المتحف؟", "Excuse me لبدء الحديث مع غريب."),
                wrong("Where is museum? Tell me.", "أين المتحف؟ قولي لي.", "حادة. ابدأ بـ Excuse me، ولا تنسَ the.", ARTICLES),
            )
            say("woman", "Go straight on, then take the second left.", "امشِ مستقيمًا، ثم خذ الشارع الثاني يسارًا.", "take the second left" to "خذ الثاني يسارًا")
            reply(
                "تتأكد أنك فهمت.",
                natural("Straight on, then second left. Thank you!", "مستقيم، ثم الثاني يسارًا. شكرًا!", "تكرار الاتجاهات يدل على أنك فهمت."),
                ok("OK, thanks.", "حسنًا، شكرًا.", "مقبولة."),
            )
            shadow("Excuse me, how do I get to the museum?", "عفوًا، كيف أصل إلى المتحف؟", "إكسكيوز مي، هاو دو آي غِت تو ذَ ميوزيام", "museum: ميو-زي-يَم، الضغط على «زي».")
        }
        mission(
            "صِف طريقك",
            "سجّل رسالة صوتية تصف فيها طريقك من البيت إلى العمل أو الجامعة.",
            "I take the …", "Then I turn left at …", "It takes about … minutes.",
            stampCode = "MAP", stampEn = "MISSION · MY ROUTE",
        )
    }

    // ------------------------------------------------------------------------------ 6
    private fun ch6() = chapter(
        6, "SHOP", "OXFORD ST · SHOPPING", "التسوق", "Shopping",
        "المقاسات والألوان والإرجاع", SceneArt.SHOP,
    ) {
        cast("assistant" to "موظفة المحل")
        scene("المقاس", SceneArt.SHOP) {
            narrate("شارع أكسفورد مزدحم. دخلت محل ملابس لتشتري جاكيتًا دافئًا.")
            say("assistant", "Are you looking for anything in particular?", "هل تبحث عن شيء معيّن؟", "looking for" to "تبحث عن")
            reply(
                "تبحث عن جاكيت شتوي.",
                natural("Yes, I'm looking for a winter jacket.", "نعم، أبحث عن جاكيت شتوي.", "I'm looking for… مفيدة في كل محل."),
                wrong("Yes, I search jacket winter.", "نعم، أبحث جاكيت شتوي.", "نقول looking for، والصفة قبل الاسم: a winter jacket.", ADJECTIVE_ORDER),
            )
            say("assistant", "What size are you?", "ما مقاسك؟", "What size are you" to "ما مقاسك؟")
            reply(
                "مقاسك متوسط.",
                natural("Medium, I think.", "متوسط على ما أظن.", "I think تجعل الرد طبيعيًا."),
                ok("M.", "M.", "مفهومة."),
            )
            shadow("I'm looking for a winter jacket.", "أبحث عن جاكيت شتوي", "آيم لُكِنغ فور أ وِنتَر جاكِت", "looking for: لُكِنغفور.")
        }
        scene("لون آخر", SceneArt.SHOP) {
            narrate("الجاكيت مناسب… لكن لونه لا يعجبك.")
            reply(
                "تسأل عن لون آخر.",
                natural("Do you have this in black?", "هل يوجد منه باللون الأسود؟", "Do you have this in + لون؟"),
                wrong("You have black one this?", "عندك أسود هذا؟", "ابدأ بـ Do you have… واجعل اللون في الآخر: in black.", MISSING_BE),
            )
            say("assistant", "Let me check… Yes! Would you like to try it on?", "دعني أتحقق… نعم! هل تريد تجربته؟", "try it on" to "تجربه (تلبسه)")
            reply(
                "تريد تجربته.",
                natural("Yes, please. Where are the fitting rooms?", "نعم من فضلك. أين غرف القياس؟", "fitting rooms = غرف القياس."),
                ok("OK.", "حسنًا.", "مقبولة."),
            )
            shadow("Do you have this in black?", "هل يوجد منه باللون الأسود؟", "دو يو هاڤ ذِس إن بلاك", "this بلسان بين الأسنان: ذِس.")
        }
        scene("إرجاع منتج", SceneArt.SHOP) {
            narrate("في البيت اكتشفت أن السحاب مكسور. رجعت للمحل في اليوم التالي.")
            reply(
                "تريد إرجاعه.",
                natural("Hi, I'd like to return this. The zip is broken.", "مرحبًا، أريد إرجاع هذا. السحاب مكسور.", "I'd like to return this = أود إرجاع هذا."),
                wrong("I want give back this.", "أريد أن أعطي هذا رجوعًا.", "قل: I'd like to return this.", WRONG_VERB),
            )
            say("assistant", "Of course. Do you have the receipt?", "طبعًا. هل معك الإيصال؟", "receipt" to "الإيصال")
            reply(
                "معك الإيصال.",
                natural("Yes, here it is.", "نعم، تفضّلي.", "ممتاز."),
                wrong("Yes, I am have it.", "نعم، أنا عندي.", "لا am مع have: I have it.", VERB_AS_ADJECTIVE),
            )
            shadow("I'd like to return this.", "أود إرجاع هذا", "آيد لايك تو ريتِرن ذِس", "I'd = I would، تُنطق «آيد».")
        }
        mission(
            "قائمة مشترياتك",
            "اكتب قائمة مشترياتك القادمة بالإنجليزية، وخذها معك للسوبرماركت.",
            "milk", "bread", "a bottle of water",
            stampCode = "LIST", stampEn = "MISSION · SHOPPING LIST",
        )
    }

    // ------------------------------------------------------------------------------ 7
    private fun ch7() = chapter(
        7, "GP", "CLINIC · GET WELL", "العيادة", "Seeing a Doctor",
        "حجز موعد ووصف الأعراض والصيدلية", SceneArt.CLINIC,
    ) {
        cast("receptionist" to "موظفة العيادة", "doctor" to "الطبيبة", "pharmacist" to "الصيدلي")
        scene("حجز موعد", SceneArt.CLINIC) {
            narrate("استيقظت متعبًا وحرارتك مرتفعة. اتصلت بالعيادة.")
            reply(
                "تريد موعدًا.",
                natural("Hi, I'd like to book an appointment, please.", "مرحبًا، أود حجز موعد.", "book an appointment = يحجز موعدًا."),
                wrong("I want appointment doctor.", "أريد موعد دكتور.", "نحتاج an، والترتيب: an appointment with a doctor.", ARTICLES),
            )
            say("receptionist", "Is it urgent? We have a slot at 3 pm today.", "هل هو مستعجل؟ لدينا موعد الساعة ٣ اليوم.", "a slot at 3 pm" to "موعد متاح الساعة ٣")
            reply(
                "توافق.",
                natural("3 pm is perfect, thank you.", "الساعة ٣ مناسبة تمامًا، شكرًا.", "perfect = ممتاز تمامًا."),
                wrong("Yes, in 3 pm.", "نعم، في الساعة ٣.", "مع الساعة نستخدم at.", PREPOSITIONS),
            )
            shadow("I'd like to book an appointment.", "أود حجز موعد", "آيد لايك تو بُك أن أپوينتمِنت", "an appointment تُدمج: أنَپوينتمِنت.")
        }
        scene("عند الطبيبة", SceneArt.CLINIC) {
            narrate("غرفة بيضاء هادئة. الطبيبة تنظر إليك باهتمام.")
            say("doctor", "So, what seems to be the problem?", "إذن، ما المشكلة؟", "what seems to be the problem" to "ما المشكلة؟")
            reply(
                "عندك صداع وحرارة.",
                natural("I have a headache and a fever.", "عندي صداع وحرارة.", "I have a + الألم."),
                wrong("My head is pain and I am fever.", "رأسي ألم وأنا حرارة.", "ترجمة حرفية. قل: I have a headache and a fever.", MISSING_BE),
            )
            say("doctor", "How long have you had these symptoms?", "منذ متى عندك هذه الأعراض؟", "How long have you had" to "منذ متى عندك")
            reply(
                "منذ يومين.",
                natural("For about two days.", "منذ يومين تقريبًا.", "for + المدة."),
                wrong("From two days.", "من يومين.", "خطأ شائع من «من يومين». الصحيح: for two days.", PREPOSITIONS),
            )
            shadow("I have a headache and a fever.", "عندي صداع وحرارة", "آي هاڤ أ هِدِيك أند أ فيڤَر", "headache: هِد-إيك.")
        }
        scene("الصيدلية", SceneArt.CLINIC) {
            narrate("معك الوصفة. الصيدلية في آخر الشارع.")
            say("pharmacist", "Take one tablet twice a day after meals.", "خذ قرصًا واحدًا مرتين يوميًا بعد الأكل.", "twice a day" to "مرتين يوميًا")
            reply(
                "تتأكد من التعليمات.",
                natural("Twice a day after meals. Got it, thanks.", "مرتين يوميًا بعد الأكل. فهمت، شكرًا.", "Got it = فهمت."),
                ok("OK.", "حسنًا.", "مقبولة، لكن التكرار يضمن الفهم."),
            )
            say("pharmacist", "Any allergies?", "هل عندك حساسية؟", "Any allergies" to "هل عندك حساسية؟")
            reply(
                "لا، ليس عندك.",
                natural("No, I'm not allergic to anything.", "لا، ليس عندي حساسية.", "allergic to = عنده حساسية من."),
                wrong("No, I don't allergic.", "لا، أنا لا حساسية.", "allergic صفة: I'm not allergic.", MISSING_BE),
            )
            shadow("Got it, thanks.", "فهمت، شكرًا", "غوتِت، ثانكس", "Got it تُدمج: غوتِت.")
        }
        mission(
            "سمِّ أشياء بيتك",
            "اكتب أسماء ٥ أشياء في مطبخك بالإنجليزية على أوراق لاصقة وألصقها عليها.",
            "fridge", "kettle", "cup", "spoon", "oven",
            stampCode = "LBL", stampEn = "MISSION · LABELS",
        )
    }

    // ------------------------------------------------------------------------------ 8
    private fun ch8() = chapter(
        8, "UNI", "UNIVERSITY · ENROLLED", "الجامعة", "University",
        "التسجيل وزميل جديد وسؤال الدكتور", SceneArt.CAMPUS,
    ) {
        cast("officer" to "موظفة التسجيل", "sam" to "زميلك سام", "prof" to "الدكتور")
        scene("مكتب التسجيل", SceneArt.CAMPUS) {
            narrate("حرم جامعي قديم ومبانٍ من الطوب الأحمر. أول يوم لك هنا.")
            say("officer", "Hello! Are you here to enrol?", "مرحبًا! هل أتيت للتسجيل؟", "here to enrol" to "هنا للتسجيل")
            reply(
                "نعم، أنت طالب جديد.",
                natural("Yes, I'm a new international student.", "نعم، أنا طالب دولي جديد.", "a new international student — الصفات قبل الاسم."),
                wrong("Yes, I am student new.", "نعم، أنا طالب جديد.", "الصفة قبل الاسم ومعها a: a new student.", ADJECTIVE_ORDER),
            )
            say("officer", "Great. I just need your passport and offer letter.", "رائع. أحتاج فقط جوازك وخطاب القبول.", "offer letter" to "خطاب القبول")
            shadow("I'm a new international student.", "أنا طالب دولي جديد", "آيم أ نيو إنترناشَنَل ستيودِنت", "international: إن-تر-نا-شَنَل.")
        }
        scene("زميل جديد", SceneArt.CAMPUS) {
            narrate("في الكافتيريا، جلس بجانبك طالب يبدو ودودًا.")
            say("sam", "Hey, I'm Sam. What are you studying?", "مرحبًا، أنا سام. ماذا تدرس؟", "What are you studying" to "ماذا تدرس؟")
            reply(
                "تدرس الهندسة.",
                natural("Nice to meet you, Sam! I'm studying engineering.", "سعيد بلقائك يا سام! أدرس الهندسة.", "رد كامل وودود."),
                wrong("I am study engineer.", "أنا أدرس مهندس.", "I'm studying engineering (المجال)، وليس engineer (الشخص).", VERB_AS_ADJECTIVE),
            )
            say("sam", "Cool! Do you want to grab a coffee after class?", "رائع! تريد أن نشرب قهوة بعد المحاضرة؟", "grab a coffee" to "نشرب قهوة سريعة")
            reply(
                "توافق بحماس.",
                natural("I'd love to!", "يسعدني ذلك!", "I'd love to = بكل سرور."),
                ok("Yes, OK.", "نعم، حسنًا.", "مقبولة."),
            )
            shadow("I'd love to!", "يسعدني ذلك!", "آيد لَڤ تو", "love بـ V مهتزة في الآخر.")
        }
        scene("سؤال للدكتور", SceneArt.CAMPUS) {
            narrate("انتهت المحاضرة، وعندك سؤال عن الواجب.")
            reply(
                "تستأذن الدكتور.",
                natural("Excuse me, could I ask a quick question?", "عفوًا، هل يمكنني طرح سؤال سريع؟", "مهذبة جدًا في البيئة الأكاديمية."),
                wrong("Doctor, I have question.", "دكتور، عندي سؤال.", "نحتاج a: a question. وفي بريطانيا يُنادى Professor أو بالاسم.", ARTICLES),
            )
            say("prof", "Of course. What's on your mind?", "طبعًا. ما الذي يدور في ذهنك؟", "What's on your mind" to "ما الذي في بالك؟")
            reply(
                "تسأل عن موعد التسليم.",
                natural("When is the essay due?", "متى موعد تسليم المقال؟", "due = موعد استحقاق / تسليم."),
                wrong("When the deadline of essay?", "متى موعد المقال؟", "نحتاج is: When is the essay due?", MISSING_BE),
            )
            shadow("Could I ask a quick question?", "هل يمكنني طرح سؤال سريع؟", "كُد آي آسك أ كويك كويسشِن", "quick question: كويك كويسشن.")
        }
        mission(
            "عرّف بنفسك ٣٠ ثانية",
            "سجّل فيديو أو صوتًا مدته ٣٠ ثانية تعرّف فيه بنفسك ودراستك أو عملك.",
            "My name is …", "I'm studying / working as …", "In my free time, I …",
            stampCode = "MIC", stampEn = "MISSION · INTRO",
        )
    }

    // ------------------------------------------------------------------------------ 9
    private fun ch9() = chapter(
        9, "JOB", "LONDON · HIRED", "مقابلة عمل", "Job Interview",
        "الاستقبال والتعريف والأسئلة", SceneArt.OFFICE,
    ) {
        cast("receptionist" to "موظف الاستقبال", "manager" to "مديرة التوظيف")
        scene("الوصول للمقابلة", SceneArt.OFFICE) {
            narrate("مبنى زجاجي في وسط لندن. وصلت قبل الموعد بعشر دقائق.")
            say("receptionist", "Good morning. How can I help you?", "صباح الخير. كيف أستطيع مساعدتك؟")
            reply(
                "عندك مقابلة.",
                natural("Hi, I have an interview with Ms. Clark at 10.", "مرحبًا، عندي مقابلة مع السيدة كلارك الساعة ١٠.", "an interview + with + at الساعة."),
                wrong("I am interview with Clark.", "أنا مقابلة مع كلارك.", "قل I have an interview.", MISSING_BE),
            )
            say("receptionist", "Please take a seat. She'll be with you shortly.", "تفضّل بالجلوس. ستأتيك بعد قليل.", "take a seat" to "تفضّل بالجلوس")
            shadow("I have an interview at ten.", "عندي مقابلة الساعة العاشرة", "آي هاڤ أن إنترڤيو آت تِن", "an interview: أنِنترڤيو.")
        }
        scene("عرّفنا بنفسك", SceneArt.OFFICE) {
            narrate("مكتب واسع. مديرة التوظيف تفتح ملفك.")
            say("manager", "So, tell me a little about yourself.", "إذن، حدّثني قليلًا عن نفسك.", "tell me a little about yourself" to "حدّثني عن نفسك")
            reply(
                "تعرّف بنفسك.",
                natural("I'm an engineer with five years of experience.", "أنا مهندس ولدي خبرة خمس سنوات.", "an engineer + five years of experience."),
                wrong("I am engineer, I have experience five years.", "أنا مهندس، عندي خبرة خمس سنوات.", "نحتاج an، والترتيب: five years of experience.", ARTICLES),
            )
            say("manager", "What would you say is your biggest strength?", "ما أكبر نقطة قوة لديك برأيك؟", "biggest strength" to "أكبر نقطة قوة")
            reply(
                "تعمل جيدًا مع الفريق.",
                natural("I work really well in a team.", "أعمل جيدًا جدًا ضمن فريق.", "well تصف الفعل work."),
                wrong("I am very good work in team.", "أنا جيد جدًا عمل في فريق.", "قل I work well in a team.", VERB_AS_ADJECTIVE),
            )
            shadow("I work really well in a team.", "أعمل جيدًا جدًا ضمن فريق", "آي وورك ريلي وِل إن أ تيم", "work (عمل) ليست walk (مشى): وُرك.")
        }
        scene("أسئلتك أنت", SceneArt.OFFICE) {
            narrate("اقتربت المقابلة من النهاية.")
            say("manager", "Do you have any questions for us?", "هل لديك أي أسئلة لنا؟", "any questions for us" to "أي أسئلة لنا")
            reply(
                "تسأل سؤالًا ذكيًا.",
                natural("What does a typical day look like in this role?", "كيف يبدو اليوم المعتاد في هذه الوظيفة؟", "سؤال يُظهر اهتمامك الحقيقي."),
                ok("How much is the salary?", "كم الراتب؟", "سؤال مشروع، لكنه ليس الأفضل كأول سؤال."),
            )
            say("manager", "Thank you, Faisal. We'll be in touch next week.", "شكرًا يا فيصل. سنتواصل معك الأسبوع القادم.", "We'll be in touch" to "سنتواصل معك")
            reply(
                "تختم المقابلة.",
                natural("Thank you for your time. I look forward to hearing from you.", "شكرًا على وقتك. أتطلع لسماع ردكم.", "ختام احترافي."),
                ok("Thanks, bye.", "شكرًا، مع السلامة.", "مقبولة، لكنها غير رسمية."),
            )
            shadow("I look forward to hearing from you.", "أتطلع لسماع ردكم", "آي لُك فوروَرد تو هيرِنغ فروم يو", "look forward to + فعل بـ ing.")
        }
        mission(
            "جملة سيرتك الذاتية",
            "اكتب جملة تعريفية لسيرتك الذاتية بالإنجليزية، وأرسلها لصديق ليراجعها.",
            "I'm a … with … years of experience in …",
            stampCode = "CV", stampEn = "MISSION · MY CV",
        )
    }

    // ------------------------------------------------------------------------------ 10
    private fun ch10() = chapter(
        10, "HOME", "FRIENDS · DINNER", "عشاء مع الأصدقاء", "Dinner with Friends",
        "الدعوة والسفرة والوداع", SceneArt.DINNER,
    ) {
        cast("sam" to "سام", "emma" to "إيما (أخت سام)")
        scene("دعوة على العشاء", SceneArt.DINNER) {
            narrate("مرّت الشهور. سام دعاك للعشاء في بيت عائلته.")
            say("sam", "Would you like to come round for dinner on Saturday?", "هل تحب أن تأتي للعشاء يوم السبت؟", "come round for dinner" to "تأتي للعشاء عندنا")
            reply(
                "تقبل الدعوة.",
                natural("I'd love to! Can I bring anything?", "يسعدني! هل أحضر شيئًا؟", "Can I bring anything? لفتة مهذبة جدًا."),
                wrong("Yes, I come in Saturday.", "نعم، آتي في السبت.", "مع الأيام on، ونقول I'll come.", PREPOSITIONS),
            )
            say("sam", "Just bring yourself! Seven o'clock.", "أحضر نفسك فقط! الساعة السابعة.", "Just bring yourself" to "أحضر نفسك فقط")
            shadow("Can I bring anything?", "هل أحضر شيئًا؟", "كان آي برِنغ إنيثِنغ", "anything: إني-ثنغ بـ th.")
        }
        scene("على السفرة", SceneArt.DINNER) {
            narrate("طاولة عامرة ونقاش دافئ. إيما أخت سام تسألك عن السعودية.")
            say("emma", "What's the food like in Saudi Arabia?", "كيف هو الطعام في السعودية؟", "What's the food like" to "كيف هو الطعام؟")
            reply(
                "تحكي عن الكبسة.",
                natural("It's amazing! You have to try kabsa. It's rice with chicken and spices.", "رائع! يجب أن تجربي الكبسة: أرز مع دجاج وبهارات.", "You have to try = لازم تجرب."),
                wrong("Food is very delicious, kabsa is rice chicken.", "الأكل لذيذ جدًا، الكبسة أرز دجاج.", "نحتاج the food و rice with chicken.", ARTICLES),
            )
            say("emma", "That sounds delicious! Would you like some more?", "يبدو لذيذًا! هل تريد المزيد؟", "Would you like some more" to "هل تريد المزيد؟")
            reply(
                "شبعت.",
                natural("I'm full, thanks. It was lovely.", "شبعت، شكرًا. كان رائعًا.", "I'm full = شبعت."),
                wrong("No, I am finish.", "لا، أنا انتهيت.", "قل I'm full أو I've finished.", VERB_AS_ADJECTIVE),
            )
            shadow("You have to try kabsa.", "لازم تجرب الكبسة", "يو هاف تو تراي كَبسة", "have to تُنطق «هافتو».")
        }
        scene("الوداع", SceneArt.DINNER) {
            narrate("انتهت الأمسية. الشوارع هادئة، وأنت سعيد.")
            reply(
                "تشكرهم.",
                natural("Thank you so much for having me. I had a great time.", "شكرًا جزيلًا على الاستضافة. قضيت وقتًا رائعًا.", "Thank you for having me = شكرًا على استضافتي."),
                ok("Thanks for the food.", "شكرًا على الأكل.", "مقبولة، لكنها مختصرة."),
            )
            say("sam", "Anytime, mate. You're always welcome here.", "في أي وقت يا صديقي. أنت مرحّب بك دائمًا.", "You're always welcome" to "مرحّب بك دائمًا")
            shadow("Thank you so much for having me.", "شكرًا جزيلًا على استضافتي", "ثانك يو سو مَتش فور هاڤِنغ مي", "having: هاڤِنغ بـ V.")
        }
        mission(
            "علّم أحدًا من عائلتك",
            "علّم أحد أفراد عائلتك ٣ عبارات تعلمتها في رحلتك. من يعلّم يتعلم مرتين!",
            "Nice to meet you.", "Could I have …, please?", "Thank you for having me.",
            stampCode = "TCH", stampEn = "MISSION · TEACHER",
        )
    }
}
