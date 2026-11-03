package com.rihla.english.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rihla.english.R
import com.rihla.english.core.journey.SceneArt
import com.rihla.english.core.model.DailyGoalLevel
import com.rihla.english.core.model.LearningGoal
import com.rihla.english.ui.components.CineButton
import com.rihla.english.ui.components.EnglishText
import com.rihla.english.ui.components.SceneBackdrop
import com.rihla.english.ui.theme.Cine
import com.rihla.english.ui.theme.EnglishTextStyles

/** Reminder hour → label (null hour = no reminder). */
private val ReminderOptions: List<Pair<Int?, Int>> = listOf(
    8 to R.string.time_8am, 13 to R.string.time_1pm, 18 to R.string.time_6pm, 21 to R.string.time_9pm, null to R.string.reminder_off,
)

/** First run, like the opening titles of a film: welcome → name → why → daily time → reminder. */
@Composable
fun OnboardingScreen(onFinish: (name: String, goal: LearningGoal?, level: DailyGoalLevel, reminderHour: Int?) -> Unit) {
    var step by rememberSaveable { mutableStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf<LearningGoal?>(null) }
    var level by rememberSaveable { mutableStateOf(DailyGoalLevel.REGULAR) }
    var reminder by rememberSaveable { mutableStateOf<Int?>(21) }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onFinish(name, goal, level, reminder) }

    fun finish() {
        if (reminder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        else onFinish(name, goal, level, reminder)
    }

    Column(Modifier.fillMaxSize().background(Cine.Ink)) {
        if (step == 0) {
            Box(Modifier.fillMaxWidth().height(340.dp)) {
                SceneBackdrop(SceneArt.TAXI, Modifier.fillMaxSize())
                Box(Modifier.fillMaxWidth().height(26.dp).background(Cine.Letterbox))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Cine.Ink)))
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().then(if (step == 0) Modifier else Modifier.statusBarsPadding()).imePadding().padding(24.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(5) { i ->
                    Box(
                        Modifier.padding(3.dp).width(if (i == step) 24.dp else 8.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                            .background(if (i <= step) Cine.Amber else Cine.Line),
                    )
                }
            }
            AnimatedContent(step, transitionSpec = { fadeIn() togetherWith fadeOut() }, modifier = Modifier.weight(1f), label = "onboarding") { current ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (current) {
                        0 -> {
                            Text(stringResource(R.string.onb_welcome_title), style = MaterialTheme.typography.displayMedium)
                            EnglishText("Riyadh → London", style = EnglishTextStyles.option, color = Cine.Amber)
                            Text(stringResource(R.string.onb_welcome_body), style = MaterialTheme.typography.bodyLarge, color = Cine.TextSoft)
                        }
                        1 -> {
                            Title(stringResource(R.string.onb_name_title))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.take(24) },
                                placeholder = { Text(stringResource(R.string.onb_name_hint)) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { step = 2 }),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cine.Amber, unfocusedBorderColor = Cine.Line, cursorColor = Cine.Amber),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        2 -> {
                            Title(stringResource(R.string.onb_goal_title))
                            LearningGoal.entries.forEach { g -> Option(g.titleAr, goal == g) { goal = g } }
                        }
                        3 -> {
                            Title(stringResource(R.string.onb_level_title))
                            DailyGoalLevel.entries.forEach { l -> Option(stringResource(R.string.onb_minutes, l.titleAr, l.minutes), level == l) { level = l } }
                        }
                        else -> {
                            Title(stringResource(R.string.onb_reminder_title))
                            Text(stringResource(R.string.onb_reminder_body), style = MaterialTheme.typography.bodyLarge, color = Cine.Muted)
                            ReminderOptions.forEach { (hour, label) -> Option(stringResource(label), reminder == hour) { reminder = hour } }
                        }
                    }
                }
            }
            CineButton(
                text = stringResource(
                    when (step) {
                        0 -> R.string.onb_start
                        4 -> R.string.onb_finish
                        else -> R.string.onb_next
                    },
                ),
                onClick = { if (step < 4) step++ else finish() },
                enabled = step != 2 || goal != null,
                modifier = Modifier.navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(text, style = MaterialTheme.typography.headlineLarge)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun Option(text: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (selected) Cine.Amber else Cine.Line),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Cine.AmberTint else Cine.Panel,
            contentColor = if (selected) Cine.AmberSoft else Cine.Text,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    }
}
