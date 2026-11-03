package com.rihla.english.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rihla.english.R
import com.rihla.english.core.model.DailyGoalLevel
import com.rihla.english.core.model.LearningGoal
import com.rihla.english.ui.components.PressableSurface
import com.rihla.english.ui.components.RihlaButton
import com.rihla.english.ui.progress.ReminderOptions
import com.rihla.english.ui.theme.Night
import com.rihla.english.ui.theme.NightSkyBrush
import com.rihla.english.ui.theme.Saffron
import com.rihla.english.ui.theme.SaffronDark

/**
 * First-run journey: welcome → name → why → daily time → reminder. Personalising the goal and
 * the reminder up front is what turns a download into a daily habit.
 */
@Composable
fun OnboardingScreen(onFinish: (name: String, goal: LearningGoal?, level: DailyGoalLevel, reminderHour: Int?) -> Unit) {
    var step by rememberSaveable { mutableStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf<LearningGoal?>(null) }
    var level by rememberSaveable { mutableStateOf(DailyGoalLevel.REGULAR) }
    var reminder by rememberSaveable { mutableStateOf<Int?>(21) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onFinish(name, goal, level, reminder)
    }

    fun finish() {
        if (reminder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onFinish(name, goal, level, reminder)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(NightSkyBrush),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp),
        ) {
            // Step dots
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(5) { i ->
                    Box(
                        Modifier
                            .padding(3.dp)
                            .width(if (i == step) 26.dp else 10.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (i <= step) Saffron else Color.White.copy(alpha = 0.25f)),
                    )
                }
            }
            AnimatedContent(
                targetState = step,
                transitionSpec = { (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith fadeOut() },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "onboarding",
            ) { current ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (current) {
                        0 -> Welcome()
                        1 -> {
                            Title(stringResource(R.string.onb_name_title), "🙋")
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.take(24) },
                                placeholder = { Text(stringResource(R.string.onb_name_hint)) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { step = 2 }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Saffron,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                    cursorColor = Saffron,
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        2 -> {
                            Title(stringResource(R.string.onb_goal_title), "🎯")
                            LearningGoal.entries.forEach { g ->
                                OptionCard("${g.emoji}  ${g.titleAr}", selected = goal == g) { goal = g }
                            }
                        }
                        3 -> {
                            Title(stringResource(R.string.onb_level_title), "⏱️")
                            DailyGoalLevel.entries.forEach { l ->
                                OptionCard("${l.emoji}  ${l.titleAr} — ${l.minutesAr}", selected = level == l) { level = l }
                            }
                        }
                        else -> {
                            Title(stringResource(R.string.onb_reminder_title), "⏰")
                            Text(stringResource(R.string.onb_reminder_body), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.85f))
                            ReminderOptions.forEach { (hour, label) ->
                                OptionCard(
                                    if (label == 0) stringResource(R.string.settings_reminder_off) else "🔔  " + stringResource(label),
                                    selected = reminder == hour,
                                ) { reminder = hour }
                            }
                        }
                    }
                }
            }
            RihlaButton(
                text = stringResource(
                    when (step) {
                        0 -> R.string.onb_start
                        4 -> R.string.onb_finish
                        else -> R.string.onb_next
                    },
                ),
                onClick = { if (step < 4) step++ else finish() },
                color = Saffron,
                shadowColor = SaffronDark,
                textColor = Night,
                enabled = step != 2 || goal != null,
            )
        }
    }
}

@Composable
private fun Welcome() {
    val float = rememberInfiniteTransition(label = "camel")
    val dy by float.animateFloat(0f, -14f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "dy")
    val pop = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, tween(600)) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("🐪", fontSize = 110.sp, modifier = Modifier.graphicsLayer { translationY = dy; scaleX = pop.value; scaleY = pop.value })
        Text("🌙  ✨  🌴", fontSize = 26.sp)
        Text(stringResource(R.string.onb_welcome_title), style = MaterialTheme.typography.displayMedium, color = Color.White, textAlign = TextAlign.Center)
        Text(stringResource(R.string.onb_welcome_body), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun Title(text: String, emoji: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 34.sp)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.headlineLarge, color = Color.White)
    }
}

@Composable
private fun OptionCard(text: String, selected: Boolean, onClick: () -> Unit) {
    PressableSurface(
        onClick = onClick,
        color = if (selected) Saffron else Color.White.copy(alpha = 0.1f),
        shadowColor = if (selected) SaffronDark else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(2.dp, if (selected) Saffron else Color.White.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) Night else Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        )
    }
}
