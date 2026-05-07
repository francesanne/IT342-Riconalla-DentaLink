package com.example.dentalinkmobile.api

import com.example.dentalinkmobile.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Auth ---

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponseDto>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponseDto>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<UserData>>

    // --- Services (public, no token needed) ---

    @GET("services")
    suspend fun getServices(): Response<ApiResponse<List<ServiceDto>>>

    // --- Dentists (requires JWT) ---

    @GET("dentists")
    suspend fun getDentists(): Response<ApiResponse<List<DentistDto>>>

    // --- Appointments (requires JWT) ---

    @POST("appointments")
    suspend fun createAppointment(@Body request: CreateAppointmentRequest): Response<ApiResponse<AppointmentItem>>

    @GET("appointments")
    suspend fun getAppointments(): Response<ApiResponse<List<AppointmentItem>>>

    // --- Payments (requires JWT) ---

    @POST("payments/create-intent")
    suspend fun createPaymentIntent(@Body request: CreateIntentRequest): Response<ApiResponse<CreateIntentResponse>>

    // --- Profile (requires JWT) ---

    @PUT("users/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserData>>

    // --- Admin dashboard ---

    @GET("admin/dashboard")
    suspend fun getAdminDashboard(): Response<ApiResponse<DashboardStats>>

    // --- Admin: services ---

    @POST("services")
    suspend fun createService(@Body request: ServiceRequest): Response<ApiResponse<ServiceDto>>

    @PUT("services/{id}")
    suspend fun updateService(@Path("id") id: Long, @Body request: ServiceRequest): Response<ApiResponse<ServiceDto>>

    @DELETE("services/{id}")
    suspend fun deleteService(@Path("id") id: Long): Response<ApiResponse<String>>

    // --- Admin: dentists ---

    @POST("dentists")
    suspend fun createDentist(@Body request: DentistRequest): Response<ApiResponse<DentistDto>>

    @PUT("dentists/{id}")
    suspend fun updateDentist(@Path("id") id: Long, @Body request: DentistRequest): Response<ApiResponse<DentistDto>>

    @DELETE("dentists/{id}")
    suspend fun deleteDentist(@Path("id") id: Long): Response<ApiResponse<String>>

    // --- Admin: appointment status update ---
    // Only COMPLETED and CANCELLED are accepted — CONFIRMED is webhook-only

    @PUT("appointments/{id}/status")
    suspend fun updateAppointmentStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<ApiResponse<AppointmentItem>>

    // --- Admin: payments (read-only) ---

    @GET("payments")
    suspend fun getPayments(): Response<ApiResponse<List<PaymentItem>>>
}