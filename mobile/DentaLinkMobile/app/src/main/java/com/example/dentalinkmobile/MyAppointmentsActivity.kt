package com.example.dentalinkmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.AppointmentItem
import com.example.dentalinkmobile.model.CreateIntentRequest
import kotlinx.coroutines.launch

class MyAppointmentsActivity : AppCompatActivity() {

    private var allAppointments = listOf<AppointmentItem>()
    private var currentFilter   = "ALL"

    private lateinit var lvAppointments: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_appointments)

        lvAppointments = findViewById(R.id.lvAppointments)
        tvEmpty        = findViewById(R.id.tvAppointmentsEmpty)

        // Filter buttons
        findViewById<Button>(R.id.btnFilterAll).setOnClickListener       { applyFilter("ALL") }
        findViewById<Button>(R.id.btnFilterConfirmed).setOnClickListener  { applyFilter("CONFIRMED") }
        findViewById<Button>(R.id.btnFilterPending).setOnClickListener    { applyFilter("PENDING_PAYMENT") }
        findViewById<Button>(R.id.btnFilterCompleted).setOnClickListener  { applyFilter("COMPLETED") }
        findViewById<Button>(R.id.btnFilterCancelled).setOnClickListener  { applyFilter("CANCELLED") }

        loadAppointments()
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAppointments()
                if (response.isSuccessful) {
                    allAppointments = response.body()?.data ?: emptyList()
                    applyFilter(currentFilter)
                } else {
                    Toast.makeText(this@MyAppointmentsActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyAppointmentsActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        val filtered = if (filter == "ALL") allAppointments
        else allAppointments.filter { it.status == filter }

        if (filtered.isEmpty()) {
            tvEmpty.visibility        = View.VISIBLE
            lvAppointments.visibility = View.GONE
            return
        }

        tvEmpty.visibility        = View.GONE
        lvAppointments.visibility = View.VISIBLE

        val labels = filtered.map { a ->
            val statusLabel   = a.status?.replace("_", " ") ?: "Unknown"
            val paymentLabel  = a.paymentStatus ?: ""
            val dateFormatted = formatDatetime(a.appointmentDatetime)
            "${a.serviceName ?: "Service"}\n${a.dentistName ?: "Dentist"} | $dateFormatted\n$statusLabel | $paymentLabel" +
                    if (a.paymentStatus == "UNPAID" && a.status != "CANCELLED") "  [Tap to Pay]" else ""
        }

        lvAppointments.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            labels
        )

        lvAppointments.setOnItemClickListener { _, _, position, _ ->
            val appointment = filtered[position]
            if (appointment.paymentStatus == "UNPAID" && appointment.status != "CANCELLED") {
                payForAppointment(appointment.id)
            }
        }
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
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)))
                    } else {
                        Toast.makeText(this@MyAppointmentsActivity, "Checkout URL not received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MyAppointmentsActivity, "Failed to initiate payment", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyAppointmentsActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDatetime(dt: String?): String {
        if (dt == null) return ""
        return try {
            val ldt = java.time.LocalDateTime.parse(dt.take(19))
            val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
            ldt.format(fmt)
        } catch (e: Exception) { dt }
    }
}