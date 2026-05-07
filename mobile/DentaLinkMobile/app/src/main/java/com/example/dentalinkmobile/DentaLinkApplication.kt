package com.example.dentalinkmobile

import android.app.Application
import com.example.dentalinkmobile.api.RetrofitClient

class DentaLinkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialise RetrofitClient with application context so the
        // AuthInterceptor can access SessionManager for every API call.
        RetrofitClient.init(this)
    }
}