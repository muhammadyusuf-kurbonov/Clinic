package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.fragments.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, SettingsFragment())
            .commit()
    }
}