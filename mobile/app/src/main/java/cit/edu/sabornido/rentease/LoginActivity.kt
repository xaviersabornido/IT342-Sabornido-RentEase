package cit.edu.sabornido.rentease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var authApi: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authPrefs = AuthPreferences(this)
        authApi = AuthApi(getString(R.string.api_base_url))

        val emailInput = findViewById<TextInputEditText>(R.id.email_input)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_input)
        val errorText = findViewById<TextView>(R.id.error_text)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val btnSignIn = findViewById<MaterialButton>(R.id.btn_sign_in)

        findViewById<TextView>(R.id.link_register).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            val email = emailInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString().orEmpty()
            errorText.visibility = View.GONE
            if (email.isEmpty() || password.isEmpty()) {
                showError(errorText, getString(R.string.email) + " / " + getString(R.string.password) + " required")
                return@setOnClickListener
            }
            btnSignIn.isEnabled = false
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val payload = authApi.login(email, password)
                    authPrefs.saveSession(
                        payload.accessToken,
                        payload.refreshToken,
                        payload.userJson,
                    )
                    startActivity(
                        Intent(this@LoginActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        },
                    )
                    finish()
                } catch (e: ApiException) {
                    showError(errorText, e.message ?: getString(R.string.sign_in) + " failed")
                } catch (_: Exception) {
                    showError(errorText, getString(R.string.sign_in) + " failed")
                } finally {
                    btnSignIn.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }

    private fun showError(errorText: TextView, message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
    }
}
