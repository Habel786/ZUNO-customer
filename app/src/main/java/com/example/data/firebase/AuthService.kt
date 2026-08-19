package com.example.data.firebase

import android.util.Log
import com.example.data.model.Customer
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class AuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(
        name: String,
        mobile: String,
        email: String,
        password: String,
        city: String
    ): AuthResult<Customer> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Failed to create user account.")
            
            val customer = Customer(
                uid = user.uid,
                name = name.trim(),
                mobile = mobile.trim(),
                email = email.trim(),
                city = city.trim(),
                role = "customer",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Save customer document in Firestore customers/{uid}
            firestore.collection("customers")
                .document(user.uid)
                .set(customer.toMap())
                .await()

            Log.d("AuthService", "User registered and profile created: ${user.uid}")
            AuthResult.Success(customer)
        } catch (e: Exception) {
            Log.e("AuthService", "Sign up failed", e)
            AuthResult.Error(parseAuthError(e), e)
        }
    }

    suspend fun login(email: String, password: String): AuthResult<Customer> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("Failed to login.")

            // Fetch customer document from Firestore
            val doc = firestore.collection("customers")
                .document(user.uid)
                .get()
                .await()

            val customer = if (doc.exists()) {
                Customer(
                    uid = doc.getString("uid") ?: user.uid,
                    name = doc.getString("name") ?: user.displayName ?: "Valued Customer",
                    mobile = doc.getString("mobile") ?: "",
                    email = doc.getString("email") ?: user.email ?: email,
                    city = doc.getString("city") ?: "India",
                    role = doc.getString("role") ?: "customer",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } else {
                // If doc does not exist yet, create a default customer profile
                val newCustomer = Customer(
                    uid = user.uid,
                    name = user.displayName ?: "Valued Customer",
                    mobile = "",
                    email = user.email ?: email,
                    city = "India",
                    role = "customer"
                )
                firestore.collection("customers").document(user.uid).set(newCustomer.toMap()).await()
                newCustomer
            }

            Log.d("AuthService", "User logged in: ${user.uid}")
            AuthResult.Success(customer)
        } catch (e: Exception) {
            Log.e("AuthService", "Login failed", e)
            AuthResult.Error(parseAuthError(e), e)
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthService", "Password reset failed", e)
            AuthResult.Error(parseAuthError(e), e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun parseAuthError(e: Throwable): String {
        return when (e) {
            is FirebaseAuthUserCollisionException -> "This email is already registered. Please sign in instead."
            is FirebaseAuthInvalidUserException -> "No account found with this email address. Please sign up."
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password. Please check and try again."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            is FirebaseNetworkException -> "Network error. Please check your internet connection and try again."
            else -> e.localizedMessage ?: "Authentication failed. Please verify your details."
        }
    }
}
