package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import com.google.gson.GsonBuilder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.network.*
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class SearchTests {

    @Test
    fun `search - found`() {

        val dummy = GsonBuilder().create()
            .fromJson(getJSON("customer.json"), CustomerDTO::class.java)
        val repository = mockk<AppRepository> {
            coEvery {
                search("+998913975538")
            } returns dummy
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            appViewModel.customerState.assertEmitted {
                it is CustomerState.Found
            }
            coVerify { repository.search("+998913975538") }
        }
    }

    @Test
    fun `search - not found`() {
        val repository = mockk<AppRepository> {
            coEvery {
                search("+998913975538")
            } throws CustomerNotFoundException()
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            appViewModel.customerState.assertEmitted {
                it == CustomerState.NotFound
            }
            coVerify { repository.search("+998913975538") }
        }
    }

    @Test
    fun `search - not auth`() {
        val repository = mockk<AppRepository> {
            coEvery {
                search("+998913975538")
            } throws AuthRequestException()
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            appViewModel.customerState.assertEmitted {
                it == CustomerState.Default
            }
            appViewModel.authState.assertEmitted {
                it == AuthState.AuthRequired
            }
            coVerify { repository.search("+998913975538") }
        }
    }

    @Test
    fun `search - api error`() {
        val repository = mockk<AppRepository> {
            coEvery {
                search("+998913975538")
            } throws APIException(400, "Bad request")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        assertFailsWith<APIException> {
            runBlocking {
                val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
                appViewModel.handle(Action.Search("+998913975538"))
                appViewModel.customerState.assertEmitted {
                    it == CustomerState.Default
                }
                coVerify { repository.search("+998913975538") }
            }
        }
    }

    @Test
    fun `search - no connection`() {
        val repository = mockk<AppRepository> {
            coEvery {
                search("+998913975538")
            } throws NotConnectedException()
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            appViewModel.customerState.assertEmitted {
                it == CustomerState.ConnectionFailed
            }
            coVerify { repository.search("+998913975538") }
        }
    }
}