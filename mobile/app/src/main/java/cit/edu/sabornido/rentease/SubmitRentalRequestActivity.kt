package cit.edu.sabornido.rentease

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.json.JSONObject

class SubmitRentalRequestActivity : AppCompatActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var api: RentEaseApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authPrefs = AuthPreferences(this)
        if (!authPrefs.isLoggedIn()) {
            SessionHelper.forceLogin(this)
            return
        }
        if (!UserJson.isRenter(authPrefs.userJson())) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_submit_rental_request)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        api = RentEaseApi(getString(R.string.api_base_url), authPrefs)

        val listingId = intent.getLongExtra(EXTRA_LISTING_ID, -1L)
        if (listingId < 0) {
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val leaseSpinner = findViewById<Spinner>(R.id.lease_months)
        val leaseAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.lease_month_options,
            android.R.layout.simple_spinner_item,
        )
        leaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        leaseSpinner.adapter = leaseAdapter

        val empSpinner = findViewById<Spinner>(R.id.employment)
        val empAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.employment_options,
            android.R.layout.simple_spinner_item,
        )
        empAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        empSpinner.adapter = empAdapter

        val err = findViewById<TextView>(R.id.error_text)
        findViewById<MaterialButton>(R.id.btn_submit).setOnClickListener {
            err.visibility = View.GONE
            val start = findViewById<TextInputEditText>(R.id.preferred_start).text?.toString()?.trim().orEmpty()
            val incomeStr = findViewById<TextInputEditText>(R.id.monthly_income).text?.toString()?.trim().orEmpty()
            val message = findViewById<TextInputEditText>(R.id.message).text?.toString()?.trim().orEmpty()
            val employment = empSpinner.selectedItem?.toString().orEmpty()
            val credit = findViewById<MaterialCheckBox>(R.id.credit_check).isChecked

            if (start.isEmpty()) {
                err.text = "Preferred start date is required."
                err.visibility = View.VISIBLE
                return@setOnClickListener
            }
            val income = incomeStr.toDoubleOrNull()
            if (income == null || income <= 0) {
                err.text = "Please enter a valid monthly income."
                err.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (employment.isEmpty()) {
                err.text = "Employment status is required."
                err.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (!credit) {
                err.text = "You must agree to a credit check."
                err.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val leasePos = leaseSpinner.selectedItemPosition
            val leaseVal = leaseSpinner.selectedItem?.toString()?.trim().orEmpty()
            val leaseMonths = if (leasePos <= 0 || leaseVal.isEmpty()) null else leaseVal.toIntOrNull()

            val body = JSONObject().apply {
                put("listingId", listingId)
                put("preferredStartDate", start)
                if (leaseMonths != null) put("leaseDurationMonths", leaseMonths) else put("leaseDurationMonths", JSONObject.NULL)
                put("monthlyIncome", income)
                put("employmentStatus", employment)
                if (message.isNotEmpty()) put("message", message) else put("message", JSONObject.NULL)
                put("hasPets", findViewById<MaterialSwitch>(R.id.has_pets).isChecked)
                put("smokes", findViewById<MaterialSwitch>(R.id.smokes).isChecked)
                put("creditCheckAgreed", true)
            }

            lifecycleScope.launch {
                try {
                    api.createRentalRequest(body)
                    finish()
                } catch (e: ApiException) {
                    if (SessionHelper.handleUnauthorized(this@SubmitRentalRequestActivity, e)) return@launch
                    err.text = e.message
                    err.visibility = View.VISIBLE
                } catch (_: Exception) {
                    err.text = "Could not send request."
                    err.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        const val EXTRA_LISTING_ID = "listing_id"
    }
}
