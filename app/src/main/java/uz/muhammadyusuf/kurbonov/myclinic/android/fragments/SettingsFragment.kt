package uz.muhammadyusuf.kurbonov.myclinic.android.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import uz.muhammadyusuf.kurbonov.myclinic.R

class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_settings, rootKey)
    }
}