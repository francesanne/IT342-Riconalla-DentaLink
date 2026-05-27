package com.example.dentalinkmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.appointments.model.CreateAppointmentRequest
import com.example.dentalinkmobile.features.appointments.model.CreateIntentRequest
import com.example.dentalinkmobile.features.dentists.model.DentistDto
import com.example.dentalinkmobile.utils.formatPeso
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private var serviceId: Long = 0
    private val dentistList = mutableListOf<DentistDto>()

    private val weekdaySlots = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
        "11:00", "11:30", "13:00", "13:30", "14:00", "14:30",
        "15:00", "15:30", "16:00", "16:30", "17:00"
    )
    private val saturdaySlots = weekdaySlots.filter { it >= "09:00" }
    private var timeSlots = weekdaySlots

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        serviceId = intent.getLongExtra("SERVICE_ID", 0)
        val serviceName  = intent.getStringExtra("SERVICE_NAME") ?: ""
        val servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        val tvServiceName  = findViewById<TextView>(R.id.tvBookingServiceName)
        val tvServicePrice = findViewById<TextView>(R.id.tvBookingServicePrice)
        val spinnerDentist = findViewById<Spinner>(R.id.spinnerDentist)
        val datePicker     = findViewById<DatePicker>(R.id.datePicker)
        val spinnerTime    = findViewById<Spinner>(R.id.spinnerTime)
        val btnConfirm      = findViewById<Button>(R.id.btnConfirmBooking)
        val btnCancel       = findViewById<Button>(R.id.btnCancelBooking)
        val progressBooking = findViewById<ProgressBar>(R.id.progressBooking)

        tvServiceName.text  = serviceName
        tvServicePrice.text = formatPeso(servicePrice)

        // Enforce minimum date = today
        val today = Calendar.getInstance()
        datePicker.minDate = today.timeInMillis

        fun updateTimeSlots() {
            val cal = Calendar.getInstance()
            cal.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            timeSlots = if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) saturdaySlots else weekdaySlots
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTime.adapter = adapter
        }

        // Time slot spinner — initialize based on current date
        updateTimeSlots()

        datePicker.setOnDateChangedListener { _, _, _, _ -> updateTimeSlots() }

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
                Snackbar.make(this@BookingActivity.findViewById(android.R.id.content), "Failed to load dentists", Snackbar.LENGTH_SHORT).show()
            }
        }

        btnConfirm.setOnClickListener {
            // Validate before disabling — early returns keep button enabled
            val selectedCal = Calendar.getInstance()
            selectedCal.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            if (selectedCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "The clinic is closed on Sundays. Please select another date.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (dentistList.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "No dentists available", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dentistId = dentistList[spinnerDentist.selectedItemPosition].id
            val year  = datePicker.year
            val month = datePicker.month + 1
            val day   = datePicker.dayOfMonth
            val time  = timeSlots[spinnerTime.selectedItemPosition]
            val appointmentDatetime = "%04d-%02d-%02dT%s:00".format(year, month, day, time)

            // Show loading state and disable button while the request is in-flight
            progressBooking.visibility = View.VISIBLE
            btnConfirm.isEnabled = false
            btnConfirm.text = "Processing…"

            lifecycleScope.launch {
                try {
                    confirmBooking(dentistId, appointmentDatetime)
                } finally {
                    progressBooking.visibility = View.GONE
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Confirm Booking"
                }
            }
        }

        btnCancel.setOnClickListener { finish() }
    }

    // suspend — called from within a coroutine; no internal launch needed
    private suspend fun confirmBooking(dentistId: Long, appointmentDatetime: String) {
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
                Snackbar.make(this@BookingActivity.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
                return
            }

            val appointmentId = apptResponse.body()?.data?.id
            if (appointmentId == null) {
                Snackbar.make(this@BookingActivity.findViewById(android.R.id.content), "Unexpected response from server", Snackbar.LENGTH_SHORT).show()
                return
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
                    Snackbar.make(this@BookingActivity.findViewById(android.R.id.content), "Checkout URL not received", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(this@BookingActivity.findViewById(android.R.id.content), "Failed to create payment. Try again.", Snackbar.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Snackbar.make(this@BookingActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }
}
