package com.example.movieticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class WalletState(
    val cardHolderName: String = "",
    val balance: Long = 0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddMoneyDialog: Boolean = false,
    val isAdmin: Boolean = false,
    val users: List<UserWallet> = emptyList()
)

data class UserWallet(
    val userId: String,
    val displayName: String,
    val email: String,
    val balance: Long
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    init {
        fetchWalletData()
    }

    fun showAddMoneyDialog() {
        _walletState.value = _walletState.value.copy(showAddMoneyDialog = true)
    }

    fun hideAddMoneyDialog() {
        _walletState.value = _walletState.value.copy(showAddMoneyDialog = false)
    }

    fun addMoney(amount: Long, targetUserId: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Kiểm tra số tiền hợp lệ
                if (amount < 10000) {
                    throw Exception("Số tiền nạp tối thiểu là 10.000 VNĐ")
                }
                if (amount > 50000000) {
                    throw Exception("Số tiền nạp tối đa là 50.000.000 VNĐ")
                }

                _walletState.value = _walletState.value.copy(isLoading = true)
                
                val currentUser = FirebaseAuth.getInstance().currentUser
                    ?: throw Exception("User not logged in")

                // Bỏ kiểm tra quyền admin, chỉ cho phép nạp tiền vào ví của chính mình
                val walletRef = firestore.collection("wallets").document(currentUser.uid)

                // Get current balance
                val walletDoc = walletRef.get().await()
                val currentBalance = walletDoc.getLong("balance") ?: 0
                val newBalance = currentBalance + amount

                // Create transaction data
                val transactionData = hashMapOf(
                    "amount" to amount,
                    "type" to "Nạp tiền",
                    "timestamp" to System.currentTimeMillis(),
                    "description" to "Nạp tiền vào ví",
                    "balanceAfter" to newBalance,
                    "adminId" to currentUser.uid
                )

                // Update wallet with new balance and add transaction
                firestore.runTransaction { transaction ->
                    // Update or create balance
                    if (walletDoc.exists()) {
                        transaction.update(walletRef, "balance", newBalance)
                    } else {
                        transaction.set(walletRef, mapOf("balance" to newBalance))
                    }

                    // Add transaction to history
                    val transactionRef = walletRef
                        .collection("transactions")
                        .document()

                    transaction.set(transactionRef, transactionData)
                }.await()

                // Fetch updated transactions
                fetchTransactions()

                // Update local state
                _walletState.value = _walletState.value.copy(
                    balance = newBalance,
                    isLoading = false,
                    showAddMoneyDialog = false,
                    error = null
                )

                onSuccess()
            } catch (e: Exception) {
                _walletState.value = _walletState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun fetchWalletData() {
        viewModelScope.launch {
            try {
                _walletState.value = _walletState.value.copy(isLoading = true)
                
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("User not logged in")

                // Lấy thông tin ví
                val walletDoc = firestore.collection("wallets")
                    .document(userId)
                    .get()
                    .await()

                val balance = walletDoc.getLong("balance") ?: 0

                // Lấy thông tin user từ Firestore để lấy tên đồng bộ
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                val cardHolderName = userDoc.getString("displayName") ?: ""

                // Lấy lịch sử giao dịch
                val transactions = firestore.collection("wallets")
                    .document(userId)
                    .collection("transactions")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject(Transaction::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }

                _walletState.value = _walletState.value.copy(
                    cardHolderName = cardHolderName,
                    balance = balance,
                    transactions = transactions,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _walletState.value = _walletState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    // Thêm hàm để refresh dữ liệu
    fun refreshWalletData() {
        fetchWalletData()
    }

    private fun fetchTransactions() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("User not logged in")

                val transactions = firestore.collection("wallets")
                    .document(userId)
                    .collection("transactions")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

                val transactionList = transactions.documents.map { doc ->
                    Transaction(
                        id = doc.id,
                        amount = doc.getLong("amount") ?: 0,
                        type = doc.getString("type") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        description = doc.getString("description") ?: "",
                        balanceAfter = doc.getLong("balanceAfter")
                    )
                }

                _walletState.value = _walletState.value.copy(
                    transactions = transactionList
                )
            } catch (e: Exception) {
                _walletState.value = _walletState.value.copy(
                    error = e.message
                )
            }
        }
    }
} 