package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgovconnect.R
import com.example.smartgovconnect.util.OnboardingSession
import com.google.android.material.button.MaterialButton

class PrivacyPolicyActivity : AppCompatActivity() {

    private val policyText = """
        SmartGov Connect — Privacy Policy

        Last updated: 2026

        1. Information We Collect
        When you create an account, we collect your name, age, phone number, state, email address, and, if you use income-based scheme categories, your annual income. We also store the scheme categories (priorities) you select.

        2. How We Use Your Information
        Your information is used to match you with government schemes relevant to your profile and to notify you when a new matching scheme is published. Your income is used only to determine eligibility for income-based schemes.

        3. Where Your Data Is Stored
        Your account data is stored securely using Firebase.

        4. Data Sharing
        Your personal information is not sold or shared with advertisers.

        5. SMS and Notifications
        You may receive SMS or in-app notifications when relevant schemes are published.

        6. Your Rights
        You may update your priorities, income, and personal details from your Profile screen. You may also delete your account.

        7. Data Security
        Firebase security rules are used to protect your account data.

        8. Changes to This Policy
        This policy may be updated as the app evolves.

        9. Contact
        For questions about this policy or your data, please contact the app administrator.

        By checking "I agree" below and continuing, you acknowledge that you have read and understood this policy.
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        val policyTextView =
            findViewById<TextView>(R.id.policyText)

        val checkbox =
            findViewById<CheckBox>(R.id.agreeCheckbox)

        val getStartedBtn =
            findViewById<MaterialButton>(R.id.getStartedBtn)

        policyTextView.text = policyText

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            getStartedBtn.isEnabled = isChecked
        }

        getStartedBtn.setOnClickListener {
            OnboardingSession.setCompleted(this)

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }
    }
}