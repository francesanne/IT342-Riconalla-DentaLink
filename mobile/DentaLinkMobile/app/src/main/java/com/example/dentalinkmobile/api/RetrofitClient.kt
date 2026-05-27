package com.example.dentalinkmobile.api

import android.content.Context
import android.content.Intent
import com.example.dentalinkmobile.BuildConfig
import com.example.dentalinkmobile.MainActivity
import com.example.dentalinkmobile.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // BASE_URL is set at build time from local.properties or build.gradle.kts.
    // Change the IP in local.properties when DHCP reassigns your laptop's address.
    private val BASE_URL = BuildConfig.BASE_URL

    // Must call init(context) once from Application.onCreate() before any API call.
    private lateinit var sessionManager: SessionManager
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext     = context.applicationContext
        sessionManager = SessionManager(context.applicationContext)
    }

    /**
     * Auth interceptor: attaches "Authorization: Bearer <token>" on every request
     * that is not a public auth endpoint (register / login).
     * Public endpoints work without a token per SecurityConfig; sending one anyway
     * does no harm, but skipping it on auth calls avoids stale-token noise in logs.
     */
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val path = original.url.encodedPath

        val isPublicEndpoint = path.endsWith("/auth/register") ||
                path.endsWith("/auth/login") ||
                path.endsWith("/auth/google") ||
                path.endsWith("/payments/webhook")

        val request = if (!isPublicEndpoint && ::sessionManager.isInitialized) {
            val token = sessionManager.getToken()
            if (token != null) {
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                original
            }
        } else {
            original
        }

        chain.proceed(request)
    }

    /**
     * 401 interceptor: if the server returns 401 Unauthorized (JWT expired or invalid),
     * clear the local session and redirect the user to the login screen.
     * Uses the same ::isInitialized guard as authInterceptor so it is safe even
     * if a request fires before init() is called (shouldn't happen in practice).
     */
    private val unauthorizedInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (response.code == 401 && ::appContext.isInitialized) {
            SessionManager(appContext).clear()
            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            appContext.startActivity(intent)
        }
        response
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}