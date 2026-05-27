package com.example.dentalinkmobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.auth.model.RegisterRequest
import com.example.dentalinkmobile.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)

        val etFirstName       = findViewById<EditText>(R.id.etFirstName)
        val etLastName        = findViewById<EditText>(R.id.etLastName)
        val etEmail           = findViewById<EditText>(R.id.etEmail)
        val etPassword        = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister       = findViewById<Button>(R.id.btnRegister)
        val btnGoLogin        = findViewById<Button>(R.id.btnGoLogin)

        btnRegister.setOnClickListener {
            val firstName       = etFirstName.text.toString().trim()
            val lastName        = etLastName.text.toString().trim()
            val email           = etEmail.text.toString().trim()
            val password        = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Client-side validation — SDD §2.4 (Basic form validation feedback)
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_fill_all_fields), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Snackbar.make(findViewById(android.R.id.content), "Password must be at least 8 characters.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_passwords_no_match), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.register(
                        RegisterRequest(firstName, lastName, email, password)
                    )

                    if (response.isSuccessful) {
                        // SDD §5.3 POST /auth/register — 201 Created
                        // Returns ApiResponse<AuthResponseDto> with user + accessToken
                        val body = response.body()
                        val accessToken = body?.data?.accessToken
                        val user = body?.data?.user

                        if (accessToken != null && user != null) {
                            sessionManager.saveToken(accessToken)
                            sessionManager.saveUserInfo(user.role, user.firstName)

                            Snackbar.make(this@RegisterActivity.findViewById(android.R.id.content), getString(R.string.register_success), Snackbar.LENGTH_SHORT).show()

                            // Navigate directly to patient dashboard — skip login screen
                            val intent = Intent(this@RegisterActivity, PatientDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Snackbar.make(this@RegisterActivity.findViewById(android.R.id.content), getString(R.string.error_invalid_response), Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        // 409 = email already registered (SDD §5.3); 400 = validation failed
                        val errorMsg = when (response.code()) {
                            409 -> "Email is already registered."
                            400 -> "Invalid input. Please check your details."
                            else -> getString(R.string.error_registration_failed)
                        }
                        Snackbar.make(this@RegisterActivity.findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Snackbar.make(this@RegisterActivity.findViewById(android.R.id.content), getString(R.string.error_network_prefix) + e.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        btnGoLogin.setOnClickListener {
            finish() // back to MainActivity (login screen)
        }
    }
}
