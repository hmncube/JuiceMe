package com.hmncube.juiceme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import kotlinx.coroutines.launch

class HomeViewModel(private val appDatabase: AppDatabase) : ViewModel() {
    //private var _loading = mu

    init {

    }
    fun saveCardNumber(cardNumber : CardNumber) {
        viewModelScope.launch {
            appDatabase.cardNumberDao().insertCardNumber(cardNumber)
        }
    }
}
