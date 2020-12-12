package uz.muhammadyusuf.kurbonov.myclinic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (intent.extras?.containsKey("title") == true)
            title = intent.extras?.getCharSequence("title")
    }
}