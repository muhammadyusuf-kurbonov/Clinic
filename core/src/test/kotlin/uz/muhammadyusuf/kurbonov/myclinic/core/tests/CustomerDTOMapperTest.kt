package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class CustomerDTOMapperTest {
    @Test
    fun `mapper - empty data`() {
        val dummy = GsonBuilder().create()
            .fromJson(getJSON("empty-data.json"), CustomerDTO::class.java)
        val repository = mock<AppRepository> {
            onBlocking {
                search("+998913975538")
            } doReturn dummy
        }

        val provider = mock<SystemFunctionProvider> {
        }

        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
                appViewModel.handle(Action.Search("+998913975538"))
                appViewModel.customerState.assertEmitted {
                    it is CustomerState.Found
                }
                verify(repository).search("+998913975538")
            }
        }
    }

    @Test
    fun `mapper - empty appointments`() {
        val dummy = GsonBuilder().create()
            .fromJson(getJSON("appointments-empty.json"), CustomerDTO::class.java)
        val repository = mock<AppRepository> {
            onBlocking {
                search("+998913975538")
            } doReturn dummy
        }

        val provider = mock<SystemFunctionProvider> {
        }

        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
                appViewModel.handle(Action.Search("+998913975538"))
                appViewModel.customerState.assertEmitted {
                    it is CustomerState.Found
                }
                verify(repository).search("+998913975538")
            }
        }
    }
}