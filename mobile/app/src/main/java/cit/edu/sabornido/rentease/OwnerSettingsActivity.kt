package cit.edu.sabornido.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class OwnerSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AuthPreferences(this)
        if (!prefs.isLoggedIn() || !UserJson.isOwner(prefs.userJson())) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_owner_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val userJson = prefs.userJson()
        findViewById<TextView>(R.id.settings_display_name).text =
            UserJson.displayName(userJson).ifEmpty { getString(R.string.role_owner) }
        findViewById<TextView>(R.id.settings_email).text =
            UserJson.email(userJson).ifEmpty { "—" }

        findViewById<MaterialButton>(R.id.btn_settings_my_listings).setOnClickListener {
            startActivity(Intent(this, MyListingsActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_settings_requests).setOnClickListener {
            startActivity(Intent(this, OwnerRentalRequestsActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_settings_sign_out).setOnClickListener {
            prefs.clear()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
            finish()
        }
    }
}
