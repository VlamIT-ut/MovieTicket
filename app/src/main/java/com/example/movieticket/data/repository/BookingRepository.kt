package com.example.movieticket.data.repository

import com.example.movieticket.data.api.BookingApi
import com.example.movieticket.data.local.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val api: BookingApi,
    private val userPrefs: UserPrefs           // <─ thêm dependency
) {

    /** Cộng điểm & cập nhật hạng */
    private fun updateUserAfterPurchase(totalPrice: Int) {
        val earned = totalPrice / 10_000        // 10 000 đ = 1 điểm
        val newPoint = userPrefs.point + earned
        userPrefs.point = newPoint

        userPrefs.memberLevel = when {
            newPoint >= 1_000 -> "VIP"
            newPoint >= 500   -> "Gold"
            else              -> "Silver"
        }
    }

    /** Lấy ghế đã đặt */
    suspend fun getBookedSeats(
        movieId: String,
        date: String,
        time: String
    ): List<String> = try {
        api.getBookedSeats(movieId, date, time)
    } catch (e: Exception) {
        emptyList()
    }

    /** Đặt ghế + cộng điểm */
    suspend fun bookSeats(
        movieId: String,
        date: String,
        time: String,
        seats: List<String>,
        seatPrice: Int               // <─ giá 1 ghế (VNĐ)
    ): Boolean = try {
        api.bookSeats(movieId, date, time, seats)

        // ✅ cộng điểm sau khi đặt thành công
        val totalPrice = seatPrice * seats.size
        updateUserAfterPurchase(totalPrice)

        true
    } catch (e: Exception) {
        false
    }
}
