package com.hmncube.juiceme.components

import androidx.compose.ui.graphics.vector.ImageVector

data class OpenAlertDialogValues(
    val onConfirmation: () -> Unit,
    val dialogTitle: String,
    val dialogText: String,
    val icon: ImageVector,
    val isDanger: Boolean,
)
