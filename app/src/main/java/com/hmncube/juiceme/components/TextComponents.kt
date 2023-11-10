package com.hmncube.juiceme.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DescriptionText(@StringRes textResource: Int) {
    Text(text = stringResource(id = textResource))
}

@Composable
fun DescriptionText(text: String) {
    Text(text = text)
}

@Composable
fun TitleText(@StringRes textResource: Int) {
    Text(text = stringResource(id = textResource), color = MaterialTheme.colorScheme.onPrimary)
}

@Composable
fun HeadingText(@StringRes textResource: Int) {
    Text(text = stringResource(id = textResource), color = MaterialTheme.colorScheme.primary)
}