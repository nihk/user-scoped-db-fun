package nick.template.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import dagger.hilt.EntryPoints
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.data.Item
import nick.template.databinding.UserItemsFragmentBinding
import nick.template.di.user.UserEntryPoint
import nick.template.ui.adapters.ItemAdapter

class UserItemsFragment @Inject constructor(
    private val loginViewModelFactory: LoginViewModel.Factory,
    private val navController: NavController
) : Fragment(R.layout.user_items_fragment) {
    private val loginViewModel: LoginViewModel by activityViewModels { loginViewModelFactory.create(this) }
    private lateinit var userItemsViewModel: UserItemsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = UserItemsFragmentBinding.bind(view)

        loginViewModel.states()
            .onEach { state ->
                binding.loading.isVisible = state == LoginViewModel.State.Loading

                when (state) {
                    is LoginViewModel.State.LoggedIn -> {
                        val userEntryPoint = EntryPoints.get(
                            state.userComponent,
                            UserEntryPoint::class.java
                        )
                        userItemsViewModel = ViewModelProvider(this, userEntryPoint.vmFactory)
                            .get(UserItemsViewModel::class.java)
                        bindControls(binding)
                    }
                    is LoginViewModel.State.LoggedOut -> navController.popBackStack()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        if (loginViewModel.states().value !is LoginViewModel.State.LoggedIn) {
            loginViewModel.loginSilently()
        }

        val logoutOnBackPress = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                loginViewModel.logout()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, logoutOnBackPress)
    }

    private fun bindControls(binding: UserItemsFragmentBinding) {
        val adapter = ItemAdapter()
        binding.recyclerView.adapter = adapter

        binding.insert.setOnClickListener {
            val text = binding.input.text.toString()
            val item = Item(name = if (text.isBlank()) UUID.randomUUID().toString() else text)
            userItemsViewModel.insert(item)
            binding.input.text.clear()
        }
        binding.insert.isEnabled = true

        userItemsViewModel.items
            .onEach { adapter.submitList(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.username.text = userItemsViewModel.user.name

        binding.logout.setOnClickListener {
            loginViewModel.logout()
        }
        binding.logout.isEnabled = true
    }
}
