package com.hmncube.juiceme.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmncube.juiceme.R
import com.hmncube.juiceme.theme.JuiceMeTheme

@Composable
fun TitledSwitch(
    onChanged: () -> Unit,
    @StringRes titleText: Int,
    @StringRes descriptionTextOn: Int,
    @StringRes descriptionTextOff: Int,
    state: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(role = Role.Switch) { onChanged() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.8f)) {
            TitleText(textResource = titleText)
            DescriptionText(
                textResource = if (state) descriptionTextOn else descriptionTextOff
            )
        }
        Switch(
            checked = state,
            onCheckedChange = { onChanged() },
        )
    }
}


// previews
@Composable
@Preview
fun TitledSwitchPreview() {
    JuiceMeTheme {
        TitledSwitch(
            onChanged = {},
            titleText = R.string.store_history,
            descriptionTextOn = R.string.store_history_summary_on,
            descriptionTextOff = R.string.store_history_summary_on,
            state = true
        )
    }
}