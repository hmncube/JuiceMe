package com.hmncube.juiceme.extensions

import androidx.compose.runtime.State

fun State<Boolean?>.getValueOrFalse() : Boolean {
    return this.value ?: false
}
