package com.rihla.english.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** A user-facing message produced by a ViewModel without needing a Context. */
sealed interface UiMessage {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiMessage
    data class Raw(val text: String) : UiMessage
}

@Composable
fun UiMessage.asText(): String = when (this) {
    is UiMessage.Raw -> text
    is UiMessage.Res -> stringResource(id, *args.toTypedArray())
}
