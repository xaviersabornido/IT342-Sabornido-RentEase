package cit.edu.sabornido.rentease

import android.content.Context

class AuthPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(accessToken: String, refreshToken: String?, userJson: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_JSON, userJson)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun accessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun userJson(): String? = prefs.getString(KEY_USER_JSON, null)

    fun isLoggedIn(): Boolean = !accessToken().isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "rentease_auth"
        private const val KEY_ACCESS_TOKEN = "accessToken"
        private const val KEY_REFRESH_TOKEN = "refreshToken"
        private const val KEY_USER_JSON = "user"
    }
}
