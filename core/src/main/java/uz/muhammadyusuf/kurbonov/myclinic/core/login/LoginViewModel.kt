package uz.muhammadyusuf.kurbonov.myclinic.core.login

import kotlinx.coroutines.launch
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionProvider
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.AuthRequestException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import uz.muhammadyusuf.kurbonov.myclinic.network.models.AuthToken
import kotlin.coroutines.CoroutineContext

class LoginViewModel(
    parentCoroutineContext: CoroutineContext,
    provider: SystemFunctionProvider,
    repository: AppRepository
) : AppViewModel<LoginActions, LoginStates>(parentCoroutineContext, provider, repository) {
    override fun handle(loginAction: LoginActions) {
        super.handle(loginAction)
        if (loginAction is LoginActions.Login) {
            launch {
                try {
                    if (loginAction.username.isEmpty()) {
                        _state.value = LoginStates.FieldRequired("username")
                        return@launch
                    }
                    if (loginAction.password.isEmpty()) {
                        _state.value = LoginStates.FieldRequired("password")
                        return@launch
                    }
                    val token: AuthToken = repository.authenticate(
                        loginAction.username,
                        loginAction.password
                    )
                    provider.writePreference("token", token.token)
                    _state.value = LoginStates.AuthSuccess
                } catch (e: AuthRequestException) {
                    provider.writePreference("token", "")
                    _state.value = LoginStates.AuthFailed
                } catch (e: NotConnectedException) {
                    _state.value = LoginStates.ConnectionFailed
                }
            }
        }
    }
}