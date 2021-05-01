package uz.muhammadyusuf.kurbonov.myclinic.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.databinding.OverlayMainBinding
import uz.muhammadyusuf.kurbonov.myclinic.utils.TAG_NOTIFICATIONS_VIEW
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class OverlayView(
    val context: Context,
    private val stateFlow: StateFlow<State>,
    private val coroutineScope: CoroutineScope
) {

    private lateinit var binding: OverlayMainBinding
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0
    private var initialY = 0
    private var startTime = System.currentTimeMillis()

    private lateinit var view: View
    private lateinit var windowManager: WindowManager

    private suspend fun showTextBalloon() = withContext(Dispatchers.Main) {
        val container = binding.container
        container.animate().scaleX(1f).scaleY(1f).withStartAction {
            container.visibility = VISIBLE
            printToLog("Showing")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    suspend fun start() {
        withContext(Dispatchers.Main) {
            binding = OverlayMainBinding.inflate(LayoutInflater.from(context))
            view = binding.root

            binding.img.setOnTouchListener { v, event ->

                val mWindowsParams = view.layoutParams as WindowManager.LayoutParams
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = mWindowsParams.x
                        initialY = mWindowsParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        startTime = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_UP -> {
                        if (System.currentTimeMillis() - startTime <= 700)
                            v.performClick()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        mWindowsParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        mWindowsParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(view, mWindowsParams)
                    }

                }
                true
            }

            binding.img.setOnClickListener {
                with(binding.container) {
                    pivotX = 0f
                    pivotY = measuredHeight.toFloat()
                    CoroutineScope(Dispatchers.Main).launch {
                        if (isVisible) {
                            hideTextBalloon()
                        } else {
                            showTextBalloon()
                        }
                    }
                }
            }

            // Pivot points for animation
            binding.container.pivotX = 0f
            binding.container.pivotY = binding.container.measuredHeight.toFloat()

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
            layoutParams.gravity = Gravity.TOP or Gravity.START

            windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            windowManager.addView(view, layoutParams)
        }

        observeState()
    }

    private suspend fun observeState() {
        stateFlow.collect { state ->
            printToLog("received state $state")
            when (state) {
                is State.AddNewCustomerRequest -> TODO()
                is State.AuthRequest -> TODO()
                is State.CommunicationInfoSent -> TODO()
                State.ConnectionError -> TODO()
                is State.Error -> TODO()
                State.Finished -> {
                    windowManager.removeView(view)
                    coroutineScope.cancel()
                }
                is State.Found -> TODO()
                State.None -> printToLog("None")
                State.NotFound -> printToLog("Not found")
                State.Searching -> printToLog("Searching $state")
                State.Started -> printToLog("Started")
                State.TooSlowConnectionError -> TODO()
            }
        }
    }

    private suspend fun hideTextBalloon() = withContext(Dispatchers.Main) {
        val container = binding.container
        container.animate().scaleX(0f).scaleY(0f).withEndAction {
            container.visibility = GONE
            printToLog("Hidden")
        }
    }


    private fun printToLog(msg: String) {
        initTimber()
        Timber.tag(TAG_NOTIFICATIONS_VIEW).d(msg)
    }
}