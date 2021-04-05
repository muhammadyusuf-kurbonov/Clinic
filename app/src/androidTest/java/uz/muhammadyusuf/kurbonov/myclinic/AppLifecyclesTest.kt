package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action

@RunWith(AndroidJUnit4::class)
class AppLifecyclesTest {

    @Test
    fun testMainWorkerLifecycle() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))
            var info = WorkManager.getInstance(context).getWorkInfosForUniqueWork("main_work").get()
            info.forEach {
                println("${it.id} is ${it.state}")
                assert(!it.state.isFinished)
            }
            delay(2500)
            App.appViewModel.reduce(Action.Finish)
            info = WorkManager.getInstance(context).getWorkInfosForUniqueWork("main_work").get()
            info.forEach {
                println("${it.id} is ${it.state}")
                assert(it.state.isFinished)
            }
        }
    }
}