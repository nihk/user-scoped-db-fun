package nick.template.data

import android.content.SharedPreferences
import javax.inject.Inject

// Alternatively store this value in a saved state handle.
class UserProcessRecreationHelper @Inject constructor(
    // fixme: use EncryptedSharedPreferences if sensitive data is to be stored this way.
    private val sharedPreferences: SharedPreferences
) {
    var lastKnownLogin: String?
        get() = sharedPreferences.getString("last_known_login", null)
        set(value) {
            sharedPreferences.edit().putString("last_known_login", value).apply()
        }
}
