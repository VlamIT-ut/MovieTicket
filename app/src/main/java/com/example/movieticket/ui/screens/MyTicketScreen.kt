package com.example.movieticket.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.movieticket.R
import com.example.movieticket.data.model.Ticket
import com.example.movieticket.ui.viewmodel.MyTicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketScreen(
    viewModel: MyTicketViewModel = hiltViewModel(),
    onTicketClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Ảnh nền
        Image(
            painter = painterResource(id = R.drawable.background_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Overlay tối
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )
        // Nội dung vé
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Vé của tôi",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error ?: "Có lỗi xảy ra",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                state.tickets.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bạn chưa có vé nào.",
                            color = Color.White
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.tickets) { ticket ->
                            TicketItem(
                                ticket = ticket,
                                onClick = { onTicketClick(ticket.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketItem(
    ticket: Ticket,
    onClick: () -> Unit
) {
    val isActive = remember(ticket.date, ticket.time) {
        isTicketActive(ticket.date, ticket.time)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF232B3E).copy(alpha = 0.92f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Movie Poster
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${ticket.moviePoster}",
                contentDescription = ticket.movieTitle,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Ticket Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = ticket.movieTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${ticket.date} | ${ticket.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = "Ghế: ${ticket.seats.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = "${formatPrice(ticket.totalAmount)} VND",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                val statusText = if (isActive) "Còn hiệu lực" else "Hết hiệu lực"
                val statusColor = if (isActive) Color(0xFF4CAF50) else Color.Gray

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Hàm kiểm tra vé còn hiệu lực
private fun isTicketActive(date: String?, time: String?): Boolean {
    if (date.isNullOrBlank() || time.isNullOrBlank()) return false

    val dateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    )

    for (sdf in dateFormats) {
        try {
            val ticketDateTime = sdf.parse("$date $time")
            if (ticketDateTime != null) {
                return ticketDateTime.after(Date())
            }
        } catch (_: Exception) { }
    }
    return false
}

private fun formatPrice(amount: Int): String {
    return String.format("%,d", amount)
        .replace(",", ".")
}