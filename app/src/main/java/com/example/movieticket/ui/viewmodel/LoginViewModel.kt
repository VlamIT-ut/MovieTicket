package com.example.movieticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                auth.signInWithEmailAndPassword(email, password).await()
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Đăng nhập thất bại"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = auth.currentUser
                if (user != null) {
                    saveUserToFirestore(user.uid, user.displayName, user.email, user.photoUrl?.toString())
                }
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Đăng nhập Google thất bại"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveUserToFirestore(uid: String, displayName: String?, email: String?, photoUrl: String?) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userMap = hashMapOf(
            "uid" to uid,
            "displayName" to (displayName ?: ""),
            "email" to (email ?: ""),
            "photoUrl" to (photoUrl ?: "")
        )
        db.collection("users").document(uid).set(userMap)
    }

    fun setError(message: String) {
        _error.value = message
    }
}
