package com.example.dentalinkmobile.features.auth.model

import com.example.dentalinkmobile.features.auth.model.UserData

/**
 * AuthResponseDto — SDD §5.3 + Locked Rule
 * Backend field is "accessToken" (NOT "token").
 * Nested inside ApiResponse<AuthResponseDto>.data.
 * Shape: { "user": UserData, "accessToken": string }
 */
data class AuthResponseDto(
    val user: UserData,
    val accessToken: String
)