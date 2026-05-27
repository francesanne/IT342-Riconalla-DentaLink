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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PatientDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var tvUpcoming: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvPending: TextView
    private lateinit var lvUpcoming: ListView
    private lateinit var tvNoUpcoming: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        sessionManager = SessionManager(this)

        val toolbar  = findViewById<MaterialToolbar>(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView  = findViewById<NavigationView>(R.id.navView)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

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

        navView.setCheckedItem(R.id.nav_dashboard)

        tvUpcoming   = findViewById(R.id.tvUpcomingCount)
        tvCompleted  = findViewById(R.id.tvCompletedCount)
        tvPending    = findViewById(R.id.tvPendingCount)
        lvUpcoming   = findViewById(R.id.lvUpcomingAppointments)
        tvNoUpcoming = findViewById(R.id.tvNoUpcoming)

        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $firstName!"

        findViewById<Button>(R.id.btnBookAppointment).setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
        findViewById<Button>(R.id.btnClinicLocation).setOnClickListener {
            startActivity(Intent(this, ClinicDirectionsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun loadDashboard() {
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
                            upcomingList.take(3),
                            onPayClick    = { /* no pay action on dashboard preview */ },
                            onCancelClick = { /* no cancel action on dashboard preview */ }
                        )
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(
                    this@PatientDashboardActivity.findViewById(android.R.id.content),
                    "Failed to load appointments",
                    Snackbar.LENGTH_SHORT
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
