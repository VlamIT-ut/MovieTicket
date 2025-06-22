package com.example.movieticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieticket.data.local.UserPrefs
import com.example.movieticket.data.model.Movie
import com.example.movieticket.data.model.Ticket
import com.example.movieticket.utils.LevelUpEventBus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

data class PaymentState(
    val orderId: String = "",
    val walletBalance: Long = 0L,
    val total: Long = 0L,
    val selectedDate: String = "",
    val selectedTime: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userPrefs: UserPrefs,
    private val levelBus: LevelUpEventBus
) : ViewModel() {

    private val _ui = MutableStateFlow(PaymentState())
    val paymentState: StateFlow<PaymentState> = _ui

    fun initialize(
        movie: Movie,
        selectedSeats: List<String>,
        selectedDate: String,
        selectedTime: String,
        total: Long
    ) {
        _ui.update {
            it.copy(
                orderId = generateOrderId(),
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                total = total
            )
        }
        fetchWalletBalance()
    }

    private fun fetchWalletBalance() = viewModelScope.launch {
        try {
            val uid = uid() ?: return@launch
            val balance = firestore.collection("wallets").document(uid)
                .get().await().getLong("balance") ?: 0L
            _ui.update { it.copy(walletBalance = balance) }
        } catch (e: Exception) {
            _ui.update { it.copy(error = e.message) }
        }
    }

    suspend fun processPayment(
        movie: Movie,
        selectedSeats: List<String>,
        earnedPoint: Int
    ): Boolean = try {
        _ui.update { it.copy(isLoading = true, error = null) }

        val uid = uid() ?: throw Exception("Bạn chưa đăng nhập")

        val balance = _ui.value.walletBalance
        val total = _ui.value.total
        if (balance < total) throw Exception("Số dư ví không đủ, hãy nạp thêm")

        if (!MyTicketViewModel.checkSeatsAvailability(
                movie.id, _ui.value.selectedDate, _ui.value.selectedTime, selectedSeats
            )
        ) throw Exception("Ghế đã được đặt, hãy chọn ghế khác")

        val newBalance = balance - total
        val ticketRef = firestore.collection("tickets").document()
        val ticket = Ticket(
            id = ticketRef.id,
            movieId = movie.id,
            movieTitle = movie.title,
            moviePoster = movie.posterPath,
            userId = uid,
            seats = selectedSeats,
            date = _ui.value.selectedDate,
            time = _ui.value.selectedTime,
            totalAmount = total.toInt(),
            timestamp = System.currentTimeMillis(),
            status = "active"
        )

        firestore.runBatch { batch ->
            batch.set(ticketRef, ticket)

            val walletRef = firestore.collection("wallets").document(uid)
            batch.update(walletRef, "balance", newBalance)

            val transRef = walletRef.collection("transactions").document()
            batch.set(
                transRef, Transaction(
                    id = transRef.id,
                    amount = total,
                    type = TransactionType.DEBIT.name,
                    description = "Thanh toán vé xem phim",
                    timestamp = System.currentTimeMillis(),
                    balanceAfter = newBalance
                )
            )
        }.await()

        // Cộng điểm và cập nhật level
        val newPoint = userPrefs.point + earnedPoint
        val newLevel = when {
            newPoint >= 1000 -> "VIP"
            newPoint >= 500 -> "Gold"
            else -> "Silver"
        }
        val levelChanged = newLevel != userPrefs.memberLevel
        userPrefs.point = newPoint
        userPrefs.memberLevel = newLevel

        firestore.collection("users").document(uid)
            .set(mapOf("point" to newPoint, "memberLevel" to newLevel), SetOptions.merge())
            .await()

        if (levelChanged) levelBus.emitLevelUp(newLevel)

        _ui.update { it.copy(walletBalance = newBalance, isLoading = false) }
        true
    } catch (e: Exception) {
        _ui.update { it.copy(isLoading = false, error = e.message) }
        false
    }

    private fun uid() = FirebaseAuth.getInstance().currentUser?.uid

    companion object {
        fun generateOrderId(): String = "${System.currentTimeMillis()}${Random.nextInt(1000, 9999)}"
    }
}
