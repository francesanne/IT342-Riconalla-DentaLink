package com.example.dentalinkmobile.features.services.model

data class ServiceDto(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val imageUrl: String?
)