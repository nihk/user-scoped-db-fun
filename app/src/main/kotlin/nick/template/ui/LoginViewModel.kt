package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import nick.template.data.AppScope
import nick.template.data.UserLogin
import nick.template.data.UserManager
import nick.template.di.user.User
import nick.template.di.user.UserComponent

class LoginViewModel(
    private val userManager: UserManager,
    private val appScope: AppScope
) : ViewModel() {
    private val states = MutableStateFlow<State>(State.Initial)
    fun states() = states.asStateFlow()
    private var work: Job? = null

    fun login(name: String) {
        work = userManager.login(User(name))
            .onEach { event -> handle(event) }
            .launchIn(viewModelScope)
    }

    fun loginSilently() {
        work = userManager.userComponent()
            .onEach { event -> handle(event) }
            .launchIn(viewModelScope)
    }

    fun logout() {
        work?.cancel()
        work = null
        states.value = State.LoggedOut
        userManager.logout().launchIn(appScope)
    }

    private fun handle(event: UserLogin.Event<UserComponent>) {
        states.value = when (event) {
            is UserLogin.Event.Working -> State.Loading
            is UserLogin.Event.Done -> State.LoggedIn(event.data)
        }
    }

    class Factory @Inject constructor(
        private val userManager: UserManager,
        private val appScope: AppScope
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel?> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return LoginViewModel(userManager, appScope) as T
                }
            }
        }
    }

    sealed class State {
        object Initial: State()
        object LoggedOut : State()
        object Loading : State()
        data class LoggedIn(val userComponent: UserComponent) : State()
    }
}
