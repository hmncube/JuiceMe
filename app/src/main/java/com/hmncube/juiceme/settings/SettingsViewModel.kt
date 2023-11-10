package com.hmncube.juiceme.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hmncube.juiceme.extensions.getValueOrFalse
import com.hmncube.juiceme.useCases.PreferencesUseCase

class SettingsViewModel(context: Context) : ViewModel(){
    //region state

    private var _customDialCode = MutableLiveData<Boolean>()
    val customDialCode : LiveData<Boolean>
        get() = _customDialCode

    private var _disableAutoDetect = MutableLiveData<Boolean>()
    val disableAutoDetect : LiveData<Boolean>
        get() = _disableAutoDetect

    //endregion

    var preferencesUseCase: PreferencesUseCase

    init{
        preferencesUseCase = PreferencesUseCase(context)
        _customDialCode.value = preferencesUseCase.getSetCustomCode()
        _disableAutoDetect.value = !_customDialCode.getValueOrFalse()
    }

    fun toggleCustomDialCode() {
        val value = _customDialCode.getValueOrFalse()
        _customDialCode.value = !value
        _disableAutoDetect.value = !_customDialCode.getValueOrFalse()
    }
}