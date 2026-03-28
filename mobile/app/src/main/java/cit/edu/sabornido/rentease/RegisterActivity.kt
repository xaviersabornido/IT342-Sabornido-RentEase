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
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var authApi: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authApi = AuthApi(getString(R.string.api_base_url))

        val firstnameInput = findViewById<TextInputEditText>(R.id.firstname_input)
        val lastnameInput = findViewById<TextInputEditText>(R.id.lastname_input)
        val emailInput = findViewById<TextInputEditText>(R.id.email_input)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_input)
        val confirmInput = findViewById<TextInputEditText>(R.id.confirm_password_input)
        val roleToggle = findViewById<MaterialButtonToggleGroup>(R.id.role_toggle)
        val errorText = findViewById<TextView>(R.id.error_text)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val btnRegister = findViewById<MaterialButton>(R.id.btn_register)

        findViewById<TextView>(R.id.link_login).setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val firstname = firstnameInput.text?.toString()?.trim().orEmpty()
            val lastname = lastnameInput.text?.toString()?.trim().orEmpty()
            val email = emailInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString().orEmpty()
            val confirm = confirmInput.text?.toString().orEmpty()
            val role = when (roleToggle.checkedButtonId) {
                R.id.btn_role_owner -> "OWNER"
                else -> "RENTER"
            }

            errorText.visibility = View.GONE

            val validationError = validate(firstname, lastname, email, password, confirm, role)
            if (validationError != null) {
                showError(errorText, validationError)
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    authApi.register(email, password, firstname, lastname, role)
                    progress.visibility = View.GONE
                    MaterialAlertDialogBuilder(this@RegisterActivity)
                        .setTitle(R.string.registration_successful)
                        .setMessage(R.string.registration_success_hint)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            startActivity(
                                Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                },
                            )
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } catch (e: ApiException) {
                    progress.visibility = View.GONE
                    btnRegister.isEnabled = true
                    showError(errorText, e.message ?: "Unable to register")
                } catch (_: Exception) {
                    progress.visibility = View.GONE
                    btnRegister.isEnabled = true
                    showError(errorText, "Unable to register")
                }
            }
        }
    }

    private fun validate(
        firstname: String,
        lastname: String,
        email: String,
        password: String,
        confirm: String,
        role: String,
    ): String? {
        if (firstname.isEmpty()) return "First name is required"
        if (lastname.isEmpty()) return "Last name is required"
        if (email.isEmpty()) return "Email is required"
        if (!EMAIL_REGEX.matches(email)) return "Please enter a valid email address"
        if (password.length < 8) return "Password must be at least 8 characters"
        if (password != confirm) return "Passwords do not match"
        if (role != "RENTER" && role != "OWNER") return "Please select a valid role"
        return null
    }

    private fun showError(errorText: TextView, message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
