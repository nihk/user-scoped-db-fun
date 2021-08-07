package nick.template.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nick.template.data.AppScope
import nick.template.data.UserLogin
import nick.template.data.UserManager

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun userLogin(userManager: UserManager): UserLogin

    companion object {
        @Provides
        fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences {
            return context.getSharedPreferences("user-scoped-db-fun-prefs", Context.MODE_PRIVATE)
        }

        @Provides
        fun appScope() = AppScope
    }
}
