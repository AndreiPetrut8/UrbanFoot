package com.example.urbanfoot.data

import android.content.Context
import com.example.urbanfoot.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(context: Context? = null) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            return Result.failure(Exception("Please enter your full name"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address (e.g., user@example.com)"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long"))
        }

        return try {
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Failed to create user account"))

            val profileUpdates = userProfileChangeRequest {
                displayName = trimmedName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            val userId = firebaseUser.uid
            val user = User(
                userId = userId,
                name = trimmedName,
                email = trimmedEmail,
                rating = 0.0f
            )

            firestore.collection("users").document(userId).set(user).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Registration failed"))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address (e.g., user@example.com)"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Please enter your password"))
        }

        return try {
            val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Login failed"))

            val userId = firebaseUser.uid
            val user = fetchUserFromFirestore(userId) ?: User(
                userId = userId,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: trimmedEmail,
                rating = 0.0f
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Invalid email or password"))
        }
    }

    suspend fun fetchCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        try {
            firebaseUser.reload().await()
        } catch (_: Exception) {
            auth.signOut()
            return null
        }

        val userId = firebaseUser.uid
        return fetchUserFromFirestore(userId) ?: User(
            userId = userId,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            rating = 0.0f
        )
    }

    private suspend fun fetchUserFromFirestore(userId: String): User? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: ""
                val email = snapshot.getString("email") ?: ""
                val rating = snapshot.getDouble("rating")?.toFloat() ?: 0.0f
                User(userId = userId, name = name, email = email, rating = rating)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            userId = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            rating = 0.0f
        )
    }

    fun logout() {
        auth.signOut()
    }
}
