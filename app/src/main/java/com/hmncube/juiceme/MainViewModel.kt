package com.hmncube.juiceme

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var _screenTitle = MutableLiveData("Home")
    val screenTitle: LiveData<String>
        get() = _screenTitle

    private var _showDeleteAll = MutableLiveData(false)
    val showDeleteAll: LiveData<Boolean>
        get() = _showDeleteAll

    fun setTitle(newTitle: String) {
        _screenTitle.value = newTitle
        _showDeleteAll.value = newTitle == "History"
    }
}