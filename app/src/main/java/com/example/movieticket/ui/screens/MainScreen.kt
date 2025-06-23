package com.example.movieticket.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.movieticket.ui.navigation.BottomNavigationBar
import com.example.movieticket.ui.navigation.BottomNavItem
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movieticket.ui.viewmodel.MovieViewModel
import com.example.movieticket.ui.viewmodel.MyTicketViewModel
import com.example.movieticket.ui.screens.MyTicketScreen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val movieViewModel: MovieViewModel = hiltViewModel()
    val myTicketViewModel: MyTicketViewModel = hiltViewModel()
    val auth = FirebaseAuth.getInstance()

    // Debug log
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(currentRoute) {
        Log.d("Navigation", "Current route: $currentRoute")
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route
            ) {
                // Main screens
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        viewModel = movieViewModel,
                        onMovieClick = { movie ->
                            navController.navigate("detail/${movie.id.toString()}")
                        },
                        onSearchClick = {
                            navController.navigate("search")
                        },
                        onProfileClick = {
                            navController.navigate("profile")
                        }
                    )
                }

                composable(BottomNavItem.MyTicket.route) {
                    MyTicketScreen(
                        onTicketClick = { ticketId ->
                            navController.navigate("ticket_detail/$ticketId")
                        }
                    )
                }

                composable(BottomNavItem.Wallet.route) {
                    com.example.movieticket.ui.screen.WalletScreen()
                }

                // Detail screens
                composable(
                    route = "detail/{movieId}",
                    arguments = listOf(
                        navArgument("movieId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
                    movieId?.let { id ->
                        val movie = movieViewModel.getMovie(id)
                        if (movie != null) {
                            val isNowShowing = movieViewModel.movies.value.any { it.id == movie.id }
                            DetailScreen(
                                movie = movie,
                                onBuyTicketClick = {
                                    if (isNowShowing) {
                                        navController.navigate("booking/${movie.id.toString()}")
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        } else {
                            // Handle case when movie is not found
                            navController.popBackStack()
                        }
                    }
                }

                composable("search") {
                    SearchScreen(
                        viewModel = movieViewModel,
                        onBackClick = { navController.popBackStack() },
                        onMovieClick = { movie ->
                            navController.navigate("detail/${movie.id.toString()}")
                        }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onSignOut = {
                            auth.signOut()
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                        onWalletClick = {
                            navController.navigate(BottomNavItem.Wallet.route)
                        }
                    )
                }

                composable(
                    route = "booking/{movieId}",
                    arguments = listOf(
                        navArgument("movieId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
                    movieId?.let { id ->
                        val movie = movieViewModel.getMovie(id)
                        if (movie != null) {
                            BookingScreen(
                                movie = movie,
                                onBackClick = { navController.popBackStack() },
                                onConfirmBooking = { date, time, seats ->
                                    val seatsString = seats.joinToString(",")
                                    navController.navigate(
                                        "checkout/${movie.id.toString()}/$seatsString/${date.replace("/", "-")}/$time"
                                    )
                                }
                            )
                        } else {
                            // Handle case when movie is not found
                            navController.popBackStack()
                        }
                    }
                }

                composable(
                    route = "checkout/{movieId}/{seats}/{date}/{time}",
                    arguments = listOf(
                        navArgument("movieId") { type = NavType.StringType },
                        navArgument("seats") { type = NavType.StringType },
                        navArgument("date") { type = NavType.StringType },
                        navArgument("time") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
                    val seats = backStackEntry.arguments?.getString("seats")?.split(",") ?: emptyList()
                    val date = backStackEntry.arguments?.getString("date")?.replace("-", "/") ?: ""
                    val time = backStackEntry.arguments?.getString("time") ?: ""
                    
                    movieId?.let { id ->
                        val movie = movieViewModel.getMovie(id)
                        if (movie != null) {
                            CheckoutScreen(
                                movie = movie,
                                selectedSeats = seats,
                                selectedDate = date,
                                selectedTime = time,
                                onBackClick = { navController.popBackStack() },
                                onPaymentClick = { total ->
                                    navController.navigate(
                                        "concession/${movie.id.toString()}/$seats/${date.replace("/", "-")}/$time"
                                    )
                                }
                            )
                        } else {
                            navController.popBackStack()
                        }
                    }
                }

                composable(
                    route = "concession/{movieId}/{seats}/{date}/{time}",
                    arguments = listOf(
                        navArgument("movieId") { type = NavType.StringType },
                        navArgument("seats") { type = NavType.StringType },
                        navArgument("date") { type = NavType.StringType },
                        navArgument("time") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
                    val seats = backStackEntry.arguments?.getString("seats")?.split(",") ?: emptyList()
                    val date = backStackEntry.arguments?.getString("date")?.replace("-", "/") ?: ""
                    val time = backStackEntry.arguments?.getString("time") ?: ""
                    
                    movieId?.let { id ->
                        val movie = movieViewModel.getMovie(id)
                        if (movie != null) {
                            ConcessionScreen(
                                ticketTotal = seats.size * 50000,
                                onBackClick = { navController.popBackStack() },
                                onConfirmClick = { total ->
                                    navController.navigate(
                                        "payment/${movie.id.toString()}/$seats/${date.replace("/", "-")}/$time/$total"
                                    )
                                }
                            )
                        } else {
                            navController.popBackStack()
                        }
                    }
                }

                composable(
                    route = "payment/{movieId}/{seats}/{date}/{time}/{total}",
                    arguments = listOf(
                        navArgument("movieId") { type = NavType.StringType },
                        navArgument("seats") { type = NavType.StringType },
                        navArgument("date") { type = NavType.StringType },
                        navArgument("time") { type = NavType.StringType },
                        navArgument("total") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
                    val seats = backStackEntry.arguments?.getString("seats")?.split(",") ?: emptyList()
                    val date = backStackEntry.arguments?.getString("date")?.replace("-", "/") ?: ""
                    val time = backStackEntry.arguments?.getString("time") ?: ""
                    val total = backStackEntry.arguments?.getInt("total") ?: 0
                    
                    movieId?.let { id ->
                        val movie = movieViewModel.getMovie(id)
                        if (movie != null) {
                            PaymentScreen(
                                movie = movie,
                                selectedSeats = seats,
                                selectedDate = date,
                                selectedTime = time,
                                total = total,
                                onBackClick = { navController.popBackStack() },
                                onPaymentSuccess = {
                                    navController.navigate(BottomNavItem.MyTicket.route) {
                                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            navController.popBackStack()
                        }
                    }
                }

                composable(
                    route = "ticket_detail/{ticketId}",
                    arguments = listOf(
                        navArgument("ticketId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val ticketId = backStackEntry.arguments?.getString("ticketId")
                    ticketId?.let { id ->
                        val ticket = myTicketViewModel.getTicket(id)
                        if (ticket != null) {
                            TicketDetailScreen(
                                ticket = ticket,
                                onBackClick = { navController.popBackStack() }
                            )
                        } else {
                            // Show error state or navigate back
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
} 