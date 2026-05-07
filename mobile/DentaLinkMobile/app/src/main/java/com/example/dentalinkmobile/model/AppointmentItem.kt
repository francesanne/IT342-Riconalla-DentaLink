package com.example.dentalinkmobile.model

data class AppointmentItem(
    val id: Long,
    val serviceId: Long?,
    val dentistId: Long?,
    val serviceName: String?,
    val servicePrice: Double?,
    val dentistName: String?,
    val appointmentDatetime: String?,
    val status: String?,
    val paymentStatus: String?
)