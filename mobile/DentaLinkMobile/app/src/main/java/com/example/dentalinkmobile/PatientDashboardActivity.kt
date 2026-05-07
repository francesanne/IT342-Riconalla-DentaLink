package com.example.dentalinkmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.utils.SessionManager
import kotlinx.coroutines.launch

class PatientDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        sessionManager = SessionManager(this)

        val tvWelcome      = findViewById<TextView>(R.id.tvWelcome)
        val tvUpcoming     = findViewById<TextView>(R.id.tvUpcomingCount)
        val tvCompleted    = findViewById<TextView>(R.id.tvCompletedCount)
        val tvPending      = findViewById<TextView>(R.id.tvPendingCount)
        val btnBook        = findViewById<Button>(R.id.btnBookAppointment)
        val btnMyAppts     = findViewById<Button>(R.id.btnMyAppointments)
        val btnServices    = findViewById<Button>(R.id.btnServices)
        val btnProfile     = findViewById<Button>(R.id.btnProfile)
        val lvUpcoming     = findViewById<ListView>(R.id.lvUpcomingAppointments)
        val tvNoUpcoming   = findViewById<TextView>(R.id.tvNoUpcoming)

        tvWelcome.text = "Welcome, ${sessionManager.getFirstName() ?: "Patient"}!"

        btnBook.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
        btnMyAppts.setOnClickListener {
            startActivity(Intent(this, MyAppointmentsActivity::class.java))
        }
        btnServices.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnClinicLocation).setOnClickListener {
            // Opens Google Maps (or any installed maps app) at the clinic location.
            // Coordinates match the web frontend: CLINIC_LAT, CLINIC_LNG.
            val geoUri = Uri.parse("geo:10.24738412405074,123.8000086426953?q=10.24738412405074,123.8000086426953(DentaLink+Dental+Clinic)")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback: open in browser if no maps app is installed
                val browserUri = Uri.parse("https://www.google.com/maps?q=10.24738412405074,123.8000086426953")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }

        loadDashboard(tvUpcoming, tvCompleted, tvPending, lvUpcoming, tvNoUpcoming)
    }

    private fun loadDashboard(
        tvUpcoming: TextView,
        tvCompleted: TextView,
        tvPending: TextView,
        lvUpcoming: ListView,
        tvNoUpcoming: TextView
    ) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAppointments()
                if (response.isSuccessful) {
                    val appointments = response.body()?.data ?: emptyList()

                    val now = System.currentTimeMillis()

                    val upcomingList = appointments.filter { a ->
                        (a.status == "CONFIRMED" || a.status == "PENDING_PAYMENT") &&
                                parseDateTime(a.appointmentDatetime) >= now
                    }.sortedBy { parseDateTime(it.appointmentDatetime) }

                    val completedCount = appointments.count { it.status == "COMPLETED" }
                    val pendingCount   = appointments.count { it.paymentStatus == "UNPAID" && it.status != "CANCELLED" }

                    tvUpcoming.text  = upcomingList.size.toString()
                    tvCompleted.text = completedCount.toString()
                    tvPending.text   = pendingCount.toString()

                    if (upcomingList.isEmpty()) {
                        tvNoUpcoming.visibility = View.VISIBLE
                        lvUpcoming.visibility   = View.GONE
                    } else {
                        tvNoUpcoming.visibility = View.GONE
                        lvUpcoming.visibility   = View.VISIBLE
                        val items = upcomingList.take(3).map { a ->
                            "${a.serviceName ?: "Service"} - ${a.dentistName ?: "Dentist"}\n${formatDatetime(a.appointmentDatetime)} | ${a.status}"
                        }
                        lvUpcoming.adapter = ArrayAdapter(
                            this@PatientDashboardActivity,
                            android.R.layout.simple_list_item_1,
                            items
                        )
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@PatientDashboardActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseDateTime(dt: String?): Long {
        if (dt == null) return 0L
        return try {
            java.time.LocalDateTime.parse(dt.take(19))
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        } catch (e: Exception) { 0L }
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