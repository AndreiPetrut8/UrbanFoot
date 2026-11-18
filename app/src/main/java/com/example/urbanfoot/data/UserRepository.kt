package com.example.urbanfoot.data

import android.content.Context
import android.content.SharedPreferences
import com.example.urbanfoot.model.User
import java.util.UUID

class UserRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("urbanfoot_db", Context.MODE_PRIVATE)

    fun registerUser(name: String, email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address (e.g., user@example.com)"))
        }

        val existingPassword = prefs.getString("email_$trimmedEmail", null)
        if (existingPassword != null) {
            return Result.failure(Exception("Email is already registered"))
        }

        val userId = UUID.randomUUID().toString()
        val user = User(
            userId = userId,
            name = name.trim(),
            email = trimmedEmail,
            rating = 0.0f
        )

        prefs.edit()
            .putString("email_$trimmedEmail", password)
            .putString("uid_email_$userId", trimmedEmail)
            .putString("user_name_$userId", user.name)
            .putString("user_email_$userId", user.email)
            .putFloat("user_rating_$userId", user.rating)
            .putString("current_user_id", userId)
            .apply()

        return Result.success(user)
    }

    fun loginUser(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address (e.g., user@example.com)"))
        }

        val savedPassword = prefs.getString("email_$trimmedEmail", null)
        if (savedPassword == null || savedPassword != password) {
            return Result.failure(Exception("Invalid email or password"))
        }

        val userId = prefs.all.entries.firstOrNull { it.key.startsWith("uid_email_") && it.value == trimmedEmail }?.key?.removePrefix("uid_email_")
            ?: return Result.failure(Exception("User not found"))

        prefs.edit().putString("current_user_id", userId).apply()

        val user = User(
            userId = userId,
            name = prefs.getString("user_name_$userId", "") ?: "",
            email = trimmedEmail,
            rating = prefs.getFloat("user_rating_$userId", 0.0f)
        )
        return Result.success(user)
    }

    fun clearDatabase() {
        prefs.edit().clear().apply()
    }

    fun getCurrentUser(): User? {
        val userId = prefs.getString("current_user_id", null) ?: return null
        val email = prefs.getString("user_email_$userId", null) ?: return null
        val name = prefs.getString("user_name_$userId", "") ?: ""
        val rating = prefs.getFloat("user_rating_$userId", 0.0f)
        return User(userId = userId, name = name, email = email, rating = rating)
    }

    fun logout() {
        prefs.edit().remove("current_user_id").apply()
    }
}
