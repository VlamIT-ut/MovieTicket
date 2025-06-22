package com.example.movieticket.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import android.content.Context
import java.io.InputStream
import com.google.firebase.messaging.FirebaseMessaging
@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun updateProfile(
        displayName: String? = null,
        imageUri: Uri? = null,
        context: Context? = null,
        additionalInfo: Map<String, Any>? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val user = auth.currentUser ?: throw Exception("User not logged in")
                Log.d("ProfileViewModel", "Updating profile for user: ${user.uid}")

                // Convert image to Base64 if provided
                val imageBase64 = imageUri?.let { uri ->
                    context?.let { ctx ->
                        try {
                            val inputStream: InputStream? = ctx.contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                            val imageBytes = baos.toByteArray()
                            Base64.encodeToString(imageBytes, Base64.DEFAULT)
                        } catch (e: Exception) {
                            Log.e("ProfileViewModel", "Error converting image: ${e.message}")
                            throw e
                        }
                    }
                }

                // Save user data to Firestore
                val userData = mutableMapOf<String, Any>()
                if (imageBase64 != null) {
                    userData["profileImage"] = imageBase64
                }
                if (displayName != null) {
                    userData["displayName"] = displayName
                }
                if (additionalInfo != null) {
                    userData.putAll(additionalInfo)
                }

                if (userData.isNotEmpty()) {
                    Log.d("ProfileViewModel", "Saving user data to Firestore...")
                    try {
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData, com.google.firebase.firestore.SetOptions.merge())
                            .await()
                        Log.d("ProfileViewModel", "Successfully saved user data")
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Error saving to Firestore: ${e.message}")
                        throw Exception("Failed to save profile: ${e.message}")
                    }
                }

                // Update Firebase Auth profile
                if (displayName != null) {
                    try {
                        val profileUpdates = userProfileChangeRequest {
                            this.displayName = displayName
                        }
                        user.updateProfile(profileUpdates).await()
                        Log.d("ProfileViewModel", "Successfully updated Auth profile")
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Error updating Auth profile: ${e.message}")
                        throw e
                    }
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Update profile failed: ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val user = auth.currentUser ?: throw Exception("User not logged in")
                user.updatePassword(newPassword).await()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserData(onSuccess: (Map<String, Any>) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val user = auth.currentUser ?: throw Exception("User not logged in")
                Log.d("ProfileViewModel", "Fetching data for user: ${user.uid}")

                try {
                    val document = firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .await()

                    if (document.exists()) {
                        Log.d("ProfileViewModel", "User data found")
                        onSuccess(document.data ?: emptyMap())
                    } else {
                        Log.d("ProfileViewModel", "No user data found")
                        onSuccess(emptyMap())
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error fetching user data: ${e.message}")
                    throw Exception("Failed to fetch profile: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Get user data failed: ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun saveFcmTokenForUser(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(userId)
                    .update("fcmToken", token)
            }
        }
    }

    // Làm mới (xoá) FCM token khi đăng xuất
    fun refreshFcmTokenForUser(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId)
            .update("fcmToken", null)
    }
}
