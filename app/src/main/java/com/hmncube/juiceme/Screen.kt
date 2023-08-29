package com.hmncube.juiceme

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class Screen(val route: String, @StringRes val resourceId: Int, @DrawableRes val iconId: Int) {
    object Home : Screen("home", R.string.home, R.drawable.ic_home)
    object History : Screen("history", R.string.history, R.drawable.ic_history)
    object Settings : Screen("settings", R.string.settings, R.drawable.ic_settings)
}
