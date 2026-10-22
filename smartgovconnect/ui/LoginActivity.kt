package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartgovconnect.R
import com.example.smartgovconnect.firebase.FirebaseAuthManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginBtn: MaterialButton
    private lateinit var signupLink: TextView
    private lateinit var forgotPasswordLink: TextView
    private lateinit var adminLoginLink: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authManager = FirebaseAuthManager()
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginBtn = findViewById(R.id.loginBtn)
        signupLink = findViewById(R.id.signupLink)
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink)
        adminLoginLink = findViewById(R.id.adminLoginLink)
        progressBar = findViewById(R.id.progressBar)

        loginBtn.setOnClickListener {
            attemptLogin()
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        forgotPasswordLink.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        adminLoginLink.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = emailInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString()?.trim() ?: ""

        if (email.isEmpty()) {
            emailInput.error = "Enter email"
            emailInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Enter password"
            passwordInput.requestFocus()
            return
        }

        lifecycleScope.launch {
            setLoading(true)
            val result = authManager.login(email, password)
            setLoading(false)

            result.onSuccess { user ->
                if (user != null) {
                    navigateAfterLogin(user.uid)
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed. User not found.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            result.onFailure { e ->
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateAfterLogin(uid: String) {
        lifecycleScope.launch {
            val result = authManager.getUserData(uid)

            result.onSuccess { data ->
                if (data == null) {
                    Toast.makeText(
                        this@LoginActivity,
                        "User profile not found.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@onSuccess
                }

                val complete = data["profileSetupComplete"] as? Boolean ?: false

                if (complete) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    val intent = Intent(this@LoginActivity, ProfileSetupActivity::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                }

                finish()
            }

            result.onFailure { e ->
                Toast.makeText(
                    this@LoginActivity,
                    "Unable to load user profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        loginBtn.isEnabled = !loading
        loginBtn.text = if (loading) "" else "Log In"
    }
}  