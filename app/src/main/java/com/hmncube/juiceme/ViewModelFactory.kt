package com.hmncube.juiceme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.history.HistoryViewModel
import com.hmncube.juiceme.home.HomeViewModel

class ViewModelFactory(private val appDatabase: AppDatabase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(appDatabase) as T
        } else if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(appDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}