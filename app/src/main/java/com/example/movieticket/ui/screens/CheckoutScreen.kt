package com.example.movieticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movieticket.data.model.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    movie: Movie,
    selectedSeats: List<String>,
    selectedDate: String,
    selectedTime: String,
    onBackClick: () -> Unit,
    onPaymentClick: (Int) -> Unit
) {
    // Use remember to keep the values in scope
    val ticketPrice = remember { 50000 }
    val numberOfTickets = remember { selectedSeats.size }
    val subtotal = remember { ticketPrice * numberOfTickets }
    val tax = remember { (subtotal * 0.03).toInt() }
    val total = remember { subtotal + tax }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A1832))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Card chứa thông tin đặt vé
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B2B4A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Movie info section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Movie poster
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            contentDescription = movie.title,
                            modifier = Modifier
                                .width(100.dp)
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Movie details
                        Column {
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = movie.genres.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = " ${movie.voteAverage}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Booking details
                    BookingDetailRow("ngày", selectedDate)
                    BookingDetailRow("thời gian", selectedTime)
                    BookingDetailRow("vị trí chỗ ngồi", selectedSeats.joinToString(", "))

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )

                    // Price details
                    BookingDetailRow(
                        "${numberOfTickets} vé",
                        "${formatPrice(subtotal)} VNĐ"
                    )
                    BookingDetailRow(
                        "VAT (3%)",
                        "${formatPrice(tax)} VNĐ"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "thành tiền",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${formatPrice(total)} VNĐ",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pay Now button
            Button(
                onClick = { onPaymentClick(total) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B4D8)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Tiếp tục")
            }
        }
    }
}

@Composable
private fun BookingDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

// Thêm hàm format giá tiền
private fun formatPrice(price: Int): String {
    return String.format("%,d", price)
        .replace(",", ".")
} 