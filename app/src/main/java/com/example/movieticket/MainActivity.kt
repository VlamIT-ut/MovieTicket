package com.example.movieticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.movieticket.data.local.UserPrefs
import com.example.movieticket.utils.LevelUpEventBus
import com.example.movieticket.ui.navigation.AuthNavigation
import com.example.movieticket.ui.screens.MainScreen
import com.example.movieticket.ui.screens.SplashScreen
import com.example.movieticket.ui.theme.MovieTicketTheme
import com.example.movieticket.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPrefs: UserPrefs
    @Inject lateinit var levelBus : LevelUpEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieTicketTheme {

                var showSplash by remember { mutableStateOf(true) }
                val navController = rememberNavController()
                val authVM : AuthViewModel = hiltViewModel()
                val isLoggedIn by authVM.isLoggedIn.collectAsState()

                /* Banner state */
                var points by remember { mutableIntStateOf(userPrefs.point) }
                var level  by remember { mutableStateOf(userPrefs.memberLevel) }

                /* Listen level-up events */
                LaunchedEffect(Unit) {
                    levelBus.levelUpFlow.collect { newLvl ->
                        level  = newLvl
                        points = userPrefs.point
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        showSplash -> SplashScreen { showSplash = false }

                        isLoggedIn -> Scaffold(
                            topBar = { MemberBanner(points, level) }
                        ) { innerPadding ->
                            Box(Modifier.padding(innerPadding)) {
                                /* 🔑 Truyền đủ 3 tham số */
                                MainScreen(
                                    navController = navController,
                                    userPrefs     = userPrefs,
                                    levelBus      = levelBus
                                )
                            }
                        }

                        else -> AuthNavigation(navController) { /* isLoggedIn sẽ đổi */ }
                    }
                }
            }
        }
    }
}

/* ---------- Banner Điểm & Hạng ---------- */
@Composable
fun MemberBanner(points: Int, level: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Điểm: $points", style = MaterialTheme.typography.bodyLarge)

        val lvlText = when (level) {
            "VIP"  -> "🥇 VIP"
            "Gold" -> "⭐ Gold"
            else   -> "🥈 Silver"
        }
        Text("Hạng: $lvlText", style = MaterialTheme.typography.bodyLarge)
    }
}
