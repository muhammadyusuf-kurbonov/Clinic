package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.view.OverlayCompose

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OverlayCompose(state = State.Started)
        }
    }
}