package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.smartgovconnect.R
import com.example.smartgovconnect.util.AdminSession

/**
 * Admin login is intentionally simple: the credentials are constants right here in
 * the code (not a Firestore-backed account). This matches how the admin link on the
 * Login screen is deliberately small/quiet — it's not meant for regular users to find
 * or use, just a way for YOU (the developer/admin) to get into the dashboard for
 * managing schemes and for your demo.
 *
 * To change the admin password, just edit the two constants below.
 */
class AdminLoginActivity : AppCompatActivity() {

    companion object {
        private const val ADMIN_EMAIL = "admin@smartgov.com"
        private const val ADMIN_PASSWORD = "Admin@123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        val emailInput = findViewById<TextInputEditText>(R.id.adminEmailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.adminPasswordInput)
        val loginBtn = findViewById<MaterialButton>(R.id.adminLoginBtn)
        val backLink = findViewById<android.widget.TextView>(R.id.backToUserLogin)

        loginBtn

            .setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
                AdminSession.setLoggedIn(this, true)
                startActivity(Intent(this, AdminHomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Incorrect admin credentials", Toast.LENGTH_SHORT).show()
            }
        }

        backLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}