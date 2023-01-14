package com.hmncube.juiceme.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hmncube.juiceme.R
import com.hmncube.juiceme.use_cases.PreferencesUseCase
import com.hmncube.juiceme.use_cases.TelephonyUseCase

class SettingsFragment : PreferenceFragmentCompat() {
    var selectNetwork: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        selectNetwork = findPreference("select_network_carrier")

        setCarrierNameAndUssdCodes()

        val detectNetwork: Preference? = findPreference("detect_network")
        detectNetwork?.setOnPreferenceClickListener {
            TelephonyUseCase(requireContext(), PreferencesUseCase(requireContext())).getNetworkProvider()
            setCarrierNameAndUssdCodes()
            true
        }
        selectNetwork?.setOnPreferenceChangeListener { preference, newValue ->
            if (preference.key == "select_network_carrier") {
                val ussd = newValue as String
                val telephonyUseCase = TelephonyUseCase(
                    requireContext(),
                    PreferencesUseCase(context = requireContext())
                )
                telephonyUseCase.setCarrierNameFromUssd(ussd)
                setCarrierNameAndUssdCodes()
            }
            true
        }
    }

    private fun setCarrierNameAndUssdCodes() {
        val preferencesUseCase = PreferencesUseCase(context = requireContext())
        val autoNetwork: Preference? = findPreference("network_carrier")
        val carrierName = preferencesUseCase.getCarrierName()
        autoNetwork?.title =  if (carrierName?.isNotEmpty() == true) {
            carrierName
        } else {
            requireContext().resources.getString(R.string.failed_to_automatically_detect_network)
        }
        autoNetwork?.summary = preferencesUseCase.getUssdCode()
        if (carrierName?.isNotEmpty() == true) {
            selectNetwork?.setValueIndex(selectNetwork?.entries?.indexOf(carrierName)!!)
        }
    }
}
