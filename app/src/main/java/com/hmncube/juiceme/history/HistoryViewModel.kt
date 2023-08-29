package com.hmncube.juiceme.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import kotlinx.coroutines.launch
import java.util.Calendar

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
            _history.value = getDummyData()// appDatabase.cardNumberDao().selectAll()
            _loading.value = false
        }
    }

    private fun getDummyData(): List<CardNumber> {
        return listOf(
            CardNumber(
                1,
                "22222233344",
                Calendar.getInstance().timeInMillis
            ),
            CardNumber(
                1,
                "0022233344",
                Calendar.getInstance().timeInMillis
            ),
            CardNumber(
                1,
                "22222233300",
                Calendar.getInstance().timeInMillis
            )
        )
    }

    fun deleteEntry(cardNumber: CardNumber) {
        viewModelScope.launch {
            appDatabase.cardNumberDao().delete(cardNumber)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            appDatabase.cardNumberDao().clearAll()
            _history.value = appDatabase.cardNumberDao().selectAll()
        }
    }

    fun redialNumber() {}

}
