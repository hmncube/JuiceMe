package com.hmncube.juiceme.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmncube.juiceme.R

@Composable
fun SmallVerticalSpacer() {
    Spacer(modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun MediumVerticalSpacer() {
    Spacer(modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun LargeVerticalSpacer() {
    Spacer(modifier = Modifier.padding(top = 16.dp))
}