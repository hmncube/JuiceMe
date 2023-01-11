package com.hmncube.juiceme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import kotlinx.coroutines.launch

class HomeViewModel(private val appDatabase: AppDatabase) : ViewModel() {

    fun saveCardNumber(cardNumber : CardNumber) {
        viewModelScope.launch {
            appDatabase.cardNumberDao().insertCardNumber(cardNumber)
        }
    }
}
