package com.hmncube.juiceme.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmncube.juiceme.ContextViewModelFactory
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.livedata.observeAsState
import com.hmncube.juiceme.R
import com.hmncube.juiceme.components.DescriptionText
import com.hmncube.juiceme.components.MediumVerticalSpacer
import com.hmncube.juiceme.components.TitleText
import com.hmncube.juiceme.components.TitledSwitch
import com.hmncube.juiceme.extensions.getValueOrFalse


@Composable
fun SettingScreen(context: Context, snackbarHostState: SnackbarHostState) {
    val viewModel: SettingsViewModel = ContextViewModelFactory(context)
        .create(SettingsViewModel::class.java)

    SettingsContent(snackbarHostState, viewModel)
}

@Composable
fun SettingsContent(snackbarHostState: SnackbarHostState, viewModel: SettingsViewModel) {
    LazyColumn(modifier = Modifier.padding(top = 64.dp)){
        item {
            TitleText(textResource = R.string.dial_codes_header)
            DescriptionText(textResource = R.string.name_codes)
            MediumVerticalSpacer()
            TextButton(onClick = { /*TODO*/ }) {
                Column{
                    TitleText(textResource = R.string.detect_network)
                    DescriptionText(textResource = R.string.click_detect_network)
                }
            }
            MediumVerticalSpacer()
            TextButton(onClick = { /*TODO*/ }) {
                Column{
                    TitleText(textResource = R.string.select_network)
                    DescriptionText(text = "Econet")
                }
            }

            MediumVerticalSpacer()
            TitledSwitch(
                onChanged = { viewModel.toggleCustomDialCode() },
                titleText = R.string.custom_dial_code,
                descriptionTextOn = R.string.custom_dial_code_summary,
                descriptionTextOff = R.string.custom_dial_code_summary,
                state = viewModel.customDialCode.observeAsState().getValueOrFalse()
            )
        }
    }
}