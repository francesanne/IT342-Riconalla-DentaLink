package com.example.dentalinkmobile

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import kotlinx.coroutines.launch

class AdminPaymentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_payments)

        val lvPayments = findViewById<ListView>(R.id.lvAdminPayments)
        val tvEmpty    = findViewById<TextView>(R.id.tvAdminPaymentsEmpty)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPayments()
                if (response.isSuccessful) {
                    val payments = response.body()?.data ?: emptyList()

                    if (payments.isEmpty()) {
                        tvEmpty.visibility    = View.VISIBLE
                        lvPayments.visibility = View.GONE
                        return@launch
                    }

                    tvEmpty.visibility    = View.GONE
                    lvPayments.visibility = View.VISIBLE

                    val labels = payments.map { p ->
                        val patientName = if (p.patient != null)
                            "${p.patient.firstName} ${p.patient.lastName}" else "Unknown"
                        val amount = p.paymentAmount?.let { "P${String.format("%.2f", it)}" } ?: "N/A"
                        "Appt #${p.appointmentId} | $patientName\n$amount | ${p.paymentStatus ?: ""}\nRef: ${p.paymongoPaymentId ?: "N/A"}"
                    }
                    lvPayments.adapter = ArrayAdapter(
                        this@AdminPaymentsActivity,
                        android.R.layout.simple_list_item_1,
                        labels
                    )
                } else {
                    Toast.makeText(this@AdminPaymentsActivity, "Failed to load payments (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPaymentsActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}