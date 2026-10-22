package com.example.smartgovconnect.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartgovconnect.R
import com.example.smartgovconnect.firebase.FirebaseAuthManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var emailInput: TextInputEditText
    private lateinit var sendResetBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        authManager = FirebaseAuthManager()
        emailInput = findViewById(R.id.resetEmailInput)
        sendResetBtn = findViewById(R.id.sendResetBtn)

        findViewById<View>(R.id.backToLoginLink).setOnClickListener {
            finish()
        }

        sendResetBtn.setOnClickListener {
            sendResetLink()
        }
    }

    private fun sendResetLink() {
        val email = emailInput.text?.toString()?.trim() ?: ""

        if (email.isEmpty()) {
            emailInput.error = "Enter your email"
            emailInput.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email"
            emailInput.requestFocus()
            return
        }

        lifecycleScope.launch {
            sendResetBtn.isEnabled = false
            sendResetBtn.text = "Sending..."

            val result = authManager.sendPasswordResetEmail(email)

            sendResetBtn.isEnabled = true
            sendResetBtn.text = "Send Reset Link"

            result.onSuccess {
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Password reset link sent!\nCheck your email, including Spam or Promotions if you don't see it in your inbox.",
                    Toast.LENGTH_LONG
                ).show()
            }

            result.onFailure {
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Unable to send reset link. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}