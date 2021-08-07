package nick.template.di

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import nick.template.R
import nick.template.ui.AppFragmentFactory
import nick.template.ui.LoginFragment
import nick.template.ui.UserItemsFragment

@Module
@InstallIn(ActivityComponent::class)
abstract class MainModule {

    companion object {
        @Provides
        fun navController(activity: Activity): NavController {
            val navHostFragment = (activity as FragmentActivity).supportFragmentManager
                .findFragmentById(R.id.navHostContainer) as NavHostFragment
            return navHostFragment.navController
        }
    }

    @Binds
    @IntoMap
    @FragmentKey(LoginFragment::class)
    abstract fun loginFragment(loginFragment: LoginFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(UserItemsFragment::class)
    abstract fun userItemsFragment(userItemsFragment: UserItemsFragment): Fragment

    @Binds
    abstract fun fragmentFactory(appFragmentFactory: AppFragmentFactory): FragmentFactory
}
