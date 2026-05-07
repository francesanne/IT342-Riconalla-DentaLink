package com.example.dentalinkmobile.api

import android.content.Context
import com.example.dentalinkmobile.BuildConfig
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

    fun init(context: Context) {
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

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
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