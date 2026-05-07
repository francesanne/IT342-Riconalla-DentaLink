package com.example.dentalinkmobile.model

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val currentPassword: String,
    val newPassword: String? = null
)