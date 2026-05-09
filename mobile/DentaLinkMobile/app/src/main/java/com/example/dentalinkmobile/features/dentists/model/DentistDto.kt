package com.example.dentalinkmobile.features.dentists.model

data class DentistDto(
    val id: Long,
    val name: String,
    val specialization: String?,
    val status: String
)