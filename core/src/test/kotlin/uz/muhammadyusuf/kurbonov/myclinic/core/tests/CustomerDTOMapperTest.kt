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
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.treatment.TreatmentDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.users.UserDTO
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class CustomerDTOMapperTest {
    @Test
    fun `mapper - empty data`() {
        val dummy = GsonBuilder().create()
            .fromJson(getJSON("empty-data.json"), CustomerDTO::class.java)
        val repository = mockk<AppRepository> {
            coEvery {
                getUser(any())
            } returns GsonBuilder().create()
                .fromJson(getJSON("user-get-found.json"), UserDTO::class.java).data[0]

            coEvery {
                getTreatment(any())
            } returns GsonBuilder().create()
                .fromJson(getJSON("treatment-get-found.json"), TreatmentDTO::class.java).data[0]

            coEvery {
                search("+998913975538")
            } returns dummy
        }


        val provider = mockk<SystemFunctionsProvider>()

        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                val appViewModel = AppStatesController(this.coroutineContext, provider, repository)
                appViewModel.handle(Action.Search("+998913975538"))
                AppStateStore.customerState.assertEmitted {
                    it is CustomerState.Found
                }
                coVerify { repository.search("+998913975538") }
            }
        }
    }

    @Test
    fun `mapper - empty appointments`() {
        val dummy = GsonBuilder().create()
            .fromJson(getJSON("appointments-empty.json"), CustomerDTO::class.java)
        val repository = mockk<AppRepository> {
            coEvery {
                getUser(any())
            } returns GsonBuilder().create()
                .fromJson(getJSON("user-get-found.json"), UserDTO::class.java).data[0]

            coEvery {
                getTreatment(any())
            } returns GsonBuilder().create()
                .fromJson(getJSON("treatment-get-found.json"), TreatmentDTO::class.java).data[0]

            coEvery {
                search("+998913975538")
            } returns dummy
        }

        val provider = mockk<SystemFunctionsProvider>()

        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                val appViewModel = AppStatesController(this.coroutineContext, provider, repository)
                appViewModel.handle(Action.Search("+998913975538"))
                AppStateStore.customerState.assertEmitted {
                    it is CustomerState.Found
                }
                coVerify {
                    repository.search("+998913975538")
                }
            }
        }
    }

    @Test
    fun `mapper - success`() {
        val dummy = GsonBuilder().create()
            .fromJson(getJSON("customer.json"), CustomerDTO::class.java)
        val repository = mockk<AppRepository> {
            coEvery {
                getUser(any())
            } returns GsonBuilder().create()
                .fromJson(getJSON("user-get-found.json"), UserDTO::class.java).data[0]

            coEvery {
                getTreatment(any())
            } returns GsonBuilder().create()
                .fromJson(getJSON("treatment-get-found.json"), TreatmentDTO::class.java).data[0]

            coEvery {
                search("+998913975538")
            } returns dummy

        }


        val provider = mockk<SystemFunctionsProvider>()

        runBlocking {
            val appViewModel = AppStatesController(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.Search("+998913975538"))
            AppStateStore.customerState.assertEmitted {
                it is CustomerState.Found &&
                        it.customer.lastAppointment != null
            }
            coVerify {
                repository.search("+998913975538")
                repository.getUser("5dc2beab0af9c9e30a0ea0f5")
                repository.getTreatment("5dc3c84d9a76124f537464fe")
            }
        }

    }
}