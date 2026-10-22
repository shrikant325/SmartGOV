package com.example.smartgovconnect.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartgovconnect.R
import com.example.smartgovconnect.firebase.FirebaseAuthManager
import com.example.smartgovconnect.model.States
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var authManager: FirebaseAuthManager

    private lateinit var nameInput: TextInputEditText
    private lateinit var ageInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var stateInput: AutoCompleteTextView
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText

    private lateinit var signupBtn: MaterialButton
    private lateinit var loginLink: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        authManager = FirebaseAuthManager()

        scrollView = findViewById(R.id.signupScrollView)
        nameInput = findViewById(R.id.nameInput)
        ageInput = findViewById(R.id.ageInput)
        phoneInput = findViewById(R.id.phoneInput)
        stateInput = findViewById(R.id.stateInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)

        signupBtn = findViewById(R.id.signupBtn)
        loginLink = findViewById(R.id.loginLink)
        progressBar = findViewById(R.id.progressBar)

        setupStateDropdown()
        setupKeyboardHandling()

        signupBtn.setOnClickListener {
            attemptSignup()
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupStateDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            States.INDIA
        )

        stateInput.setAdapter(adapter)

        stateInput.threshold = 1

        stateInput.setOnClickListener {
            stateInput.showDropDown()
        }

        stateInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                stateInput.showDropDown()
            }
        }
    }

    private fun setupKeyboardHandling() {
        val fields: List<View> = listOf(
            nameInput,
            ageInput,
            phoneInput,
            stateInput,
            emailInput,
            passwordInput,
            confirmPasswordInput
        )

        fields.forEach { field ->
            field.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    scrollFieldIntoView(view)
                }
            }
        }

        scrollView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    val visibleRect = Rect()

                    scrollView.getWindowVisibleDisplayFrame(visibleRect)

                    val screenHeight = scrollView.rootView.height
                    val keyboardHeight =
                        screenHeight - visibleRect.bottom

                    if (keyboardHeight > screenHeight * 0.15f) {
                        currentFocus?.let { focusedView ->
                            scrollView.post {
                                scrollFieldIntoView(focusedView)
                            }
                        }
                    }
                }
            }
        )
    }

    private fun scrollFieldIntoView(view: View) {
        scrollView.postDelayed({

            val rect = Rect()

            view.getDrawingRect(rect)

            scrollView.offsetDescendantRectToMyCoords(
                view,
                rect
            )

            val topLimit = 40
            val bottomLimit = scrollView.height - 100

            when {
                rect.bottom > bottomLimit -> {
                    scrollView.smoothScrollBy(
                        0,
                        rect.bottom - bottomLimit + 40
                    )
                }

                rect.top < topLimit -> {
                    scrollView.smoothScrollBy(
                        0,
                        rect.top - topLimit - 40
                    )
                }
            }

        }, 150)
    }

    private fun attemptSignup() {

        val name = nameInput.text.toString().trim()
        val age = ageInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val state = stateInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        clearErrors()

        // NAME
        if (name.isEmpty()) {
            nameInput.error = "Please enter your name"
            nameInput.requestFocus()
            return
        }

        // AGE
        if (age.isEmpty()) {
            ageInput.error = "Please enter your age"
            ageInput.requestFocus()
            return
        }

        val ageValue = age.toIntOrNull()

        if (ageValue == null || ageValue < 1 || ageValue > 120) {
            ageInput.error = "Enter a valid age"
            ageInput.requestFocus()
            return
        }

        // PHONE
        if (phone.isEmpty()) {
            phoneInput.error = "Please enter your phone number"
            phoneInput.requestFocus()
            return
        }

        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            phoneInput.error = "Enter a valid 10-digit phone number"
            phoneInput.requestFocus()
            return
        }

        // STATE
        if (state.isEmpty()) {
            stateInput.error = "Please select your state"
            stateInput.requestFocus()
            stateInput.showDropDown()
            return
        }

        if (state !in States.INDIA) {
            stateInput.error = "Please select a valid state"
            stateInput.requestFocus()
            stateInput.showDropDown()
            return
        }

        // EMAIL
        if (email.isEmpty()) {
            emailInput.error = "Please enter your email"
            emailInput.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {
            emailInput.error = "Enter a valid email address"
            emailInput.requestFocus()
            return
        }

        // PASSWORD
        if (password.isEmpty()) {
            passwordInput.error = "Please enter a password"
            passwordInput.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            passwordInput.requestFocus()
            return
        }

        // CONFIRM PASSWORD
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.error = "Please confirm your password"
            confirmPasswordInput.requestFocus()
            return
        }

        if (password != confirmPassword) {
            confirmPasswordInput.error = "Passwords do not match"
            confirmPasswordInput.requestFocus()
            return
        }

        // ALL VALID
        lifecycleScope.launch {

            setLoading(true)

            val result = authManager.signUp(
                name = name,
                email = email,
                password = password,
                phone = phone,
                age = age,
                state = state
            )

            setLoading(false)

            result.onSuccess { user ->

                if (user != null) {

                    val intent = Intent(
                        this@SignupActivity,
                        ProfileSetupActivity::class.java
                    )

                    intent.putExtra(
                        "uid",
                        user.uid
                    )

                    startActivity(intent)
                    finish()
                }
            }

            result.onFailure { e ->

                Toast.makeText(
                    this@SignupActivity,
                    getSignupErrorMessage(e),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun clearErrors() {
        nameInput.error = null
        ageInput.error = null
        phoneInput.error = null
        stateInput.error = null
        emailInput.error = null
        passwordInput.error = null
        confirmPasswordInput.error = null
    }

    private fun getSignupErrorMessage(e: Throwable): String {

        val message = e.message?.lowercase() ?: ""

        return when {

            "email address is already in use" in message ||
                    "email-already-in-use" in message ->
                "This email is already registered. Please log in or use another email."

            "badly formatted" in message ||
                    "invalid-email" in message ->
                "Please enter a valid email address."

            "weak-password" in message ->
                "Password must be at least 6 characters."

            "network" in message ->
                "Network error. Please check your internet connection."

            else ->
                "Signup failed. Please try again."
        }
    }

    private fun setLoading(loading: Boolean) {

        progressBar.visibility =
            if (loading) View.VISIBLE else View.GONE

        signupBtn.isEnabled = !loading

        signupBtn.text =
            if (loading) "" else "Create Account"

        nameInput.isEnabled = !loading
        ageInput.isEnabled = !loading
        phoneInput.isEnabled = !loading
        stateInput.isEnabled = !loading
        emailInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
        confirmPasswordInput.isEnabled = !loading
    }
}