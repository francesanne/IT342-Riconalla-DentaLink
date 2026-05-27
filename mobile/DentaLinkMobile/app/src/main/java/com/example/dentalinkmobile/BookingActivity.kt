package com.example.dentalinkmobile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.appointments.model.CreateAppointmentRequest
import com.example.dentalinkmobile.features.appointments.model.CreateIntentRequest
import com.example.dentalinkmobile.features.dentists.model.DentistDto
import com.example.dentalinkmobile.utils.formatPeso
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
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

    // Selected date state — initialized to today so form is ready immediately
    private val _today = Calendar.getInstance()
    private var selectedYear  = _today.get(Calendar.YEAR)
    private var selectedMonth = _today.get(Calendar.MONTH) + 1   // 1-based
    private var selectedDay   = _today.get(Calendar.DAY_OF_MONTH)

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

        val tvServiceName   = findViewById<TextView>(R.id.tvBookingServiceName)
        val tvServicePrice  = findViewById<TextView>(R.id.tvBookingServicePrice)
        val spinnerDentist  = findViewById<Spinner>(R.id.spinnerDentist)
        val rowSelectDate   = findViewById<LinearLayout>(R.id.rowSelectDate)
        val tvSelectedDate  = findViewById<TextView>(R.id.tvSelectedDate)
        val spinnerTime     = findViewById<Spinner>(R.id.spinnerTime)
        val btnConfirm      = findViewById<Button>(R.id.btnConfirmBooking)
        val btnCancel       = findViewById<Button>(R.id.btnCancelBooking)
        val progressBooking = findViewById<ProgressBar>(R.id.progressBooking)

        tvServiceName.text  = serviceName
        tvServicePrice.text = formatPeso(servicePrice)

        // ── Time slot spinner (updates whenever date changes) ──
        fun updateTimeSlots() {
            val cal = Calendar.getInstance()
            cal.set(selectedYear, selectedMonth - 1, selectedDay)   // Calendar months are 0-based
            timeSlots = if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) saturdaySlots else weekdaySlots
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTime.adapter = adapter
        }

        // Show today's date and populate slots immediately
        tvSelectedDate.text = formatDate(selectedYear, selectedMonth, selectedDay)
        tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        updateTimeSlots()

        // ── Date picker row — opens Material date picker dialog ──
        rowSelectDate.setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())   // disable past dates
                .build()

            // Seed selection with currently chosen date (UTC midnight)
            val initMs = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                set(selectedYear, selectedMonth - 1, selectedDay, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Appointment Date")
                .setSelection(initMs)
                .setCalendarConstraints(constraints)
                .build()

            picker.addOnPositiveButtonClickListener { selectionMs ->
                // MaterialDatePicker returns UTC midnight millis — extract with UTC calendar
                val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                utcCal.timeInMillis = selectionMs
                selectedYear  = utcCal.get(Calendar.YEAR)
                selectedMonth = utcCal.get(Calendar.MONTH) + 1
                selectedDay   = utcCal.get(Calendar.DAY_OF_MONTH)
                tvSelectedDate.text = formatDate(selectedYear, selectedMonth, selectedDay)
                tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                updateTimeSlots()
            }

            picker.show(supportFragmentManager, "date_picker")
        }

        // ── Load active dentists ──
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getDentists()
                if (response.isSuccessful) {
                    val all    = response.body()?.data ?: emptyList()
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
                Snackbar.make(
                    this@BookingActivity.findViewById(android.R.id.content),
                    "Failed to load dentists",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        // ── Confirm booking ──
        btnConfirm.setOnClickListener {
            // Validate: no Sundays
            val selectedCal = Calendar.getInstance()
            selectedCal.set(selectedYear, selectedMonth - 1, selectedDay)
            if (selectedCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "The clinic is closed on Sundays. Please select another date.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (dentistList.isEmpty()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "No dentists available",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Collect selections
            val selectedDentist     = dentistList[spinnerDentist.selectedItemPosition]
            val dentistId           = selectedDentist.id
            val dentistName         = selectedDentist.name
            val year                = selectedYear
            val month               = selectedMonth    // already 1-based
            val day                 = selectedDay
            val time                = timeSlots[spinnerTime.selectedItemPosition]
            val appointmentDatetime = "%04d-%02d-%02dT%s:00".format(year, month, day, time)

            val formattedDate = formatDate(year, month, day)
            val formattedTime = try {
                java.time.LocalTime.parse(time)
                    .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
            } catch (e: Exception) { time }

            // Confirmation summary dialog
            AlertDialog.Builder(this)
                .setTitle("Confirm Your Booking")
                .setMessage(
                    "Service: $serviceName\n" +
                    "Dentist: Dr. $dentistName\n" +
                    "Date: $formattedDate\n" +
                    "Time: $formattedTime\n" +
                    "Amount: ${formatPeso(servicePrice)}\n\n" +
                    "You will be redirected to complete payment."
                )
                .setPositiveButton("Proceed to Payment") { _, _ ->
                    progressBooking.visibility = View.VISIBLE
                    btnConfirm.isEnabled = false
                    btnConfirm.text = "Processing…"

                    lifecycleScope.launch {
                        try {
                            confirmBooking(dentistId, appointmentDatetime)
                        } finally {
                            progressBooking.visibility = View.GONE
                            btnConfirm.isEnabled = true
                            btnConfirm.text = "Book and Pay"
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnCancel.setOnClickListener { finish() }
    }

    /** Format a date (1-based month) to human-readable string. */
    private fun formatDate(year: Int, month: Int, day: Int): String {
        return try {
            java.time.LocalDate.of(year, month, day)
                .format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        } catch (e: Exception) { "$month/$day/$year" }
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
                Snackbar.make(
                    this@BookingActivity.findViewById(android.R.id.content),
                    msg,
                    Snackbar.LENGTH_LONG
                ).show()
                return
            }

            val appointmentId = apptResponse.body()?.data?.id
            if (appointmentId == null) {
                Snackbar.make(
                    this@BookingActivity.findViewById(android.R.id.content),
                    "Unexpected response from server",
                    Snackbar.LENGTH_SHORT
                ).show()
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
                    Snackbar.make(
                        this@BookingActivity.findViewById(android.R.id.content),
                        "Checkout URL not received",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    this@BookingActivity.findViewById(android.R.id.content),
                    "Failed to create payment. Try again.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {
            Snackbar.make(
                this@BookingActivity.findViewById(android.R.id.content),
                "Network error: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
