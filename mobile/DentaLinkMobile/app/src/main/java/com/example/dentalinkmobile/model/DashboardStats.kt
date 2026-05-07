package com.example.dentalinkmobile.model

// Matches GET /admin/dashboard response data object
data class DashboardStats(
    val totalAppointments: Long,
    val pendingPayments: Long,
    val confirmedAppointments: Long,
    val totalRevenue: Double,
    val recentAppointments: List<RecentAppointment>
)

// Shape of each item in the recentAppointments list
data class RecentAppointment(
    val id: Long,
    val patientName: String?,
    val dentistName: String?,
    val appointmentDatetime: String?,
    val appointmentStatus: String?
)