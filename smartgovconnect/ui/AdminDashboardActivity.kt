package com.example.smartgovconnect.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.smartgovconnect.R
import com.example.smartgovconnect.model.Domains
import com.example.smartgovconnect.model.IncomeBrackets
import com.example.smartgovconnect.model.Scheme
import com.example.smartgovconnect.model.States
import com.example.smartgovconnect.util.SmsAlertHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var scrollView: ScrollView
    private lateinit var backBtn: ImageView
    private lateinit var screenTitle: TextView
    private lateinit var titleInput: TextInputEditText
    private lateinit var eligibilityInput: TextInputEditText
    private lateinit var benefitsInput: TextInputEditText
    private lateinit var linkInput: TextInputEditText
    private lateinit var imageUrlInput: TextInputEditText
    private lateinit var imagePreview: ImageView
    private lateinit var domainSpinner: Spinner
    private lateinit var levelSpinner: Spinner
    private lateinit var stateContainer: LinearLayout
    private lateinit var stateSpinner: Spinner
    private lateinit var incomeBracketContainer: LinearLayout
    private lateinit var incomeBracketSpinner: Spinner
    private lateinit var addSchemeBtn: MaterialButton
    private lateinit var progressBar: ProgressBar

    private val domainLabels =
        Domains.ALL.map { "${it.emoji} ${it.label}" }

    private val levels =
        listOf("central", "state")

    private val incomeBracketLabels =
        IncomeBrackets.ALL.map { it.label }

    private var editMode = false
    private var editingSchemeId: String? = null

    private val smsPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    "SMS permission denied — scheme will still publish, but users won't get a text alert.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()

        scrollView =
            findViewById(R.id.adminDashboardScrollView)

        backBtn =
            findViewById(R.id.adminDashboardBackBtn)

        screenTitle =
            findViewById(R.id.adminDashboardTitle)

        titleInput =
            findViewById(R.id.titleInput)

        eligibilityInput =
            findViewById(R.id.eligibilityInput)

        benefitsInput =
            findViewById(R.id.benefitsInput)

        linkInput =
            findViewById(R.id.linkInput)

        imageUrlInput =
            findViewById(R.id.imageUrlInput)

        imagePreview =
            findViewById(R.id.imagePreview)

        domainSpinner =
            findViewById(R.id.domainSpinner)

        levelSpinner =
            findViewById(R.id.levelSpinner)

        stateContainer =
            findViewById(R.id.stateContainer)

        stateSpinner =
            findViewById(R.id.stateSpinner)

        incomeBracketContainer =
            findViewById(R.id.incomeBracketContainer)

        incomeBracketSpinner =
            findViewById(R.id.incomeBracketSpinner)

        addSchemeBtn =
            findViewById(R.id.addSchemeBtn)

        progressBar =
            findViewById(R.id.progressBar)

        setupSpinners()

        setupKeyboardHandling()

        backBtn.setOnClickListener {
            finish()
        }

        imageUrlInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                imageUrlInput.postDelayed({
                    ensureFieldVisible(imageUrlInput)
                }, 150)
            } else {
                updateImagePreview()
            }
        }

        editMode =
            intent.getBooleanExtra("editMode", false)

        if (editMode) {
            editingSchemeId =
                intent.getStringExtra("schemeId")

            prefillFromIntent()

            screenTitle.text =
                "Edit Scheme"

            addSchemeBtn.text =
                "Update Scheme"
        } else {
            if (!hasSmsPermission()) {
                smsPermissionLauncher.launch(
                    Manifest.permission.SEND_SMS
                )
            }
        }

        addSchemeBtn.setOnClickListener {
            saveScheme()
        }
    }

    private fun setupSpinners() {

        domainSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                domainLabels
            )

        levelSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                levels
            )

        stateSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                States.INDIA
            )

        incomeBracketSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                incomeBracketLabels
            )

        levelSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    stateContainer.visibility =
                        if (
                            levels[position]
                                .equals("state", true)
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        domainSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedKey =
                        Domains.ALL[position].key

                    incomeBracketContainer.visibility =
                        if (
                            Domains.isIncomeDomain(selectedKey)
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }
    }

    private fun setupKeyboardHandling() {

        val fields = listOf(
            titleInput,
            imageUrlInput,
            eligibilityInput,
            benefitsInput,
            linkInput
        )

        fields.forEach { field ->

            field.setOnFocusChangeListener { view, hasFocus ->

                if (hasFocus) {

                    view.postDelayed({
                        ensureFieldVisible(view)
                    }, 180)

                } else if (field == imageUrlInput) {

                    updateImagePreview()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            scrollView
        ) { view, insets ->

            val imeInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.ime()
                )

            val systemInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            val bottomPadding =
                maxOf(
                    imeInsets.bottom,
                    systemInsets.bottom
                ) + dp(32)

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                bottomPadding
            )

            if (imeInsets.bottom > 0) {

                currentFocus?.let { focusedView ->

                    if (
                        focusedView is TextInputEditText
                    ) {
                        focusedView.postDelayed({
                            ensureFieldVisible(
                                focusedView
                            )
                        }, 100)
                    }
                }
            }

            insets
        }

        ViewCompat.requestApplyInsets(scrollView)
    }

    private fun ensureFieldVisible(view: View) {

        scrollView.post {

            val rect = Rect()

            view.getDrawingRect(rect)

            scrollView.offsetDescendantRectToMyCoords(
                view,
                rect
            )

            val topLimit =
                scrollView.scrollY +
                        scrollView.paddingTop +
                        dp(24)

            val bottomLimit =
                scrollView.scrollY +
                        scrollView.height -
                        scrollView.paddingBottom -
                        dp(24)

            val extraSpace =
                dp(30)

            if (
                rect.bottom + extraSpace >
                bottomLimit
            ) {

                scrollView.smoothScrollBy(
                    0,
                    rect.bottom +
                            extraSpace -
                            bottomLimit
                )

            } else if (
                rect.top < topLimit
            ) {

                scrollView.smoothScrollBy(
                    0,
                    rect.top -
                            topLimit
                )
            }
        }
    }

    private fun dp(value: Int): Int {
        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }

    private fun prefillFromIntent() {

        titleInput.setText(
            intent.getStringExtra("title")
        )

        eligibilityInput.setText(
            intent.getStringExtra("eligibility")
        )

        benefitsInput.setText(
            intent.getStringExtra("benefits")
        )

        linkInput.setText(
            intent.getStringExtra("applyLink")
        )

        val imageUrl =
            intent.getStringExtra("imageUrl")
                ?: ""

        imageUrlInput.setText(imageUrl)

        if (imageUrl.isNotEmpty()) {

            imagePreview.visibility =
                View.VISIBLE

            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(imagePreview)
        }

        val domainKey =
            intent.getStringExtra("domain")
                ?: ""

        val domainIndex =
            Domains.ALL.indexOfFirst {
                it.key == domainKey
            }

        if (domainIndex >= 0) {
            domainSpinner.setSelection(
                domainIndex
            )
        }

        val level =
            intent.getStringExtra("level")
                ?: "central"

        val levelIndex =
            levels.indexOfFirst {
                it.equals(level, true)
            }

        if (levelIndex >= 0) {
            levelSpinner.setSelection(
                levelIndex
            )
        }

        val existingState =
            intent.getStringExtra("state")
                ?: ""

        stateContainer.visibility =
            if (
                level.equals("state", true)
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        val stateIndex =
            States.INDIA.indexOfFirst {
                it.equals(existingState, true)
            }

        if (stateIndex >= 0) {
            stateSpinner.setSelection(
                stateIndex
            )
        }

        incomeBracketContainer.visibility =
            if (
                Domains.isIncomeDomain(domainKey)
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        val incomeCap =
            intent.getLongExtra(
                "incomeCap",
                -1L
            )

        if (incomeCap >= 0) {

            val bracketIndex =
                IncomeBrackets.ALL.indexOfFirst {
                    it.cap == incomeCap
                }

            if (bracketIndex >= 0) {

                incomeBracketSpinner.setSelection(
                    bracketIndex
                )
            }
        }
    }

    private fun updateImagePreview() {

        val url =
            imageUrlInput.text
                .toString()
                .trim()

        if (url.isNotEmpty()) {

            imagePreview.visibility =
                View.VISIBLE

            Glide.with(this)
                .load(url)
                .centerCrop()
                .into(imagePreview)

        } else {

            imagePreview.visibility =
                View.GONE
        }
    }

    private fun saveScheme() {

        val title =
            titleInput.text
                .toString()
                .trim()

        val eligibility =
            eligibilityInput.text
                .toString()
                .trim()

        val benefits =
            benefitsInput.text
                .toString()
                .trim()

        val link =
            linkInput.text
                .toString()
                .trim()

        val imageUrl =
            imageUrlInput.text
                .toString()
                .trim()

        val domainKey =
            Domains.ALL[
                domainSpinner.selectedItemPosition
            ].key

        val level =
            levels[
                levelSpinner.selectedItemPosition
            ]

        if (
            title.isEmpty() ||
            eligibility.isEmpty() ||
            benefits.isEmpty() ||
            link.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Please fill all required fields",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val state =
            if (level.equals("state", true)) {
                States.INDIA[
                    stateSpinner.selectedItemPosition
                ]
            } else {
                ""
            }

        if (
            level.equals("state", true) &&
            state.isBlank()
        ) {

            Toast.makeText(
                this,
                "Please select a state",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val incomeCap =
            if (
                Domains.isIncomeDomain(domainKey)
            ) {

                IncomeBrackets.ALL[
                    incomeBracketSpinner
                        .selectedItemPosition
                ].cap

            } else {
                -1L
            }

        val data =
            mapOf(
                "title" to title,
                "domain" to domainKey,
                "level" to level,
                "state" to state,
                "eligibility" to eligibility,
                "benefits" to benefits,
                "applyLink" to link,
                "imageUrl" to imageUrl,
                "incomeCap" to incomeCap,
                "createdAt" to
                        System.currentTimeMillis()
            )

        lifecycleScope.launch {

            progressBar.visibility =
                View.VISIBLE

            addSchemeBtn.isEnabled =
                false

            try {

                if (
                    editMode &&
                    editingSchemeId != null
                ) {

                    db.collection("schemes")
                        .document(
                            editingSchemeId!!
                        )
                        .update(data)
                        .await()

                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Scheme updated!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    db.collection("schemes")
                        .add(data)
                        .await()

                    if (hasSmsPermission()) {

                        val schemeForSms =
                            Scheme(
                                title = title,
                                domain = domainKey,
                                level = level,
                                state = state,
                                eligibility = eligibility,
                                benefits = benefits,
                                applyLink = link,
                                imageUrl = imageUrl,
                                incomeCap = incomeCap
                            )

                        val sentCount =
                            SmsAlertHelper
                                .notifyMatchingUsers(
                                    schemeForSms
                                )

                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "Scheme published! SMS sent to $sentCount matching user(s).",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "Scheme published! (SMS alert skipped — permission not granted)",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    clearForm()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            } finally {

                progressBar.visibility =
                    View.GONE

                addSchemeBtn.isEnabled =
                    true
            }
        }
    }

    private fun clearForm() {

        titleInput.text?.clear()

        eligibilityInput.text?.clear()

        benefitsInput.text?.clear()

        linkInput.text?.clear()

        imageUrlInput.text?.clear()

        imagePreview.visibility =
            View.GONE

        domainSpinner.setSelection(0)

        levelSpinner.setSelection(0)

        stateSpinner.setSelection(0)

        incomeBracketSpinner.setSelection(0)

        scrollView.post {
            scrollView.smoothScrollTo(
                0,
                0
            )
        }

        titleInput.requestFocus()
    }
}