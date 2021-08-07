package nick.template.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.google.android.material.internal.TextWatcherAdapter
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.LoginFragmentBinding

class LoginFragment @Inject constructor(
    private val vmFactory: LoginViewModel.Factory,
    private val navController: NavController
) : Fragment(R.layout.login_fragment) {
    private val loginViewModel: LoginViewModel by activityViewModels { vmFactory.create(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = LoginFragmentBinding.bind(view)
        binding.login.setOnClickListener {
            val name = binding.user.text.toString()
            if (name.isBlank()) return@setOnClickListener
            loginViewModel.login(name.lowercase(Locale.getDefault()))
        }

        binding.user.typing()
            .onEach { text ->
                binding.login.isEnabled = text.isNotEmpty()
                    && loginViewModel.states().value != LoginViewModel.State.Loading
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        loginViewModel.states()
            .onEach { state ->
                when (state) {
                    is LoginViewModel.State.LoggedIn -> {
                        // Wait for Fragment navigation to be possible -- user may have
                        // backgrounded the app during login.
                        viewLifecycleOwner.lifecycle.doOnEvent(Lifecycle.Event.ON_START) {
                            navController.navigate("user-scoped-di-fun/user_items")
                        }
                    }
                }

                val isLoading = state == LoginViewModel.State.Loading
                binding.loading.isVisible = isLoading
                binding.user.isEnabled = !isLoading
                binding.login.isEnabled = binding.user.text.isNotBlank() && !isLoading
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun Lifecycle.doOnEvent(which: Lifecycle.Event, block: () -> Unit) {
        val observer = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (which != event) return
                removeObserver(this)
                block()
            }
        }

        addObserver(observer)
    }

    private fun EditText.typing(): Flow<CharSequence> = callbackFlow {
        val watcher = object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                trySend(s)
            }
        }

        addTextChangedListener(watcher)

        awaitClose { removeTextChangedListener(watcher) }
    }
}
