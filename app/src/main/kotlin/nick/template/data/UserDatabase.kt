package nick.template.data

import androidx.room.Database
import androidx.room.RoomDatabase
import nick.template.di.user.User

@Database(
    entities = [Item::class],
    version = 1
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        fun name(user: User): String = "${user.name}.db"
    }
}
