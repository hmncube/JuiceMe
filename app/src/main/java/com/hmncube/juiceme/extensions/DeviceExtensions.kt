package com.hmncube.juiceme.extensions

import android.content.res.Configuration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//handle orientation changes todo
fun Configuration.getScreenHeightFraction(fraction: Int): Dp {
    return this.screenHeightDp.dp / fraction
}

//handle orientation changes todo
fun Configuration.getScreenHeight(): Dp {
    return this.screenHeightDp.dp
}
