package com.example.movieticket.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movieticket.R
import com.example.movieticket.ui.viewmodel.WalletViewModel
import com.example.movieticket.ui.viewmodel.Transaction
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import androidx.compose.ui.text.font.FontWeight
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.walletState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image giống HomeScreen
        Image(
            painter = painterResource(id = R.drawable.background_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Overlay tối hơn
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
        )
        // Nội dung ví
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = { 
                    Text(
                        "Ví của tôi",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF232B3E).copy(alpha = 0.92f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = state.cardHolderName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Số dư: ${formatCurrency(state.balance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF00B4D8),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.showAddMoneyDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B4D8)
                        )
                    ) {
                        Text("Nạp tiền", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Lịch sử giao dịch",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyColumn {
                items(state.transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
                if (state.transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có giao dịch nào",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showAddMoneyDialog) {
        AddMoneyDialog(
            onDismiss = { viewModel.hideAddMoneyDialog() },
            onConfirm = { amount ->
                viewModel.addMoney(amount) {
                    // Show success message
                }
            }
        )
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2196F3)
            )
        }
    }

    state.error?.let { error ->
        Toast.makeText(LocalContext.current, error, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF232B3E).copy(alpha = 0.92f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = formatDate(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.type == "Nạp tiền") 
                    Color(0xFF4CAF50) else Color(0xFFE91E63),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatCurrency(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(amount)} VNĐ"
}

@Composable
fun AddMoneyDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nạp tiền vào ví") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it.filter { char -> char.isDigit() }
                        showError = false 
                    },
                    label = { Text("Số tiền") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1A1A1A),
                        unfocusedContainerColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF2196F3),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountLong = amount.toLongOrNull() ?: 0
                    when {
                        amountLong < 10000 -> {
                            showError = true
                            errorMessage = "Số tiền nạp tối thiểu là 10.000 VNĐ"
                        }
                        amountLong > 50000000 -> {
                            showError = true
                            errorMessage = "Số tiền nạp tối đa là 50.000.000 VNĐ"
                        }
                        else -> {
                            onConfirm(amountLong)
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Hủy", color = Color.White)
            }
        },
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
} 