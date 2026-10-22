package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgovconnect.R
import com.example.smartgovconnect.util.OnboardingSession
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (OnboardingSession.isCompleted(this)) {
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )
            finish()
            return
        }

        setContentView(R.layout.activity_onboarding)

        findViewById<MaterialButton>(
            R.id.onboardingContinueBtn
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    PrivacyPolicyActivity::class.java
                )
            )
            finish()
        }
    }
}