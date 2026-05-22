package com.example.dentalinkmobile.features.auth.model

/** Request body for POST /auth/google — SDD §5.3 */
data class GoogleLoginRequest(val idToken: String)
