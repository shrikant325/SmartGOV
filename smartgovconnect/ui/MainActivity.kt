package com.example.smartgovconnect.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smartgovconnect.R
import com.example.smartgovconnect.adapter.SchemeAdapter
import com.example.smartgovconnect.firebase.FirebaseAuthManager
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.Scheme
import com.example.smartgovconnect.util.NotificationHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var authManager: FirebaseAuthManager
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var emptyMessage: TextView
    private lateinit var greetingText: TextView
    private lateinit var sectionLabel: TextView
    private lateinit var seeMoreBtn: MaterialButton
    private lateinit var searchInput: EditText
    private lateinit var clearSearchBtn: ImageView
    private lateinit var profileBtn: CardView
    private lateinit var profileInitial: TextView
    private lateinit var adapter: SchemeAdapter

    private var schemes = mutableListOf<Scheme>()
    private var userPriorities: List<String> = emptyList()
    private var userIncome: Long? = null
    private var userState: String = ""
    private var isSearching = false

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        authManager = FirebaseAuthManager()
        db = FirebaseFirestore.getInstance()

        if (!authManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        NotificationHelper.createChannel(this)
        ensureNotificationPermission()

        recyclerView = findViewById(R.id.mySchemesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)
        emptyMessage = findViewById(R.id.emptyMessage)
        greetingText = findViewById(R.id.greetingText)
        sectionLabel = findViewById(R.id.sectionLabel)
        seeMoreBtn = findViewById(R.id.seeMoreBtn)
        searchInput = findViewById(R.id.searchInput)
        clearSearchBtn = findViewById(R.id.clearSearchBtn)
        profileBtn = findViewById(R.id.profileBtn)
        profileInitial = findViewById(R.id.profileInitial)

        adapter = SchemeAdapter(schemes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        seeMoreBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AllSchemesActivity::class.java
                )
            )
        }

        profileBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )
        }

        setupSearch()
        loadUserAndSchemes()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::db.isInitialized &&
            authManager.isLoggedIn() &&
            !isSearching
        ) {
            loadUserAndSchemes()
        }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                    val query =
                        s?.toString()?.trim() ?: ""

                    clearSearchBtn.visibility =
                        if (query.isEmpty()) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }

                    if (query.isEmpty()) {
                        isSearching = false
                        sectionLabel.text = "Recommended For You"
                        seeMoreBtn.visibility = View.VISIBLE
                        loadUserAndSchemes()
                    } else {
                        isSearching = true
                        seeMoreBtn.visibility = View.GONE
                        performSearch(query)
                    }
                }
            }
        )

        clearSearchBtn.setOnClickListener {
            searchInput.text.clear()
        }
    }

    private fun performSearch(query: String) {
        val results = schemes
            .filter { scheme ->
                scheme.title.contains(query, true) ||
                        scheme.domain.contains(query, true) ||
                        Domains.labelOf(scheme.domain)
                            .contains(query, true) ||
                        scheme.state.contains(query, true)
            }
            .distinctBy { it.id }

        sectionLabel.text = "Search Results (${results.size})"

        if (results.isEmpty()) {
            showEmpty(
                "No matching schemes found in your available schemes."
            )
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            adapter.updateSchemes(results)
        }
    }

    private fun loadUserAndSchemes() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.GONE

            try {
                val currentUser =
                    auth.currentUser
                        ?: return@launch

                val userDoc =
                    db.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()

                val name =
                    userDoc.getString("name")
                        ?: "there"

                userState =
                    userDoc.getString("state")
                        ?.trim()
                        ?: ""

                greetingText.text =
                    "Hi, $name 👋"

                profileInitial.text =
                    name.take(1).uppercase()

                userPriorities =
                    userDoc.get("priorities")
                            as? List<String>
                        ?: emptyList()

                userIncome =
                    userDoc.getLong("income")

                NotificationHelper.checkForNewSchemes(
                    this@MainActivity,
                    currentUser.uid,
                    userState
                )

                if (userPriorities.isNotEmpty()) {
                    loadMatchingSchemes()
                } else {
                    showEmpty(
                        "No priorities selected yet. Go to your profile to pick some, or tap 'See All Schemes' to browse everything."
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                showEmpty(
                    "Something went wrong. Please try again."
                )
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun loadMatchingSchemes() {
        schemes.clear()

        try {
            val snapshot =
                db.collection("schemes")
                    .get()
                    .await()

            val seenIds = mutableSetOf<String>()

            for (doc in snapshot.documents) {
                if (!seenIds.add(doc.id)) {
                    continue
                }

                val domain =
                    doc.getString("domain")
                        ?.trim()
                        ?: ""

                val level =
                    doc.getString("level")


                        
                        ?.trim()
                        ?: "central"

                val schemeState =
                    doc.getString("state")
                        ?.trim()
                        ?: ""

                val incomeCap =
                    doc.getLong("incomeCap")
                        ?: -1L

                val priorityMatches =
                    userPriorities.any { priority ->
                        priority.trim().equals(
                            domain,
                            ignoreCase = true
                        )
                    }

                if (!priorityMatches) {
                    continue
                }

                val locationMatches =
                    if (level.equals("central", true)) {
                        true
                    } else if (level.equals("state", true)) {
                        userState.isNotBlank() &&
                                schemeState.isNotBlank() &&
                                schemeState.equals(
                                    userState,
                                    ignoreCase = true
                                )
                    } else {
                        false
                    }

                if (!locationMatches) {
                    continue
                }

                if (Domains.isIncomeDomain(domain)) {
                    val income =
                        userIncome
                            ?: continue

                    if (
                        incomeCap >= 0 &&
                        income > incomeCap
                    ) {
                        continue
                    }
                }

                schemes.add(
                    Scheme(
                        id = doc.id,
                        title =
                            doc.getString("title")
                                ?: "",
                        domain = domain,
                        level = level,
                        state = schemeState,
                        eligibility =
                            doc.getString("eligibility")
                                ?: "",
                        benefits =
                            doc.getString("benefits")
                                ?: "",
                        applyLink =
                            doc.getString("applyLink")
                                ?: "",
                        imageUrl =
                            doc.getString("imageUrl")
                                ?: "",
                        incomeCap = incomeCap,
                        createdAt =
                            doc.getLong("createdAt")
                                ?: 0L
                    )
                )
            }

            schemes = schemes
                .distinctBy { it.id }
                .sortedByDescending {
                    it.createdAt
                }
                .toMutableList()

            if (schemes.isEmpty()) {
                val incomeMissing =
                    userPriorities.any {
                        Domains.isIncomeDomain(it)
                    } && userIncome == null

                if (incomeMissing) {
                    showEmpty(
                        "Add your annual income in your Profile to see income-based schemes."
                    )
                } else {
                    showEmpty(
                        if (userState.isBlank()) {
                            "Please select your state in your Profile to see state-based schemes."
                        } else {
                            "No schemes found for your selected priorities and state."
                        }
                    )
                }
            } else {
                recyclerView.visibility =
                    View.VISIBLE

                emptyState.visibility =
                    View.GONE

                sectionLabel.text =
                    "Recommended For You"

                adapter.updateSchemes(schemes)
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@MainActivity,
                "Failed to load schemes: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            showEmpty(
                "Unable to load schemes. Please try again."
            )
        }
    }

    private fun showEmpty(message: String) {
        recyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        emptyMessage.text = message
    }

    private fun navigateToLogin() {
        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
        finish()
    }
}