package nick.template.di.user

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import nick.template.BuildConfig
import nick.template.data.ItemDao
import nick.template.data.UserDatabase

@Module
@InstallIn(UserComponent::class)
class UserModule {
    @UserScope
    @Provides
    fun appDatabase(
        @ApplicationContext appContext: Context,
        user: User
    ): UserDatabase {
        return Room.databaseBuilder(
            appContext,
            UserDatabase::class.java,
            UserDatabase.name(user)
        )
            .apply {
                if (BuildConfig.DEBUG) {
                    setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                }
            }
            .build()
    }

    @Provides
    fun itemDao(userDatabase: UserDatabase): ItemDao {
        return userDatabase.itemDao()
    }
}
