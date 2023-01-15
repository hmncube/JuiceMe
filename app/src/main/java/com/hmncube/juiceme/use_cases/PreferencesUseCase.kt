package com.hmncube.juiceme.use_cases

import android.content.Context
import androidx.preference.PreferenceManager

class PreferencesUseCase(private val context: Context) {
    fun saveCarrierName(carrierName: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(CARRIER_NAME, carrierName)
        editor.apply()
    }

    fun getCarrierName(): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(CARRIER_NAME, "")
    }
    fun saveUssdCode(ussdCode: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(USSD_CODE, ussdCode)
        editor.apply()
    }

    fun getUssdCode(): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(USSD_CODE, "")
    }

    fun getStoreHistory(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(STORE_HISTORY, false)
    }

    fun getDirectDial(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DIAL_ACTION, false)
    }

    fun getAutomaticallyDelete(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IMAGES, true)
    }

    companion object {
        const val CARRIER_NAME = "carrier_name"
        const val USSD_CODE = "ussd_code"
        const val STORE_HISTORY = "store_history"
        const val DIAL_ACTION = "dial_action"
        const val IMAGES = "images"
    }
}