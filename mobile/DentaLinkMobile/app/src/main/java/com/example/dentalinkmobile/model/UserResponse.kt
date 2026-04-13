package com.example.dentalinkmobile.model

data class UserData(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val profileImageUrl: String?
)

data class UserResponse(
    val success: Boolean,
    val data: UserData
)