package com.hmncube.juiceme.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.hmncube.juiceme.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}