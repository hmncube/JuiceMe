package com.hmncube.juiceme

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

class AppSnackbarVisuals(
    override val message: String,
    val isError: Boolean
) : SnackbarVisuals {
    override val actionLabel: String
        get() = ""
    override val withDismissAction: Boolean
        get() = false
    override val duration: SnackbarDuration
        get() = if (isError) SnackbarDuration.Long else SnackbarDuration.Short
}

class AppSnackbarVisualsFeedback(
    override val message: String,
    val label: String,
) : SnackbarVisuals {
    override val actionLabel: String
        get() = label
    override val withDismissAction: Boolean
        get() = true
    override val duration: SnackbarDuration
        get() = SnackbarDuration.Indefinite
}