package com.example.dentalinkmobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.payments.model.PaymentItem
import com.example.dentalinkmobile.utils.formatPeso
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class AdminPaymentsActivity : AppCompatActivity() {

    private lateinit var lvPayments: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_payments)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        lvPayments = findViewById(R.id.lvAdminPayments)
        tvEmpty    = findViewById(R.id.tvAdminPaymentsEmpty)
    }

    override fun onResume() {
        super.onResume()
        loadPayments()
    }

    private fun loadPayments() {
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
                    lvPayments.adapter    = AdminPaymentAdapter(this@AdminPaymentsActivity, payments)
                } else {
                    Toast.makeText(this@AdminPaymentsActivity, "Failed to load payments (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPaymentsActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private class AdminPaymentAdapter(
    context: Context,
    private val items: List<PaymentItem>
) : ArrayAdapter<PaymentItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_admin_payment, parent, false)

        val item = items[position]

        view.findViewById<TextView>(R.id.tvPayApptId).text =
            "Appt #${item.appointmentId ?: "?"}"

        val patientName = if (item.patient != null)
            "${item.patient.firstName} ${item.patient.lastName}" else "Unknown"
        view.findViewById<TextView>(R.id.tvPayPatientName).text = patientName

        view.findViewById<TextView>(R.id.tvPayServiceName).text =
            item.serviceName ?: "Unknown Service"

        val amount = item.paymentAmount?.let { formatPeso(it) } ?: "N/A"
        view.findViewById<TextView>(R.id.tvPayAmount).text = amount

        view.findViewById<TextView>(R.id.tvPayRef).text =
            "Ref: ${item.paymongoPaymentId ?: "N/A"}"

        val tvStatus = view.findViewById<TextView>(R.id.tvPayStatus)
        val isPaid   = item.paymentStatus == "PAID"
        tvStatus.text = item.paymentStatus ?: "UNKNOWN"
        if (isPaid) {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_completed)
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_completed_text))
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_pending)
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_pending_text))
        }

        return view
    }
}