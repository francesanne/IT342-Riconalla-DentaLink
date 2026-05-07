package com.example.dentalinkmobile.api

import com.example.dentalinkmobile.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /** POST /api/v1/auth/register — SDD §5.3 — returns ApiResponse<AuthResponseDto> (201) */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponseDto>>

    /** POST /api/v1/auth/login — SDD §5.3 — returns ApiResponse<AuthResponseDto> (200) */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponseDto>>

    /** GET /api/v1/auth/me — SDD §5.3 — Bearer JWT injected by OkHttp interceptor (Step 3) */
    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<UserData>>
}