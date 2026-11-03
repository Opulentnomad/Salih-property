# رحلة — Rihla

An offline-first Android app that teaches English to Arabic speakers as an **interactive
film**. You play a Saudi traveller who leaves Riyadh, lands in London and builds a life there,
chapter by chapter. There are no hearts, points or leagues: what pulls you forward is the story,
a notebook of phrases you collect, and a passport that fills with stamps.

## How it teaches

| Method | In the app |
|---|---|
| **Interactive story** (the backbone) | 10 chapters × 3 scenes: Riyadh airport → Heathrow → hotel → café → the Tube → shopping → clinic → university → job interview → dinner with friends |
| **Comprehensible input** | Characters speak in English (read aloud by text-to-speech, at normal speed or 0.65× slow), with Arabic subtitles. Nothing is quizzed before you've understood it |
| **Lexical chunks** | Each line highlights a ready-made phrase (*purpose of your visit*, *Could I have…?*) and saves it to your notebook |
| **Natural replies** | You choose what to say. The app tells you whether a reply sounds natural, acceptable or unnatural, and why. Unnatural replies are typical Arabic-speaker traps (*I am agree*, *since two weeks*, *a suitcase black*), so they get targeted coaching and you try again |
| **Shadowing** (كرّر بعدي) | Hear a phrase, then repeat it. Your real microphone level is drawn as a waveform under the reference, and speech recognition gives a pronunciation match score. You can skip with "I can't speak right now" |
| **Sentence mining** (دفتري) | Every phrase you meet is kept in its sentence and scheduled for review with SM-2. A review session plays the sentence, you recall the meaning, then grade yourself |
| **Real-life missions** | After each chapter you get a task to do outside the app (order a coffee in English, describe your route, teach your family three phrases). Completing it earns a passport stamp |

Habit support: first-run onboarding (name, reason for learning, daily minutes, reminder time),
a daily reminder notification whose message changes with your situation (Ramadan, days of
commitment, the next scene), a "days of commitment" count that never punishes you, and seasonal
greetings (Ramadan, Eid, National Day) based on the Umm al-Qura calendar.

## Design

The look is cinematic: a near-black cinema-ink background, warm amber "film light" as the only
accent, and blue reserved for "natural". Fonts are bundled so the app works offline (all SIL
OFL): Reem Kufi for Arabic titles, IBM Plex Sans Arabic for body text, and Inter for English
"subtitles". The interface is Arabic RTL, and every piece of English is forced LTR. Each of the
12 scene backdrops is drawn in code (`SceneBackdrop.kt`) with letterbox bars. Icons are
thin-stroke drawings, with no emoji.

## Architecture

```
:core   Pure Kotlin/JVM, unit-tested
  journey/Model.kt        Chapter → Scene → Beat (Narration, Line+Chunk, Reply, Shadow), Mission
  journey/Script.kt       The screenplay: 10 chapters, 30 scenes, 10 missions
  journey/ScenePlayer.kt  Immutable scene state machine
  journey/Progress.kt     Progress, notebook (SM-2), missions, stamps, commitment days, repositories
  engine/                 AnswerMatcher (speech scoring), Sm2Scheduler
  season/                 SeasonCalendar (Hijri seasons)
  model/                  ErrorPattern (Arabic-speaker traps), UserProfile

:app    Android (Kotlin, Jetpack Compose, Material 3, MVVM + StateFlow)
  data/JourneyStore.kt    DataStore (JSON) persistence for progress + profile
  audio/                  TextToSpeech and SpeechRecognizer wrappers
  reminders/              WorkManager daily reminder
  ui/journey|scene|notebook|passport|onboarding|navigation|components|theme
```

To edit the story, change `core/.../journey/Script.kt`. The ids are stable (`ch2_s1`), so saved
progress survives edits. The unit tests check that every scene can be played to the end, that
ids and stamps are unique, and that every line's speaker is in the chapter's cast.

## Build and run

Each push to the branch is built by GitHub Actions. The debug APK is kept as a **private
artifact** on the run page (Actions → Build APK → Artifacts).

```bash
./gradlew :app:installDebug
./gradlew :core:test :app:testDebugUnitTest
```

For audio to work offline, the device needs English voice data installed (Settings →
Accessibility → Text-to-speech).
