package uz.muhammadyusuf.kurbonov.myclinic.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.NewCustomerActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.databinding.OverlayMainBinding
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.utils.TAG_NOTIFICATIONS_VIEW
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class OverlayView(
    val context: Context,
    private val stateFlow: StateFlow<State>,
    private val coroutineScope: CoroutineScope
) {

    companion object {
        const val OVERLAY_X_PREF_KEY = "overlay_x"
        const val OVERLAY_Y_PREF_KEY = "overlay_y"
    }

    private lateinit var binding: OverlayMainBinding
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0
    private var initialY = 0
    private var startTime = System.currentTimeMillis()

    private lateinit var view: View
    private lateinit var windowManager: WindowManager

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
            layoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.START

            with(App.pref) {
                layoutParams.x = getInt(OVERLAY_X_PREF_KEY, 0)
                layoutParams.y = getInt(OVERLAY_Y_PREF_KEY, 0)
            }

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
                is State.AddNewCustomerRequest -> requestAddNewCustomer(state.phone)
                is State.AuthRequest -> requestAuth(state.phone)
                is State.PurposeRequest -> requestPurpose(state.customer, state.communicationId)
                State.NoConnectionState -> noConnection()
                is State.Error -> error(state.exception)
                State.Finished -> {
                    onFinished()
                    coroutineScope.cancel()
                }
                is State.Found -> found(state.customer, state.callDirection)
                State.None -> printToLog("None")
                State.NotFound -> notFound()
                State.Searching -> searching()
                State.Started -> welcome()
                State.TooSlowConnectionError -> tooSlowInternet()
            }
        }
    }

    private fun onFinished() {
        val params = view.layoutParams as WindowManager.LayoutParams
        App.pref.edit()
            .putInt(OVERLAY_X_PREF_KEY, params.x)
            .putInt(OVERLAY_Y_PREF_KEY, params.y)
            .apply()
        windowManager.removeView(view)
    }

    //==============================================================================================

    private suspend fun onlyText() = withContext(Dispatchers.Main) {
        binding.customerInfoStub.visibility = GONE
        binding.text.visibility = VISIBLE
        binding.btn.visibility = GONE
        showTextBalloon()
    }

    private suspend fun onlyButton() = withContext(Dispatchers.Main) {
        binding.customerInfoStub.visibility = GONE
        binding.text.visibility = GONE
        binding.btn.visibility = VISIBLE
        showTextBalloon()
    }

    private suspend fun textAndButton() = withContext(Dispatchers.Main) {
        binding.customerInfoStub.visibility = GONE
        binding.text.visibility = VISIBLE
        binding.btn.visibility = VISIBLE
        showTextBalloon()
    }

    //==============================================================================================

    private suspend fun ask(msg: String, buttonMsg: String = "OK", block: () -> Unit) =
        withContext(Dispatchers.Main) {
            textAndButton()
            binding.text.text = msg
            binding.btn.text = buttonMsg
            binding.btn.setOnClickListener {
                block()
            }
        }

    private suspend fun say(msg: String) = withContext(Dispatchers.Main) {
        onlyText()
        binding.text.text = msg
    }

    private suspend fun say(resId: Int) {
        say(context.getString(resId))
    }

    // =============================================================================================

    private suspend fun requestAuth(phone: String) {
        ask(context.getString(R.string.auth_text)) {
            context.startActivity(Intent(context, LoginActivity::class.java).apply {
                putExtra(LoginActivity.EXTRA_PHONE, phone)
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private suspend fun requestAddNewCustomer(phone: String) = withContext(Dispatchers.Main) {
        ask(context.getString(R.string.add_user_request, phone)) {
            context.startActivity(Intent(context, NewCustomerActivity::class.java).apply {
                putExtra("phone", phone)
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            })
        }
        binding.container.addView(
            Button(
                context,
                null,
                R.style.Widget_MaterialComponents_Button_TextButton
            ).apply {
                setText(android.R.string.cancel)
                setOnClickListener {
                    App.getAppViewModelInstance().reduce(Action.Finish)
                }
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            })
    }

    private suspend fun requestPurpose(customer: Customer, communicationId: String) =
        withContext(Dispatchers.Main) {
            binding.customerInfoStub.visibility = GONE
            ask(context.getString(R.string.purpose_msg, customer.name)) {
                context.startActivity(Intent(context, NoteActivity::class.java).apply {
                    putExtra(
                        "communicationId", communicationId
                    )
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }

    private suspend fun searching() {
        say(R.string.searching_text)
    }

    private suspend fun noConnection() {
        say(R.string.no_connection)
    }

    private suspend fun notFound() {
        say(R.string.not_found)
    }

    private suspend fun error(e: Exception) {
        say(R.string.unknown_error)
        Timber.e(e)
    }

    private suspend fun tooSlowInternet() {
        say(R.string.too_slow)
    }

    private suspend fun welcome() {
        say(R.string.logging_in_caption)
    }

    private suspend fun found(customer: Customer, callDirection: CallDirection) =
        withContext(Dispatchers.Main) {
            onlyButton()
            binding.customerInfoStub.visibility = VISIBLE
            binding.btn.setText(R.string.open_medical_card)
            val infoView = binding.includeLayout

            with(infoView) {
                // Drawing icon for notification
                when (callDirection) {
                    CallDirection.INCOME -> imgType.setImageResource(
                        R.drawable.ic_phone_in_24
                    )
                    CallDirection.OUTGOING -> imgType.setImageResource(
                        R.drawable.ic_phone_outgoing
                    )
                }

                tvName.text = customer.name
                tvPhone.text = customer.phoneNumber

                tvBalance.text = context.getString(R.string.balance, customer.balance)

                try {
                    Picasso.get().load(customer.avatarLink).into(imgAvatar)
                } catch (e: Exception) {
                    Timber.e(e)
                }

                if (customer.lastAppointment != null) {
                    val lastAppointment = customer.lastAppointment!!
                    val lastAppointmentText =
                        "${lastAppointment.date} - ${lastAppointment.doctor?.name ?: ""} - ${lastAppointment.diagnosys}"
                    tvLastVisit.text = lastAppointmentText
                }

                if (customer.nextAppointment != null) {
                    val nextAppointment = customer.nextAppointment!!
                    val nextAppointmentText = "${
                        nextAppointment.date
                    } - ${nextAppointment.doctor} - ${nextAppointment.diagnosys}"
                    tvNextVisit.text = nextAppointmentText
                }
            }
        }

    //==============================================================================================

    private suspend fun showTextBalloon() = withContext(Dispatchers.Main) {
        val container = binding.container
        if (container.isVisible) return@withContext
        container.animate().scaleX(1f).scaleY(1f).withStartAction {
            container.visibility = VISIBLE
            printToLog("Showing")
        }
    }

    private suspend fun hideTextBalloon() = withContext(Dispatchers.Main) {
        val container = binding.container
        container.animate().scaleX(0f).scaleY(0f).withEndAction {
            container.visibility = GONE
            printToLog("Hidden")
        }
    }

    //==============================================================================================

    private fun printToLog(msg: String) {
        initTimber()
        Timber.tag(TAG_NOTIFICATIONS_VIEW).d(msg)
    }
}