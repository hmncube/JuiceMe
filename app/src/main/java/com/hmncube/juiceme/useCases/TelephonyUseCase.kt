package com.hmncube.juiceme.useCases

import android.content.Context
import android.telephony.TelephonyManager
import com.hmncube.juiceme.R

class TelephonyUseCase(private val context: Context,
                       private val preferenceUseCase: PreferencesUseCase) {
    fun getNetworkProvider() {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val carrierName = telephonyManager.networkOperatorName

        val networkProviders = context.resources.getStringArray(R.array.network_codes_entries)
        val ussdCodes = context.resources.getStringArray(R.array.network_codes_values)
        val index = networkProviders.indexOf(carrierName)

        val ussdCode = if (index >= 0) {
            ussdCodes[index]
        } else {
            " "
        }
        preferenceUseCase.saveCarrierName(carrierName)
        preferenceUseCase.saveUssdCode(ussdCode)
    }

    fun setCarrierNameFromUssd(ussd: String) {
        val networkProviders = context.resources.getStringArray(R.array.network_codes_entries)
        val ussdCodes = context.resources.getStringArray(R.array.network_codes_values)
        val index = ussdCodes.indexOf(ussd)
        val carrierName = if (index == -1) {
            ""
        } else {
            networkProviders[index]
        }
        preferenceUseCase.saveCarrierName(carrierName)
        preferenceUseCase.saveUssdCode(ussd)
    }
}
