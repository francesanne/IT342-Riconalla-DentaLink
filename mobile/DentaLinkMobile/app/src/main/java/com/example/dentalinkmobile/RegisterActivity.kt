package com.example.dentalinkmobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.RegisterRequest
import com.example.dentalinkmobile.utils.SessionManager
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
                Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, getString(R.string.error_passwords_no_match), Toast.LENGTH_SHORT).show()
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

                            Toast.makeText(this@RegisterActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()

                            // Navigate to login so user explicitly authenticates after registration
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, getString(R.string.error_invalid_response), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 409 = email already registered (SDD §5.3); 400 = validation failed
                        val errorMsg = when (response.code()) {
                            409 -> "Email is already registered."
                            400 -> "Invalid input. Please check your details."
                            else -> getString(R.string.error_registration_failed)
                        }
                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, getString(R.string.error_network_prefix) + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        btnGoLogin.setOnClickListener {
            finish() // back to MainActivity (login screen)
        }
    }
}