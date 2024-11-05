package com.hmncube.juiceme.useCases

import android.content.Context
import androidx.preference.PreferenceManager

@SuppressWarnings("TooManyFunctions")
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

    fun saveRechargeCardLength(length: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(USSD_LENGTH, length)
        editor.apply()
    }

    fun getRechargeCardLength() : Int {
        val len = PreferenceManager
            .getDefaultSharedPreferences(context).getString(USSD_LENGTH, DEFAULT_RECHARGE_CARD_LENGTH)
        return len!!.toInt()
    }

    fun saveSetCustomCode(isSet: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(SET_CUSTOM_USSD, isSet)
        editor.apply()
    }

    fun getSetCustomCode() : Boolean {
        return PreferenceManager
            .getDefaultSharedPreferences(context).getBoolean(SET_CUSTOM_USSD, false)
    }
    companion object {
        const val CARRIER_NAME = "carrier_name"
        const val USSD_CODE = "ussd_code"
        const val USSD_LENGTH = "ussd_length"
        const val STORE_HISTORY = "store_history"
        const val DIAL_ACTION = "dial_action"
        const val IMAGES = "images"
        const val SET_CUSTOM_USSD = "set_custom_ussd"

        const val DEFAULT_RECHARGE_CARD_LENGTH = "17"
    }
}
