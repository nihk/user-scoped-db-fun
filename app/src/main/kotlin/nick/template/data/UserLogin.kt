package nick.template.data

import kotlinx.coroutines.flow.Flow
import nick.template.di.user.User
import nick.template.di.user.UserComponent

interface UserLogin {
    fun login(user: User): Flow<Event<UserComponent>>
    fun logout(): Flow<Event<Unit>>

    sealed class Event<T> {
        class Working<T> : Event<T>()
        data class Done<T>(val data: T) : Event<T>()
    }
}
