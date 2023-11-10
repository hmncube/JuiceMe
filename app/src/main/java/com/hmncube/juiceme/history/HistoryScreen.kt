package com.hmncube.juiceme.history

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hmncube.juiceme.AppSnackbarVisuals
import com.hmncube.juiceme.MainActivity
import com.hmncube.juiceme.R
import com.hmncube.juiceme.ViewModelFactory
import com.hmncube.juiceme.components.AppAlertDialog
import com.hmncube.juiceme.components.OpenAlertDialogValues
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import com.hmncube.juiceme.extensions.getValueOrFalse
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HistoryScreen(context: Context, snackbarHostState: SnackbarHostState) {
    val viewModel: HistoryViewModel = ViewModelFactory(AppDatabase.getDatabase(context))
        .create(HistoryViewModel::class.java)
    HistoryContent(
        viewModel = viewModel,
        context = context,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun HistoryContent(
    viewModel: HistoryViewModel,
    context: Context,
    snackbarHostState: SnackbarHostState
) {
    val cardHistory = viewModel.history.observeAsState()
    if (viewModel.loading.getValueOrFalse()) {
        CircularProgressIndicator()
    } else {
        LazyColumn(modifier = Modifier.padding(top = 64.dp)) {
            items(items = cardHistory.value!!, itemContent = { item ->
                CardHistoryItem(
                    card = item,
                    context = context,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )

            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardHistoryItem(
    card: CardNumber,
    context: Context,
    viewModel: HistoryViewModel,
    snackbarHostState: SnackbarHostState
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
    ) {
        var expanded by remember { mutableStateOf(false) }
        val copiedString = stringResource(id = R.string.copied_recharge_code)
        val noPrefixErrorString = stringResource(id = R.string.no_prefix_error)
        val localCoroutineScope = rememberCoroutineScope()
        val activity = LocalContext.current as MainActivity
        val openAlertDialog = remember { mutableStateOf(false) }
        val openAlertDialogValues = remember { mutableStateOf<OpenAlertDialogValues?>(null) }

        when {
            openAlertDialog.value -> {
                val values = openAlertDialogValues.value
                if (values != null) {
                    AppAlertDialog(
                        onDismissRequest = {
                            openAlertDialog.value = false
                            openAlertDialogValues.value = null
                        },
                        onConfirmation = {
                            openAlertDialog.value = false
                            values.onConfirmation()
                            openAlertDialogValues.value = null
                        },
                        dialogTitle = values.dialogTitle,
                        dialogText = values.dialogText,
                        icon = values.icon,
                        isDanger = values.isDanger
                    )
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true },
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = card.number, style = MaterialTheme.typography.bodySmall
                        .copy(color = Color.White)
                )
                Text(
                    text = card.number, style = MaterialTheme.typography.bodySmall
                        .copy(color = Color.White)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = card.number,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = MaterialTheme.colorScheme.primary)
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                text = { Text("Redial") },
                onClick = {
                    expanded = false
                    val prefix = viewModel.getUssd(context = context)
                    if (prefix != null) {
                        activity.dialNumber(prefix, card.number)
                    } else {
                        localCoroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                AppSnackbarVisuals(
                                    message = noPrefixErrorString,
                                    isError = true
                                )
                            )
                        }
                    }
                }
            )
            Divider(color = MaterialTheme.colorScheme.primary)
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = {
                    expanded = false
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData =
                        ClipData.newPlainText(
                            context.resources.getString(R.string.recharge_code), card.number
                        )
                    clipboard.setPrimaryClip(clip)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        localCoroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                AppSnackbarVisuals(
                                    message = String.format(copiedString, card.number),
                                    isError = false
                                )
                            )
                        }
                    }
                }
            )
            Divider(color = MaterialTheme.colorScheme.primary)
            DropdownMenuItem(
                text = { Text("Delete", textAlign = TextAlign.Center) },
                onClick = {
                    openAlertDialog.value = true
                    expanded = false
                    openAlertDialogValues.value = OpenAlertDialogValues(
                        onConfirmation = { viewModel.deleteEntry(card) },
                        dialogTitle = "Delete",
                        dialogText = "Are you sure you want to delete ${card.number}?",
                        icon = Icons.Default.Warning,
                        isDanger = true
                    )
                }
            )
        }
    }
}