package nick.template.data

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import nick.template.di.user.User
import nick.template.di.user.UserComponent

@Singleton
class UserManager @Inject constructor(
    private val userComponentProvider: Provider<UserComponent.Builder>,
    private val helper: UserProcessRecreationHelper
) : UserLogin {
    private var userComponent: UserComponent? = null

    private fun done(): UserLogin.Event.Done<UserComponent> {
        return UserLogin.Event.Done(requireNotNull(userComponent))
    }

    fun userComponent(): Flow<UserLogin.Event<UserComponent>> = flow {
        if (userComponent == null) {
            val lastKnownLogin = helper.lastKnownLogin
                ?: error("Design assumption violated")
            emitAll(login(User(lastKnownLogin)))
        } else {
            emit(done())
        }
    }

    override fun login(user: User): Flow<UserLogin.Event<UserComponent>> = flow {
        if (userComponent != null) {
            error("Trying to login when already logged-in!")
        }
        emit(UserLogin.Event.Working())
        // Simulate work
        delay(2_000L)
        userComponent = userComponentProvider.get().setUser(user).build()
        helper.lastKnownLogin = user.name
        emit(done())
    }

    override fun logout(): Flow<UserLogin.Event<Unit>> = flow {
        userComponent = null
        helper.lastKnownLogin = null
        emit(UserLogin.Event.Working())
        // Simulate work
        delay(1_000L)
        emit(UserLogin.Event.Done(Unit))
    }
}
