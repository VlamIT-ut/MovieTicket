package com.example.movieticket.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _error.value = "Mật khẩu xác nhận không khớp"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val currentUser = auth.currentUser
                
                currentUser?.let { user ->
                    val userProfile = hashMapOf(
                        "name" to name,
                        "email" to email
                    )
                    
                    firestore.collection("users")
                        .document(user.uid)
                        .set(userProfile)
                        .await()
                    
                    _isRegistered.value = true
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Đăng ký thất bại"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 