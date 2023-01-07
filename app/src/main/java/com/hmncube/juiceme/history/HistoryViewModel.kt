package com.hmncube.juiceme.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import kotlinx.coroutines.launch

class HistoryViewModel(private val appDatabase: AppDatabase) : ViewModel() {

    private var _history = MutableLiveData<List<CardNumber>>()
    val history : LiveData<List<CardNumber>>
    get() = _history

    private var _loading = MutableLiveData(true)
    val loading : LiveData<Boolean>
        get() = _loading

    init {
        _loading.value = true
        viewModelScope.launch {
            _history.value = appDatabase.cardNumberDao().selectAll()
            _loading.value = false
        }
    }

    fun deleteEntry(cardNumber: CardNumber): Unit {
        viewModelScope.launch {
            appDatabase.cardNumberDao().delete(cardNumber)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            appDatabase.cardNumberDao().clearAll()
        }
    }
}