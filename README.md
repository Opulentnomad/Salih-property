# رحلة الإنجليزية — Rihla English

An offline-first Android app that teaches English to native Arabic speakers from absolute zero
(A0/A1), with a full gamification layer: XP and combos, daily streaks with streak freezes, hearts,
gems, treasure chests, a gem shop, weekly leagues and achievement badges.

- **Arabic RTL interface.** Navigation, instructions, grammar tips and feedback are all in Arabic.
- **English shown LTR.** Every English letter, word and sentence is forced left-to-right and set in
  large, high-contrast type, including word chips, which stay in English word order.
- **Built for Arabic speakers:**
  - Minimal-pair drills for sounds Arabic doesn't have: P/B (pear/bear, pin/bin), F/V (fan/van,
    fine/vine), the digraphs sh/ch/th, and silent letters (knife, write, hour).
  - Grammar Bridges: a visual mapping from an Arabic nominal sentence to its English version. For
    example, «أنا طالب» → *I **am** **a** student*. The copula and article that Arabic leaves out
    are highlighted.
  - Pronunciation is spelled in Arabic letters (هَلُو) next to the IPA.

## Tech stack

| Concern | Choice |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3 |
| Architecture | Clean Architecture + MVVM, unidirectional data flow (`StateFlow<UiState>`) |
| Curriculum and learning progress | Room (exercise payloads stored as polymorphic JSON via kotlinx.serialization) |
| Gamification stats | Jetpack DataStore (Preferences), updated in atomic transactions |
| Spaced repetition | SM-2 per vocabulary item |
| Audio | `TextToSpeech` (en-US, falls back to en-GB, prefers an offline voice). 🔊 normal speed and 🐢 0.65× slow |
| Speaking | `SpeechRecognizer` (prefers offline) with fuzzy matching (Levenshtein, contractions, digits) |
| DI | Manual container (`AppContainer`), no annotation processing besides Room |

## Modules

```
:core   Pure Kotlin/JVM: no Android. Unit-tested.
  model/        Curriculum, Exercise (6 types, sealed + @Serializable), UserStats, Badge, League, ShopItem
  engine/       GamificationEngine, LeagueEngine, Sm2Scheduler, AnswerMatcher, ExerciseGrader,
                SkillPath (unlock rules), LessonQueue (mistake-recovery queue)
  repository/   CurriculumRepository, UserStatsRepository (interfaces)
  curriculum/   SeedCurriculumData.kt + a small builder DSL

:app    Android
  data/local/       Room entities, DAOs, RihlaDatabase, UserStatsDataStore
  data/repository/  CurriculumRepositoryImpl (seeding, practice sessions, SM-2 + mistakes)
  audio/            TtsManager, SpeechRecognizerManager
  di/               AppContainer
  ui/gamification/  GamificationViewModel (activity-scoped)
  ui/lesson/        LessonViewModel, LessonArenaScreen, FeedbackSheet, exercises/*
  ui/home/          SkillTreeHomeScreen (snake path)
  ui/victory/       LessonVictoryScreen (confetti + chest opening)
  ui/shop/          ShopAndAchievementsScreen (shop, badges, league)
  ui/components/    TopGamificationBar, GrammarBridgeCard, 3D buttons, LTR/RTL helpers
```

The gamification rules all live in `GamificationEngine`, a set of pure functions from
`UserStats` to a new `UserStats` plus a list of events. ViewModels apply them inside a single
DataStore transaction, so updates can't race and every rule can be tested on the JVM.

## Gamification rules

| Mechanic | Rule |
|---|---|
| XP | +10 per correct answer. From the 3rd correct answer in a row, each answer gets +5 combo bonus. Finishing a lesson adds +15, and a perfect lesson adds another +5. |
| Double XP ⚡ | 15-minute boost bought in the shop. It multiplies all XP by 2. |
| Daily goal and streak 🔥 | Reaching 30 XP in a day extends the streak (once per day). |
| Streak freeze ❄️ | You can own up to 2. When you open the app after missing days, freezes are used to cover them. If you don't have enough, the streak resets. |
| Hearts ❤️ | Max 5. Each mistake in a lesson costs 1. One heart comes back every 30 minutes. A refill costs 40 💎. Each finished practice session gives +1 heart. Practice never costs hearts. |
| Gems 💎 | +5 per lesson, +5 more for a perfect lesson, +20 per badge, and 15–40 per chest (25% chance of a bonus freeze). Everyone starts with 100. |
| Shop | Streak freeze 50 💎 · Heart refill 40 💎 · Double XP 60 💎 |
| Leagues 🏆 | Bronze → Silver → Gold → Diamond, in groups of 10. At the end of the week the top 3 move up and the bottom 3 move down. Rivals are simulated offline and deterministically. |
| Badges 🎖️ | 11 in total, including «أول درس», «سلسلة 7 أيام 🔥», «قناص الدقة 100% 🎯», «سيّد الحروف», «صائد الكنوز». |
| Mistake recovery | A missed exercise moves to the end of the queue. A lesson only ends once every item has been answered correctly. |
| SM-2 | Each word gets one review per session: quality 5 if right first time, 3 if right after a retry, 1 if never right. "Practice Mistakes" mixes recent mistakes with words that are due. |

## Curriculum (seed data)

The course has 4 units, 20 lessons, 8 chests and 270 exercises. Every lesson uses flashcards,
multiple choice, matching, listening and speaking, and most use word-bank sentences too.

1. **Alphabet & Essential Phonics:** letters A–Z in upper and lower case, P vs B, F vs V, sh/ch/th
   and silent letters.
2. **First Words & Greetings:** hello and goodbye, introducing yourself, pronouns
   (I/you/he/she/we/they), numbers 1–10, colours (adjective before noun).
3. **Building Your First Sentences:** *am / is / are*, negatives with *not*, *This is a…*, *a/an*.
4. **Daily Survival English:** family (*my*), food (*I like…*), ordering at a café, common verbs
   (no copula before a verb).

To change the content, edit `core/.../curriculum/SeedCurriculumData.kt` and bump `VERSION`. The
app re-seeds the content tables and keeps learner progress, because ids are stable.

## Build and run

You need Android Studio (Ladybug or newer) with JDK 17+ and Android SDK 35.

```bash
./gradlew :app:installDebug     # build and install on a device or emulator
./gradlew :core:test            # domain rules + seed-data integrity tests
./gradlew :app:testDebugUnitTest
```

For audio to work offline, the device needs English voice data installed (Settings → Accessibility
→ Text-to-speech). Speech recognition works offline only on devices that have an offline English
model. If recognition isn't available, the speaking challenges offer "لا أستطيع التحدث الآن"
("I can't speak right now") to skip them without penalty.

## Verification status

The authoring environment couldn't reach Google's Maven repository, so AGP and AndroidX couldn't be
downloaded and the APK has **not** been built here. What was checked:

- `:core` compiled and passed all 25 JVM tests: engine rules, SM-2, matcher, grader, unlock rules,
  the queue, and seed-data checks such as unique ids, valid answers, every canonical answer grading
  as correct, and a JSON round-trip of every exercise.
- All `:app` Kotlin sources type-checked with zero errors against JetBrains Compose Desktop 1.7.3
  (the same Compose and Material 3 APIs as the Compose BOM used here), real `android.*` framework
  classes, and signature stubs for the Room, DataStore, Navigation, Lifecycle and Activity APIs.
- `LessonViewModelTest` (6 tests) ran against the real ViewModel and engine with fake repositories.

Still to check on a real build: Room's KSP query validation, the first build's schema export to
`app/schemas/`, and on-device behaviour of TTS and speech recognition.
