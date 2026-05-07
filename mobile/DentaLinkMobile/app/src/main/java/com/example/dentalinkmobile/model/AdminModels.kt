package com.example.dentalinkmobile.model

// Request body for POST /services and PUT /services/{id}
data class ServiceRequest(
    val name: String,
    val description: String,
    val price: Double
)

// Request body for POST /dentists and PUT /dentists/{id}
data class DentistRequest(
    val name: String,
    val specialization: String,
    val status: String
)

// Request body for PUT /appointments/{id}/status
// Only COMPLETED and CANCELLED are allowed — CONFIRMED is webhook-only
data class UpdateStatusRequest(
    val status: String
)

// Nested patient info inside PaymentItem
data class PatientSummary(
    val id: Long,
    val firstName: String,
    val lastName: String
)

// One payment record returned by GET /payments
data class PaymentItem(
    val id: Long,
    val appointmentId: Long?,
    val paymongoPaymentId: String?,
    val paymentAmount: Double?,
    val paymentStatus: String?,
    val paymentCreatedAt: String?,
    val patient: PatientSummary?
)