package com.example.dentalinkmobile

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.profile.model.UpdateProfileRequest
import com.example.dentalinkmobile.utils.ImageLoader
import com.example.dentalinkmobile.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var ivProfilePicture: ImageView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            displayLocalImage(uri)
            uploadProfilePicture(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ivProfilePicture      = findViewById(R.id.ivProfilePicture)
        val btnChangePhoto    = findViewById<Button>(R.id.btnChangePhoto)
        val etFirstName       = findViewById<EditText>(R.id.etProfileFirstName)
        val etLastName        = findViewById<EditText>(R.id.etProfileLastName)
        val tvEmail           = findViewById<TextView>(R.id.tvProfileEmail)
        val etCurrentPassword = findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword     = findViewById<EditText>(R.id.etNewPassword)
        val btnSave           = findViewById<Button>(R.id.btnSaveProfile)
        val btnLogout         = findViewById<Button>(R.id.btnLogout)

        btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Load current profile from /auth/me
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMe()
                if (response.isSuccessful) {
                    val user = response.body()?.data
                    if (user != null) {
                        etFirstName.setText(user.firstName)
                        etLastName.setText(user.lastName)
                        tvEmail.text = user.email
                        // Load remote profile picture from Supabase if available
                        if (!user.profileImageUrl.isNullOrBlank()) {
                            ImageLoader.loadInto(user.profileImageUrl, ivProfilePicture)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }

        btnSave.setOnClickListener {
            val firstName       = etFirstName.text.toString().trim()
            val lastName        = etLastName.text.toString().trim()
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword     = etNewPassword.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(this, "First name, last name and current password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val request = UpdateProfileRequest(
                        firstName       = firstName,
                        lastName        = lastName,
                        currentPassword = currentPassword,
                        newPassword     = newPassword.ifEmpty { null }
                    )
                    val response = RetrofitClient.apiService.updateProfile(request)

                    if (response.isSuccessful) {
                        val updated = response.body()?.data
                        if (updated != null) {
                            sessionManager.saveUserInfo(updated.role, updated.firstName)
                        }
                        Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                        etCurrentPassword.text.clear()
                        etNewPassword.text.clear()
                    } else {
                        val code = response.code()
                        val msg = if (code == 400) "Incorrect current password or invalid input."
                        else "Failed to update profile ($code)"
                        Toast.makeText(this@ProfileActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnLogout.setOnClickListener {
            sessionManager.clear()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun displayLocalImage(uri: Uri) {
        try {
            val stream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream)
            stream?.close()
            if (bitmap != null) ivProfilePicture.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // silently ignore display errors — the upload may still succeed
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        lifecycleScope.launch {
            try {
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = contentResolver.openInputStream(uri)?.readBytes()
                    ?: return@launch

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "profile.jpg", requestBody)

                val response = RetrofitClient.apiService.uploadProfilePicture(part)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    // Reload the remote URL returned by the server so the displayed image
                    // reflects exactly what's stored (server may resize/process it)
                    val remoteUrl = response.body()?.data?.profileImageUrl
                    if (!remoteUrl.isNullOrBlank()) {
                        ImageLoader.loadInto(remoteUrl, ivProfilePicture)
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Upload failed (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Upload error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
