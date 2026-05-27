package com.example.dentalinkmobile

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var navView: NavigationView   // class-level so onResume can reset selection

    private lateinit var tvUpcoming: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvPending: TextView
    private lateinit var llUpcoming: LinearLayout
    private lateinit var tvNoUpcoming: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        sessionManager = SessionManager(this)

        val toolbar  = findViewById<MaterialToolbar>(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView      = findViewById(R.id.navView)

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

        tvUpcoming   = findViewById(R.id.tvUpcomingCount)
        tvCompleted  = findViewById(R.id.tvCompletedCount)
        tvPending    = findViewById(R.id.tvPendingCount)
        llUpcoming   = findViewById(R.id.llUpcomingAppointments)
        tvNoUpcoming = findViewById(R.id.tvNoUpcoming)

        findViewById<TextView>(R.id.tvWelcome).text = "Welcome, $firstName!"

        findViewById<Button>(R.id.btnBookAppointment).setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
        findViewById<Button>(R.id.btnClinicLocation).setOnClickListener {
            startActivity(Intent(this, ClinicDirectionsActivity::class.java))
        }
        findViewById<TextView>(R.id.tvViewAllAppointments).setOnClickListener {
            startActivity(Intent(this, MyAppointmentsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Always re-highlight Dashboard in the drawer — prevents the stale selection
        // that occurs when the user navigates to a sub-page and presses back.
        navView.setCheckedItem(R.id.nav_dashboard)
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
                        llUpcoming.visibility   = View.GONE
                    } else {
                        tvNoUpcoming.visibility = View.GONE
                        llUpcoming.visibility   = View.VISIBLE
                        llUpcoming.removeAllViews()

                        val inflater = LayoutInflater.from(this@PatientDashboardActivity)
                        upcomingList.take(3).forEach { appt ->
                            val v = inflater.inflate(R.layout.item_appointment, llUpcoming, false)

                            // Clear horizontal margins — the ScrollView container already
                            // applies screen_padding, so item's own margins cause double-indent
                            val lp = v.layoutParams as LinearLayout.LayoutParams
                            lp.marginStart = 0
                            lp.marginEnd   = 0
                            v.layoutParams = lp

                            v.findViewById<TextView>(R.id.tvApptServiceName).text =
                                appt.serviceName ?: "Service"
                            v.findViewById<TextView>(R.id.tvApptDentistName).text =
                                if (appt.dentistName != null) "Dr. ${appt.dentistName}" else "Unknown Dentist"
                            v.findViewById<TextView>(R.id.tvApptDatetime).text =
                                formatDatetime(appt.appointmentDatetime)

                            val tvStatus = v.findViewById<TextView>(R.id.tvApptStatus)
                            tvStatus.text = (appt.status ?: "").replace("_", " ")
                            val (textColor, bgColor) = statusColors(appt.status)
                            tvStatus.setTextColor(textColor)
                            tvStatus.backgroundTintList = ColorStateList.valueOf(bgColor)

                            v.findViewById<TextView>(R.id.tvApptPaymentStatus).text =
                                "Payment: ${appt.paymentStatus ?: ""}"

                            // Hide action buttons — dashboard is preview only
                            v.findViewById<TextView>(R.id.tvPayNow).visibility    = View.GONE
                            v.findViewById<TextView>(R.id.tvCancelAppt).visibility = View.GONE

                            llUpcoming.addView(v)
                        }
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

    private fun statusColors(status: String?): Pair<Int, Int> {
        return when (status) {
            "CONFIRMED"       -> Pair(
                ContextCompat.getColor(this, R.color.status_confirmed),
                ContextCompat.getColor(this, R.color.status_confirmed_bg)
            )
            "PENDING_PAYMENT" -> Pair(
                ContextCompat.getColor(this, R.color.status_pending),
                ContextCompat.getColor(this, R.color.status_pending_bg)
            )
            "COMPLETED"       -> Pair(
                ContextCompat.getColor(this, R.color.status_completed),
                ContextCompat.getColor(this, R.color.status_completed_bg)
            )
            "CANCELLED"       -> Pair(
                ContextCompat.getColor(this, R.color.status_cancelled),
                ContextCompat.getColor(this, R.color.status_cancelled_bg)
            )
            else -> Pair(
                ContextCompat.getColor(this, R.color.text_secondary),
                ContextCompat.getColor(this, R.color.surface_variant)
            )
        }
    }

    private fun formatDatetime(dt: String?): String {
        if (dt == null) return ""
        return try {
            val ldt = java.time.LocalDateTime.parse(dt.take(19))
            ldt.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))
        } catch (e: Exception) { dt }
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
