package com.hmncube.juiceme.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.text.isDigitsOnly
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.hmncube.juiceme.R
import com.hmncube.juiceme.UserFeedback
import com.hmncube.juiceme.useCases.PreferencesUseCase
import com.hmncube.juiceme.useCases.TelephonyUseCase

class SettingsFragment : PreferenceFragmentCompat() {
    private var selectNetwork: ListPreference? = null
    private var networkCarrierName : Preference? = null
    private var detectNetwork : Preference? = null
    private var customDialCodeEt : EditTextPreference? = null
    private var autoNetwork: Preference? = null
    private var rechargeCodeLength : EditTextPreference? = null
    private var preferencesUseCase : PreferencesUseCase? = null
    private var customDialCodeSet : SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferencesUseCase = PreferencesUseCase(context = requireContext())

        initViews()

        setCarrierNameAndUssdCodes()

        rechargeCodeLength?.title = preferencesUseCase?.getRechargeCardLength().toString()

        setListeners()

        updateNetworkCodesViews(customDialCodeSet?.isChecked!!)

        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("sn")
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private fun initViews() {
        selectNetwork = findPreference("select_network_carrier")
        networkCarrierName = findPreference("network_carrier")
        detectNetwork = findPreference("detect_network")
        customDialCodeEt = findPreference("custom_dial_code_et")
        autoNetwork = findPreference("network_carrier")
        rechargeCodeLength = findPreference("recharge_code_length_et")
        customDialCodeSet = findPreference("custom_dial_code")
    }

    private fun setListeners() {
        rechargeCodeLength?.setOnPreferenceClickListener { pref ->
            if (pref.key == "recharge_code_length_et") {
                rechargeCodeLength?.text = preferencesUseCase?.getRechargeCardLength().toString()
            }
            true
        }
        rechargeCodeLength?.setOnPreferenceChangeListener { preference, newValue ->
            if (preference.key == "recharge_code_length_et") {
                var length = newValue as String
                length = length.trim()
                if (checkIfUssdLengthIsCorrect(length)) {
                    preferencesUseCase?.saveRechargeCardLength(length)
                    UserFeedback()
                        .displayFeedback(requireView(), R.string.ussd_length_saved, UserFeedback.LENGTH_SHORT)
                    rechargeCodeLength?.title = length
                }
            }
            true
        }
        customDialCodeSet?.setOnPreferenceChangeListener{ pref, value ->
            if (pref.key == "custom_dial_code") {
                updateNetworkCodesViews(value as Boolean)
                if (value) {
                    autoNetwork?.title = "Custom network"
                    autoNetwork?.summary = ""
                } else {
                    detectNetworkAndSave()
                }
                preferencesUseCase?.saveSetCustomCode(value)
            }
            true
        }
        customDialCodeEt?.setOnPreferenceChangeListener { pref, newValue ->
            if (pref.key == "custom_dial_code_et") {
                var newCode = newValue as String
                newCode = newCode.trim()
                checkAndSaveNewCode(newCode)
            }
            true
        }

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

    }

    private fun checkAndSaveNewCode(newCode : String) {
        if (newCode.isNotBlank()) {
            saveNewUssdCodeAndDisplayFeedback(newCode)
        } else {
            UserFeedback().displayFeedback(
                view = requireView(),
                msg = R.string.enter_correct_code,
                length = UserFeedback.LENGTH_SHORT
            )
        }
    }
    private fun saveNewUssdCodeAndDisplayFeedback(newCode : String) {
        preferencesUseCase?.saveUssdCode(newCode)
        autoNetwork?.summary = newCode
        customDialCodeEt?.text = newCode
        UserFeedback().displayFeedback(
            view = requireView(),
            msg = R.string.dial_code_changed,
            length = UserFeedback.LENGTH_SHORT
        )
    }

    @SuppressWarnings("ReturnCount")
    private fun checkIfUssdLengthIsCorrect(length : String) : Boolean {
        if (length.isEmpty() || length.isBlank()) {
            UserFeedback()
                .displayFeedback(requireView(), R.string.cannot_save_empty_length, UserFeedback.LENGTH_SHORT)
            return false
        }

        if (!length.isDigitsOnly()) {
            UserFeedback()
                .displayFeedback(requireView(), R.string.ussd_length_not_digits_error, UserFeedback.LENGTH_SHORT)
            return false
        }

        val intValue = length.toInt()
        if (intValue < 1 || intValue > MAX_RECHARGE_CARD_NUMBERS) {
            UserFeedback()
                .displayFeedback(requireView(), R.string.ussd_length_not_digits_error, UserFeedback.LENGTH_SHORT)
            return false
        }
        return true
    }
    private fun detectNetworkAndSave() {
        TelephonyUseCase(requireContext(), PreferencesUseCase(requireContext())).getNetworkProvider()
        setCarrierNameAndUssdCodes()
        UserFeedback().displayFeedback(requireView(), R.string.updated_the_network_details, UserFeedback.LENGTH_LONG)
    }
    private fun updateNetworkCodesViews(checked: Boolean) {
        selectNetwork?.isEnabled = !checked
        networkCarrierName?.isEnabled = !checked
        detectNetwork?.isEnabled = !checked
        customDialCodeEt?.isVisible = checked
    }

    private fun setCarrierNameAndUssdCodes() {
        val carrierName = preferencesUseCase?.getCarrierName()
        autoNetwork?.title =  if (carrierName?.isNotEmpty() == true) {
            carrierName
        } else {
            requireContext().resources.getString(R.string.failed_to_automatically_detect_network)
        }
        autoNetwork?.summary = preferencesUseCase?.getUssdCode()

        val index = selectNetwork?.entries?.indexOf(carrierName) ?: -1
        if (index >= 0) {
            selectNetwork?.setValueIndex(index)
        }

        if (preferencesUseCase?.getSetCustomCode() == true) {
            autoNetwork?.title = "Custom network"
            autoNetwork?.summary = preferencesUseCase?.getUssdCode()
        }
    }
    companion object {
        private const val MAX_RECHARGE_CARD_NUMBERS = 50
    }
}
