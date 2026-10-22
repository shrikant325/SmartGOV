package com.example.smartgovconnect.firebase

import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String,
        age: String,
        state: String
    ): Result<FirebaseUser?> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            )?.await()

            user?.let {
                db.collection("users")
                    .document(it.uid)
                    .set(
                        mapOf(
                            "uid" to it.uid,
                            "name" to name,
                            "email" to it.email.orEmpty(),
                            "phone" to phone,
                            "age" to age,
                            "state" to state,
                            "priorities" to emptyList<String>(),
                            "profileSetupComplete" to false,
                            "createdAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser?> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://smartgovconnect.firebaseapp.com/resetpassword")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    "com.example.smartgovconnect",
                    false,
                    null
                )
                .build()

            auth.sendPasswordResetEmail(email, actionCodeSettings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun getUserData(uid: String): Result<Map<String, Any>?> {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            Result.success(doc.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePriorities(
        uid: String,
        priorities: List<String>,
        income: Long? = null
    ): Result<Void?> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "priorities" to priorities,
                "profileSetupComplete" to true
            )

            if (income != null) {
                updates["income"] = income
            }

            db.collection("users")
                .document(uid)
                .update(updates)
                .await()

            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(
                    IllegalStateException("No logged-in user found.")
                )

            val email = user.email
                ?: return Result.failure(
                    IllegalStateException("This account does not have an email.")
                )

            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()

            db.collection("users")
                .document(user.uid)
                .delete()
                .await()

            user.delete().await()
            auth.signOut()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}