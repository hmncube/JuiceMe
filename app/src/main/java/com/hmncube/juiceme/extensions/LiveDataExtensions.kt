package com.hmncube.juiceme.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun LiveData<Boolean>.getValueOrFalse() : Boolean {
    return this.value ?: false
}

fun MutableLiveData<Boolean>.getValueOrFalse() : Boolean {
    return this.value ?: false
}