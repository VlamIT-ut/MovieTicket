package com.example.movieticket.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieticket.data.local.UserPrefs
import com.example.movieticket.utils.LevelUpEventBus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPrefs: UserPrefs,
    private val levelBus: LevelUpEventBus
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /* ---------- Helper: Uri -> Base64 ---------- */
    private fun uriToBase64(uri: Uri, ctx: Context): String {
        val input = ctx.contentResolver.openInputStream(uri)!!
        val bitmap = BitmapFactory.decodeStream(input)
        val baos   = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    /* ---------- Public APIs ---------- */

    fun updateProfile(
        displayName: String? = null,
        imageUri: Uri? = null,
        context: Context? = null,
        additionalInfo: Map<String, Any>? = null,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        try {
            _isLoading.value = true
            _error.value = null

            val user = auth.currentUser ?: throw Exception("User not logged in")
            val data = mutableMapOf<String, Any>()

            /* 1. Ảnh đại diện */
            imageUri?.let { uri ->
                val ctx = context ?: throw Exception("Context is null for image processing")
                data["profileImage"] = uriToBase64(uri, ctx)
            }

            /* 2. Tên hiển thị */
            displayName?.let { name ->
                data["displayName"] = name
                val updateReq = userProfileChangeRequest { this.displayName = name }
                user.updateProfile(updateReq).await()
            }

            /* 3. Thêm info khác (nếu có) */
            additionalInfo?.let { data.putAll(it) }

            /* 4. Ghi Firestore */
            if (data.isNotEmpty()) {
                firestore.collection("users")
                    .document(user.uid)
                    .set(data, SetOptions.merge())
                    .await()
            }

            /* 5. (Tuỳ chọn) Cộng điểm khi hoàn thiện profile lần đầu */
            if (additionalInfo?.get("firstTimeComplete") == true) {
                userPrefs.point += 50
                levelBus.emitLevelUp(userPrefs.memberLevel) // cập nhật banner nếu hạng thay đổi
            }

            onSuccess()
        } catch (e: Exception) {
            Log.e("ProfileVM", "updateProfile: ${e.message}")
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit) = viewModelScope.launch {
        try {
            _isLoading.value = true; _error.value = null
            auth.currentUser?.updatePassword(newPassword)?.await()
            onSuccess()
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun getUserData(onSuccess: (Map<String, Any>) -> Unit) = viewModelScope.launch {
        try {
            _isLoading.value = true; _error.value = null
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snap = firestore.collection("users").document(uid).get().await()
            onSuccess(snap.data ?: emptyMap())
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }
}
