package com.hmncube.juiceme.extensions

import androidx.lifecycle.LiveData

fun LiveData<Boolean>.getValueOrFalse() : Boolean {
    return this.value ?: false
}
