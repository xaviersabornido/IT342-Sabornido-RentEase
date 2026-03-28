package cit.edu.sabornido.rentease

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authPrefs = AuthPreferences(this)
        if (!authPrefs.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val welcome = findViewById<TextView>(R.id.welcome_text)
        val displayName = authPrefs.userJson()?.let { json ->
            runCatching {
                val o = JSONObject(json)
                o.optString("firstname").ifEmpty { o.optString("email") }
            }.getOrNull()
        }.orEmpty().ifEmpty { getString(R.string.brand_name) }
        welcome.text = getString(R.string.welcome_user, displayName)

        findViewById<MaterialButton>(R.id.btn_sign_out).setOnClickListener {
            authPrefs.clear()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
            finish()
        }
    }
}
