package com.example.dentalinkmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.dashboard.model.RecentAppointment
import com.example.dentalinkmobile.utils.SessionManager
import com.example.dentalinkmobile.utils.formatPeso
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var tvTotal: TextView
    private lateinit var tvPending: TextView
    private lateinit var tvConfirmed: TextView
    private lateinit var tvRevenue: TextView
    private lateinit var lvRecent: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollContent: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

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

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.nav_admin_dashboard    -> { /* already here */ }
                R.id.nav_admin_services     -> startActivity(Intent(this, AdminServicesActivity::class.java))
                R.id.nav_admin_dentists     -> startActivity(Intent(this, AdminDentistsActivity::class.java))
                R.id.nav_admin_appointments -> startActivity(Intent(this, AdminAppointmentsActivity::class.java))
                R.id.nav_admin_payments     -> startActivity(Intent(this, AdminPaymentsActivity::class.java))
                R.id.nav_admin_logout       -> logout()
            }
            true
        }
        navView.setCheckedItem(R.id.nav_admin_dashboard)

        tvTotal      = findViewById(R.id.tvTotalAppointments)
        tvPending    = findViewById(R.id.tvPendingPayments)
        tvConfirmed  = findViewById(R.id.tvConfirmedAppointments)
        tvRevenue    = findViewById(R.id.tvTotalRevenue)
        lvRecent     = findViewById(R.id.lvRecentAppointments)
        progressBar  = findViewById(R.id.progressBar)
        scrollContent = findViewById(R.id.scrollContent)

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
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun loadDashboard() {
        progressBar.visibility   = View.VISIBLE
        scrollContent.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAdminDashboard()
                if (response.isSuccessful) {
                    val stats = response.body()?.data ?: return@launch

                    tvTotal.text     = stats.totalAppointments.toString()
                    tvPending.text   = stats.pendingPayments.toString()
                    tvConfirmed.text = stats.confirmedAppointments.toString()
                    tvRevenue.text   = formatPeso(stats.totalRevenue)

                    lvRecent.adapter = RecentAppointmentAdapter(
                        this@AdminDashboardActivity,
                        stats.recentAppointments,
                        ::formatDatetime
                    )
                } else {
                    Snackbar.make(this@AdminDashboardActivity.findViewById(android.R.id.content), "Failed to load dashboard", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@AdminDashboardActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility   = View.GONE
                scrollContent.visibility = View.VISIBLE
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

    private fun formatDatetime(dt: String?): String {
        if (dt == null) return ""
        return try {
            val ldt = java.time.LocalDateTime.parse(dt.take(19))
            ldt.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))
        } catch (e: Exception) { dt }
    }
}

private class RecentAppointmentAdapter(
    context: Context,
    private val items: List<RecentAppointment>,
    private val fmt: (String?) -> String
) : ArrayAdapter<RecentAppointment>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_recent_appointment, parent, false)

        val item = items[position]

        view.findViewById<TextView>(R.id.tvRecentPatient).text  = item.patientName ?: "Patient"
        view.findViewById<TextView>(R.id.tvRecentDentist).text  = if (item.dentistName != null) "Dr. ${item.dentistName}" else "Unknown Dentist"
        view.findViewById<TextView>(R.id.tvRecentDatetime).text = fmt(item.appointmentDatetime)

        val tvStatus = view.findViewById<TextView>(R.id.tvRecentStatus)
        tvStatus.text = (item.appointmentStatus ?: "").replace("_", " ")
        when (item.appointmentStatus) {
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

        return view
    }
}