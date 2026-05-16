package cit.edu.sabornido.rentease

import android.app.Activity
import android.content.Context
import android.content.Intent

object SessionHelper {

    fun forceLogin(context: Context) {
        AuthPreferences(context).clear()
        val i = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(i)
        if (context is Activity) {
            context.finish()
        }
    }

    fun handleUnauthorized(context: Context, e: ApiException): Boolean {
        if (e.httpCode != 401) return false
        forceLogin(context)
        return true
    }
}
