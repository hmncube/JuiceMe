package com.hmncube.juiceme.history

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.hmncube.juiceme.MainActivity
import com.hmncube.juiceme.R
import com.hmncube.juiceme.ViewModelFactory
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import com.hmncube.juiceme.extensions.getValueOrFalse
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HistoryScreen(context: Context) {
    val viewModel: HistoryViewModel = ViewModelFactory(AppDatabase.getDatabase(context))
        .create(HistoryViewModel::class.java)
    HistoryContent(viewModel, context)
}

@Composable
private fun HistoryContent(viewModel: HistoryViewModel, context: Context) {
    val cardHistory = viewModel.history.observeAsState()
    if (viewModel.loading.getValueOrFalse()) {
        CircularProgressIndicator()
    } else {
        LazyColumn(modifier = Modifier.padding(top = 64.dp)) {
            items(items = cardHistory.value!!, itemContent = { item ->
                CardHistoryItem(card = item, context = context, viewModel = viewModel)
            })
        }
    }
}

@Composable
private fun CardHistoryItem(card: CardNumber, context: Context, viewModel: HistoryViewModel) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
    ) {
        var expanded by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        val copiedString = stringResource(id = R.string.copied_recharge_code)
        val localCoroutineScope = rememberCoroutineScope()
        val activity = LocalContext.current as MainActivity

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
                Text(text = card.number, style = MaterialTheme.typography.bodySmall
                    .copy(color = Color.White))
                Text(text = card.number, style = MaterialTheme.typography.bodySmall
                    .copy(color = Color.White))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = "23232323",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = MaterialTheme.colorScheme.primary)
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                text = { Text("Redial") },
                onClick = {
                    Log.d("pundez", "CardHistoryItem: $activity")
                    //val activity = context as MainActivity
                    activity.dialNumber("22", "9999")
                    }
            )
            Divider(color = MaterialTheme.colorScheme.primary)
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData =
                        ClipData.newPlainText(
                            context.resources.getString(R.string.recharge_code), card.number
                        )
                    clipboard.setPrimaryClip(clip)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        localCoroutineScope.launch {
                            snackbarHostState.showSnackbar(String.format(copiedString, card.number))
                        }
                    }
                }
            )
            Divider(color = MaterialTheme.colorScheme.primary)
            DropdownMenuItem(
                text = { Text("Delete", textAlign = TextAlign.Center) },
                onClick = { viewModel.deleteEntry(card) }
            )
        }
    }
}