package nick.template.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nick.template.data.Item
import nick.template.data.ItemDao
import nick.template.di.user.User

class UserItemsViewModel(
    private val itemDao: ItemDao,
    val user: User
) : ViewModel() {

    val items = itemDao.items()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_500L),
            initialValue = emptyList()
        )

    fun insert(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    class Factory @Inject constructor(
        private val itemDao: ItemDao,
        private val user: User
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UserItemsViewModel(itemDao, user) as T
        }
    }
}
