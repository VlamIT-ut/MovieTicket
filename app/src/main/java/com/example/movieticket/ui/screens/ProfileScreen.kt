package com.example.movieticket.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieticket.R
import com.example.movieticket.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
    onWalletClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(user?.displayName ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var profileImage by remember { mutableStateOf<String?>(null) }
    var firestoreDisplayName by remember { mutableStateOf<String?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load user data
    LaunchedEffect(Unit) {
        if (user != null) {
            viewModel.getUserData { userData ->
                profileImage = userData["profileImage"] as? String
                firestoreDisplayName = userData["displayName"] as? String
                Log.d("ProfileScreen", "Received profile image data: ${profileImage?.take(100)}...")
            }
        }
    }

    // Convert Base64 to ImageBitmap
    val profileImageBitmap = remember(profileImage) {
        profileImage?.let { base64String ->
            try {
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error decoding image: ${e.message}")
                null
            }
        }
    }
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfile(
                imageUri = it,
                context = context,
                onSuccess = {
                    Log.d("ProfileScreen", "Image upload successful")
                    // Refresh user data
                    viewModel.getUserData { userData ->
                        profileImage = userData["profileImage"] as? String
                        Log.d("ProfileScreen", "Profile data refreshed after upload")
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
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
            // Top Bar with back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            if (user != null) {
                // Profile Picture and Name
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3).copy(alpha = 0.1f))
                            .clickable { imagePickerLauncher.launch("image/*") }
                    ) {
                        if (profileImageBitmap != null) {
                            Log.d("ProfileScreen", "Displaying profile image from Bitmap")
                            Image(
                                bitmap = profileImageBitmap,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Log.d("ProfileScreen", "Displaying default avatar")
                            Image(
                                painter = painterResource(id = R.drawable.default_avatar),
                                contentDescription = "Default Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        // Edit overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile Picture",
                                tint = Color(0xFF2196F3)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = firestoreDisplayName ?: user.displayName ?: "User",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    
                    Text(
                        text = user.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Menu Items with Card background
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
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
                            onClick = {
                                Log.d("ProfileScreen", "Sign out clicked")
                                auth.signOut()
                                onSignOut()
                            },
                            tintColor = Color.Red
                        )
                    }
                }
            } else {
                // Show sign in message and button
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Please sign in to view your profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onSignOut,  // This will navigate to auth screen
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In")
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = Color(0xFF2A2A2A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Edit Display Name") },
            text = {
                TextField(
                    value = newDisplayName,
                    onValueChange = { newDisplayName = it },
                    label = { Text("Display Name") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1A1A1A),
                        unfocusedContainerColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF2196F3),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            displayName = newDisplayName,
                            onSuccess = {
                                showEditNameDialog = false
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            containerColor = Color(0xFF2A2A2A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Change Password") },
            text = {
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1A1A1A),
                        unfocusedContainerColor = Color(0xFF1A1A1A),
                        focusedLabelColor = Color(0xFF2196F3),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePassword(
                            newPassword = newPassword,
                            onSuccess = {
                                showChangePasswordDialog = false
                                newPassword = ""
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Show error in a Snackbar if there is one
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Log.e("ProfileScreen", "Error: $errorMessage")
        }
    }

    // Show loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2196F3)
            )
        }
    }
}

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
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tintColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tintColor == Color.Red) Color.Red else Color.White
        )
    }
} 