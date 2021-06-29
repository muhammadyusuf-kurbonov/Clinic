package uz.muhammadyusuf.kurbonov.myclinic.android.workers

import android.content.Context
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
import uz.muhammadyusuf.kurbonov.myclinic.android.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.OverlayScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.AppViewModelProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme.AppTheme
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

class SearchWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
), OverlayDraggableListener {

    private lateinit var windowManager: WindowManager
    private lateinit var view: FrameLayout

    @ExperimentalAnimationApi
    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val provider = SystemFunctionsProvider()
        val appViewModel = AppViewModel(
            coroutineContext,
            provider,
            AppRepository(provider.readPreference("token", ""))
        )

        val overlayLayout = FrameLayout(applicationContext)
        overlayLayout.addView(ComposeView(applicationContext).apply {
            setContent {
                CompositionLocalProvider(AppViewModelProvider provides appViewModel) {
                    AppTheme {
                        OverlayScreen()
                    }
                }
            }
        })

        view = overlayLayout
        val params = overlayLayout.makeOverlayDraggable(listener = this@SearchWorker)
        windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE)
                as WindowManager

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
            Result.success()
        }
    }

    override fun onParamsChanged(updatedParams: WindowManager.LayoutParams) {
        windowManager.updateViewLayout(view, updatedParams)
    }
}