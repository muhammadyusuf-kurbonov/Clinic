package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.network.*
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import kotlin.test.assertFailsWith

class SearchTests {

    @Test
    fun `search - found`() {

        val dummy = GsonBuilder().create()
            .fromJson(getJSON("customer.json"), CustomerDTO::class.java)
        val repository = mock<AppRepository> {
            onBlocking {
                search("+998913975538")
            } doReturn dummy
        }

        val provider = mock<SystemFunctionProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            appViewModel.customerState.assertEmitted {
                it is CustomerState.Found
            }
            verify(repository).search("+998913975538")
        }
    }

    @Test
    fun `search - not found`() {
        val repository = mock<AppRepository> {
            onBlocking {
                search("+998913975538")
            } doAnswer {
                throw CustomerNotFoundException()
            }
        }

        val provider = mock<SystemFunctionProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            appViewModel.customerState.assertEmitted {
                it == CustomerState.NotFound
            }
            verify(repository).search("+998913975538")
        }
    }

    @Test
    fun `search - not auth`() {
        val repository = mock<AppRepository> {
            onBlocking {
                search("+998913975538")
            } doAnswer {
                throw AuthRequestException()
            }
        }

        val provider = mock<SystemFunctionProvider> {
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
            verify(repository).search("+998913975538")
        }
    }

    @Test
    fun `search - api error`() {
        val repository = mock<AppRepository> {
            onBlocking {
                search("+998913975538")
            } doAnswer {
                throw APIException(400, "Bad request")
            }
        }

        val provider = mock<SystemFunctionProvider> {
        }

        assertFailsWith<APIException> {
            runBlocking {
                val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
                appViewModel.handle(Action.Search("+998913975538"))
                appViewModel.customerState.assertEmitted {
                    it == CustomerState.Default
                }
                verify(repository).search("+998913975538")
            }
        }
    }

    @Test
    fun `search - no connection`() {
        val repository = mock<AppRepository> {
            onBlocking {
                search("+998913975538")
            } doAnswer {
                throw NotConnectedException()
            }
        }

        val provider = mock<SystemFunctionProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            appViewModel.customerState.assertEmitted {
                it == CustomerState.ConnectionFailed
            }
            verify(repository).search("+998913975538")
        }
    }
}