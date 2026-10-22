package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartgovconnect.R
import com.example.smartgovconnect.firebase.FirebaseAuthManager
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.States
import com.example.smartgovconnect.util.OnboardingSession
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var priorityChipsContainer: LinearLayout
    private lateinit var profileState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        authManager = FirebaseAuthManager()

        priorityChipsContainer = findViewById(R.id.priorityChipsContainer)
        profileState = findViewById(R.id.profileState)

        findViewById<ImageView>(R.id.profileBackBtn).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.editPrioritiesBtn).setOnClickListener {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            intent.putExtra("uid", auth.currentUser?.uid)
            intent.putExtra("editMode", true)
            startActivity(intent)
        }

        profileState.setOnClickListener {
            showStateSelectionDialog()
        }

        findViewById<MaterialButton>(R.id.profileLogoutBtn).setOnClickListener {
            confirmLogout()
        }

        findViewById<MaterialButton>(R.id.profileDeleteAccountBtn).setOnClickListener {
            confirmDeleteAccount()
        }

        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()

                val name = doc.getString("name")
                    ?: auth.currentUser?.displayName
                    ?: "User"

                val email = auth.currentUser?.email
                    ?: doc.getString("email")
                    ?: ""

                val age = doc.getString("age") ?: "-"
                val phone = doc.getString("phone") ?: "-"
                val state = doc.getString("state") ?: "-"
                val income = doc.getLong("income")
                val priorities = doc.get("priorities") as? List<String> ?: emptyList()

                findViewById<TextView>(R.id.profileName).text = name
                findViewById<TextView>(R.id.profileEmail).text =
                    if (email.isNotBlank()) email else "Email not available"
                findViewById<TextView>(R.id.avatarInitial).text = name.take(1).uppercase()
                findViewById<TextView>(R.id.profileAge).text = age
                findViewById<TextView>(R.id.profilePhone).text = phone

                profileState.text =
                    if (state == "-") "Select State  ✎" else "$state  ✎"

                val incomeRow = findViewById<LinearLayout>(R.id.incomeRow)

                if (income != null) {
                    incomeRow.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.profileIncome).text =
                        "₹${"%,d".format(income)} / year"
                } else {
                    incomeRow.visibility = View.GONE
                }

                renderPriorityChips(priorities)
            } catch (_: Exception) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Unable to load profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showStateSelectionDialog() {
        val uid = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                val currentState = doc.getString("state") ?: ""
                val states = States.INDIA
                var selectedIndex = states.indexOf(currentState)

                if (selectedIndex < 0) {
                    selectedIndex = 0
                }

                val dialog = AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("Select Your State")
                    .setSingleChoiceItems(
                        states.toTypedArray(),
                        selectedIndex
                    ) { _, which ->
                        selectedIndex = which
                    }
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Save", null)
                    .create()

                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val selectedState = states[selectedIndex]

                        lifecycleScope.launch {
                            try {
                                db.collection("users")
                                    .document(uid)
                                    .update("state", selectedState)
                                    .await()

                                profileState.text = "$selectedState  ✎"

                                Toast.makeText(
                                    this@ProfileActivity,
                                    "State updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                dialog.dismiss()
                            } catch (_: Exception) {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Failed to update state",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                dialog.show()
            } catch (_: Exception) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Unable to open state selection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun renderPriorityChips(priorities: List<String>) {
        priorityChipsContainer.removeAllViews()

        if (priorities.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No priorities selected yet — tap Edit to choose"
            tv.setTextColor(getColor(R.color.text_hint))
            tv.textSize = 13f
            priorityChipsContainer.addView(tv)
            return
        }

        priorities.forEach { key ->
            val tv = TextView(this)
            tv.text = "${Domains.emojiOf(key)}  ${Domains.labelOf(key)}"
            tv.setTextColor(getColor(R.color.text_primary))
            tv.textSize = 14f
            tv.setPadding(16, 14, 16, 14)
            tv.setBackgroundResource(R.drawable.bg_chip_selector)
            tv.isClickable = false

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 10
            tv.layoutParams = params
            priorityChipsContainer.addView(tv)
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log out?")
            .setMessage("You'll need to log in again to see your schemes.")
            .setPositiveButton("Yes, Logout") { _, _ ->
                OnboardingSession.reset(this)
                authManager.logout()

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account?")
            .setMessage(
                "This will permanently delete your account, profile data and saved information. This action cannot be undone."
            )
            .setPositiveButton("Continue") { _, _ ->
                showPasswordDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPasswordDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Enter your password"
        input.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(50, 10, 50, 0)
        container.addView(input)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirm Account Deletion")
            .setMessage("Enter your password to permanently delete your account.")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete Account", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val password = input.text.toString()

                if (password.isBlank()) {
                    input.error = "Password required"
                    return@setOnClickListener
                }

                dialog.dismiss()
                deleteAccount(password)
            }
        }

        dialog.show()
    }

    private fun deleteAccount(password: String) {
        lifecycleScope.launch {
            try {
                val result = authManager.deleteAccount(password)

                if (result.isSuccess) {
                    OnboardingSession.reset(this@ProfileActivity)

                    Toast.makeText(
                        this@ProfileActivity,
                        "Account deleted successfully",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(
                        Intent(
                            this@ProfileActivity,
                            LoginActivity::class.java
                        )
                    )
                    finishAffinity()
                } else {
                    val exception = result.exceptionOrNull()

                    Toast.makeText(
                        this@ProfileActivity,
                        when {
                            exception?.message?.contains(
                                "password",
                                ignoreCase = true
                            ) == true -> "Incorrect password"

                            exception?.message?.contains(
                                "recent",
                                ignoreCase = true
                            ) == true -> "Please log in again and try deleting your account."

                            else -> "Account deletion failed. Please try again."
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Account deletion failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}