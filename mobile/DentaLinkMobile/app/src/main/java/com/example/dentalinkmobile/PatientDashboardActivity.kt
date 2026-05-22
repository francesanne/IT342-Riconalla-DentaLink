package com.example.dentalinkmobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class PatientDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        sessionManager = SessionManager(this)

        // ── Toolbar + Drawer ──
        val toolbar   = findViewById<MaterialToolbar>(R.id.toolbar)
        drawerLayout  = findViewById(R.id.drawerLayout)
        val navView   = findViewById<NavigationView>(R.id.navView)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Personalise the nav drawer header with the patient's name
        val firstName = sessionManager.getFirstName() ?: "Patient"
        navView.getHeaderView(0)
            ?.findViewById<TextView>(R.id.tvNavWelcome)
            ?.text = "Welcome, $firstName!"

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.nav_dashboard    -> { /* already here */ }
                R.id.nav_services     -> startActivity(Intent(this, ServicesActivity::class.java))
                R.id.nav_appointments -> startActivity(Intent(this, MyAppointmentsActivity::class.java))
                R.id.nav_profile      -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_directions   -> startActivity(Intent(this, ClinicDirectionsActivity::class.java))
                R.id.nav_logout       -> logout()
            }
            true
        }

        // Mark Dashboard as selected by default
        navView.setCheckedItem(R.id.nav_dashboard)

        // ── Views ──
        val tvWelcome    = findViewById<TextView>(R.id.tvWelcome)
        val tvUpcoming   = findViewById<TextView>(R.id.tvUpcomingCount)
        val tvCompleted  = findViewById<TextView>(R.id.tvCompletedCount)
        val tvPending    = findViewById<TextView>(R.id.tvPendingCount)
        val lvUpcoming   = findViewById<ListView>(R.id.lvUpcomingAppointments)
        val tvNoUpcoming = findViewById<TextView>(R.id.tvNoUpcoming)

        tvWelcome.text = "Welcome, $firstName!"

        // ── Quick action buttons ──
        findViewById<Button>(R.id.btnBookAppointment).setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
        findViewById<Button>(R.id.btnClinicLocation).setOnClickListener {
            startActivity(Intent(this, ClinicDirectionsActivity::class.java))
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
                    val pendingCount   = appointments.count {
                        it.paymentStatus == "UNPAID" && it.status != "CANCELLED"
                    }

                    tvUpcoming.text  = upcomingList.size.toString()
                    tvCompleted.text = completedCount.toString()
                    tvPending.text   = pendingCount.toString()

                    if (upcomingList.isEmpty()) {
                        tvNoUpcoming.visibility = View.VISIBLE
                        lvUpcoming.visibility   = View.GONE
                    } else {
                        tvNoUpcoming.visibility = View.GONE
                        lvUpcoming.visibility   = View.VISIBLE
                        lvUpcoming.adapter = AppointmentAdapter(
                            this@PatientDashboardActivity,
                            upcomingList.take(3)
                        ) { /* no tap action on dashboard preview */ }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PatientDashboardActivity,
                    "Failed to load appointments",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun logout() {
        sessionManager.clear()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun parseDateTime(dt: String?): Long {
        if (dt == null) return 0L
        return try {
            java.time.LocalDateTime.parse(dt.take(19))
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        } catch (e: Exception) { 0L }
    }
}
