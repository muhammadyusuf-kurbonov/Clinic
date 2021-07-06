package uz.muhammadyusuf.kurbonov.myclinic.android.workers

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.*
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.hyuwah.draggableviewlib.OverlayDraggableListener
import io.github.hyuwah.draggableviewlib.makeOverlayDraggable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import uz.muhammadyusuf.kurbonov.myclinic.android.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.OverlayScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalPhoneNumberProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme.AppTheme
import uz.muhammadyusuf.kurbonov.myclinic.appComponent
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState

class SearchWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
), OverlayDraggableListener {

    private lateinit var windowManager: WindowManager
    private lateinit var view: FrameLayout

    @ExperimentalAnimationApi
    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {

        var phone = inputData.getString("phone") ?: throw IllegalStateException("No phone sent")

        val provider = SystemFunctionsProvider()
        val appViewModel = applicationContext
            .appComponent()
            .appStatesControllerFactory()
            .factory(this.coroutineContext).create()

        if (phone.length < 13 && !phone.startsWith("+998")) phone = "+998$phone"

        appViewModel.handle(Action.Search(phone))

        delay(1000)

        val overlayLayout = FrameLayout(applicationContext)
        val composeView = ComposeView(applicationContext)
        overlayLayout.addView(composeView.apply {
            setContent {
                CompositionLocalProvider(
                    LocalAppControllerProvider provides appViewModel,
                    LocalPhoneNumberProvider provides phone
                ) {
                    AppTheme {
                        OverlayScreen()
                    }
                }
            }
        })

        view = overlayLayout
        @Suppress("DEPRECATION")
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val positionParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            provider.readPreference("OVERLAY_X_POS", 0),
            provider.readPreference("OVERLAY_Y_POS", 0),
            layoutFlag,
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val params = composeView.makeOverlayDraggable(listener = this@SearchWorker, positionParams)
        view.layoutParams = params
        windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE)
                as WindowManager


        launch(Dispatchers.Main) {
            AppStateStore.reportState.collect {
                try {
                    val viewParams = view.layoutParams as WindowManager.LayoutParams
                    if ((it is ReportState.PurposeRequested) or (it is ReportState.AskToAddNewCustomer)) {
                        viewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    } else {
                        viewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    }
                    windowManager.updateViewLayout(view, viewParams)
                } catch (e: Exception) {
                }
            }
        }

        withContext(Dispatchers.Main) {
            val viewModelStore = ViewModelStore()
            val lifecycleOwner = WorkerLifecycle()
            lifecycleOwner.performRestore(null)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            ViewTreeLifecycleOwner.set(view, lifecycleOwner)
            ViewTreeViewModelStoreOwner.set(view) { viewModelStore }
            ViewTreeSavedStateRegistryOwner.set(view, lifecycleOwner)
            val coroutineContext = AndroidUiDispatcher.CurrentThread
            val runRecomposeScope = CoroutineScope(coroutineContext)
            val recomposer = Recomposer(coroutineContext)
            view.compositionContext = recomposer
            runRecomposeScope.launch {
                recomposer.runRecomposeAndApplyChanges()
            }
            windowManager.addView(view, params)
        }
        try {
            awaitCancellation()
        } finally {
            windowManager.removeView(view)
            provider.writePreference(
                "OVERLAY_X_POS",
                (view.layoutParams as WindowManager.LayoutParams).x
            )
            provider.writePreference(
                "OVERLAY_Y_POS",
                (view.layoutParams as WindowManager.LayoutParams).y
            )
            Result.success()
        }
    }

    override fun onParamsChanged(updatedParams: WindowManager.LayoutParams) {
        windowManager.updateViewLayout(view, updatedParams)
    }
}