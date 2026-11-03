package com.rihla.english.core.story

/**
 * "مواقف من حياتك" — interactive dialogues for situations Gulf learners actually face.
 * Each BAD option is a typical Arabic-speaker mistake, so the reaction teaches the fix.
 */
object SeedStories {

    val all: List<Story> by lazy { listOf(airport(), driver(), clinic(), hotel(), interview(), university()) }

    fun byId(id: String): Story? = all.firstOrNull { it.id == id }

    private fun airport() = story(
        id = "airport", titleAr = "في المطار", titleEn = "Airport check-in", emoji = "✈️",
        scenarioAr = "أنت مسافر إلى لندن وتقف أمام موظفة تسجيل الدخول في المطار.",
        color = 0xFF3B82F6,
    ) {
        character("agent", "موظفة الطيران", "👩‍✈️")
        character("me", "أنت", "🧑")
        say("agent", "Good morning! Can I see your passport, please?", "صباح الخير! هل يمكنني رؤية جواز سفرك من فضلك؟")
        choice(
            "سلّمها الجواز. ماذا تقول؟",
            good("Here you are.", "تفضّلي.", "ممتاز! Here you are هي العبارة المناسبة عند تسليم شيء."),
            ok("Yes, passport.", "نعم، الجواز.", "مفهومة، لكن Here you are أكثر لباقة."),
            bad("Take.", "خذي.", "Take وحدها تبدو أمرًا جافًا. قل Here you are.", "agent", "Oh... okay. Thank you.", "أوه... حسنًا. شكرًا."),
        )
        say("agent", "Thank you. Where are you flying to today?", "شكرًا. إلى أين تسافر اليوم؟")
        choice(
            "أخبرها بوجهتك.",
            good("I'm flying to London.", "سأسافر إلى لندن.", "رائع! جملة كاملة: I'm flying to + المكان."),
            ok("London.", "لندن.", "مفهومة، لكن حاول أن تقول جملة كاملة."),
            bad("I am fly London.", "أنا أطير لندن.", "خطأ شائع: لا نضع am قبل fly، ونحتاج to قبل المكان: I'm flying to London.", "agent", "Sorry? Where are you going?", "عفوًا؟ إلى أين أنت ذاهب؟"),
        )
        say("agent", "Do you have any bags to check in?", "هل لديك حقائب لتسجيلها؟")
        choice(
            "معك حقيبة واحدة كبيرة.",
            good("Yes, one bag, please.", "نعم، حقيبة واحدة من فضلك.", "ممتاز! واضحة ومهذبة."),
            bad("Yes, I have two bag.", "نعم، عندي حقيبتين.", "انتبه: الجمع يحتاج s ‑ two bags. وأنت معك حقيبة واحدة فقط!", "agent", "Two bags? I can only see one.", "حقيبتان؟ أرى واحدة فقط."),
            ok("One.", "واحدة.", "مفهومة، لكن أضف please لتكون ألطف."),
        )
        say("agent", "Would you like a window seat or an aisle seat?", "هل تفضّل مقعدًا بجانب النافذة أم بجانب الممر؟")
        choice(
            "تريد مقعدًا بجانب النافذة.",
            good("A window seat, please.", "مقعد بجانب النافذة من فضلك.", "ممتاز!"),
            bad("I want the window open.", "أريد النافذة مفتوحة.", "هذا يعني أنك تريد فتح النافذة! قل: A window seat, please.", "agent", "Sorry, the windows on the plane don't open!", "عذرًا، نوافذ الطائرة لا تُفتح!"),
            ok("Window.", "نافذة.", "مفهومة، لكن الجملة الكاملة أفضل: A window seat, please."),
        )
        say("agent", "Here is your boarding pass. Gate 12. Have a nice flight!", "هذه بطاقة صعود الطائرة. البوابة ١٢. رحلة سعيدة!")
        choice(
            "ودّعها بلطف.",
            good("Thank you very much!", "شكرًا جزيلًا!", "رائع! ختام مهذب."),
            ok("Okay, bye.", "حسنًا، مع السلامة.", "مقبولة، لكن Thank you أجمل."),
        )
        ending = "✅ حصلت على بطاقة صعود الطائرة! أنت جاهز للسفر."
    }

