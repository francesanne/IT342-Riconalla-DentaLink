package com.example.dentalinkmobile.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

object ImageLoader {

    // Plain client — Supabase public URLs need no auth header
    private val client = OkHttpClient()

    suspend fun loadInto(url: String?, imageView: ImageView) {
        if (url.isNullOrBlank()) return
        val bitmap = fetchBitmap(url) ?: return
        withContext(Dispatchers.Main) {
            imageView.setImageBitmap(bitmap)
        }
    }

    suspend fun fetchBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()?.use { BitmapFactory.decodeStream(it) }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
