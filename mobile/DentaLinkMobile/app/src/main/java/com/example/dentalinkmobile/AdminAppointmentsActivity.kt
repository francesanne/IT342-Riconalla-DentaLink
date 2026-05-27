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
import kotlinx.coroutines.launch

class AdminAppointmentsActivity : AppCompatActivity() {

    private var allAppointments = listOf<AppointmentItem>()
    private lateinit var lvAppointments: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_appointments)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        lvAppointments = findViewById(R.id.lvAdminAppointments)
        tvEmpty        = findViewById(R.id.tvAdminAppointmentsEmpty)
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
                    renderList()
                } else {
                    Toast.makeText(this@AdminAppointmentsActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAppointmentsActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderList() {
        if (allAppointments.isEmpty()) {
            tvEmpty.visibility        = View.VISIBLE
            lvAppointments.visibility = View.GONE
            return
        }
        tvEmpty.visibility        = View.GONE
        lvAppointments.visibility = View.VISIBLE

        lvAppointments.adapter = AdminAppointmentAdapter(this, allAppointments, ::formatDatetime)
        lvAppointments.setOnItemClickListener { _, _, position, _ ->
            showStatusOptions(allAppointments[position])
        }
    }

    private fun showStatusOptions(appointment: AppointmentItem) {
        val currentStatus = appointment.status ?: ""

        if (currentStatus == "COMPLETED" || currentStatus == "CANCELLED") {
            Toast.makeText(this, "This appointment is already $currentStatus", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf("Mark Completed", "Mark Cancelled")
        AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setMessage("${appointment.serviceName} | ${formatDatetime(appointment.appointmentDatetime)}")
            .setItems(options) { _, which ->
                val newStatus = if (which == 0) "COMPLETED" else "CANCELLED"
                updateStatus(appointment.id, newStatus)
            }
            .show()
    }

    private fun updateStatus(id: Long, status: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateAppointmentStatus(
                    id, UpdateStatusRequest(status)
                )
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminAppointmentsActivity, "Status updated to $status", Toast.LENGTH_SHORT).show()
                    loadAppointments()
                } else {
                    Toast.makeText(this@AdminAppointmentsActivity, "Failed to update status (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAppointmentsActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
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

        view.findViewById<TextView>(R.id.tvAdminApptDentist).text  = item.dentistName ?: ""
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

        val isTerminal = item.status == "COMPLETED" || item.status == "CANCELLED"
        view.alpha = if (isTerminal) 0.6f else 1.0f

        return view
    }
}