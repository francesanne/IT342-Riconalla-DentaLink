package com.example.dentalinkmobile

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.appointments.model.AppointmentItem
import com.example.dentalinkmobile.features.payments.model.UpdateStatusRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminAppointmentsActivity : AppCompatActivity() {

    private var allAppointments = listOf<AppointmentItem>()
    private var currentFilter   = ""          // "" = All; otherwise an exact status string

    private lateinit var lvAppointments: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_appointments)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        lvAppointments = findViewById(R.id.lvAdminAppointments)
        tvEmpty        = findViewById(R.id.tvAdminAppointmentsEmpty)
        progressBar    = findViewById(R.id.progressBar)

        // Filter Spinner — matches the 5 web chip options (All / Pending Payment / Confirmed / Completed / Cancelled)
        val spinnerFilter = findViewById<Spinner>(R.id.spinnerApptFilter)
        val filterLabels  = arrayOf("All", "Pending Payment", "Confirmed", "Completed", "Cancelled")
        val filterValues  = arrayOf("", "PENDING_PAYMENT", "CONFIRMED", "COMPLETED", "CANCELLED")

        spinnerFilter.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterLabels
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                currentFilter = filterValues[pos]
                renderList()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onResume() {
        super.onResume()
        loadAppointments()
    }

    private fun loadAppointments() {
        progressBar.visibility    = View.VISIBLE
        lvAppointments.visibility = View.GONE
        tvEmpty.visibility        = View.GONE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAppointments()
                if (response.isSuccessful) {
                    allAppointments = response.body()?.data ?: emptyList()
                    renderList()
                } else {
                    Snackbar.make(this@AdminAppointmentsActivity.findViewById(android.R.id.content), "Failed to load appointments", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@AdminAppointmentsActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun renderList() {
        // Apply the active filter (empty string = show all)
        val filtered = if (currentFilter.isEmpty()) allAppointments
                       else allAppointments.filter { it.status == currentFilter }

        if (filtered.isEmpty()) {
            tvEmpty.visibility        = View.VISIBLE
            lvAppointments.visibility = View.GONE
            return
        }
        tvEmpty.visibility        = View.GONE
        lvAppointments.visibility = View.VISIBLE

        lvAppointments.adapter = AdminAppointmentAdapter(this, filtered, ::formatDatetime)
        // Use filtered[position] so taps map to the correct item after filtering
        lvAppointments.setOnItemClickListener { _, _, position, _ ->
            showStatusOptions(filtered[position])
        }
    }

    private fun showStatusOptions(appointment: AppointmentItem) {
        val currentStatus = appointment.status ?: ""

        // Terminal states — no further updates allowed
        if (currentStatus == "COMPLETED" || currentStatus == "CANCELLED") {
            val label = currentStatus.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercaseChar() }
            Snackbar.make(
                findViewById(android.R.id.content),
                "This appointment is already $label — no further changes",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        // Build options based on current state:
        //   PENDING_PAYMENT → Confirm | Cancel
        //   CONFIRMED       → Complete | Cancel
        val (options, statusValues) = when (currentStatus) {
            "PENDING_PAYMENT" -> Pair(
                arrayOf("✓  Confirm appointment", "✗  Cancel appointment"),
                arrayOf("CONFIRMED", "CANCELLED")
            )
            "CONFIRMED" -> Pair(
                arrayOf("✓  Mark as Completed", "✗  Cancel appointment"),
                arrayOf("COMPLETED", "CANCELLED")
            )
            else -> Pair(
                arrayOf("✗  Cancel appointment"),
                arrayOf("CANCELLED")
            )
        }

        // NOTE: setMessage() + setItems() are mutually exclusive in AlertDialog —
        // the items list won't render if a message is also set.
        // Put the appointment summary in the title so setItems() works correctly.
        val dialogTitle = buildString {
            append(appointment.serviceName ?: "Appointment")
            append("  •  ")
            append(formatDatetime(appointment.appointmentDatetime))
        }

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setItems(options) { _, which ->
                val newStatus = statusValues[which]
                // Double-confirm before applying
                AlertDialog.Builder(this)
                    .setTitle("Confirm change")
                    .setMessage("Set status to \"${newStatus.replace("_", " ")}\"?")
                    .setPositiveButton("Yes") { _, _ -> updateStatus(appointment.id, newStatus) }
                    .setNegativeButton("No", null)
                    .show()
            }
            .setNegativeButton("Dismiss", null)
            .show()
    }

    private fun updateStatus(id: Long, status: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateAppointmentStatus(
                    id, UpdateStatusRequest(status)
                )
                if (response.isSuccessful) {
                    Snackbar.make(this@AdminAppointmentsActivity.findViewById(android.R.id.content), "Status updated to $status", Snackbar.LENGTH_SHORT).show()
                    loadAppointments()
                } else {
                    Snackbar.make(this@AdminAppointmentsActivity.findViewById(android.R.id.content), "Failed to update status (${response.code()})", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@AdminAppointmentsActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
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

private class AdminAppointmentAdapter(
    context: Context,
    private val items: List<AppointmentItem>,
    private val fmt: (String?) -> String
) : ArrayAdapter<AppointmentItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_admin_appointment, parent, false)

        val item = items[position]

        view.findViewById<TextView>(R.id.tvAdminApptService).text  = item.serviceName ?: "Service"

        view.findViewById<TextView>(R.id.tvAdminApptPatient).text =
            "Patient: ${"${item.patient?.firstName ?: ""} ${item.patient?.lastName ?: ""}".trim().ifEmpty { "Unknown" }}"

        view.findViewById<TextView>(R.id.tvAdminApptDentist).text  = if (item.dentistName != null) "Dr. ${item.dentistName}" else "Unknown Dentist"
        view.findViewById<TextView>(R.id.tvAdminApptDatetime).text = fmt(item.appointmentDatetime)

        val tvStatus = view.findViewById<TextView>(R.id.tvAdminApptStatus)
        tvStatus.text = (item.status ?: "").replace("_", " ")
        when (item.status) {
            "CONFIRMED" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_confirmed)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_confirmed_text))
            }
            "COMPLETED" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_completed)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_completed_text))
            }
            "PENDING_PAYMENT" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_pending)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_pending_text))
            }
            "CANCELLED" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelled)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_cancelled_text))
            }
            else -> {
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
        }

        val tvPayment = view.findViewById<TextView>(R.id.tvAdminApptPayment)
        val isPaid = item.paymentStatus == "PAID"
        tvPayment.text = if (isPaid) "PAID" else "UNPAID"
        if (isPaid) {
            tvPayment.setBackgroundResource(R.drawable.bg_badge_completed)
            tvPayment.setTextColor(ContextCompat.getColor(context, R.color.badge_completed_text))
        } else {
            tvPayment.setBackgroundResource(R.drawable.bg_badge_pending)
            tvPayment.setTextColor(ContextCompat.getColor(context, R.color.badge_pending_text))
        }

        // Show "Tap to update ›" hint only for actionable (non-terminal) appointments
        val isTerminal = item.status == "COMPLETED" || item.status == "CANCELLED"
        view.findViewById<TextView>(R.id.tvTapHint).visibility =
            if (isTerminal) View.GONE else View.VISIBLE
        view.alpha = 1.0f   // never dim — grayed-out cards are confusing UX

        return view
    }
}
