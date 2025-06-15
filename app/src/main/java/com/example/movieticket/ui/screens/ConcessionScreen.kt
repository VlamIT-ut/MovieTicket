package com.example.movieticket.ui.screens

import androidx.compose.foundation.shape.CircleShape
import com.example.movieticket.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.foundation.Image

data class ConcessionItem(
    val id: Int,
    val name: String,
    val price: Int,
    val imageResId: Int  // Đổi từ imageUrl thành imageResId kiểu Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcessionScreen(
    ticketTotal: Int,
    onBackClick: () -> Unit,
    onConfirmClick: (Int) -> Unit // Tổng tiền = tiền vé + tiền bắp nước
) {
    val concessionItems = listOf(
        ConcessionItem(
            id = 1,
            name = "Combo 2 Big",
            price = 109000,
            imageResId = R.drawable.combo1
        ),
        ConcessionItem(
            id = 2,
            name = "Combo 2 Big Extra",
            price = 129000,
            imageResId = R.drawable.combo2
        ),
        ConcessionItem(
            id = 3,
            name = "Combo 1 Big",
            price = 89000,
            imageResId = R.drawable.combo3
        ),
        ConcessionItem(
            id = 4,
            name = "Combo 1 Big Extra",
            price = 109000,
            imageResId = R.drawable.combo4
        )
    )

    var itemQuantities by remember { mutableStateOf(concessionItems.associate { it.id to 0 }) }
    
    val concessionTotal = itemQuantities.entries.sumOf { (id, quantity) ->
        concessionItems.find { it.id == id }?.price?.times(quantity) ?: 0
    }
    
    val subtotal = ticketTotal + concessionTotal
    val tax = (subtotal * 0.03).toInt() // Thuế 3%
    val grandTotal = subtotal + tax

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bắp Nước", color = Color.White) },
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
                    containerColor = Color(0xFF1B2B4A)  // Màu xanh đậm cho TopBar
                )
            )
        },
        containerColor = Color(0xFF0A1832)  // Màu xanh đậm cho background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)  // Khoảng cách giữa các item
            ) {
                items(concessionItems) { item ->
                    ConcessionItemCard(
                        item = item,
                        quantity = itemQuantities[item.id] ?: 0,
                        onQuantityChanged = { newQuantity ->
                            itemQuantities = itemQuantities.toMutableMap().apply {
                                put(item.id, newQuantity)
                            }
                        }
                    )
                }
            }

            // Summary Card
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
                    PriceRow("Tiền vé", formatPrice(ticketTotal))
                    PriceRow("Bắp nước", formatPrice(concessionTotal))
                    PriceRow("Thuế (3%)", formatPrice(tax))
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                    PriceRow(
                        "Tổng cộng",
                        formatPrice(grandTotal),
                        isTotal = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onConfirmClick(grandTotal) },
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
    }
}

@Composable
private fun ConcessionItemCard(
    item: ConcessionItem,
    quantity: Int,
    onQuantityChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B2B4A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White  // Đổi màu chữ thành trắng
                    )
                    Text(
                        text = "${formatPrice(item.price)}đ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray  // Màu xám cho giá
                    )
                }
            }

            // Quantity controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (quantity > 0) onQuantityChanged(quantity - 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.Remove,
                        contentDescription = "Giảm",
                        tint = Color(0xFF00B4D8),  // Màu xanh cho nút
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                IconButton(
                    onClick = { onQuantityChanged(quantity + 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Tăng",
                        tint = Color(0xFF00B4D8),  // Màu xanh cho nút
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    amount: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = if (isTotal) Color.White else Color.Gray,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = "$amount VNĐ",
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = if (isTotal) Color.White else Color.Gray,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatPrice(price: Int): String {
    return String.format("%,d", price).replace(",", ".")
}

