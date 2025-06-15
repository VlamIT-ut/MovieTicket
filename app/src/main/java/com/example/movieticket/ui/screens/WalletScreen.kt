package com.example.movieticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movieticket.ui.viewmodel.WalletViewModel
import com.example.movieticket.ui.viewmodel.Transaction
import com.example.movieticket.ui.viewmodel.TransactionType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBackClick: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.walletState.collectAsState()
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = if (state.isAdmin) "Quản lý ví" else "Ví của tôi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A1A1A)
            )
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Unknown error occurred",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            if (state.isAdmin) {
                // Admin View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "Danh sách người dùng",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(state.users) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A2A)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = user.displayName,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = user.email,
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(user.balance),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { 
                                        selectedUserId = user.userId
                                        viewModel.showAddMoneyDialog()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Nạp tiền cho người dùng")
                                }
                            }
                        }
                    }
                }
            } else {
                // Regular User View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Wallet Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = state.cardHolderName,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Số dư: ${formatCurrency(state.balance)}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Thông báo cho user thường
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Cần nạp tiền?",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Vui lòng liên hệ Admin để được hỗ trợ nạp tiền vào tài khoản",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Lịch sử giao dịch",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    TransactionList(state.transactions)
                }
            }
        }
    }

    // Add Money Dialog - Chỉ hiển thị cho admin
    if (state.showAddMoneyDialog && state.isAdmin) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.hideAddMoneyDialog()
                selectedUserId = null
            },
            containerColor = Color(0xFF2A2A2A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { 
                Text(
                    text = if (state.isAdmin && selectedUserId != null) 
                        "Nạp tiền cho người dùng" 
                    else 
                        "Nạp tiền vào ví"
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                amount = newValue
                            }
                        },
                        label = { Text("Số tiền (VNĐ)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedLabelColor = Color(0xFF2196F3),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountLong = amount.toLongOrNull() ?: 0
                        if (amountLong > 0) {
                            viewModel.addMoney(
                                amount = amountLong,
                                targetUserId = selectedUserId
                            ) {
                                amount = ""
                                selectedUserId = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    enabled = amount.isNotEmpty()
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.hideAddMoneyDialog()
                        amount = ""
                        selectedUserId = null
                    }
                ) {
                    Text("Hủy", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun TransactionList(transactions: List<Transaction>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B2B4A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fixed description for movie ticket payment
            Text(
                text = "Thanh toán vé xem phim",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date and time
            Text(
                text = formatDateTime(transaction.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Amount
            Text(
                text = "${formatAmount(transaction.amount)} VND",
                style = MaterialTheme.typography.bodyLarge,
                color = if (transaction.type == TransactionType.DEBIT.name) Color.Red else Color.Green,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatAmount(amount: Long): String {
    return DecimalFormat("#,###").format(amount).replace(",", ".")
}

private fun formatCurrency(amount: Long): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(amount)} VNĐ"
} 