package com.example.dentalinkmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.appointments.model.AppointmentItem
import com.example.dentalinkmobile.features.appointments.model.CreateIntentRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAppointmentsActivity : AppCompatActivity() {

    private var allAppointments = listOf<AppointmentItem>()
    private var currentFilter   = "ALL"

    private lateinit var lvAppointments: ListView
    private lateinit var tvEmpty: TextView
    // TextViews, not Buttons — Material3 Button overrides background/textColor selectors;
    // plain TextView respects android:background state selector and android:textColor selector natively.
    private lateinit var filterButtons: List<TextView>

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val appointmentId = result.data?.getLongExtra(PaymentWebViewActivity.EXTRA_APPOINTMENT_ID, -1L) ?: -1L
            if (appointmentId != -1L) {
                pollForConfirmation(appointmentId)
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Payment successful! Check your email for confirmation.", Snackbar.LENGTH_LONG).show()
                loadAppointments()
            }
        } else {
            loadAppointments()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_appointments)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        lvAppointments = findViewById(R.id.lvAppointments)
        tvEmpty        = findViewById(R.id.tvAppointmentsEmpty)

        // Chips are TextViews so android:textColor / android:background selectors work correctly
        val btnAll       = findViewById<TextView>(R.id.btnFilterAll)
        val btnConfirmed = findViewById<TextView>(R.id.btnFilterConfirmed)
        val btnPending   = findViewById<TextView>(R.id.btnFilterPending)
        val btnCompleted = findViewById<TextView>(R.id.btnFilterCompleted)
        val btnCancelled = findViewById<TextView>(R.id.btnFilterCancelled)
        filterButtons    = listOf(btnAll, btnConfirmed, btnPending, btnCompleted, btnCancelled)

        btnAll.setOnClickListener       { applyFilter("ALL",             btnAll) }
        btnConfirmed.setOnClickListener { applyFilter("CONFIRMED",       btnConfirmed) }
        btnPending.setOnClickListener   { applyFilter("PENDING_PAYMENT", btnPending) }
        btnCompleted.setOnClickListener { applyFilter("COMPLETED",       btnCompleted) }
        btnCancelled.setOnClickListener { applyFilter("CANCELLED",       btnCancelled) }

        // Initialise "All" as selected; the background + text selectors react automatically
        setActiveFilter(btnAll)
    }

    override fun onResume() {
        super.onResume()
        loadAppointments()
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAppointments()
                if (response.isSuccessful) {
                    allAppointments = response.body()?.data ?: emptyList()
                    applyFilter(currentFilter, filterButtons.first())
                } else {
                    Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), "Failed to load appointments", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), "Network error. Please check your connection.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun pollForConfirmation(appointmentId: Long) {
        lifecycleScope.launch {
            var confirmed = false
            var attempts  = 0
            while (attempts < 5 && !confirmed) {
                try {
                    val resp = RetrofitClient.apiService.getAppointmentById(appointmentId)
                    if (resp.isSuccessful && resp.body()?.data?.status == "CONFIRMED") {
                        confirmed = true
                    } else {
                        delay(1000)
                        attempts++
                    }
                } catch (e: Exception) {
                    break
                }
            }
            val message = if (confirmed)
                "Payment confirmed! A confirmation email has been sent to your registered email."
            else
                "Payment received! Your appointment confirmation may take a moment. Check your email shortly."
            Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
            loadAppointments()
        }
    }

    private fun applyFilter(filter: String, activeBtn: TextView) {
        currentFilter = filter
        setActiveFilter(activeBtn)

        val filtered = if (filter == "ALL") allAppointments
        else allAppointments.filter { it.status == filter }

        if (filtered.isEmpty()) {
            tvEmpty.visibility        = View.VISIBLE
            lvAppointments.visibility = View.GONE
            return
        }

        tvEmpty.visibility        = View.GONE
        lvAppointments.visibility = View.VISIBLE
        lvAppointments.adapter    = AppointmentAdapter(
            this, filtered,
            onPayClick    = { payForAppointment(it.id) },
            onCancelClick = { cancelAppointment(it.id) }
        )
    }

    /**
     * Updates the chip selection state.
     * With <TextView> chips, setting isSelected triggers refreshDrawableState() which
     * automatically applies the correct background drawable AND textColor from the selectors.
     * No programmatic setTextColor() needed — the XML selectors handle everything.
     */
    private fun setActiveFilter(activeBtn: TextView) {
        filterButtons.forEach { btn ->
            btn.isSelected = (btn == activeBtn)
        }
    }

    private fun cancelAppointment(appointmentId: Long) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Cancel this appointment? This cannot be undone.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.apiService.cancelAppointment(appointmentId)
                        if (response.isSuccessful) {
                            Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), "Your appointment has been cancelled.", Snackbar.LENGTH_SHORT).show()
                            loadAppointments()
                        } else {
                            val msg = when (response.code()) {
                                400  -> "This appointment cannot be cancelled (payment may already be recorded)."
                                403  -> "You can only cancel your own appointments."
                                404  -> "Appointment not found."
                                else -> "Failed to cancel appointment. Please try again."
                            }
                            Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), "Network error. Please check your connection.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun payForAppointment(appointmentId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createPaymentIntent(
                    CreateIntentRequest(appointmentId)
                )
                if (response.isSuccessful) {
                    val checkoutUrl = response.body()?.data?.checkoutUrl
                    if (!checkoutUrl.isNullOrEmpty()) {
                        val intent = Intent(this@MyAppointmentsActivity, PaymentWebViewActivity::class.java)
                        intent.putExtra(PaymentWebViewActivity.EXTRA_CHECKOUT_URL, checkoutUrl)
                        paymentLauncher.launch(intent)
                    } else {
                        Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), "Checkout URL not received", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), "Failed to initiate payment", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@MyAppointmentsActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

