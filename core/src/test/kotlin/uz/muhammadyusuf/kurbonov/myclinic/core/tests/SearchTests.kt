package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SettingsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels.SearchResult
import uz.muhammadyusuf.kurbonov.myclinic.shared.printToConsole
import uz.muhammadyusuf.kurbonov.myclinic.shared.recordException

@RunWith(JUnit4::class)
@FixMethodOrder(MethodSorters.DEFAULT)
class SearchTests {

    @Before
    fun init() {
        printToConsole = {
            println(it)
        }
        recordException = {
            it.printStackTrace()
        }
    }

    @Test
    fun testNoConnection() {
        val mockedRepo = mock<AppRepository> {
            onBlocking { search("+998913975538") } doReturn SearchResult.NoConnection
        }
        val mockedProvider = mock<SettingsProvider> {

        }
        val viewModel = AppViewModel(mockedRepo, mockedProvider)
        viewModel.reduce(Action.Search("+998913975538", CallDirection.INCOME))
        runBlocking {
            viewModel.stateFlow.waitUntil(15000) { it == State.NoConnectionState }
        }
    }

    @Test
    fun testAuthRequest() {
        val mockedRepo = mock<AppRepository> {
            onBlocking { search("+998913975538") } doReturn SearchResult.AuthRequested
        }
        val mockedProvider = mock<SettingsProvider> {

        }
        val viewModel = AppViewModel(mockedRepo, mockedProvider)
        viewModel.reduce(Action.Search("+998913975538", CallDirection.INCOME))
        runBlocking {
            viewModel.stateFlow.waitUntil(15000) { it is State.AuthRequest }
        }
    }

    @Test
    fun testInternal() {
        val mockedRepo = mock<AppRepository> {
            onBlocking { search("+998913975538") } doReturn SearchResult.UnknownError
        }
        val mockedProvider = mock<SettingsProvider> {

        }
        val viewModel = AppViewModel(mockedRepo, mockedProvider)
        viewModel.reduce(Action.Search("+998913975538", CallDirection.INCOME))
        runBlocking {
            viewModel.stateFlow.waitUntil(15000) { it is State.Error }
        }
    }

    @Test
    fun testNotFound() {
        val mockedRepo = mock<AppRepository> {
            onBlocking { search("+998913975538") } doReturn SearchResult.NotFound
        }
        val mockedProvider = mock<SettingsProvider> {

        }
        val viewModel = AppViewModel(mockedRepo, mockedProvider)
        viewModel.reduce(Action.Search("+998913975538", CallDirection.INCOME))
        runBlocking {
            viewModel.stateFlow.waitUntil(15000) { it == State.NotFound }
        }
    }

    @Test
    fun testFound() {
        val mockedRepo = mock<AppRepository> {
            onBlocking { search("+998913975538") } doReturn SearchResult.Found(
                dummyCustomer
            )
        }
        val mockedProvider = mock<SettingsProvider> {

        }
        val viewModel = AppViewModel(mockedRepo, mockedProvider)
        viewModel.reduce(Action.Search("+998913975538", CallDirection.INCOME))
        runBlocking {
            viewModel.stateFlow.waitUntil(15000) { it is State.Found }
        }
    }

    @Test
    fun testTimeout() {
        val mockedRepo = mock<AppRepository> {
            onBlocking { search("+998913975538") } doSuspendableAnswer {
                delay(20000)
                SearchResult.Found(dummyCustomer)
            }
        }
        val mockedProvider = mock<SettingsProvider> {

        }
        val viewModel = AppViewModel(mockedRepo, mockedProvider)
        viewModel.reduce(Action.Search("+998913975538", CallDirection.INCOME))
        runBlocking {
            viewModel.stateFlow.waitUntil(30000) {
                it is State.ConnectionTimeoutState
            }
        }
    }

    @Test
    fun testErrorPropagation() {
        val mockedRepo = mock<AppRepository> {
            onBlocking { search("+998913975538") } doThrow IllegalStateException("Test")
        }
        val mockedProvider = mock<SettingsProvider> {
        }
        val viewModel = AppViewModel(mockedRepo, mockedProvider)
        runBlocking {
            viewModel.reduce(Action.Search("+998913975538", CallDirection.INCOME))
        }
    }
}