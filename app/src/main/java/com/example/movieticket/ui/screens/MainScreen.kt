package com.example.movieticket.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController               // 🔥 import
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.movieticket.data.local.UserPrefs       // 🔥 import
import com.example.movieticket.utils.LevelUpEventBus      // 🔥 import
import com.example.movieticket.ui.navigation.BottomNavigationBar
import com.example.movieticket.ui.navigation.BottomNavItem
import com.example.movieticket.ui.viewmodel.MovieViewModel
import com.example.movieticket.ui.viewmodel.MyTicketViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(                                         // 🔥 THAY ĐỔI chữ ký
    navController: NavHostController,
    userPrefs: UserPrefs,
    levelBus: LevelUpEventBus
) {
    val movieVM: MovieViewModel = hiltViewModel()
    val ticketVM: MyTicketViewModel = hiltViewModel()
    val auth = FirebaseAuth.getInstance()

    /* ---- Debug current route ---- */
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(currentRoute) { Log.d("Navigation", "Route: $currentRoute") }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route
            ) {

                /* ---------- HOME ---------- */
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        viewModel = movieVM,
                        onMovieClick = { movie -> navController.navigate("detail/${movie.id}") },
                        onSearchClick = { navController.navigate("search") },
                        onProfileClick = { navController.navigate("profile") }
                    )
                }

                /* ---------- MY TICKET ---------- */
                composable(BottomNavItem.MyTicket.route) {
                    MyTicketScreen { ticketId ->
                        navController.navigate("ticket_detail/$ticketId")
                    }
                }

                /* ---------- WALLET ---------- */
                composable(BottomNavItem.Wallet.route) {
                    com.example.movieticket.ui.screen.WalletScreen()
                }

                /* ---------- PROFILE ---------- */
                composable("profile") {
                    ProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onSignOut   = {
                            auth.signOut()
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                        onWalletClick = { navController.navigate(BottomNavItem.Wallet.route) },
                        userPrefs = userPrefs,          // 🔥 truyền
                        levelBus  = levelBus            // 🔥 truyền
                    )
                }

                /* ---------- CÁC MÀN KHÁC không đổi ---------- */
                // -- Detail --
                composable(
                    "detail/{movieId}",
                    arguments = listOf(navArgument("movieId") { type = NavType.StringType })
                ) { back ->
                    val movieId = back.arguments?.getString("movieId")?.toIntOrNull()
                    movieId?.let { id ->
                        movieVM.getMovie(id)?.let { movie ->
                            val isNowShowing = movieVM.movies.value.any { it.id == movie.id }
                            DetailScreen(
                                movie = movie,
                                onBuyTicketClick = {
                                    if (isNowShowing) navController.navigate("booking/${movie.id}")
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        } ?: navController.popBackStack()
                    }
                }

                /* ---- Search, Booking, Checkout, Concession, Payment, TicketDetail ---- */
                // (giữ nguyên code của bạn, KHÔNG cần thay đổi)
                /* ... */
            }
        }
    }
}
