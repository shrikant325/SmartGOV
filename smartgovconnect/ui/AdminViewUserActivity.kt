package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgovconnect.R
import com.example.smartgovconnect.adapter.AdminUserAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminViewUsersActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var userCountText: TextView
    private lateinit var emptyText: TextView
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_view_users)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.adminUsersRecyclerView)
        progressBar = findViewById(R.id.adminUsersProgressBar)
        userCountText = findViewById(R.id.adminUserCountText)
        emptyText = findViewById(R.id.adminUsersEmptyText)

        findViewById<android.widget.ImageView>(R.id.adminUsersBackBtn).setOnClickListener {
            finish()
        }

        adapter = AdminUserAdapter { user ->
            val intent = Intent(this, AdminUserDetailActivity::class.java)
            intent.putExtra("userId", user.uid)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = db.collection("users")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val users = snapshot.documents.map { doc ->
                    AdminUserAdapter.AdminUser(
                        uid = doc.id,
                        name = doc.getString("name") ?: "Unknown User",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        age = doc.getString("age") ?: "",
                        state = doc.getString("state") ?: "",
                        income = doc.getLong("income"),
                        priorities = doc.get("priorities") as? List<String> ?: emptyList()
                    )
                }

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (users.isEmpty()) {
                        userCountText.text = "0 registered users"
                        emptyText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        userCountText.text =
                            "${users.size} registered user${if (users.size == 1) "" else "s"}"
                        emptyText.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateUsers(users)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    Toast.makeText(
                        this@AdminViewUsersActivity,
                        "Failed to load users: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            loadUsers()
        }
    }
}