    private fun driver() = story(
        id = "driver", titleAr = "مع السائق", titleEn = "Giving directions", emoji = "🚗",
        scenarioAr = "ركبت سيارة أجرة والسائق لا يتحدث العربية. عليك أن توجّهه إلى المول.",
        color = 0xFFF59E0B,
    ) {
        character("driver", "السائق", "🧔")
        character("me", "أنت", "🧑")
        say("driver", "Hello, sir! Where do you want to go?", "مرحبًا يا سيدي! إلى أين تريد أن تذهب؟")
        choice(
            "تريد الذهاب إلى المول.",
            good("To the mall, please.", "إلى المول من فضلك.", "ممتاز! قصيرة ومهذبة."),
            ok("Mall.", "مول.", "مفهومة، لكن To the mall, please ألطف."),
            bad("I am go mall.", "أنا أذهب مول.", "لا نضع am قبل الفعل go. قل: To the mall, please.", "driver", "Sorry? Which place?", "عفوًا؟ أي مكان؟"),
        )
        say("driver", "Okay. Which way? Left or right?", "حسنًا. أي طريق؟ يسار أم يمين؟")
        choice(
            "يجب أن ينعطف يمينًا عند الإشارة.",
            good("Turn right at the traffic light.", "انعطف يمينًا عند الإشارة.", "ممتاز! turn right / turn left عبارات أساسية للسائق."),
            bad("Go right in the light.", "اذهب يمين في الإشارة.", "نقول turn right، ونستخدم at مع الأماكن المحددة: at the traffic light.", "driver", "In the light? I don't understand.", "في الإشارة؟ لم أفهم."),
            ok("Right.", "يمين.", "مفهومة، لكن أضف turn و at the traffic light ليكون أوضح."),
        )
        say("driver", "Is it far from here?", "هل هو بعيد من هنا؟")
        choice(
            "ليس بعيدًا، حوالي خمس دقائق.",
            good("No, it's about five minutes.", "لا، حوالي خمس دقائق.", "رائع! about = تقريبًا."),
            ok("Five minutes.", "خمس دقائق.", "مفهومة تمامًا."),
            bad("It's not far, five minute.", "ليس بعيدًا، خمس دقيقة.", "الجمع يحتاج s: five minutes.", "driver", "Five... minute? One minute?", "خمس... دقيقة؟ دقيقة واحدة؟"),
        )
        say("driver", "We are here. That's 25 riyals.", "وصلنا. الأجرة ٢٥ ريالًا.")
        choice(
            "تريد أن تتوقف السيارة عند الباب.",
            good("Please stop here, at the main door.", "توقف هنا من فضلك، عند الباب الرئيسي.", "ممتاز! stop here عبارة مفيدة جدًا."),
            bad("Close the car here.", "سكّر السيارة هنا.", "close تعني يغلق وليس يوقف. قل: Stop here, please.", "driver", "Close the car? The doors are closed!", "أغلق السيارة؟ الأبواب مغلقة!"),
        )
        say("driver", "No problem. Have a good day!", "لا مشكلة. يومك سعيد!")
        choice(
            "ادفع واشكره.",
            good("Thanks! Keep the change.", "شكرًا! احتفظ بالباقي.", "Keep the change = خلّ الباقي لك. لطيفة جدًا!"),
            ok("Thank you.", "شكرًا لك.", "ممتاز وبسيط."),
        )
        ending = "✅ وصلت إلى المول بسلام! وجّهت السائق بالإنجليزية."
    }

