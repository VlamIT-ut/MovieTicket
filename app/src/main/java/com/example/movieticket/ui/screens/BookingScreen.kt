package com.example.movieticket.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.movieticket.R
import com.example.movieticket.data.model.Movie
import com.example.movieticket.data.model.Seat
import com.example.movieticket.ui.viewmodel.BookingViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import androidx.activity.compose.BackHandler

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    movie: Movie,
    onBackClick: () -> Unit,
    onConfirmBooking: (String, String, List<String>) -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    
    // Collect states from ViewModel
    val seats by viewModel.seats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Effect to load booked seats when date and time are selected
    LaunchedEffect(selectedDate, selectedTime) {
        selectedDate?.let { date ->
            selectedTime?.let { time ->
                viewModel.loadBookedSeats(
                    movieId = movie.id,
                    date = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    time = time
                )
            }
        }
    }

    // Effect to clear selected seats when date or time changes
    LaunchedEffect(selectedDate, selectedTime) {
        viewModel.clearSelectedSeats()
    }

    // Handle back press
    BackHandler {
        onBackClick()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt vé xem phim", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B2B4A)
                )
            )
        },
        containerColor = Color(0xFF0A1832)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Movie title
                item {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }
                
                // Date selection
                item {
                    Text(
                        text = "Chọn ngày",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val dates = (0..6).map { LocalDate.now().plusDays(it.toLong()) }
                        items(dates) { date ->
                            DateCard(
                                date = date,
                                isSelected = date == selectedDate,
                                onClick = { selectedDate = date }
                            )
                        }
                    }
                }
                
                // Time selection
                item {
                    Text(
                        text = "Chọn suất chiếu",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val times = listOf("10:00", "12:30", "15:00", "17:30", "20:00", "22:30")
                        items(times) { time ->
                            TimeCard(
                                time = time,
                                isSelected = time == selectedTime,
                                selectedDate = selectedDate,
                                onClick = { selectedTime = time }
                            )
                        }
                    }
                }

                // Screen
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .padding(top = 32.dp, bottom = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.screen),
                            contentDescription = "Màn chiếu",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        
                        Text(
                            text = "MÀN CHIẾU",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Seats section
                item {
                    Text(
                        text = "Chọn ghế",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00B4D8))
                        }
                    } else if (error != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = error ?: "Đã có lỗi xảy ra",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                                TextButton(
                                    onClick = {
                                        selectedDate?.let { date ->
                                            selectedTime?.let { time ->
                                                viewModel.loadBookedSeats(
                                                    movieId = movie.id,
                                                    date = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                                    time = time
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Text("Thử lại", color = Color(0xFF00B4D8))
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp) // Fixed height for the grid
                        ) {
                            SeatGrid(
                                seats = seats,
                                onSeatSelected = { seatId ->
                                    viewModel.toggleSeatSelection(seatId)
                                }
                            )
                        }
                    }
                }

                // Seat legend
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SeatLegend(
                            color = Color(0xFF1B2B4A),
                            text = "Ghế trống"
                        )
                        SeatLegend(
                            color = Color(0xFF4B5563),
                            text = "Đã đặt"
                        )
                        SeatLegend(
                            color = Color(0xFF00B4D8),
                            text = "Đang chọn"
                        )
                    }
                }

                // Booking summary and confirm button
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B2B4A)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            val selectedSeats = viewModel.getSelectedSeats()
                            Text(
                                text = "Tổng cộng",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            
                            Text(
                                text = "${formatPrice(selectedSeats.size * 50000)} VNĐ",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B4D8)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    selectedDate?.let { date ->
                                        selectedTime?.let { time ->
                                            try {
                                                onConfirmBooking(
                                                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                                    time,
                                                    viewModel.getSelectedSeats()
                                                )
                                            } catch (e: Exception) {
                                                Log.e("BookingScreen", "Error during booking confirmation", e)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && error == null && selectedDate != null && 
                                         selectedTime != null && viewModel.getSelectedSeats().isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00B4D8)
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text("Xác nhận đặt vé")
                                }
                            }
                        }
                    }
                }
            }

            // Show error dialog if there's an error
            error?.let { errorMessage ->
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Lỗi") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedDate?.let { date ->
                                    selectedTime?.let { time ->
                                        viewModel.loadBookedSeats(
                                            movieId = movie.id,
                                            date = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                            time = time
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("Thử lại")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = onBackClick
                        ) {
                            Text("Quay lại")
                        }
                    }
                )
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(60.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF00B4D8) else Color(0xFF1B2B4A)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun TimeCard(
    time: String,
    isSelected: Boolean,
    selectedDate: LocalDate? = null,
    onClick: () -> Unit
) {
    val currentDate = LocalDate.now()
    val currentTime = LocalTime.now()
    val showTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
    
    val isEnabled = when {
        selectedDate == null -> false
        selectedDate > currentDate -> true
        selectedDate == currentDate -> showTime > currentTime
        else -> false
    }

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isEnabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isEnabled -> Color.Gray.copy(alpha = 0.5f)
                isSelected -> Color(0xFF00B4D8)
                else -> Color(0xFF1B2B4A)
            }
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun SeatGrid(
    seats: List<Seat>,
    onSeatSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(seats) { seat ->
            SeatItem(
                seat = seat,
                onClick = { if (!seat.isBooked) onSeatSelected(seat.id) }
            )
        }
    }
}

@Composable
fun SeatItem(
    seat: Seat,
    onClick: () -> Unit
) {
    val seatColor = when {
        seat.isBooked -> Color(0xFF4B5563) // Darker gray for booked seats
        seat.isSelected -> Color(0xFF00B4D8) // Bright blue for selected seats
        else -> Color(0xFF1B2B4A) // Default dark blue for available seats
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(seatColor)
            .clickable(
                enabled = !seat.isBooked,
                onClick = {
                    if (!seat.isBooked) {
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seat.id,
            color = when {
                seat.isBooked -> Color(0xFF9CA3AF) // Light gray for booked seat text
                else -> Color.White
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SeatLegend(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatPrice(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN"))
        .format(amount)
} 