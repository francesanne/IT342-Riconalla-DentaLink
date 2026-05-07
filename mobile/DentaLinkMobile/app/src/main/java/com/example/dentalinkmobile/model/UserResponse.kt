package com.example.dentalinkmobile.model

/**
 * Generic API envelope — SDD §5.1
 * { "success": boolean, "data": T|null, "error": ErrorDetail|null, "timestamp": string }
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorDetail?,
    val timestamp: String?
)

data class ErrorDetail(
    val code: String?,
    val message: String?,
    val details: Any?
)

/**
 * UserData — matches UserDto from backend SDD §5.3 GET /auth/me
 * Fields: id, firstName, lastName, email, role, profileImageUrl
 */
data class UserData(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val profileImageUrl: String?
)