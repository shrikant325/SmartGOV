package com.example.smartgovconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.smartgovconnect.R
import com.example.smartgovconnect.util.AdminSession
import com.example.smartgovconnect.util.OnboardingSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logoCard = findViewById<CardView>(R.id.logoCard)

        logoCard.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.fade_scale_in
            )
        )

        lifecycleScope.launch {
            delay(1400)
            routeUser()
        }
    }

    private fun routeUser() {
        if (AdminSession.isLoggedIn(this)) {
            goTo(AdminHomeActivity::class.java)
            return
        }

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            lifecycleScope.launch {
                try {
                    val document = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(firebaseUser.uid)
                        .get()
                        .await()

                    if (!document.exists()) {
                        FirebaseAuth.getInstance().signOut()
                        OnboardingSession.reset(this@SplashActivity)
                        goTo(OnboardingActivity::class.java)
                        return@launch
                    }

                    val profileComplete =
                        document.getBoolean("profileSetupComplete") ?: false

                    if (profileComplete) {
                        goTo(MainActivity::class.java)
                    } else {
                        val intent = Intent(
                            this@SplashActivity,
                            ProfileSetupActivity::class.java
                        )
                        intent.putExtra("uid", firebaseUser.uid)
                        startActivity(intent)
                        finish()
                    }
                } catch (_: Exception) {
                    goTo(MainActivity::class.java)
                }
            }
            return
        }

        if (!OnboardingSession.isCompleted(this)) {
            goTo(OnboardingActivity::class.java)
            return
        }

        goTo(LoginActivity::class.java)
    }

    private fun goTo(activity: Class<*>) {
        startActivity(
            Intent(this, activity)
        )
        finish()
    }
}