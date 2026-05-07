package com.example.dentalinkmobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        lvAppointments.adapter    = AppointmentAdapter(this, filtered) { appointment ->
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
}

class AppointmentAdapter(
    context: Context,
    private val items: List<AppointmentItem>,
    private val onItemClick: (AppointmentItem) -> Unit
) : ArrayAdapter<AppointmentItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_appointment, parent, false)

        val item = items[position]

        view.findViewById<TextView>(R.id.tvApptServiceName).text  = item.serviceName ?: "Service"
        view.findViewById<TextView>(R.id.tvApptDentistName).text  = item.dentistName ?: ""
        view.findViewById<TextView>(R.id.tvApptDatetime).text     = formatDatetime(item.appointmentDatetime)

        val tvStatus = view.findViewById<TextView>(R.id.tvApptStatus)
        val statusLabel = item.status?.replace("_", " ") ?: ""
        tvStatus.text = statusLabel
        val (textColor, bgColor) = statusColors(context, item.status)
        tvStatus.setTextColor(textColor)
        tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(bgColor)

        val tvPayment = view.findViewById<TextView>(R.id.tvApptPaymentStatus)
        tvPayment.text = "Payment: ${item.paymentStatus ?: ""}"

        val tvPayNow = view.findViewById<TextView>(R.id.tvPayNow)
        tvPayNow.visibility = if (item.paymentStatus == "UNPAID" && item.status != "CANCELLED")
            View.VISIBLE else View.GONE

        view.setOnClickListener { onItemClick(item) }
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