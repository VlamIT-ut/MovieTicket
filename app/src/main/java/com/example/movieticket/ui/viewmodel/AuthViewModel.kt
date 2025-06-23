package com.example.movieticket.ui.viewmodel

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
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    /* ---------- Auth state ---------- */
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        auth.addAuthStateListener { _isLoggedIn.value = it.currentUser != null }
    }

    /* ---------- Public getters ---------- */
    val currentUserName: String? get() = auth.currentUser?.displayName
    val currentUserEmail: String? get() = auth.currentUser?.email

    fun signOut() = auth.signOut()

    /* ---------- Register & set displayName ---------- */
    fun register(
        email: String,
        password: String,
        fullName: String,
        onResult: (Boolean, String?) -> Unit
    ) = viewModelScope.launch {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = auth.currentUser ?: throw Exception("Registration failed")

            /* 1. Cập nhật tên hiển thị vào FirebaseAuth */
            val profileUpdates = userProfileChangeRequest { displayName = fullName }
            user.updateProfile(profileUpdates).await()

            /* 2. Ghi Firestore */
            firestore.collection("users")
                .document(user.uid)
                .set(mapOf("displayName" to fullName))
                .await()

            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }

    /* ---------- Đổi tên hiển thị sau này ---------- */
    fun updateDisplayName(
        newName: String,
        onResult: (Boolean, String?) -> Unit
    ) = viewModelScope.launch {
        try {
            val user = auth.currentUser ?: throw Exception("Bạn chưa đăng nhập")
            user.updateProfile(userProfileChangeRequest { displayName = newName }).await()

            firestore.collection("users")
                .document(user.uid)
                .update("displayName", newName)
                .await()

            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }
    fun getCurrentUserId(): String? = auth.currentUser?.uid
}
