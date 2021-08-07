package nick.template.di.user

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import nick.template.ui.UserItemsViewModel

@InstallIn(UserComponent::class)
@EntryPoint
interface UserEntryPoint {
    val vmFactory: UserItemsViewModel.Factory
}
