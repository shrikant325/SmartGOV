package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.smartgovconnect.R
import com.example.smartgovconnect.util.AdminSession

class AdminHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        findViewById<CardView>(R.id.addSchemeOption).setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }

        findViewById<CardView>(R.id.viewSchemesOption).setOnClickListener {
            startActivity(Intent(this, AdminViewSchemesActivity::class.java))
        }

        findViewById<CardView>(R.id.viewUsersOption).setOnClickListener {
            startActivity(Intent(this, AdminViewUsersActivity::class.java))
        }

        findViewById<ImageView>(R.id.adminHomeLogoutBtn).setOnClickListener {
            confirmLogout()
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout?")
            .setMessage("Are you sure you want to logout of the admin panel?")
            .setPositiveButton("Yes") { _, _ ->
                AdminSession.setLoggedIn(this, false)
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }
}