    private fun clinic() = story(
        id = "clinic", titleAr = "في العيادة", titleEn = "At the clinic", emoji = "🏥",
        scenarioAr = "تشعر بالتعب وذهبت إلى عيادة يعمل فيها طبيب لا يتحدث العربية.",
        color = 0xFF10B981, requires = "u3_l1",
    ) {
        character("doctor", "الطبيب", "👨‍⚕️")
        character("me", "أنت", "🧑")
        say("doctor", "Hello. What's the problem today?", "مرحبًا. ما المشكلة اليوم؟")
        choice(
            "عندك صداع.",
            good("I have a headache.", "عندي صداع.", "ممتاز! I have a + الألم: headache, stomachache, toothache."),
            bad("My head is pain.", "رأسي ألم.", "ترجمة حرفية. قل: I have a headache أو My head hurts.", "doctor", "Your head is... pain? Do you mean it hurts?", "رأسك... ألم؟ تقصد أنه يؤلمك؟"),
            ok("Headache.", "صداع.", "مفهومة، لكن الجملة الكاملة أفضل."),
        )
        say("doctor", "I see. Since when?", "فهمت. منذ متى؟")
        choice(
            "منذ يومين.",
            good("For two days.", "منذ يومين.", "ممتاز! for + مدة زمنية."),
            bad("From two days.", "من يومين.", "خطأ شائع من «من يومين». نقول for two days أو since Monday.", "doctor", "From two days... until when?", "من يومين... إلى متى؟"),
            ok("Two days.", "يومين.", "مفهومة."),
        )
        say("doctor", "Do you have a fever?", "هل عندك حرارة؟")
        choice(
            "نعم، قليلًا.",
            good("Yes, a little.", "نعم، قليلًا.", "ممتاز!"),
            bad("Yes, I am fever.", "نعم، أنا حرارة.", "نقول I have a fever (عندي حرارة) وليس I am fever.", "doctor", "You are... a fever? You have a fever, right?", "أنت... حرارة؟ تقصد عندك حرارة؟"),
        )
        say("doctor", "Are you allergic to any medicine?", "هل عندك حساسية من أي دواء؟")
        choice(
            "لا، ليس عندك حساسية.",
            good("No, I'm not allergic to anything.", "لا، ليس عندي حساسية من أي شيء.", "ممتاز! allergic to = عنده حساسية من."),
            ok("No.", "لا.", "مقبولة، الإجابة واضحة."),
            bad("No, I don't allergic.", "لا، أنا لا حساسية.", "allergic صفة وليست فعلًا: I'm not allergic.", "doctor", "Sorry, are you allergic or not?", "عفوًا، هل عندك حساسية أم لا؟"),
        )
        say("doctor", "Okay. Take this medicine twice a day, after food.", "حسنًا. خذ هذا الدواء مرتين في اليوم بعد الأكل.")
        choice(
            "تأكّد من التعليمات.",
            good("Twice a day after food. Thank you, doctor.", "مرتين يوميًا بعد الأكل. شكرًا يا دكتور.", "رائع! تكرار التعليمات يدل على أنك فهمت."),
            ok("Okay, thanks.", "حسنًا، شكرًا.", "مقبولة."),
        )
        ending = "✅ شرحت حالتك للطبيب وحصلت على العلاج. سلامتك!"
    }

