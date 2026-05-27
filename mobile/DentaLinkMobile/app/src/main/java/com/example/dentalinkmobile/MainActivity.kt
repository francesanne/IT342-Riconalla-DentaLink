package com.example.dentalinkmobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.auth.model.GoogleLoginRequest
import com.example.dentalinkmobile.features.auth.model.LoginRequest
import com.example.dentalinkmobile.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    // Registered before onCreate so it is ready before the Activity reaches STARTED state
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                sendGoogleTokenToBackend(idToken)
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Google sign-in failed: no ID token received", Snackbar.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Snackbar.make(findViewById(android.R.id.content), "Google sign-in error (code ${e.statusCode})", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            navigateToDashboard()
            return
        }

        // ── Google Sign-In setup ──
        // requestIdToken must use the Web Client ID so the backend can verify the token audience
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogleSignIn = findViewById<SignInButton>(R.id.btnGoogleSignIn)
        btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE)

        btnGoogleSignIn.setOnClickListener {
            // Sign out any cached account first so the account picker always shows
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        // ── Email / password login (unchanged) ──
        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_fill_all_fields), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.login(LoginRequest(email, password))

                    if (response.isSuccessful) {
                        val body        = response.body()
                        val accessToken = body?.data?.accessToken
                        val user        = body?.data?.user

                        if (accessToken != null && user != null) {
                            sessionManager.saveToken(accessToken)
                            sessionManager.saveUserInfo(user.role, user.firstName)
                            navigateToDashboard()
                        } else {
                            Snackbar.make(this@MainActivity.findViewById(android.R.id.content), getString(R.string.error_invalid_response), Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(this@MainActivity.findViewById(android.R.id.content), getString(R.string.error_invalid_credentials), Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(this@MainActivity.findViewById(android.R.id.content), getString(R.string.error_network_prefix) + e.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        findViewById<Button>(R.id.btnGoRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun sendGoogleTokenToBackend(idToken: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.googleLogin(GoogleLoginRequest(idToken))

                if (response.isSuccessful) {
                    val body        = response.body()
                    val accessToken = body?.data?.accessToken
                    val user        = body?.data?.user

                    if (accessToken != null && user != null) {
                        sessionManager.saveToken(accessToken)
                        sessionManager.saveUserInfo(user.role, user.firstName)
                        navigateToDashboard()
                    } else {
                        Snackbar.make(this@MainActivity.findViewById(android.R.id.content), getString(R.string.error_invalid_response), Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    val code = response.code()
                    val msg  = if (code == 401) "Google account not recognized. Please try again."
                               else "Google login failed ($code)"
                    Snackbar.make(this@MainActivity.findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@MainActivity.findViewById(android.R.id.content), getString(R.string.error_network_prefix) + e.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToDashboard() {
        val role   = sessionManager.getRole()
        val target = if (role == "ADMIN") AdminDashboardActivity::class.java
                     else PatientDashboardActivity::class.java
        val intent = Intent(this, target)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
