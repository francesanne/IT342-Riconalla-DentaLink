package com.example.dentalinkmobile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.AppointmentItem
import com.example.dentalinkmobile.model.UpdateStatusRequest
import kotlinx.coroutines.launch

class AdminAppointmentsActivity : AppCompatActivity() {

    private var allAppointments = listOf<AppointmentItem>()
    private lateinit var lvAppointments: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_appointments)

        lvAppointments = findViewById(R.id.lvAdminAppointments)
        tvEmpty        = findViewById(R.id.tvAdminAppointmentsEmpty)

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

        val labels = allAppointments.map { a ->
            "${a.serviceName ?: "Service"} | ${a.dentistName ?: "Dentist"}\n${formatDatetime(a.appointmentDatetime)}\nStatus: ${a.status} | Payment: ${a.paymentStatus}"
        }
        lvAppointments.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)

        lvAppointments.setOnItemClickListener { _, _, position, _ ->
            showStatusOptions(allAppointments[position])
        }
    }

    private fun showStatusOptions(appointment: AppointmentItem) {
        val currentStatus = appointment.status ?: ""

        // Only show update option for statuses that admin can change to COMPLETED or CANCELLED
        // CONFIRMED is webhook-only, COMPLETED and CANCELLED are already terminal
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