    private fun hotel() = story(
        id = "hotel", titleAr = "في الفندق", titleEn = "Hotel check-in", emoji = "🏨",
        scenarioAr = "وصلت إلى فندقك في دبي، وهناك مشكلة صغيرة في الغرفة.",
        color = 0xFF8B5CF6, requires = "u3_l4",
    ) {
        character("clerk", "موظف الاستقبال", "🧑‍💼")
        character("me", "أنت", "🧑")
        say("clerk", "Welcome! Do you have a reservation?", "أهلًا بك! هل لديك حجز؟")
        choice(
            "عندك حجز باسم أحمد.",
            good("Yes, I have a reservation under Ahmed.", "نعم، عندي حجز باسم أحمد.", "ممتاز! under + الاسم = باسم."),
            ok("Yes. Ahmed.", "نعم. أحمد.", "مفهومة."),
            bad("Yes, I am reservation.", "نعم، أنا حجز.", "قل I have a reservation (عندي حجز) وليس I am reservation.", "clerk", "You are... a reservation?", "أنت... حجز؟"),
        )
        say("clerk", "Here is your key. Room 305, on the third floor.", "هذا مفتاحك. غرفة ٣٠٥ في الطابق الثالث.")
        choice(
            "تسأل عن وقت الإفطار.",
            good("What time is breakfast?", "متى موعد الإفطار؟", "ممتاز! What time is...? سؤال مفيد جدًا."),
            bad("When the breakfast?", "متى الإفطار؟", "السؤال يحتاج فعلًا: What time is breakfast?", "clerk", "Sorry, could you repeat that?", "عفوًا، هل يمكنك الإعادة؟"),
        )
        say("clerk", "Breakfast is from 6 to 10 in the morning.", "الإفطار من ٦ إلى ١٠ صباحًا.")
        say("clerk", "Hello, reception. How can I help you?", "مرحبًا، الاستقبال. كيف يمكنني مساعدتك؟")
        choice(
            "التكييف في غرفتك لا يعمل.",
            good("The air conditioner in my room isn't working.", "المكيّف في غرفتي لا يعمل.", "ممتاز! isn't working = لا يعمل."),
            bad("The AC is not walking.", "المكيّف لا يمشي.", "ترجمة حرفية لـ «ما يمشي». الصحيح isn't working.", "clerk", "Not walking? Sorry, I don't understand.", "لا يمشي؟ عذرًا، لم أفهم."),
            ok("AC problem.", "مشكلة في المكيّف.", "مفهومة، لكن الجملة الكاملة أوضح."),
        )
        say("clerk", "I'm sorry about that. I'll send someone right away.", "آسف على ذلك. سأرسل أحدًا فورًا.")
        choice(
            "اشكره.",
            good("Thank you, I appreciate it.", "شكرًا، أقدّر ذلك.", "رائع! I appreciate it تعبير لبق جدًا."),
            ok("Okay, thank you.", "حسنًا، شكرًا.", "ممتاز."),
        )
        ending = "✅ استلمت غرفتك وحُلّت المشكلة. إقامة سعيدة!"
    }

    private fun interview() = story(
        id = "interview", titleAr = "مقابلة عمل", titleEn = "Job interview", emoji = "💼",
        scenarioAr = "عندك مقابلة عمل في شركة عالمية، والمقابلة باللغة الإنجليزية.",
        color = 0xFF0EA5E9, requires = "u4_l1",
    ) {
        character("hr", "مديرة التوظيف", "👩‍💼")
        character("me", "أنت", "🧑")
        say("hr", "Good morning. Please, have a seat.", "صباح الخير. تفضّل بالجلوس.")
        choice(
            "رد التحية.",
            good("Good morning. Thank you for having me.", "صباح الخير. شكرًا لاستضافتي.", "ممتاز! بداية احترافية."),
            ok("Thanks.", "شكرًا.", "مقبولة، لكن في المقابلات كن أكثر رسمية."),
        )
        say("hr", "Can you tell me about yourself?", "هل يمكنك أن تحدثني عن نفسك؟")
        choice(
            "عرّف بنفسك.",
            good("My name is Khalid. I am an engineer with five years of experience.", "اسمي خالد. أنا مهندس ولدي خبرة خمس سنوات.", "ممتاز! لاحظ an engineer و five years of experience."),
            bad("I am engineer, I have experience five years.", "أنا مهندس، عندي خبرة خمس سنوات.", "نحتاج an قبل engineer، والترتيب الصحيح: five years of experience.", "hr", "Okay... so how many years of experience?", "حسنًا... إذن كم سنة خبرة؟"),
        )
        say("hr", "Why do you want to work with us?", "لماذا تريد العمل معنا؟")
        choice(
            "أعجبتك الشركة وتريد أن تتعلم.",
            good("I like your company, and I want to learn and grow.", "تعجبني شركتكم وأريد أن أتعلم وأتطور.", "ممتاز! learn and grow عبارة جميلة في المقابلات."),
            bad("Because I am like your company.", "لأني أنا أحب شركتكم.", "لا نضع am قبل like: I like your company.", "hr", "You are like our company?", "أنت تشبه شركتنا؟"),
            ok("For the salary.", "من أجل الراتب.", "صريح، لكن ليس أفضل إجابة في المقابلة 😄"),
        )
        say("hr", "What is your biggest strength?", "ما أكبر نقاط قوتك؟")
        choice(
            "أنت تعمل جيدًا مع الفريق.",
            good("I work well in a team.", "أعمل جيدًا ضمن فريق.", "ممتاز! work well in a team."),
            bad("I am very good work in team.", "أنا جيد جدًا عمل في فريق.", "قل I work well in a team. well تصف الفعل.", "hr", "Sorry, could you say that again?", "عفوًا، هل يمكنك الإعادة؟"),
        )
        say("hr", "Thank you, Khalid. We will contact you soon.", "شكرًا يا خالد. سنتواصل معك قريبًا.")
        choice(
            "اختم المقابلة.",
            good("Thank you for your time. I look forward to hearing from you.", "شكرًا على وقتك. أتطلع لسماع ردكم.", "رائع! ختام احترافي جدًا."),
            ok("Thank you, bye.", "شكرًا، مع السلامة.", "مقبولة."),
        )
        ending = "✅ أنهيت مقابلة العمل بثقة! بالتوفيق."
    }

