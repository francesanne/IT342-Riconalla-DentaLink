package com.example.dentalinkmobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.LoginRequest
import com.example.dentalinkmobile.utils.SessionManager
import kotlinx.coroutines.launch
import com.example.dentalinkmobile.RegisterActivity
class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoRegister = findViewById<Button>(R.id.btnGoRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.login(
                        LoginRequest(email, password)
                    )

                    if (response.isSuccessful) {
                        val token = response.body()?.token

                        if (token != null) {
                            sessionManager.saveToken(token)

                            Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                            // 👉 TEMP: just stay here or go to dashboard later
                        } else {
                            Toast.makeText(this@MainActivity, "Invalid response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}