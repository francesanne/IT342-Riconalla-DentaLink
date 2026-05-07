package com.example.dentalinkmobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.utils.SessionManager
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        sessionManager = SessionManager(this)

        val tvTotal       = findViewById<TextView>(R.id.tvTotalAppointments)
        val tvPending     = findViewById<TextView>(R.id.tvPendingPayments)
        val tvConfirmed   = findViewById<TextView>(R.id.tvConfirmedAppointments)
        val tvRevenue     = findViewById<TextView>(R.id.tvTotalRevenue)
        val lvRecent      = findViewById<ListView>(R.id.lvRecentAppointments)

        findViewById<Button>(R.id.btnAdminServices).setOnClickListener {
            startActivity(Intent(this, AdminServicesActivity::class.java))
        }
        findViewById<Button>(R.id.btnAdminDentists).setOnClickListener {
            startActivity(Intent(this, AdminDentistsActivity::class.java))
        }
        findViewById<Button>(R.id.btnAdminAppointments).setOnClickListener {
            startActivity(Intent(this, AdminAppointmentsActivity::class.java))
        }
        findViewById<Button>(R.id.btnAdminPayments).setOnClickListener {
            startActivity(Intent(this, AdminPaymentsActivity::class.java))
        }
        findViewById<Button>(R.id.btnAdminLogout).setOnClickListener {
            sessionManager.clear()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        loadDashboard(tvTotal, tvPending, tvConfirmed, tvRevenue, lvRecent)
    }

    private fun loadDashboard(
        tvTotal: TextView,
        tvPending: TextView,
        tvConfirmed: TextView,
        tvRevenue: TextView,
        lvRecent: ListView
    ) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAdminDashboard()
                if (response.isSuccessful) {
                    val stats = response.body()?.data ?: return@launch

                    tvTotal.text     = stats.totalAppointments.toString()
                    tvPending.text   = stats.pendingPayments.toString()
                    tvConfirmed.text = stats.confirmedAppointments.toString()
                    tvRevenue.text   = "P${String.format("%.2f", stats.totalRevenue)}"

                    val items = stats.recentAppointments.map { r ->
                        "${r.patientName ?: "Patient"} | ${r.dentistName ?: "Dentist"}\n${formatDatetime(r.appointmentDatetime)} | ${r.appointmentStatus ?: ""}"
                    }
                    lvRecent.adapter = ArrayAdapter(
                        this@AdminDashboardActivity,
                        android.R.layout.simple_list_item_1,
                        items
                    )
                } else {
                    Toast.makeText(this@AdminDashboardActivity, "Failed to load dashboard", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminDashboardActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
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