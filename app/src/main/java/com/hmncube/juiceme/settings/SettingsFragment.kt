package com.hmncube.juiceme.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.hmncube.juiceme.R
import com.hmncube.juiceme.UserFeedback
import com.hmncube.juiceme.use_cases.PreferencesUseCase
import com.hmncube.juiceme.use_cases.TelephonyUseCase

class SettingsFragment : PreferenceFragmentCompat() {
    private var selectNetwork: ListPreference? = null
    private var networkCarrierName : Preference? = null
    private var detectNetwork : Preference? = null
    private var customDialCodeEt : EditTextPreference? = null
    private var autoNetwork: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        selectNetwork = findPreference("select_network_carrier")
        networkCarrierName = findPreference("network_carrier")
        detectNetwork = findPreference("detect_network")
        customDialCodeEt = findPreference("custom_dial_code_et")
        autoNetwork = findPreference("network_carrier")

        setCarrierNameAndUssdCodes()

        val detectNetwork: Preference? = findPreference("detect_network")
        detectNetwork?.setOnPreferenceClickListener {
            detectNetworkAndSave()
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

        val customDialCodeSet : SwitchPreferenceCompat? = findPreference("custom_dial_code")
        updateNetworkCodesViews(customDialCodeSet?.isChecked!!)
        customDialCodeSet.setOnPreferenceChangeListener{ pref, value ->
            if (pref.key == "custom_dial_code") {
                updateNetworkCodesViews(value as Boolean)
                if (value) {
                    autoNetwork?.title = "Custom network"
                    autoNetwork?.summary = ""
                } else {
                    detectNetworkAndSave()
                }
            }
            true
        }

        customDialCodeEt?.setOnPreferenceChangeListener { pref, newValue ->
            if (pref.key == "custom_dial_code_et") {
                val preferencesUseCase = PreferencesUseCase(context = requireContext())
                var newCode = newValue as String
                newCode = newCode.trim()
                if (newCode.isNotBlank()) {
                    preferencesUseCase.saveUssdCode(newCode)
                    autoNetwork?.summary = newCode
                    customDialCodeEt?.text = newCode
                    UserFeedback.displayFeedback(
                        view = requireView(),
                        msg = R.string.dial_code_changed,
                        length = UserFeedback.LENGTH_SHORT
                    )
                }
            }
            true
        }
    }

    private fun detectNetworkAndSave() {
        TelephonyUseCase(requireContext(), PreferencesUseCase(requireContext())).getNetworkProvider()
        setCarrierNameAndUssdCodes()
        UserFeedback.displayFeedback(requireView(), R.string.updated_the_network_details, UserFeedback.LENGTH_LONG)
    }
    private fun updateNetworkCodesViews(checked: Boolean) {
        selectNetwork?.isEnabled = !checked
        networkCarrierName?.isEnabled = !checked
        detectNetwork?.isEnabled = !checked
        customDialCodeEt?.isVisible = checked
    }

    private fun setCarrierNameAndUssdCodes() {
        val preferencesUseCase = PreferencesUseCase(context = requireContext())
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
