package com.example.movieticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.movieticket.ui.navigation.AuthNavigation
import com.example.movieticket.ui.screens.MainScreen
import com.example.movieticket.ui.screens.SplashScreen
import com.example.movieticket.ui.theme.MovieTicketTheme
import com.example.movieticket.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movieticket.ui.viewmodel.ProfileViewModel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Thêm biến launcher xin quyền
    private lateinit var requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo launcher xin quyền
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Được cấp quyền, có thể gửi thông báo
            } else {
                // Người dùng từ chối, có thể hướng dẫn họ vào Settings
            }
        }

        // Kiểm tra và xin quyền thông báo (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Hiển thị giải thích nếu cần, sau đó xin quyền
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {

            MovieTicketTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                    val profileViewModel: ProfileViewModel = hiltViewModel()

                    // Lấy userId từ AuthViewModel
                    val userId = authViewModel.getCurrentUserId()

                    // Gọi lưu token mỗi khi đăng nhập thành công
                    LaunchedEffect(isLoggedIn, userId) {
                        if (isLoggedIn && userId != null) {
                            profileViewModel.saveFcmTokenForUser(userId)
                        }
                    }

                    if (showSplash) {
                        SplashScreen(
                            onSplashFinished = {
                                showSplash = false
                            }
                        )
                    } else {
                    if (isLoggedIn) {
                        MainScreen()
                    } else {
                        AuthNavigation(
                            navController = navController,
                            onAuthSuccess = {
                                // Navigation to main screen will be handled by isLoggedIn state
                            }
                        )
                        }
                    }
                }
            }
        }
    }
}