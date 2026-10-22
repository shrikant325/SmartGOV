package com.example.smartgovconnect.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgovconnect.R
import com.example.smartgovconnect.model.Domains
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminUserDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var content: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_detail)

        db = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.userDetailProgressBar)
        content = findViewById(R.id.userDetailContent)

        findViewById<android.widget.ImageView>(R.id.adminUserDetailBackBtn).setOnClickListener {
            finish()
        }

        val userId = intent.getStringExtra("userId")

        if (userId.isNullOrBlank()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUser(userId)
    }

    private fun loadUser(userId: String) {
        progressBar.visibility = View.VISIBLE
        content.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doc = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (!doc.exists()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminUserDetailActivity,
                            "User profile not found",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    return@launch
                }

                val name = doc.getString("name") ?: "Unknown User"
                val email = doc.getString("email") ?: "Not available"
                val phone = doc.getString("phone") ?: "Not available"
                val age = doc.getString("age") ?: "Not available"
                val state = doc.getString("state") ?: "Not available"
                val income = doc.getLong("income")
                val priorities = doc.get("priorities") as? List<String> ?: emptyList()

                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.detailAvatar).text =
                        name.take(1).uppercase()

                    findViewById<TextView>(R.id.detailName).text = name
                    findViewById<TextView>(R.id.detailEmail).text = email
                    findViewById<TextView>(R.id.detailPhone).text = phone
                    findViewById<TextView>(R.id.detailAge).text = age
                    findViewById<TextView>(R.id.detailState).text = state

                    val incomeTextView =
                        findViewById<TextView>(R.id.detailIncome)

                    incomeTextView.text = if (income != null) {
                        "₹${"%,d".format(income)} / year"
                    } else {
                        "Not provided"
                    }

                    val prioritiesTextView =
                        findViewById<TextView>(R.id.detailPriorities)

                    prioritiesTextView.text = if (priorities.isEmpty()) {
                        "No priorities selected"
                    } else {
                        priorities.joinToString("\n") { key ->
                            "${Domains.emojiOf(key)}  ${Domains.labelOf(key)}"
                        }
                    }

                    progressBar.visibility = View.GONE
                    content.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@AdminUserDetailActivity,
                        "Failed to load user: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}