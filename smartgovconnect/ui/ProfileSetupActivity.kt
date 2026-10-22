package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smartgovconnect.R
import com.example.smartgovconnect.firebase.FirebaseAuthManager
import com.example.smartgovconnect.model.Domains
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Used in TWO modes:
 *  - First-time (after Signup): editMode = false → picks priorities fresh, then goes to Home
 *  - Edit (from Profile screen): editMode = true → pre-checks existing priorities loaded
 *    from Firestore, saves the same way, but returns to Profile instead of Home
 *
 * If "Parent Income Based Scheme" is checked, an income input card appears
 * (shown/hidden live as the checkbox is toggled) and becomes required before saving.
 */
class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var authManager: FirebaseAuthManager
    private lateinit var db: FirebaseFirestore
    private lateinit var prioritiesContainer: LinearLayout
    private lateinit var selectedCountText: TextView
    private lateinit var incomeCard: CardView
    private lateinit var incomeInput: TextInputEditText
    private lateinit var saveBtn: MaterialButton
    private lateinit var progressBar: ProgressBar
    private var uid: String = ""
    private var editMode: Boolean = false

    private val checkboxes = mutableListOf<CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        uid = intent.getStringExtra("uid") ?: ""
        editMode = intent.getBooleanExtra("editMode", false)
        authManager = FirebaseAuthManager()
        db = FirebaseFirestore.getInstance()

        prioritiesContainer = findViewById(R.id.prioritiesContainer)
        selectedCountText = findViewById(R.id.selectedCountText)
        incomeCard = findViewById(R.id.incomeCard)
        incomeInput = findViewById(R.id.incomeInput)
        saveBtn = findViewById(R.id.saveBtn)
        progressBar = findViewById(R.id.progressBar)

        buildPriorityCards()
        saveBtn.setOnClickListener { onSaveClicked() }

        if (editMode) {
            saveBtn.text = "Save Changes"
            loadExisting()
        }
    }

    private fun buildPriorityCards() {
        val inflater = LayoutInflater.from(this)
        Domains.ALL.forEach { domain ->
            val checkBox = inflater.inflate(R.layout.item_priority_card, prioritiesContainer, false) as CheckBox
            checkBox.text = "${domain.emoji}   ${domain.label}"
            checkBox.tag = domain.key
            checkBox.setOnCheckedChangeListener { _, _ ->
                updateSelectedCount()
                updateIncomeCardVisibility()
            }
            checkboxes.add(checkBox)
            prioritiesContainer.addView(checkBox)
        }
        updateSelectedCount()
    }

    private fun updateIncomeCardVisibility() {
        val incomeDomainChecked = checkboxes.any {
            it.isChecked && Domains.isIncomeDomain(it.tag as String)
        }
        incomeCard.visibility = if (incomeDomainChecked) View.VISIBLE else View.GONE
    }

    private fun loadExisting() {
        lifecycleScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                val existing = doc.get("priorities") as? List<String> ?: emptyList()
                checkboxes.forEach { cb -> cb.isChecked = existing.contains(cb.tag as String) }
                updateSelectedCount()
                updateIncomeCardVisibility()

                val existingIncome = doc.getLong("income")
                if (existingIncome != null) {
                    incomeInput.setText(existingIncome.toString())
                }
            } catch (_: Exception) {
                // If this fails, screen just starts with nothing checked — not critical
            }
        }
    }

    private fun updateSelectedCount() {
        val count = checkboxes.count { it.isChecked }
        selectedCountText.text = when (count) {
            0 -> "Select at least 1 to continue"
            5 -> "All 5 selected — you'll see every scheme"
            else -> "$count selected"
        }
    }

    private fun onSaveClicked() {
        val selected = checkboxes.filter { it.isChecked }.map { it.tag as String }
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 priority", Toast.LENGTH_SHORT).show()
            return
        }

        val incomeDomainSelected = selected.any { Domains.isIncomeDomain(it) }
        var income: Long? = null

        if (incomeDomainSelected) {
            val incomeText = incomeInput.text.toString().trim()
            if (incomeText.isEmpty()) {
                Toast.makeText(this, "Please enter your annual income", Toast.LENGTH_SHORT).show()
                return
            }
            income = incomeText.toLongOrNull()
            if (income == null || income < 0) {
                Toast.makeText(this, "Enter a valid income amount", Toast.LENGTH_SHORT).show()
                return
            }
        }

        savePriorities(selected, income)
    }

    private fun savePriorities(priorities: List<String>, income: Long?) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            saveBtn.isEnabled = false

            val result = authManager.updatePriorities(uid, priorities, income)

            progressBar.visibility = View.GONE
            saveBtn.isEnabled = true

            result.onSuccess {
                if (editMode) {
                    Toast.makeText(this@ProfileSetupActivity, "Priorities updated!", Toast.LENGTH_SHORT).show()
                    finish() // back to Profile screen, which refreshes onResume
                } else {
                    startActivity(Intent(this@ProfileSetupActivity, MainActivity::class.java))
                    finish()
                }
            }
            result.onFailure { e ->
                Toast.makeText(this@ProfileSetupActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}