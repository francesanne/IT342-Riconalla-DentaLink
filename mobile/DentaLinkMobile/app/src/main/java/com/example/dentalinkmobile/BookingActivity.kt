package com.example.dentalinkmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.CreateAppointmentRequest
import com.example.dentalinkmobile.model.CreateIntentRequest
import com.example.dentalinkmobile.model.DentistDto
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private var serviceId: Long = 0
    private val dentistList = mutableListOf<DentistDto>()

    // Time slots matching web frontend
    private val timeSlots = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
        "11:00", "11:30", "13:00", "13:30", "14:00", "14:30",
        "15:00", "15:30", "16:00", "16:30", "17:00"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        serviceId = intent.getLongExtra("SERVICE_ID", 0)
        val serviceName  = intent.getStringExtra("SERVICE_NAME") ?: ""
        val servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        val tvServiceName  = findViewById<TextView>(R.id.tvBookingServiceName)
        val tvServicePrice = findViewById<TextView>(R.id.tvBookingServicePrice)
        val spinnerDentist = findViewById<Spinner>(R.id.spinnerDentist)
        val datePicker     = findViewById<DatePicker>(R.id.datePicker)
        val spinnerTime    = findViewById<Spinner>(R.id.spinnerTime)
        val btnConfirm     = findViewById<Button>(R.id.btnConfirmBooking)
        val btnCancel      = findViewById<Button>(R.id.btnCancelBooking)

        tvServiceName.text  = serviceName
        tvServicePrice.text = "P${String.format("%.2f", servicePrice)}"

        // Enforce minimum date = today
        val today = Calendar.getInstance()
        datePicker.minDate = today.timeInMillis

        // Time slot spinner
        val timeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTime.adapter = timeAdapter

        // Load dentists
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getDentists()
                if (response.isSuccessful) {
                    val all = response.body()?.data ?: emptyList()
                    // Show only ACTIVE dentists in the booking form
                    val active = all.filter { it.status == "ACTIVE" }
                    dentistList.clear()
                    dentistList.addAll(active)

                    val names = active.map { "${it.name} - ${it.specialization ?: ""}" }
                    val dentistAdapter = ArrayAdapter(
                        this@BookingActivity,
                        android.R.layout.simple_spinner_item,
                        names
                    )
                    dentistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDentist.adapter = dentistAdapter
                }
            } catch (e: Exception) {
                Toast.makeText(this@BookingActivity, "Failed to load dentists", Toast.LENGTH_SHORT).show()
            }
        }

        btnConfirm.setOnClickListener {
            if (dentistList.isEmpty()) {
                Toast.makeText(this, "No dentists available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dentistId = dentistList[spinnerDentist.selectedItemPosition].id
            val year  = datePicker.year
            val month = datePicker.month + 1
            val day   = datePicker.dayOfMonth
            val time  = timeSlots[spinnerTime.selectedItemPosition]

            val appointmentDatetime = "%04d-%02d-%02dT%s:00".format(year, month, day, time)

            confirmBooking(dentistId, appointmentDatetime)
        }

        btnCancel.setOnClickListener { finish() }
    }

    private fun confirmBooking(dentistId: Long, appointmentDatetime: String) {
        lifecycleScope.launch {
            try {
                // Step 1: create appointment
                val apptResponse = RetrofitClient.apiService.createAppointment(
                    CreateAppointmentRequest(serviceId, dentistId, appointmentDatetime)
                )

                if (!apptResponse.isSuccessful) {
                    val code = apptResponse.code()
                    val msg = if (code == 409) {
                        "This dentist is already booked at that time. Please select a different slot."
                    } else {
                        "Booking failed (${code})"
                    }
                    Toast.makeText(this@BookingActivity, msg, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val appointmentId = apptResponse.body()?.data?.id
                if (appointmentId == null) {
                    Toast.makeText(this@BookingActivity, "Unexpected response from server", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Step 2: create payment intent and open checkout
                val intentResponse = RetrofitClient.apiService.createPaymentIntent(
                    CreateIntentRequest(appointmentId)
                )

                if (intentResponse.isSuccessful) {
                    val checkoutUrl = intentResponse.body()?.data?.checkoutUrl
                    if (!checkoutUrl.isNullOrEmpty()) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                        startActivity(browserIntent)
                        finish()
                    } else {
                        Toast.makeText(this@BookingActivity, "Checkout URL not received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BookingActivity, "Failed to create payment. Try again.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@BookingActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}