package com.example.dentalinkmobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.UpdateProfileRequest
import com.example.dentalinkmobile.utils.SessionManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        val etFirstName       = findViewById<EditText>(R.id.etProfileFirstName)
        val etLastName        = findViewById<EditText>(R.id.etProfileLastName)
        val tvEmail           = findViewById<TextView>(R.id.tvProfileEmail)
        val etCurrentPassword = findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword     = findViewById<EditText>(R.id.etNewPassword)
        val btnSave           = findViewById<Button>(R.id.btnSaveProfile)
        val btnLogout         = findViewById<Button>(R.id.btnLogout)

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
}