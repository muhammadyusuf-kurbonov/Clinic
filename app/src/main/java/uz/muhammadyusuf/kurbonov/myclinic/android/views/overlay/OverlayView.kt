package uz.muhammadyusuf.kurbonov.myclinic.android.views.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.*
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.android.views.BaseView
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.shared.printToConsole

class OverlayView(
    val context: Context, viewModel: AppViewModel
) : BaseView(viewModel) {
    companion object {
        const val OVERLAY_X_PREF_KEY = "overlay_x"
        const val OVERLAY_Y_PREF_KEY = "overlay_y"
    }

    private lateinit var view: ComposeView
    private lateinit var windowManager: WindowManager

    @SuppressLint("ClickableViewAccessibility")
    override suspend fun onCreate() = withContext(Dispatchers.Main) {
        view = ComposeView(context)
        view.setContent {
            val state = viewModel.stateFlow.collectAsState()
            printToConsole("composable: received ${state.value}")
            OverlayCompose(state = state.value, onFinished = {
                viewModel.reduce(Action.Finish)
            })
        }
        ViewTreeLifecycleOwner.set(view, this@OverlayView)
        ViewTreeSavedStateRegistryOwner.set(view, this@OverlayView)
        val layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.START

//        with(App.pref) {
//            viewModel.reduce(
//                Action.ChangePos(
//                    getFloat(OVERLAY_Y_PREF_KEY, 0f),
//                )
//            )
//        }
        windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        windowManager.addView(view, layoutParams)

    }

    override suspend fun onStateChange(state: State) {
        printToConsole("received state $state")
        // Composable is handling state itself
    }

    override fun onFinished() {
        val params = view.layoutParams as WindowManager.LayoutParams
        App.pref.edit()
            .putFloat(OVERLAY_X_PREF_KEY, params.x.toFloat())
            .putFloat(OVERLAY_Y_PREF_KEY, params.y.toFloat())
            .apply()
        viewModel.coroutineScope.cancel()
        lifecycleScope.launch(Dispatchers.Main) {
            windowManager.removeView(view)
        }
    }

}