package com.example.movieticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random
import com.example.movieticket.data.model.Movie
import com.example.movieticket.data.model.Ticket
import com.example.movieticket.ui.viewmodel.Transaction
import com.example.movieticket.ui.viewmodel.TransactionType

data class PaymentState(
    val orderId: String = PaymentViewModel.generateOrderId(),
    val promoCode: String? = null,
    val walletBalance: Long = 0,
    val total: Long = 0,
    val selectedDate: String = "",
    val selectedTime: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _paymentState = MutableStateFlow(PaymentState())
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    fun initialize(
        movie: Movie,
        selectedSeats: List<String>,
        selectedDate: String,
        selectedTime: String,
        total: Long
    ) {
        _paymentState.value = _paymentState.value.copy(
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            total = total
        )
        fetchWalletBalance()
    }

    private fun fetchWalletBalance() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("User not logged in")

                val walletDoc = firestore.collection("wallets")
                    .document(userId)
                    .get()
                    .await()

                val balance = walletDoc.getLong("balance") ?: 0
                _paymentState.value = _paymentState.value.copy(
                    walletBalance = balance
                )
            } catch (e: Exception) {
                _paymentState.value = _paymentState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun applyPromoCode(code: String) {
        // Implement promo code logic here
        _paymentState.value = _paymentState.value.copy(
            promoCode = code
        )
    }

    fun processPayment(
        movie: Movie,
        selectedSeats: List<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _paymentState.value = _paymentState.value.copy(isLoading = true)

                val userId = FirebaseAuth.getInstance().currentUser?.uid 
                    ?: throw Exception("User not logged in")

                // Kiểm tra số dư ví
                val currentBalance = _paymentState.value.walletBalance
                val totalAmount = _paymentState.value.total
                if (currentBalance < totalAmount) {
                    _paymentState.value = _paymentState.value.copy(
                        error = "Số dư ví không đủ để thanh toán. Vui lòng nạp thêm tiền!",
                        isLoading = false
                    )
                    return@launch
                }

                // Kiểm tra ghế
                val seatsAvailable = MyTicketViewModel.checkSeatsAvailability(
                    movie.id,
                    _paymentState.value.selectedDate,
                    _paymentState.value.selectedTime,
                    selectedSeats
                )

                if (!seatsAvailable) {
                    _paymentState.value = _paymentState.value.copy(
                        error = "Ghế đã được đặt, vui lòng chọn ghế khác",
                        isLoading = false
                    )
                    return@launch
                }

                // Tạo ticket
                val ticket = Ticket(
                    movieId = movie.id,
                    movieTitle = movie.title,
                    moviePoster = movie.posterPath,
                    userId = userId,
                    seats = selectedSeats,
                    date = _paymentState.value.selectedDate,
                    time = _paymentState.value.selectedTime,
                    totalAmount = _paymentState.value.total.toInt(),
                    timestamp = System.currentTimeMillis(),
                    status = "active"
                )

                // Lưu ticket
                val ticketSaved = MyTicketViewModel.saveTicket(ticket)
                if (!ticketSaved) {
                    _paymentState.value = _paymentState.value.copy(
                        error = "Không thể đặt vé, vui lòng thử lại",
                        isLoading = false
                    )
                    return@launch
                }

                // Trừ tiền trong ví
                val newBalance = currentBalance - totalAmount

                // Tạo transaction mới
                val transaction = Transaction(
                    amount = _paymentState.value.total,
                    type = TransactionType.DEBIT.name,
                    description = "Thanh toán vé xem phim",
                    timestamp = System.currentTimeMillis(),
                    balanceAfter = newBalance
                )

                // Thực hiện transaction trong Firestore với batch
                firestore.runBatch { batch ->
                    // Cập nhật số dư ví
                    val walletRef = firestore.collection("wallets")
                        .document(userId)
                    batch.update(walletRef, "balance", newBalance)

                    // Thêm transaction mới
                    val transactionRef = firestore.collection("wallets")
                        .document(userId)
                        .collection("transactions")
                        .document()
                    batch.set(transactionRef, transaction.copy(id = transactionRef.id))
                }.await()

                // Cập nhật state và gọi callback thành công
                _paymentState.value = _paymentState.value.copy(
                    isLoading = false,
                    error = null
                )
                
                onSuccess()
            } catch (e: Exception) {
                _paymentState.value = _paymentState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    companion object {
        fun generateOrderId(): String {
            val timestamp = System.currentTimeMillis()
            val random = Random.nextInt(1000, 9999)
            return "$timestamp$random"
        }
    }
} 