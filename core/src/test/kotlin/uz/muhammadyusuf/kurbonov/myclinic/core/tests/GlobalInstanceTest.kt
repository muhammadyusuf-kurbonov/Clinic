package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import com.google.gson.GsonBuilder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.models.AuthToken
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.treatment.TreatmentDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.users.UserDTO
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class GlobalInstanceTest {
    @Test
    fun `global instance - check adding to registry`() {
        runBlocking {
            val dummy = GsonBuilder().create()
                .fromJson(getJSON("customer.json"), CustomerDTO::class.java)

            val repository = mockk<AppRepository> {
                coEvery {
                    search("+998913975538")
                } returns dummy
                coEvery {
                    authenticate(any(), any())
                } returns AuthToken("test-token")
                coEvery {
                    getUser(any())
                } returns GsonBuilder().create()
                    .fromJson(getJSON("user-get-found.json"), UserDTO::class.java).data[0]

                coEvery {
                    getTreatment(any())
                } returns GsonBuilder().create()
                    .fromJson(getJSON("treatment-get-found.json"), TreatmentDTO::class.java).data[0]
            }

            val provider = mockk<SystemFunctionsProvider>(relaxed = true) {
            }

            launch {
                val appViewModel1 = AppViewModel(
                    coroutineContext,
                    provider, repository
                )
                val appViewModel2 = AppViewModel(
                    coroutineContext,
                    provider, repository
                )
                val appViewModel3 = AppViewModel(
                    coroutineContext,
                    provider, repository
                )

                AppViewModel.pushAction(Action.Search("+998913975538"))
                coVerify(exactly = 3) {
                    repository.search("+998913975538")
                }
            }

            delay(2000)
        }
    }

    @Test
    fun `global instance - check removing from registry`() {
        runBlocking {
            val dummy = GsonBuilder().create()
                .fromJson(getJSON("customer.json"), CustomerDTO::class.java)

            val repository = mockk<AppRepository>(relaxed = true) {
                coEvery {
                    authenticate(any(), any())
                } returns AuthToken("test-token")
            }

            val provider = mockk<SystemFunctionsProvider>(relaxed = true) {
            }

            val appViewModel1 = AppViewModel(
                coroutineContext,
                provider, repository
            )
            val appViewModel2 = AppViewModel(
                coroutineContext,
                provider, repository
            )
            launch {
                val appViewModel3 = AppViewModel(
                    coroutineContext,
                    provider, repository
                )
                assertEquals(3, AppViewModel.instances.size)
            }

            AppViewModel.pushAction(Action.Login("demo@32desk.com", "demo"))
            coVerify(exactly = 2) {
                repository.authenticate("demo@32desk.com", "demo")
            }
        }
    }

    @Test
    fun `global instance - no instances at start`() {
        assertFailsWith<IllegalStateException> {
            AppViewModel.pushAction(Action.Search("+998913975538"))
        }
    }
}