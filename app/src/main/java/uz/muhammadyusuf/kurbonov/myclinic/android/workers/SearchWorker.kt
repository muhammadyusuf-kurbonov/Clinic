package uz.muhammadyusuf.kurbonov.myclinic.android.workers

import android.content.Context
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.*
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.hyuwah.draggableviewlib.OverlayDraggableListener
import io.github.hyuwah.draggableviewlib.makeOverlayDraggable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

class SearchWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
), OverlayDraggableListener {

    private lateinit var windowManager: WindowManager
    private lateinit var view: FrameLayout

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
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = ""
                )
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
            windowManager.addView(view, params)
        }
        Result.success()
    }

    override fun onParamsChanged(updatedParams: WindowManager.LayoutParams) {
        windowManager.updateViewLayout(view, updatedParams)
    }
}