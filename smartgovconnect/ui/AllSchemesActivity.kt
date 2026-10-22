package com.example.smartgovconnect.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgovconnect.R
import com.example.smartgovconnect.adapter.SchemeAdapter
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.Scheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AllSchemesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var emptyMessage: TextView
    private lateinit var chipsContainer: LinearLayout
    private lateinit var backBtn: ImageView
    private lateinit var adapter: SchemeAdapter

    private var allSchemes: MutableList<Scheme> = mutableListOf()
    private val chips = mutableListOf<TextView>()

    private var selectedCategory = "All"
    private var userIncome: Long? = null
    private var userState: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_schemes)

        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.allSchemesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)
        emptyMessage = findViewById(R.id.emptyMessage)
        chipsContainer = findViewById(R.id.categoryChipsContainer)
        backBtn = findViewById(R.id.backBtn)

        adapter = SchemeAdapter(emptyList())

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        backBtn.setOnClickListener {
            finish()
        }

        buildCategoryChips()
        loadUserAndSchemes()
    }

    private fun buildCategoryChips() {
        val inflater = LayoutInflater.from(this)

        addChip(
            inflater,
            "All",
            "🗂 All"
        )

        Domains.ALL.forEach { domain ->
            addChip(
                inflater,
                domain.key,
                "${domain.emoji} ${domain.label}"
            )
        }

        if (chips.isNotEmpty()) {
            chips.first().isSelected = true
            selectedCategory = "All"
        }
    }

    private fun addChip(
        inflater: LayoutInflater,
        key: String,
        label: String
    ) {
        val chip = inflater.inflate(
            R.layout.item_category_chip,
            chipsContainer,
            false
        ) as TextView

        chip.text = label
        chip.tag = key

        chip.setOnClickListener {
            chips.forEach {
                it.isSelected = false
            }

            chip.isSelected = true
            selectedCategory = key

            applyFilter()
        }

        chips.add(chip)
        chipsContainer.addView(chip)
    }

    private fun loadUserAndSchemes() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.GONE

            try {
                val uid = FirebaseAuth.getInstance()
                    .currentUser
                    ?.uid

                if (uid == null) {
                    showEmpty("Please login again.")
                    return@launch
                }

                val userDoc = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                userIncome = userDoc.getLong("income")
                userState = userDoc.getString("state")?.trim() ?: ""

                val snapshot = db.collection("schemes")
                    .get()
                    .await()

                allSchemes = snapshot.documents
                    .mapNotNull { doc ->
                        try {
                            Scheme(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                domain = doc.getString("domain") ?: "",
                                level = doc.getString("level") ?: "",
                                state = doc.getString("state") ?: "",
                                eligibility = doc.getString("eligibility") ?: "",
                                benefits = doc.getString("benefits") ?: "",
                                applyLink = doc.getString("applyLink") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                incomeCap = doc.getLong("incomeCap") ?: -1L,
                                createdAt = doc.getLong("createdAt") ?: 0L
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAt }
                    .toMutableList()

                applyFilter()

            } catch (e: Exception) {
                showEmpty("Unable to load schemes. Please try again.")
                Toast.makeText(
                    this@AllSchemesActivity,
                    "Failed to load schemes",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun applyFilter() {
        val locationFiltered = allSchemes.filter { scheme ->
            if (scheme.level.equals("state", ignoreCase = true)) {
                scheme.state.equals(userState, ignoreCase = true)
            } else {
                true
            }
        }

        val categoryFiltered = if (selectedCategory == "All") {
            locationFiltered
        } else {
            locationFiltered.filter { scheme ->
                scheme.domain.equals(
                    selectedCategory,
                    ignoreCase = true
                )
            }
        }

        val incomeFiltered = categoryFiltered.filter { scheme ->
            if (!Domains.isIncomeDomain(scheme.domain)) {
                true
            } else {
                val income = userIncome

                if (income == null) {
                    false
                } else {
                    scheme.incomeCap < 0 ||
                            income <= scheme.incomeCap
                }
            }
        }

        val filtered = incomeFiltered.distinctBy { it.id }

        if (filtered.isEmpty()) {
            val incomeMissing =
                userIncome == null &&
                        categoryFiltered.any {
                            Domains.isIncomeDomain(it.domain)
                        }

            if (incomeMissing) {
                showEmpty(
                    "Add your annual income in your Profile to see income-based schemes here."
                )
            } else {
                showEmpty("No schemes in this category yet.")
            }

            return
        }

        recyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE

        adapter.updateList(filtered)
    }

    private fun showEmpty(message: String) {
        recyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        emptyMessage.text = message
    }
}