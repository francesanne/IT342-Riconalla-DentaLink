package com.example.dentalinkmobile.model

data class CreateAppointmentRequest(
    val serviceId: Long,
    val dentistId: Long,
    val appointmentDatetime: String
)

data class CreateIntentRequest(
    val appointmentId: Long
)

data class CreateIntentResponse(
    val checkoutUrl: String?,
    val paymentIntentId: String?
)