package com.example.movieticket.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf          // ✔ import
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movieticket.R
import com.example.movieticket.data.local.UserPrefs
import com.example.movieticket.utils.LevelUpEventBus
import com.example.movieticket.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
    onWalletClick: () -> Unit,
    userPrefs: UserPrefs,
    levelBus: LevelUpEventBus,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    /* -------- Firebase & Context -------- */
    val auth  = FirebaseAuth.getInstance()
    val user  = auth.currentUser
    val ctx   = LocalContext.current

    /* -------- State cũ -------- */
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(user?.displayName ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var profileImage by remember { mutableStateOf<String?>(null) }
    var firestoreDisplayName by remember { mutableStateOf<String?>(null) }

    /* -------- Điểm & Hạng -------- */
    var points by remember { mutableIntStateOf(userPrefs.point) }
    var level  by remember { mutableStateOf(userPrefs.memberLevel) }
    var showLevelDialog by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()

    /* Load dữ liệu Firestore lần đầu */
    LaunchedEffect(Unit) {
        user?.let {
            viewModel.getUserData { data ->
                profileImage         = data["profileImage"] as? String
                firestoreDisplayName = data["displayName"] as? String
            }
        }
    }

    /* Lắng nghe sự kiện lên hạng */
    LaunchedEffect(Unit) {
        levelBus.levelUpFlow.collect { newLevel ->
            level  = newLevel
            points = userPrefs.point
            showLevelDialog = newLevel
        }
    }

    /* Convert Base64 -> Bitmap */
    val profileImageBitmap = remember(profileImage) {
        profileImage?.let {
            runCatching {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        }
    }

    /* Image picker */
    val imgPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfile(
                imageUri  = it,
                context   = ctx,
                onSuccess = {
                    viewModel.getUserData { data ->
                        profileImage = data["profileImage"] as? String
                    }
                }
            )
        }
    }

    /* ================= UI ================= */
    Box(Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.background_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            /* Top bar */
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            if (user != null) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /* Avatar */
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.1f))
                            .clickable { imgPicker.launch("image/*") }
                    ) {
                        if (profileImageBitmap != null) {
                            Image( bitmap = profileImageBitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_avatar),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color(0xFF2196F3))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text  = firestoreDisplayName ?: user.displayName ?: "User",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text  = user.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(Modifier.height(16.dp))

                    /* Điểm & Hạng */
                    Text("Điểm: $points",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    val levelText = when (level) {
                        "VIP"  -> "🥇 VIP"
                        "Gold" -> "⭐ Gold"
                        else   -> "🥈 Silver"
                    }
                    Text("Hạng: $levelText",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }

                /* --- Menu --- */
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    Column(Modifier.padding(16.dp)) {

                        ProfileMenuItem(
                            icon = Icons.Default.Edit,
                            title = "Edit Profile",
                            onClick = { showEditNameDialog = true }
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        ProfileMenuItem(
                            icon = Icons.Default.Wallet,
                            title = "My Wallet",
                            onClick = onWalletClick
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        ProfileMenuItem(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            onClick = { showChangePasswordDialog = true }
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        ProfileMenuItem(
                            icon = Icons.Default.ExitToApp,
                            title = "Sign Out",
                            onClick = { auth.signOut(); onSignOut() },
                            tintColor = Color.Red
                        )
                    }
                }
            } else {
                /* Chưa đăng nhập */
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Please sign in to view your profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onSignOut,
                        colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.ExitToApp, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign In")
                    }
                }
            }
        }
    }

    /* Dialog chúc mừng lên hạng */
    showLevelDialog?.let { lvl ->
        AlertDialog(
            onDismissRequest = { showLevelDialog = null },
            confirmButton = { TextButton(onClick = { showLevelDialog = null }) { Text("OK") } },
            title = { Text("🎉 Chúc mừng!") },
            text  = { Text("Bạn đã lên hạng $lvl.") }
        )
    }

    /* TODO: Giữ nguyên code dialogs edit name / change password + loading + error */
}

/* -------- ProfileMenuItem -------- */
@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    tintColor: Color = Color(0xFF2196F3)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = tintColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text  = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tintColor == Color.Red) Color.Red else Color.White
        )
    }
}
