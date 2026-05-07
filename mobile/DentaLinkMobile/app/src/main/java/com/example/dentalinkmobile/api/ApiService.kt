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
}