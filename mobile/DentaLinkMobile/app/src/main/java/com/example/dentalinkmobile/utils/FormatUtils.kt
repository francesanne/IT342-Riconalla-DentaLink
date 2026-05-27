package com.example.dentalinkmobile.utils

fun formatPeso(amount: Double): String {
    val fmt = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "PH"))
    fmt.minimumFractionDigits = 2
    fmt.maximumFractionDigits = 2
    return "₱${fmt.format(amount)}"
}