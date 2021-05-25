package uz.muhammadyusuf.kurbonov.myclinic.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.utils.TAG_NOTIFICATIONS_VIEW
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class OverlayView(
        val context: Context,
        private val stateFlow: StateFlow<State>,
        private val coroutineScope: CoroutineScope
) : LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    companion object {
        const val OVERLAY_X_PREF_KEY = "overlay_x"
        const val OVERLAY_Y_PREF_KEY = "overlay_y"
    }
//
//    private lateinit var binding: OverlayMainBinding
//    private var initialTouchX = 0f
//    private var initialTouchY = 0f
//    private var initialX = 0
//    private var initialY = 0
//    private var startTime = System.currentTimeMillis()

    private lateinit var view: ComposeView
    private lateinit var windowManager: WindowManager

    @SuppressLint("ClickableViewAccessibility")
    suspend fun start() = withContext(Dispatchers.Main) {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        view = ComposeView(context)
        view.setContent {
            val state by App.getAppViewModelInstance().stateFlow.collectAsState()
            OverlayCompose(state = state)
        }
        ViewTreeLifecycleOwner.set(view, this@OverlayView)
        ViewTreeSavedStateRegistryOwner.set(view, this@OverlayView)
//            binding.img.setOnTouchListener { v, event ->
//
//                val mWindowsParams = view.layoutParams as WindowManager.LayoutParams
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        initialX = mWindowsParams.x
//                        initialY = mWindowsParams.y
//                        initialTouchX = event.rawX
//                        initialTouchY = event.rawY
//                        startTime = System.currentTimeMillis()
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        if (System.currentTimeMillis() - startTime <= 700)
//                            v.performClick()
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        mWindowsParams.x = initialX + (event.rawX - initialTouchX).toInt()
//                        mWindowsParams.y = initialY + (event.rawY - initialTouchY).toInt()
//                        windowManager.updateViewLayout(view, mWindowsParams)
//                    }
//
//                }
//                true
//            }
//
//            binding.img.setOnClickListener {
//                with(binding.container) {
//                    pivotX = 0f
//                    pivotY = measuredHeight.toFloat()
//                    CoroutineScope(Dispatchers.Main).launch {
//                        if (isVisible) {
//                            hideTextBalloon()
//                        } else {
//                            showTextBalloon()
//                        }
//                    }
//                }
//            }
//
//            // Pivot points for animation
//            binding.container.pivotX = 0f
//            binding.container.pivotY = binding.container.measuredHeight.toFloat()

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

        with(App.pref) {
            layoutParams.x = getInt(OVERLAY_X_PREF_KEY, 0)
            layoutParams.y = getInt(OVERLAY_Y_PREF_KEY, 0)
        }

        windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        windowManager.addView(view, layoutParams)
        observeState()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    private suspend fun observeState() {
        stateFlow.collect { state ->
            printToLog("received state $state")
            if (state is State.Finished)
                onFinished()
        }
    }

    private fun onFinished() {
        val params = view.layoutParams as WindowManager.LayoutParams
        App.pref.edit()
                .putInt(OVERLAY_X_PREF_KEY, params.x)
                .putInt(OVERLAY_Y_PREF_KEY, params.y)
                .apply()
        coroutineScope.cancel()
        lifecycleScope.launch(Dispatchers.Main) {
            windowManager.removeView(view)
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }

    //==============================================================================================

    private fun printToLog(msg: String) {
        initTimber()
        Timber.tag(TAG_NOTIFICATIONS_VIEW).d(msg)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
    override fun getSavedStateRegistry(): SavedStateRegistry =
            savedStateRegistryController.savedStateRegistry
}