    private fun university() = story(
        id = "university", titleAr = "أول يوم في الجامعة", titleEn = "First day abroad", emoji = "🎓",
        scenarioAr = "أنت مبتعث وهذا أول يوم لك في جامعة بالخارج. تتعرف على زميل جديد.",
        color = 0xFFEC4899, requires = "u4_l4",
    ) {
        character("friend", "زميلك سام", "🧑‍🎓")
        character("me", "أنت", "🧑")
        say("friend", "Hi! Are you new here? I'm Sam.", "مرحبًا! هل أنت جديد هنا؟ أنا سام.")
        choice(
            "عرّف بنفسك.",
            good("Hi Sam! Yes, I'm new. I'm Faisal, from Saudi Arabia.", "مرحبًا سام! نعم أنا جديد. أنا فيصل من السعودية.", "ممتاز! تعريف كامل وودود."),
            ok("Yes. Faisal.", "نعم. فيصل.", "مفهومة، لكن كن أكثر ودًّا."),
            bad("Yes, I new. My name Faisal.", "نعم، أنا جديد. اسمي فيصل.", "نسيت am و is: I'm new. My name is Faisal.", "friend", "Sorry? What's your name?", "عفوًا؟ ما اسمك؟"),
        )
        say("friend", "Nice to meet you! What are you studying?", "سعيد بلقائك! ماذا تدرس؟")
        choice(
            "تدرس علوم الحاسب.",
            good("I'm studying computer science.", "أدرس علوم الحاسب.", "ممتاز!"),
            bad("I am study computer.", "أنا أدرس كمبيوتر.", "قل I'm studying أو I study (بدون am قبل study).", "friend", "You study... computers?", "تدرس... الحواسيب؟"),
        )
        say("friend", "Cool! Do you know where the library is?", "رائع! هل تعرف أين المكتبة؟")
        choice(
            "لا تعرف وتسأل إن كان يعرف.",
            good("No, I don't. Do you know?", "لا، لا أعرف. هل تعرف أنت؟", "ممتاز! I don't = لا أعرف."),
            bad("No, I am not know.", "لا، أنا لا أعرف.", "نفي الفعل يكون بـ don't: I don't know.", "friend", "You are not... know?", "أنت لست... تعرف؟"),
        )
        say("friend", "Come with me. Let's go together!", "تعال معي. لنذهب معًا!")
        choice(
            "وافق بحماس.",
            good("Great, thanks! That's very kind of you.", "رائع، شكرًا! هذا لطف منك.", "رائع! That's very kind of you عبارة جميلة."),
            ok("Okay.", "حسنًا.", "مقبولة."),
        )
        ending = "✅ كوّنت أول صداقة في الجامعة! بداية موفقة."
    }
}