class AppointmentAdapter(
    context: Context,
    private val items: List<AppointmentItem>,
    private val onPayClick: (AppointmentItem) -> Unit,
    private val onCancelClick: (AppointmentItem) -> Unit
) : ArrayAdapter<AppointmentItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_appointment, parent, false)

        val item = items[position]

        view.findViewById<TextView>(R.id.tvApptServiceName).text = item.serviceName ?: "Service"
        view.findViewById<TextView>(R.id.tvApptDentistName).text = if (item.dentistName != null) "Dr. ${item.dentistName}" else "Unknown Dentist"
        view.findViewById<TextView>(R.id.tvApptDatetime).text    = formatDatetime(item.appointmentDatetime)

        val tvStatus = view.findViewById<TextView>(R.id.tvApptStatus)
        tvStatus.text = item.status?.replace("_", " ") ?: ""
        val (textColor, bgColor) = statusColors(context, item.status)
        tvStatus.setTextColor(textColor)
        tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(bgColor)

        val tvPayment = view.findViewById<TextView>(R.id.tvApptPaymentStatus)
        tvPayment.text = "Payment: ${item.paymentStatus ?: ""}"

        val isUnpaidPending = item.paymentStatus == "UNPAID" && item.status == "PENDING_PAYMENT"

        val tvPayNow = view.findViewById<TextView>(R.id.tvPayNow)
        tvPayNow.visibility = if (isUnpaidPending) View.VISIBLE else View.GONE
        tvPayNow.setOnClickListener { onPayClick(item) }

        val tvCancel = view.findViewById<TextView>(R.id.tvCancelAppt)
        tvCancel.visibility = if (isUnpaidPending) View.VISIBLE else View.GONE
        tvCancel.setOnClickListener { onCancelClick(item) }

        return view
    }

    private fun statusColors(context: Context, status: String?): Pair<Int, Int> {
        return when (status) {
            "CONFIRMED"       -> Pair(ContextCompat.getColor(context, R.color.status_confirmed), ContextCompat.getColor(context, R.color.status_confirmed_bg))
            "COMPLETED"       -> Pair(ContextCompat.getColor(context, R.color.status_completed), ContextCompat.getColor(context, R.color.status_completed_bg))
            "PENDING_PAYMENT" -> Pair(ContextCompat.getColor(context, R.color.status_pending),   ContextCompat.getColor(context, R.color.status_pending_bg))
            "CANCELLED"       -> Pair(ContextCompat.getColor(context, R.color.status_cancelled), ContextCompat.getColor(context, R.color.status_cancelled_bg))
            else              -> Pair(ContextCompat.getColor(context, R.color.text_secondary),   ContextCompat.getColor(context, R.color.surface_variant))
        }
    }

    private fun formatDatetime(dt: String?): String {
        if (dt == null) return ""
        return try {
            val ldt = java.time.LocalDateTime.parse(dt.take(19))
            ldt.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))
        } catch (e: Exception) { dt }
    }
}
