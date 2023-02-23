package com.hmncube.juiceme

import android.view.View
import com.google.android.material.snackbar.Snackbar

class UserFeedback {
    companion object {
        const val LENGTH_SHORT = Snackbar.LENGTH_SHORT
        const val LENGTH_LONG = Snackbar.LENGTH_LONG

        fun displayFeedback(view : View, msg : Int, length : Int) {
            Snackbar.make(view, msg, length).show()
        }

        fun displayFeedback(view : View, msg : String, length : Int) {
            Snackbar.make(view, msg, length).show()
        }
    }
}