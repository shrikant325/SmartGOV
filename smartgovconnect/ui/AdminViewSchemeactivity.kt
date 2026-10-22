package com.example.smartgovconnect.ui

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smartgovconnect.R
import com.example.smartgovconnect.adapter.AdminSchemeAdapter
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.Scheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewSchemesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: LinearLayout
    private lateinit var emptyMessage: TextView
    private lateinit var countText: TextView
    private lateinit var backBtn: ImageView
    private lateinit var searchInput: EditText

    private lateinit var adapter: AdminSchemeAdapter
    private var allSchemes: MutableList<Scheme> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_view_schemes)

        db = FirebaseFirestore.getInstance()

        recyclerView =
            findViewById(R.id.adminSchemesRecyclerView)

        progressBar =
            findViewById(R.id.viewSchemesProgress)

        emptyState =
            findViewById(R.id.viewSchemesEmptyState)

        emptyMessage =
            findViewById(R.id.viewSchemesEmptyMessage)

        countText =
            findViewById(R.id.viewSchemesCountText)

        backBtn =
            findViewById(R.id.viewSchemesBackBtn)

        searchInput =
            findViewById(R.id.adminSearchInput)

        adapter =
            AdminSchemeAdapter(
                mutableListOf()
            ) { scheme ->
                confirmDelete(scheme)
            }

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        backBtn.setOnClickListener {
            finish()
        }

        setupSearch()
        loadSchemes()
    }

    override fun onResume() {
        super.onResume()
        loadSchemes()
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
                    applySearch(
                        s?.toString()
                            ?.trim()
                            ?: ""
                    )
                }
            }
        )
    }

    private fun applySearch(query: String) {
        val filtered =
            if (query.isEmpty()) {
                allSchemes
            } else {
                allSchemes.filter {
                    it.title.contains(
                        query,
                        ignoreCase = true
                    ) ||
                            Domains.labelOf(
                                it.domain
                            ).contains(
                                query,
                                ignoreCase = true
                            ) ||
                            it.state.contains(
                                query,
                                ignoreCase = true
                            )
                }
            }

        showList(filtered)
    }

    private fun loadSchemes() {
        lifecycleScope.launch {
            progressBar.visibility =
                View.VISIBLE

            try {
                val snapshot =
                    db.collection("schemes")
                        .get()
                        .await()

                allSchemes =
                    snapshot.documents.map { doc ->
                        Scheme(
                            id = doc.id,
                            title =
                                doc.getString(
                                    "title"
                                ) ?: "",
                            domain =
                                doc.getString(
                                    "domain"
                                ) ?: "",
                            level =
                                doc.getString(
                                    "level"
                                ) ?: "central",
                            state =
                                doc.getString(
                                    "state"
                                ) ?: "",
                            eligibility =
                                doc.getString(
                                    "eligibility"
                                ) ?: "",
                            benefits =
                                doc.getString(
                                    "benefits"
                                ) ?: "",
                            applyLink =
                                doc.getString(
                                    "applyLink"
                                ) ?: "",
                            imageUrl =
                                doc.getString(
                                    "imageUrl"
                                ) ?: "",
                            incomeCap =
                                doc.getLong(
                                    "incomeCap"
                                ) ?: -1L,
                            createdAt =
                                doc.getLong(
                                    "createdAt"
                                ) ?: 0L
                        )
                    }
                        .sortedByDescending {
                            it.createdAt
                        }
                        .toMutableList()

                countText.text =
                    "${allSchemes.size} Published Schemes"

                applySearch(
                    searchInput.text
                        .toString()
                        .trim()
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminViewSchemesActivity,
                    "Couldn't load: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility =
                    View.GONE
            }
        }
    }

    private fun showList(list: List<Scheme>) {
        if (list.isEmpty()) {
            recyclerView.visibility =
                View.GONE

            emptyState.visibility =
                View.VISIBLE

            emptyMessage.text =
                if (allSchemes.isEmpty()) {
                    "No schemes published yet"
                } else {
                    "No matches found"
                }
        } else {
            recyclerView.visibility =
                View.VISIBLE

            emptyState.visibility =
                View.GONE

            adapter.updateList(list)
        }
    }

    private fun confirmDelete(scheme: Scheme) {
        AlertDialog.Builder(this)
            .setTitle("Delete scheme?")
            .setMessage(
                "\"${scheme.title}\" will be removed for all users."
            )
            .setPositiveButton("Yes") { _, _ ->
                deleteScheme(scheme)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteScheme(scheme: Scheme) {
        lifecycleScope.launch {
            try {
                db.collection("schemes")
                    .document(scheme.id)
                    .delete()
                    .await()

                loadSchemes()
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminViewSchemesActivity,
                    "Delete failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}