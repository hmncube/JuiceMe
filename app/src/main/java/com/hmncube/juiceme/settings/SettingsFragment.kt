package com.hmncube.juiceme.settings

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.hmncube.juiceme.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val telephonyManager =
            requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val carrierName = telephonyManager.networkOperatorName
        val autoNetwork: EditTextPreference? = findPreference("network_carrier")
        autoNetwork?.title = carrierName

        val networkProviders = resources.getStringArray(R.array.network_codes_entries)
        val ussdCodes = resources.getStringArray(R.array.network_codes_values)
        val index = networkProviders.indexOf(carrierName)
        val editor = sharedPreferences.edit()
        if (index >= 0) {
            editor.putString("network_carrier", ussdCodes[index])
        } else {
            autoNetwork?.title = "The network provider $carrierName is unknown"
            editor.putString("network_carrier", "")
            Log.e("SettingsFragment", "onCreatePreferences: carrier $carrierName is unknown", )
        }
        editor.apply()
    }
}